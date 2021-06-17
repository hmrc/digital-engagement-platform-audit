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

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import play.api.libs.json._

class TranscriptMapper {
  private def transcriptPath = JsPath() \ 'transcript
  private def isoPath = JsPath() \ 'iso
  def mapTranscriptEntry(transcript: JsValue, engagementId: String, index: Int): JsResult[JsValue] = {

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
          putValue(JsPath() \ 'detail \ 'transcriptIndex, Json.toJson(index))
        )
      case e => e
    }
  }

  private def deleteValue(path: JsPath) = path.json.prune
  private def putString(path: JsPath, value: String) = putValue(path, Json.toJson(value))
  private def putValue(path: JsPath, value: JsValue): Reads[JsObject] = {
    __.json.update(path.json.put(value))
  }

  def mapTranscript(engagement: JsValue): Seq[JsValue] = {
    val transcript = engagement.transform(transcriptPath.json.pick)
    val engagementId = engagement.transform((JsPath() \ 'engagementID).json.pick)

    (transcript, engagementId) match {
      case (JsSuccess(transcripts: JsArray, _), JsSuccess(JsString(engagementId), _)) =>
        val mappedTranscripts = transcripts.value.zipWithIndex.map {
          case (t, index) => mapTranscriptEntry(t, engagementId, index)
        }

        mappedTranscripts.flatMap {
          case JsSuccess(value, _) => Some(value)
          case _ => None
        }
      case _ => Seq()
    }
  }
}
