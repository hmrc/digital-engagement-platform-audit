/*
 * Copyright 2023 HM Revenue & Customs
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

import mappers.TestEngagementData._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json._
import services.NuanceIdDecryptionService
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import java.time.Instant

class MetadataMapperSpec extends AnyWordSpec with Matchers with MockitoSugar {
  private val nuanceIdDecryptionService = mock[NuanceIdDecryptionService]
  when(nuanceIdDecryptionService.decryptDeviceId(any())).thenReturn("DecryptedDeviceId")
  when(nuanceIdDecryptionService.decryptSessionId(any())).thenReturn("DecryptedSessionId")

  "mapToMetadataEvent" must {
    "create an audit event" in {
      val jsInput = testEngagementJson
      val mapper = new MetadataMapper(nuanceIdDecryptionService)
      val result = mapper.mapToMetadataEvent(jsInput)
      result mustBe Some(ExtendedDataEvent(
        "digital-engagement-platform",
        "EngagementMetadata",
        "2e48619e-018b-3dda-8c2c-0b2e76143a03",
        Map[String, String](
          "clientIP" -> "81.97.99.4",
          "path" -> "https://www.tax.service.gov.uk/account-recovery/lost-user-id-password/check-emails?ui_locales=en&nuance=2008HMRCSITTest",
          "deviceID" -> "DecryptedDeviceId",
          "X-Session-ID" -> "DecryptedSessionId"
        ),
        TestEngagementData.testEngagementWithoutTranscriptJson,
        Instant.parse("2020-07-07T09:26:27.520Z")
      ))
    }
    "not create an audit event if no engagement id" in {
      val jsInput = Json.obj()
      val mapper = new MetadataMapper(nuanceIdDecryptionService)
      val result = mapper.mapToMetadataEvent(jsInput)
      result mustBe None
    }
    "not create an audit event if no end date" in {
      val jsInput = Json.obj("engagementID" -> "someId")
      val mapper = new MetadataMapper(nuanceIdDecryptionService)
      val result = mapper.mapToMetadataEvent(jsInput)
      result mustBe None
    }
  }
}
