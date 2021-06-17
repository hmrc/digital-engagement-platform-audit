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

class MetadataMapperSpec extends AnyWordSpec with Matchers {
  "mapEngagement" should {
    "work with standard engagement" in {
      val jsInput = testEngagementJson
      val mapper = new MetadataMapper
      val result = mapper.mapEngagement(jsInput)
      result.isSuccess mustBe true
      result.get mustBe Json.obj(
        "auditSource" -> "digital-engagement-platform",
        "auditType" -> "EngagementMetadata",
        "eventId" -> "Metadata-187286680131967188",
        "generatedAt" -> "2021-03-02T13:23:44",
        "detail" -> jsInput
      )
    }
    "fail if engagement value is missing engagement" in {
      val mapper = new MetadataMapper
      val result = mapper.mapEngagement(Json.obj())
      result.isError mustBe true
    }
  }
}