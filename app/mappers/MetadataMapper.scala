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

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneOffset}

import javax.inject.Inject
import mappers.JsonUtils._
import play.api.Logging
import play.api.libs.json._
import services.NuanceIdDecryptionService
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

class MetadataMapper @Inject()(nuanceDecryptionService: NuanceIdDecryptionService) extends Logging {
  private val engagementIDPick = (__ \ 'engagementID).json.pick
  private val endDatePick = (__ \ 'endDate \ 'iso).json.pick

  def mapEngagement(engagement: JsValue): JsResult[JsValue] = {
    val engagementId = engagement.transform(engagementIDPick)
    val endDate = engagement.transform(endDatePick)
    (engagementId, endDate) match {
      case (JsSuccess(JsString(engagementId), _), JsSuccess(JsString(endDate), _)) =>
        val generatedAtDate = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME)

        Json.obj().transform(
          putString(__ \ 'auditSource, "digital-engagement-platform") andThen
          putString(__ \ 'auditType, "EngagementMetadata") andThen
          putString(__ \ 'eventId, s"Metadata-$engagementId") andThen
          putValue(__ \ 'generatedAt, Json.toJson(generatedAtDate)) andThen
          putValue(__ \ 'detail, Json.toJson(engagement)) andThen
          TagsReads(engagement, nuanceDecryptionService)
        )
      case (e: JsError, _) =>
        logger.warn(s"[MetadataMapper] Couldn't read engagement id from engagement")
        e
      case (_, e) =>
        logger.warn(s"[MetadataMapper] Couldn't read end date from engagement")
        e
    }
  }

  private def removeTranscript(engagement: JsValue): JsValue = {
    val transcriptPath = __ \ 'transcript
    engagement.transform(transcriptPath.json.prune) match {
      case JsSuccess(value, _) => value
      case _ => engagement
    }
  }

  def mapToMetadataEvent(engagement: JsValue): Option[ExtendedDataEvent] = {
    val engagementId = engagement.transform(engagementIDPick)
    val endDate = engagement.transform(endDatePick)
    (engagementId, endDate) match {
      case (JsSuccess(JsString(engagementId), _), JsSuccess(JsString(endDate), _)) =>
        val generatedAtDate = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME)
        Some(ExtendedDataEvent(
          "digital-engagement-platform",
          "EngagementMetadata",
          s"Metadata-$engagementId",
          TagsReads.extractTags(engagement, nuanceDecryptionService),
          removeTranscript(engagement),
          generatedAtDate.toInstant(ZoneOffset.UTC)
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
