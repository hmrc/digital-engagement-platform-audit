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

import mappers.JsonUtils.{copyValue, doNothing, putValue}
import play.api.libs.json._
import services.NuanceIdDecryptionService

object TagsReads {
  def extractTags(engagement: JsValue, decryptionService: NuanceIdDecryptionService): Map[String, String] = {
     Map[String, String](
       "clientIP" -> extractClientIP(engagement),
       "path" -> extractPath(engagement),
       "deviceID" -> extractDeviceId(engagement, decryptionService),
       "X-Session-ID" -> extractSessionId(engagement, decryptionService)
     ).filterNot(_._2.isEmpty)
  }

  private def extractClientIP(engagement: JsValue) = {
    extractValue(engagement, __ \ 'visitorAttribute \ 'clientIp) { value: JsValue => value(0) }
  }

  private def extractPath(engagement: JsValue) = {
    extractValue(engagement, __ \ 'pages \ 'launchPageURL) { value: JsValue => value }
  }

  private def extractDeviceId(engagement: JsValue, decryptionService: NuanceIdDecryptionService): String = {
    extractValue(engagement, __ \ 'visitorAttribute \ 'deviceId) {
      value => JsString(decryptionService.decryptDeviceId(value(0).as[String]))
    }
  }

  private def extractSessionId(engagement: JsValue, decryptionService: NuanceIdDecryptionService): String = {
    extractValue(engagement, __ \ 'visitorAttribute \ 'mdtpSessionId) {
      value => JsString(decryptionService.decryptSessionId(value(0).as[String]))
    }
  }

  private def extractValue(source: JsValue, path: JsPath)(getValue: (JsValue) => JsValue): String = {
    source.transform(path.json.pick) match {
      case JsSuccess(value, _) => getValue(value).as[String]
      case _ => ""
    }
  }

  def apply(engagement: JsValue, decryptionService: NuanceIdDecryptionService): Reads[JsObject] = {
    Json.obj().transform(
      copyClientIp(engagement) andThen
      copyPath(engagement) andThen
      copyDeviceId(engagement, decryptionService) andThen
      copySessionId(engagement, decryptionService)
    ) match {
      case JsSuccess(result, _) => putValue(__ \ 'tags, result)
      case _ => doNothing()
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

  def copyDeviceId(engagement: JsValue, decryptionService: NuanceIdDecryptionService): Reads[JsObject] = {
    copyValue(engagement, __ \ 'visitorAttribute \ 'deviceId, __ \ 'deviceID) {
      value => JsString(decryptionService.decryptDeviceId(value(0).as[String]))
    }
  }

  def copySessionId(engagement: JsValue, decryptionService: NuanceIdDecryptionService): Reads[JsObject] = {
    copyValue(engagement, __ \ 'visitorAttribute \ 'mdtpSessionId, __ \ "X-Session-ID") {
      value => JsString(decryptionService.decryptSessionId(value(0).as[String]))
    }
  }
}
