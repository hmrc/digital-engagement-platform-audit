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

import java.time.{LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter

import play.api.libs.json._
import JsonUtils._
import javax.inject.Inject
import play.api.Logging
import services.NuanceIdDecryptionService
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

class TranscriptMapper @Inject()(nuanceIdDecryptionService: NuanceIdDecryptionService) extends Logging {
  private def transcriptPath = JsPath() \ 'transcript
  private def isoPath = JsPath() \ 'iso

  private def mapTranscriptDetail(transcript: JsValue, engagementId: String, index: Int): JsValue = {
    transcript.transform(
      deleteValue(JsPath() \ 'iso) andThen
        deleteValue(JsPath() \ 'timestamp) andThen
        putString(JsPath() \ 'engagementID, engagementId) andThen
        putValue(JsPath() \ 'transcriptIndex, Json.toJson(index)) andThen
        createSenderPidInDetailIfExists(transcript)
    ) match {
      case JsSuccess(value, _) => value
    }
  }

  def mapTranscriptEntryEvent(transcript: JsValue, engagementId: String, index: Int, tags: Map[String, String]): Option[ExtendedDataEvent] = {
    transcript.transform(isoPath.json.pick) match {
      case JsSuccess(JsString(datetime), _) =>
        val dt = LocalDateTime.parse(datetime, DateTimeFormatter.ISO_DATE_TIME)
        Some(ExtendedDataEvent(
          "digital-engagement-platform",
          "EngagementTranscript",
          s"Transcript-$engagementId-$index",
          tags,
          mapTranscriptDetail(transcript, engagementId, index),
          dt.toInstant(ZoneOffset.UTC)
        ))
      case _ =>
        logger.warn(s"[TranscriptMapper] Couldn't read iso date from transcript entry")
        None
    }
  }

  def mapTranscriptEntry(transcript: JsValue, engagementId: String, index: Int, tagsReads: Reads[JsObject]): JsResult[JsValue] = {

    transcript.transform(isoPath.json.pick) match {
      case JsSuccess(JsString(datetime), _) =>
        val dt = LocalDateTime.parse(datetime, DateTimeFormatter.ISO_DATE_TIME)
        Json.obj().transform(
          putString(JsPath() \ 'auditSource, "digital-engagement-platform") andThen
          putString(JsPath() \ 'auditType, "EngagementTranscript") andThen
          putString(JsPath() \ 'eventId, s"Transcript-$engagementId-$index") andThen
          putValue(JsPath() \ 'generatedAt, Json.toJson(dt)) andThen
          putValue(JsPath() \ 'detail, Json.toJson(transcript)) andThen
          deleteValue(JsPath() \ 'detail \ 'iso) andThen
          deleteValue(JsPath() \ 'detail \ 'timestamp) andThen
          putString(JsPath() \ 'detail \ 'engagementID, engagementId) andThen
          putValue(JsPath() \ 'detail \ 'transcriptIndex, Json.toJson(index)) andThen
          createSenderPidIfExists(transcript) andThen
          tagsReads
        )
      case e =>
        logger.warn(s"[TranscriptMapper] Couldn't read iso date from transcript entry")
        e
    }
  }

  private def createSenderPidInDetailIfExists(transcript: JsValue) : Reads[JsObject] = {
    val senderIdPath = __ \ 'senderId
    val senderPidPath = __ \ 'senderPID
    transcript.transform(senderIdPath.json.pick) match {
      case JsSuccess(JsString(senderId), _) if isHmrcId(senderId) =>
        putString(senderPidPath, extractHmrcId(senderId))
      case _ => doNothing()
    }
  }

  private def createSenderPidIfExists(transcript: JsValue) : Reads[JsObject] = {
    val senderIdPath = __ \ 'senderId
    val senderPidPath = __ \ 'detail \ 'senderPID
    transcript.transform(senderIdPath.json.pick) match {
      case JsSuccess(JsString(senderId), _) if isHmrcId(senderId) =>
        putString(senderPidPath, extractHmrcId(senderId))
      case _ => doNothing()
    }
  }

  private def isHmrcId(id: String): Boolean = id.contains("@hmrc")

  private def extractHmrcId(str: String): String = {
    str.split("@")(0)
  }

  def mapTranscript(engagement: JsValue): Seq[JsValue] = {
    val transcript = engagement.transform(transcriptPath.json.pick)
    val engagementId = engagement.transform((JsPath() \ 'engagementID).json.pick)
    val tagsReads = TagsReads(engagement, nuanceIdDecryptionService)

    (transcript, engagementId) match {
      case (JsSuccess(transcripts: JsArray, _), JsSuccess(JsString(engagementId), _)) =>
        val mappedTranscripts = transcripts.value.zipWithIndex.map {
          case (t, index) => mapTranscriptEntry(t, engagementId, index, tagsReads)
        }

        mappedTranscripts.flatMap {
          case JsSuccess(value, _) => Some(value)
          case _ => None
        }
      case _ =>
        logger.warn(s"[TranscriptMapper] Couldn't read transcript or engagement id from engagement.")
        Seq()
    }
  }
}
