/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mappers

import auditing.EngagementAuditing
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json._
import services.NuanceIdDecryptionService
import uk.gov.hmrc.audit.serialiser.AuditSerialiser
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.{ExtendedDataEvent, RedactionLog, TruncationLog}
import utils.JsonUtils._

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BigMappingSpec extends AnyWordSpec with Matchers with MockitoSugar {

  def answer[T](f: InvocationOnMock => T): Answer[T] = {
    invocation: InvocationOnMock => f(invocation)
  }

  private val nuanceIdDecryptionService = mock[NuanceIdDecryptionService]
  when(nuanceIdDecryptionService.decryptDeviceId(any())).thenReturn("DecryptedDeviceId")
  when(nuanceIdDecryptionService.decryptSessionId(any())).thenReturn("DecryptedSessionId")

  "mapping" should {
    "handle full historic file" in {

      val events = new ListBuffer[JsValue]()

      val auditConnector = mock[AuditConnector]
      when(auditConnector.sendExtendedEvent(any())(any(), any())).thenAnswer(answer({ invocation =>
				implicit val serializer: AuditSerialiser = new AuditSerialiser()

        val event: ExtendedDataEvent = invocation.getArguments.head.asInstanceOf[ExtendedDataEvent]
        events += serializer.serialise(event)
        Future.successful(AuditResult.Success)
      }))

      val metadataMapper = new MetadataMapper(nuanceIdDecryptionService)
      val transcriptMapper = new TranscriptMapper(nuanceIdDecryptionService)
      val engagementMapper = new EngagementMapper(metadataMapper, transcriptMapper)

      val json = getJsonValueFromFile("HistoricSample.json")

      val engagementAuditing = new EngagementAuditing(engagementMapper, auditConnector)
      val engagements = json.transform((__ \ 'engagements).json.pick).get.as[JsArray]

      engagementAuditing.processEngagements(engagements)

      verify(auditConnector, times(6)).sendExtendedEvent(any())(any(), any())

      JsArray(events) mustBe getJsonValueFromFile("HistoricSample_AuditEvents.json")
    }
  }
}
