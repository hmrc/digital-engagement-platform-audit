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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._
import TestEngagementData.testEngagementJson
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import services.NuanceIdDecryptionService

class MetadataMapperSpec extends AnyWordSpec with Matchers with MockitoSugar {
  private val nuanceIdDecryptionService = mock[NuanceIdDecryptionService]
  when(nuanceIdDecryptionService.decryptDeviceId(any())).thenReturn("DecryptedDeviceId")
  when(nuanceIdDecryptionService.decryptSessionId(any())).thenReturn("DecryptedSessionId")

  "mapEngagement" must {
    "work with standard engagement" in {
      val jsInput = testEngagementJson
      val mapper = new MetadataMapper(nuanceIdDecryptionService)
      val result = mapper.mapEngagement(jsInput)
      result.isSuccess mustBe true
      result.get mustBe Json.parse(
        s"""
          | {
          |   "auditSource": "digital-engagement-platform",
          |   "auditType": "EngagementMetadata",
          |   "eventId": "Metadata-187286680131967188",
          |   "generatedAt": "2021-03-02T13:23:44",
          |   "detail": $jsInput,
          |   "tags": {
          |     "clientIP": "81.97.99.4",
          |     "path": "https://www.tax.service.gov.uk/account-recovery/lost-user-id-password/check-emails?ui_locales=en&nuance=2008HMRCSITTest",
          |     "deviceID": "DecryptedDeviceId",
          |     "X-Session-ID": "DecryptedSessionId"
          |   }
          | }
          |""".stripMargin
      )
    }
    "fail if engagement value is missing engagement" in {
      val mapper = new MetadataMapper(nuanceIdDecryptionService)
      val result = mapper.mapEngagement(Json.obj())
      result.isError mustBe true
    }
  }
}