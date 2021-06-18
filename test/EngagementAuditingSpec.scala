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

import mappers.EngagementMapper
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import scala.concurrent.ExecutionContext.Implicits.global

class EngagementAuditing(engagementMapper: EngagementMapper, auditConnector: AuditConnector) {
  def processEngagement(engagement: JsValue): Any = {
    val events = engagementMapper.mapEngagement(engagement)
    events.foreach(event => auditConnector.sendExtendedEvent(event))
  }
}

class EngagementAuditingSpec extends AnyWordSpec with Matchers with MockitoSugar {
  "EngagementAuditing" must {
    "send audit events for engagement" in {
      val metadataEvent = ExtendedDataEvent("metadata", "Metadata")
      val transcriptEvent1 = ExtendedDataEvent("transcript1", "Transcript1")
      val transcriptEvent2 = ExtendedDataEvent("transcript2", "Transcript2")

      val engagement = Json.obj("field" -> "value")

      val engagementMapper: EngagementMapper = mock[EngagementMapper]
      when(engagementMapper.mapEngagement(any())).thenReturn(Seq(metadataEvent, transcriptEvent1, transcriptEvent2))

      val auditConnector = mock[AuditConnector]

      val something = new EngagementAuditing(engagementMapper, auditConnector)
      something.processEngagement(engagement)

      verify(auditConnector).sendExtendedEvent(metadataEvent)
      verify(auditConnector).sendExtendedEvent(transcriptEvent1)
      verify(auditConnector).sendExtendedEvent(transcriptEvent2)
    }
  }
}

