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
import models.transcript.{AutomatonContentSentToCustomerEntry, AutomatonStartedEntry}
import play.api.Logging
import play.api.libs.json._
import services.NuanceIdDecryptionService
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent


class TranscriptMapper @Inject()(nuanceIdDecryptionService: NuanceIdDecryptionService) extends Logging {
  private def transcriptPath = JsPath() \ 'transcript
  private def typePath = JsPath() \ 'type
  private def isoPath = JsPath() \ 'iso

  private def mapBasicDetails(transcript: JsValue) = {
    getType(transcript) match {
      case Some(AutomatonStartedEntry.eventType) => Some(Json.toJson(transcript.as[AutomatonStartedEntry]))
      case Some(AutomatonContentSentToCustomerEntry.eventType) => Some(Json.toJson(transcript.as[AutomatonContentSentToCustomerEntry]))
      case _ => None
    }
  }

  def mapTranscriptDetail(transcript: JsValue, engagementId: String, index: Int): Option[JsValue] = {

    mapBasicDetails(transcript) match {
      case Some(value) =>
        value.transform(
          putString(JsPath() \ 'engagementID, engagementId) andThen
            putValue(JsPath() \ 'transcriptIndex, Json.toJson(index)) andThen
            createSenderPidInDetailIfExists(transcript)
        ) match {
          case JsSuccess(value, _) => Some(value)
          case _ =>
            logger.warn(s"[TranscriptMapper] Couldn't add details to transcript entry - should never happen")
            None
        }
      case e => e
    }
  }

  private def getType(transcript: JsValue): Option[String] = {
    transcript.transform(typePath.json.pick) match {
      case JsSuccess(JsString(theType), _) => Some(theType)
      case _ => None
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
          mapTranscriptDetail(transcript, engagementId, index).get,
          dt.toInstant(ZoneOffset.UTC)
        ))
      case _ =>
        logger.warn(s"[TranscriptMapper] Couldn't read iso date from transcript entry")
        None
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

  private def isHmrcId(id: String): Boolean = id.contains("@hmrc")

  private def extractHmrcId(str: String): String = {
    str.split("@")(0)
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
