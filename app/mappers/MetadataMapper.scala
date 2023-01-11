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

import java.time.{Instant, ZoneId, ZonedDateTime}
import javax.inject.Inject
import play.api.Logging
import play.api.libs.json._
import services.NuanceIdDecryptionService
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import JsonUtils._

class MetadataMapper @Inject()(nuanceDecryptionService: NuanceIdDecryptionService) extends Logging {
  private val engagementIDPick = (__ \ "engagementID").json.pick
  private val endDatePick = (__ \ "endDate" \ "timestamp").json.pick[JsNumber]

  private def removeTranscript(engagement: JsValue): JsValue = {
    val transcriptPath = __ \ "transcript"
    engagement.transform(deleteValue(transcriptPath)) match {
      case JsSuccess(value, _) => value
      case _ => engagement
    }
  }

  import java.util.UUID

  private def generateUUIDString(input: String) = UUID.nameUUIDFromBytes(input.getBytes).toString

  def mapToMetadataEvent(engagement: JsValue): Option[ExtendedDataEvent] = {
    val engagementId = engagement.transform(engagementIDPick)
    val endDate = engagement.transform(endDatePick)
    (engagementId, endDate) match {
      case (JsSuccess(JsString(engagementId), _), JsSuccess(JsNumber(endDate), _)) =>

        val instant = Instant.ofEpochMilli(endDate.toLong)
        val generatedAtDate = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"))

        Some(ExtendedDataEvent(
          "digital-engagement-platform",
          "EngagementMetadata",
          generateUUIDString(s"Metadata-$engagementId"),
          TagsReads.extractTags(engagement, nuanceDecryptionService),
          removeTranscript(engagement),
          generatedAtDate.toInstant
        ))
      case (_: JsError, _) =>
        logger.warn(s"[MetadataMapper] Couldn't read engagement id from engagement")
        None
      case (_, _) =>
        logger.warn(s"[MetadataMapper] Couldn't read end date from engagement")
        None
    }
  }
}
