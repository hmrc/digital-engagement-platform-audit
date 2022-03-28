/*
 * Copyright 2022 HM Revenue & Customs
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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.when
import play.api.libs.json._
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

class EngagementMapperSpec extends AnyWordSpec with Matchers with MockitoSugar {
  "mapEngagement" must {
    "map a valid engagement" in {
      val metadataEvent = ExtendedDataEvent("metadata", "Metadata")
      val transcriptEvent1 = ExtendedDataEvent("transcript1", "Transcript1")
      val transcriptEvent2 = ExtendedDataEvent("transcript2", "Transcript2")

      val engagement = Json.obj("field" -> "value")

      val metadataMapper = mock[MetadataMapper]
      when(metadataMapper.mapToMetadataEvent(engagement)).thenReturn(Some(metadataEvent))

      val transcriptMapper = mock[TranscriptMapper]
      when(transcriptMapper.mapTranscriptEvents(engagement)).thenReturn(
        Seq(
          transcriptEvent1,
          transcriptEvent2
        ))

      val engagementMapper = new EngagementMapper(metadataMapper, transcriptMapper)
      engagementMapper.mapEngagement(engagement) mustBe Seq(metadataEvent, transcriptEvent1, transcriptEvent2)
    }

    "not map an invalid engagement" in {
      val engagement = Json.obj("field" -> "value")

      val metadataMapper = mock[MetadataMapper]
      when(metadataMapper.mapToMetadataEvent(engagement)).thenReturn(None)

      val transcriptMapper = mock[TranscriptMapper]

      val engagementMapper = new EngagementMapper(metadataMapper, transcriptMapper)
      engagementMapper.mapEngagement(engagement) mustBe Seq()
    }
  }
}
