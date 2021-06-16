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

import javax.inject.Inject
import play.api.libs.json._
import services.LocalDateTimeService

class MetadataMapper @Inject()(dateTimeService: LocalDateTimeService) {
  private val engagementIDPick = (JsPath() \ 'engagementID).json.pick

  def mapEngagement(engagement: JsValue): JsResult[JsValue] = {
    engagement.transform(engagementIDPick) match {
      case JsSuccess(JsString(engagementId), _) =>
        JsSuccess(Json.obj(
          "auditSource" -> "digital-engagement-platform",
          "auditType" -> "EngagementMetadata",
          "eventId" -> s"Metadata-$engagementId",
          "generatedAt" -> dateTimeService.now,
          "detail" -> engagement
        ))
      case e: JsResult[JsValue] => e
    }
  }
}
