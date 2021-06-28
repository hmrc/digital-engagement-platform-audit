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

package auditing

import mappers.EngagementMapper
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsArray, Json}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import scala.concurrent.ExecutionContext.Implicits.global

class EngagementAuditingSpec extends AnyWordSpec with Matchers with MockitoSugar {
  "auditing.EngagementAuditing" must {
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
    "send audit events for all engagements" in {
      val metadataEventA = ExtendedDataEvent("metadataA", "Metadata")
      val transcriptEventA1 = ExtendedDataEvent("transcriptA1", "Transcript1")
      val transcriptEventA2 = ExtendedDataEvent("transcriptA2", "Transcript2")
      val metadataEventB = ExtendedDataEvent("metadataB", "Metadata")
      val transcriptEventB1 = ExtendedDataEvent("transcriptB1", "Transcript1")
      val transcriptEventB2 = ExtendedDataEvent("transcriptB2", "Transcript2")

      val engagementA = Json.obj("field" -> "valueA")
      val engagementB = Json.obj("field" -> "valueB")

      val engagementMapper: EngagementMapper = mock[EngagementMapper]
      when(engagementMapper.mapEngagement(engagementA)).thenReturn(Seq(metadataEventA, transcriptEventA1, transcriptEventA2))
      when(engagementMapper.mapEngagement(engagementB)).thenReturn(Seq(metadataEventB, transcriptEventB1, transcriptEventB2))

      val auditConnector = mock[AuditConnector]

      val engagements = JsArray(Seq(engagementA, engagementB))

      val something = new EngagementAuditing(engagementMapper, auditConnector)
      something.processEngagements(engagements)

      verify(auditConnector).sendExtendedEvent(metadataEventA)
      verify(auditConnector).sendExtendedEvent(transcriptEventA1)
      verify(auditConnector).sendExtendedEvent(transcriptEventA2)
      verify(auditConnector).sendExtendedEvent(metadataEventB)
      verify(auditConnector).sendExtendedEvent(transcriptEventB1)
      verify(auditConnector).sendExtendedEvent(transcriptEventB2)
    }
  }
}

