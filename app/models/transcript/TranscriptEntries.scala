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

package models.transcript

import play.api.libs.json.{Format, Json}

case class AgentRequestedEntry(
                                  `type`: String,
                                  senderId: String,
                                  senderName: String,
                                  result: String,
                                  businessUnit: String,
                                  agentGroup: String
                                )
object AgentRequestedEntry {
  implicit val format: Format[AgentRequestedEntry] = Json.format[AgentRequestedEntry]
  val eventType = "agent.requested"
}

case class AutomatonStartedEntry(
                                  `type`: String,
                                  senderId: String,
                                  senderName: String
                                )
object AutomatonStartedEntry {
  implicit val format: Format[AutomatonStartedEntry] = Json.format[AutomatonStartedEntry]
  val eventType = "automaton.started"
}

case class AutomatonContentSentToCustomerEntry(
                                                `type`: String,
                                                `custom.decisiontree.nodeID`: Option[String],
                                                `custom.decisiontree.view`: Option[String],
                                                `custom.decisiontree.questions`: Option[List[String]]
                                              )
object AutomatonContentSentToCustomerEntry {
  implicit val format: Format[AutomatonContentSentToCustomerEntry] = Json.format[AutomatonContentSentToCustomerEntry]
  val eventType = "automaton.contentSentToCustomer"
}


case class AutomatonCustomerResponded(
                                     `type`: String,
                                     senderName: String,
                                     `custom.decisiontree.nodeID`: Option[String],
                                     `custom.decisiontree.questions`: Option[List[String]]
                                   )

object AutomatonCustomerResponded {
  implicit val format: Format[AutomatonCustomerResponded] = Json.format[AutomatonCustomerResponded]
  val eventType = "automaton.customerResponded"
}

case class AutomatonEnded(`type`: String)

object AutomatonEnded {
  implicit val format: Format[AutomatonEnded] = Json.format[AutomatonEnded]
  val eventType = "automaton.ended"
}

case class ChatAgentEnteredChatEntry(
                                     `type`: String,
                                     senderName: String,
                                     senderId: String,
                                     senderAlias: Option[String],
                                     content: Option[String],
                                     enterType: String
                                   )

object ChatAgentEnteredChatEntry {
  implicit val format: Format[ChatAgentEnteredChatEntry] = Json.format[ChatAgentEnteredChatEntry]
  val eventType = "chat.agentEnterChat"
}

case class ChatClickstreamEntry(
                                 `type`: String,
                                 senderName: String,
                                 pageMarker: Option[String],
                                 historicPageMarkers: Option[String],
                                 pageURL: Option[String],
                                 systemInfo: Option[String],
                                 datapass: Option[String]
                               )

object ChatClickstreamEntry {
  implicit val format: Format[ChatClickstreamEntry] = Json.format[ChatClickstreamEntry]
  val eventType = "chat.clickstream"
}

case class ChatCustomerChatlineSentEntry(
                                     `type`: String,
                                     senderName: String,
                                     content: String
                                   )

object ChatCustomerChatlineSentEntry {
  implicit val format: Format[ChatCustomerChatlineSentEntry] = Json.format[ChatCustomerChatlineSentEntry]
  val eventType = "chat.customerChatlineSent"
}

case class ChatOpenerDisplayed(
                                `type`: String,
                                senderName: String,
                                senderAlias: Option[String],
                                content: Option[String]
                              )

object ChatOpenerDisplayed {
  implicit val format: Format[ChatOpenerDisplayed] = Json.format[ChatOpenerDisplayed]
  val eventType = "chat.openerDisplayed"
}


case class EngagementRequestedEntry(
                                     `type`: String,
                                     senderName: String,
                                     resourceNeeded: String,
                                     `automaton.automatonID`: Option[String],
                                     businessUnit: String,
                                     agentGroup: String
                                   )
object EngagementRequestedEntry {
  implicit val format: Format[EngagementRequestedEntry] = Json.format[EngagementRequestedEntry]
  val eventType = "engagement.requested"
}
