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
import JsonUtils._

object TagsReads {
  def createReads(engagement: JsValue): Reads[JsObject] = {
    Json.obj().transform(
      copyClientIp(engagement) andThen
      copyPath(engagement) andThen
      copyDeviceId(engagement) andThen
      copySessionId(engagement)
    ) match {
      case JsSuccess(result, _) => putValue(__ \ 'tags, result)
    }
  }

  def copyClientIp(engagement: JsValue): Reads[JsObject] = {
    val clientIpPath = __ \ 'visitorAttribute \ 'clientIp
    engagement.transform(clientIpPath.json.pick) match {
      case JsSuccess(clientIpArray, _) => putValue(__ \ 'clientIP, clientIpArray(0))
      case _ => doNothing()
    }
  }

  def copyPath(engagement: JsValue): Reads[JsObject] = {
    val launchPageUrlPath = __ \ 'pages \ 'launchPageURL
    engagement.transform(launchPageUrlPath.json.pick) match {
      case JsSuccess(launchPageUrl, _) => putValue(__ \ 'path, launchPageUrl)
      case _ => doNothing()
    }
  }

  def copyDeviceId(engagement: JsValue): Reads[JsObject] = {
    val deviceIdPath = __ \ 'visitorAttribute \ 'deviceId
    engagement.transform(deviceIdPath.json.pick) match {
      case JsSuccess(deviceId, _) => putValue(__ \ 'deviceID, deviceId(0))
      case _ => doNothing()
    }
  }

  def copySessionId(engagement: JsValue): Reads[JsObject] = {
    val sessionIdPath = __ \ 'visitorAttribute \ 'mdtpSessionId
    engagement.transform(sessionIdPath.json.pick) match {
      case JsSuccess(sessionId, _) => putValue(__ \ "X-Session-ID", sessionId(0))
      case _ => doNothing()
    }
  }

}

//    "tags": {
//        "X-Session-ID": "session-fe15211e-0d70-43e5-ae80-cd5c5e214ef1",
//        "clientIP": "197.123.44.1",
//        "deviceID": "mdtpdi#05785340-2769-45be-b174-d8d25aa8d139#1562656337324_1PdHu1BtBEIBPBMl88ISlw==",
//        "path": "/company-registration/corporation-tax-registration/xxx/confirmation-references"
//      }

class TagsReadsSpec extends AnyWordSpec with Matchers {
  "tagsMapper" should {
    "extract tags" in {
      val jsInput = testEngagementJson

      val result = Json.obj().transform(TagsReads.createReads(jsInput))
      result.isSuccess mustBe true
      result.get mustBe Json.parse(
        """
          | {
          |   "tags": {
          |     "clientIP": "81.97.99.4",
          |     "path": "https://www.tax.service.gov.uk/account-recovery/lost-user-id-password/check-emails?ui_locales=en&nuance=2008HMRCSITTest",
          |     "deviceID": "ENCRYPTED-7a0O8KdpAtAKQIbfo62FvLkdnvSTcYpWo++IvpAzx88DEzFJYGHRriq+w/bAwCv3wXQTZIyMtkvUrxz9pEQeflMi6gvenmBDQX8+Yl8jmVu3o48Pdbt4BzGKSE6/KMnwMsnVT/d7+qESnWbqHshXzMMvqMY+UrQMdQ==",
          |     "X-Session-ID": "ENCRYPTED-UU1USedMqML7Yj3XulYIHtNkOGpmoQzXx4X20+H3OfDeoIzzVoGbsVKY1rC8Z5LqUj2YtjkwaK9qFmxgACHK4u8TrGXi8hiKjo2X8rRBoT7YflRD9pJ25E9lEBT/ih8kA5NxReUSTABOhf+fkBPioYNTW1wOM4jBFg=="
          |   }
          | }
          |""".stripMargin)
    }
  }
}
