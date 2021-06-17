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

import mappers.JsonUtils.{copyValue, putValue}
import play.api.libs.json._

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
    copyValue(engagement, __ \ 'visitorAttribute \ 'clientIp, __ \ 'clientIP) {
      value => value(0)
    }
  }

  def copyPath(engagement: JsValue): Reads[JsObject] = {
    copyValue(engagement, __ \ 'pages \ 'launchPageURL, __ \ 'path){ value => value}
  }

  def copyDeviceId(engagement: JsValue): Reads[JsObject] = {
    copyValue(engagement, __ \ 'visitorAttribute \ 'deviceId, __ \ 'deviceID) {
      value => value(0)
    }
  }

  def copySessionId(engagement: JsValue): Reads[JsObject] = {
    copyValue(engagement, __ \ 'visitorAttribute \ 'mdtpSessionId, __ \ "X-Session-ID") {
      value => value(0)
    }
  }
}
