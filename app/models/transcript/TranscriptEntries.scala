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

case class Agent_RequestedEntry(
                                  `type`: String,
                                  senderId: String,
                                  senderName: String,
                                  result: String,
                                  businessUnit: String,
                                  agentGroup: String
                                )
object Agent_RequestedEntry {
  implicit val format: Format[Agent_RequestedEntry] = Json.format[Agent_RequestedEntry]
  val eventType = "agent.requested"
}

case class Automaton_StartedEntry(
                                  `type`: String,
                                  senderId: String,
                                  senderName: String
                                )
object Automaton_StartedEntry {
  implicit val format: Format[Automaton_StartedEntry] = Json.format[Automaton_StartedEntry]
  val eventType = "automaton.started"
}

case class Automaton_ContentSentToCustomerEntry(
                                                `type`: String,
                                                `custom.decisiontree.nodeID`: Option[String],
                                                `custom.decisiontree.view`: Option[String],
                                                `custom.decisiontree.questions`: Option[List[String]]
                                              )
object Automaton_ContentSentToCustomerEntry {
  implicit val format: Format[Automaton_ContentSentToCustomerEntry] = Json.format[Automaton_ContentSentToCustomerEntry]
  val eventType = "automaton.contentSentToCustomer"
}


case class Automaton_CustomerResponded(
                                     `type`: String,
                                     senderName: String,
                                     `custom.decisiontree.nodeID`: Option[String],
                                     `custom.decisiontree.questions`: Option[List[String]]
                                   )

object Automaton_CustomerResponded {
  implicit val format: Format[Automaton_CustomerResponded] = Json.format[Automaton_CustomerResponded]
  val eventType = "automaton.customerResponded"
}

case class Automaton_Ended(`type`: String)

object Automaton_Ended {
  implicit val format: Format[Automaton_Ended] = Json.format[Automaton_Ended]
  val eventType = "automaton.ended"
}

case class Chat_AgentEnteredChatEntry(
                                     `type`: String,
                                     senderName: String,
                                     senderId: String,
                                     senderAlias: Option[String],
                                     content: Option[String],
                                     enterType: String
                                   )

object Chat_AgentEnteredChatEntry {
  implicit val format: Format[Chat_AgentEnteredChatEntry] = Json.format[Chat_AgentEnteredChatEntry]
  val eventType = "chat.agentEnterChat"
}

case class Chat_ClickstreamEntry(
                                 `type`: String,
                                 senderName: String,
                                 pageMarker: Option[String],
                                 historicPageMarkers: Option[String],
                                 pageURL: Option[String],
                                 systemInfo: Option[String],
                                 datapass: Option[String]
                               )

object Chat_ClickstreamEntry {
  implicit val format: Format[Chat_ClickstreamEntry] = Json.format[Chat_ClickstreamEntry]
  val eventType = "chat.clickstream"
}

case class Chat_CustomerChatlineSentEntry(
                                     `type`: String,
                                     senderName: String,
                                     content: String
                                   )

object Chat_CustomerChatlineSentEntry {
  implicit val format: Format[Chat_CustomerChatlineSentEntry] = Json.format[Chat_CustomerChatlineSentEntry]
  val eventType = "chat.customerChatlineSent"
}

case class Chat_OpenerDisplayed(
                                `type`: String,
                                senderName: String,
                                senderAlias: Option[String],
                                content: Option[String]
                              )

object Chat_OpenerDisplayed {
  implicit val format: Format[Chat_OpenerDisplayed] = Json.format[Chat_OpenerDisplayed]
  val eventType = "chat.openerDisplayed"
}


case class Engagement_RequestedEntry(
                                     `type`: String,
                                     senderName: String,
                                     resourceNeeded: String,
                                     `automaton.automatonID`: Option[String],
                                     businessUnit: String,
                                     agentGroup: String
                                   )
object Engagement_RequestedEntry {
  implicit val format: Format[Engagement_RequestedEntry] = Json.format[Engagement_RequestedEntry]
  val eventType = "engagement.requested"
}
