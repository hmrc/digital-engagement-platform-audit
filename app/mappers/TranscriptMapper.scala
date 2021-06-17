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
          __.json.update((JsPath() \ 'auditSource).json.put(Json.toJson("digital-engagement-platform"))) andThen
            __.json.update((JsPath() \ 'auditType).json.put(Json.toJson("EngagementTranscript"))) andThen
            __.json.update((JsPath() \ 'eventId).json.put(Json.toJson(s"Transcript-$engagementId-$index"))) andThen
            __.json.update((JsPath() \ 'generatedAt).json.put(Json.toJson(dt))) andThen
            __.json.update((JsPath() \ 'detail).json.put(transcript)) andThen
            (JsPath() \ 'detail \ 'iso).json.prune andThen
            (JsPath() \ 'detail \ 'timestamp).json.prune andThen
            __.json.update((JsPath() \ 'detail \ 'engagementID).json.put(Json.toJson(engagementId))) andThen
            __.json.update((JsPath() \ 'detail \ 'transcriptIndex).json.put(Json.toJson(index)))
        )
      case e => e
    }
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
