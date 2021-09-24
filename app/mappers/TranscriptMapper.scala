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
import java.time.{Instant, LocalDateTime, ZoneId, ZoneOffset, ZonedDateTime}
import javax.inject.Inject
import play.api.Logging
import play.api.libs.json._
import services.NuanceIdDecryptionService
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import java.util.UUID

class TranscriptMapper @Inject()(nuanceIdDecryptionService: NuanceIdDecryptionService) extends Logging {
  private def transcriptPath = JsPath() \ 'transcript
  private def timestampPath = JsPath() \ 'timestamp

  private def generateUUIDString(input: String) = UUID.nameUUIDFromBytes(input.getBytes).toString

  private def mapTranscriptEntry(transcript: JsValue, engagementId: String, index: Int, tags: Map[String, String], datetime: Long) = {
    TranscriptEntryMapper.mapTranscriptDetail(transcript, engagementId, index) match {
      case Some(detail) =>
        val instant = Instant.ofEpochMilli(datetime)
        val dt = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"))
        Some(ExtendedDataEvent(
          "digital-engagement-platform",
          "EngagementTranscript",
          generateUUIDString(s"Transcript-$engagementId-$index"),
          tags,
          detail,
          dt.toInstant
        ))
      case _ => None
    }
  }

  def mapTranscriptEntryEvent(transcript: JsValue, engagementId: String, index: Int, tags: Map[String, String]): Option[ExtendedDataEvent] = {
    transcript.transform(timestampPath.json.pick[JsNumber]) match {
      case JsSuccess(JsNumber(datetime), _) =>
        mapTranscriptEntry(transcript, engagementId, index, tags, datetime.toLong)
      case _ =>
        val o = transcript.as[JsObject]
        logger.warn(s"[TranscriptMapper] Couldn't read iso date from transcript entry with fields ${o.keys}")
        None
    }
  }

  def mapTranscriptEvents(engagement: JsValue): Seq[ExtendedDataEvent] = {
    val transcript = engagement.transform(transcriptPath.json.pick)
    val engagementId = engagement.transform((JsPath() \ 'engagementID).json.pick)
    val tags = TagsReads.extractTags(engagement, nuanceIdDecryptionService)

    (transcript, engagementId) match {
      case (JsSuccess(transcripts: JsArray, _), JsSuccess(JsString(engagementId), _)) =>
        val mappedTranscripts = transcripts.value.zipWithIndex.map {
          case (t, index) => mapTranscriptEntryEvent(t, engagementId, index, tags)
        }
        mappedTranscripts.flatten
      case _ =>
        logger.warn(s"[TranscriptMapper] Couldn't read transcript or engagement id from engagement.")
        Seq()
    }
  }
}
