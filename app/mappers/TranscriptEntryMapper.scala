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

import mappers.JsonUtils.{doNothing, putString, putValue}
import models.transcript._
import play.api.Logging
import play.api.libs.json._

object TranscriptEntryMapper extends Logging {

  private def mapAsType[T](transcript: JsValue)(implicit r: Reads[T], w: Writes[T]) = Json.toJson(transcript.as[T])

  val mappings: Map[String, (JsValue) => JsValue] = Map(
    AgentRequestedEntry.eventType -> mapAsType[AgentRequestedEntry],
    AutomatonStartedEntry.eventType -> mapAsType[AutomatonStartedEntry],
    AutomatonContentSentToCustomerEntry.eventType -> mapAsType[AutomatonContentSentToCustomerEntry],
    AutomatonCustomerResponded.eventType -> mapAsType[AutomatonCustomerResponded],
    AutomatonEnded.eventType -> mapAsType[AutomatonEnded],
    ChatAgentEnteredChatEntry.eventType -> mapAsType[ChatAgentEnteredChatEntry],
    ChatCustomerChatlineSentEntry.eventType -> mapAsType[ChatCustomerChatlineSentEntry],
    EngagementRequestedEntry.eventType -> mapAsType[EngagementRequestedEntry]
  )

  def mapTranscriptDetail(transcript: JsValue, engagementId: String, index: Int): Option[JsValue] = {
    mapBasicDetails(transcript) match {
      case Some(value) =>
        value.transform(
          putString(JsPath() \ 'engagementID, engagementId) andThen
            putValue(JsPath() \ 'transcriptIndex, Json.toJson(index)) andThen
            createSenderPidInDetailIfExists(transcript)
        ) match {
          case JsSuccess(value, _) => Some(value)
          case JsError(errors) =>
            logger.warn(s"[TranscriptMapper] Couldn't add details to transcript entry (should never happen): $errors")
            None
        }
      case _ => None
    }
  }

  private def typePath = JsPath() \ 'type

  private def getType(transcript: JsValue): Option[String] = {
    transcript.transform(typePath.json.pick) match {
      case JsSuccess(JsString(theType), _) => Some(theType)
      case _ => None
    }
  }

  private def mapBasicDetails(transcript: JsValue): Option[JsValue] = {
    getType(transcript) match {
      case Some(t) if (mappings.contains(t)) => Some(mappings(t)(transcript))
      case Some(t) =>
        logger.warn(s"[TranscriptEntryMapper] Unknown entry type: $transcript")
        None
      case _ =>
        logger.warn(s"[TranscriptEntryMapper] Couldn't read type from entry: $transcript")
        None
    }
  }

  private def createSenderPidInDetailIfExists(transcript: JsValue) : Reads[JsObject] = {
    transcript.transform((__ \ 'senderId).json.pick) match {
      case JsSuccess(JsString(senderId), _) if hasHmrcPid(senderId) =>
        putString(__ \ 'senderPID, extractHmrcPid(senderId))
      case _ => doNothing()
    }
  }

  private def hasHmrcPid(id: String): Boolean = id.contains("@hmrc")

  private def extractHmrcPid(str: String): String = {
    str.split("@")(0)
  }
}
