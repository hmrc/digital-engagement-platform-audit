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

import mappers.TestEngagementData.testEngagementJson
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._
import services.NuanceIdDecryptionService

class TagsReadsSpec extends AnyWordSpec with Matchers {
  object TestDecryptionService extends NuanceIdDecryptionService {
    override def decryptDeviceId(deviceId: String): String = "DecryptedDeviceId"
    override def decryptSessionId(sessionId: String): String = "DecryptedSessionId"
  }

  "tagsMapper" should {
    "extract tags" in {
      val jsInput = testEngagementJson

      val result = Json.obj().transform(TagsReads.createReads(jsInput, TestDecryptionService))
      result.isSuccess mustBe true
      result.get mustBe Json.parse(
        """
          | {
          |   "tags": {
          |     "clientIP": "81.97.99.4",
          |     "path": "https://www.tax.service.gov.uk/account-recovery/lost-user-id-password/check-emails?ui_locales=en&nuance=2008HMRCSITTest",
          |     "deviceID": "DecryptedDeviceId",
          |     "X-Session-ID": "DecryptedSessionId"
          |   }
          | }
          |""".stripMargin)
    }
    "extract tags with no matching values" in {
      val jsInput = Json.obj()

      val result = Json.obj().transform(TagsReads.createReads(jsInput, TestDecryptionService))
      result.isSuccess mustBe true
      result.get mustBe Json.parse(
        """
          | {
          |   "tags": {
          |   }
          | }
          |""".stripMargin)
    }
  }
}
