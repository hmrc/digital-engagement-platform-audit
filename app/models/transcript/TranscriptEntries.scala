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
                                  senderId: Option[String],
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

case class Chat_AgentAutoOpenerSentEntry(
                                       `type`: String,
                                       senderName: String,
                                       senderId: String,
                                       senderAlias: Option[String],
                                       content: Option[String]
                                     )

object Chat_AgentAutoOpenerSentEntry {
  implicit val format: Format[Chat_AgentAutoOpenerSentEntry] = Json.format[Chat_AgentAutoOpenerSentEntry]
  val eventType = "chat.agentAutoOpenerScriptSent"
}

case class Chat_AgentChatlineSentEntry(
                                          `type`: String,
                                          senderName: String,
                                          senderId: String,
                                          senderAlias: Option[String],
                                          content: Option[String],
                                          lineType: Option[String]
                                        )

object Chat_AgentChatlineSentEntry {
  implicit val format: Format[Chat_AgentChatlineSentEntry] = Json.format[Chat_AgentChatlineSentEntry]
  val eventType = "chat.agentChatlineSent"
}

case class Chat_AgentEnterChatEntry(
                                     `type`: String,
                                     senderName: String,
                                     senderId: String,
                                     senderAlias: Option[String],
                                     content: Option[String],
                                     enterType: String
                                   )

object Chat_AgentEnterChatEntry {
  implicit val format: Format[Chat_AgentEnterChatEntry] = Json.format[Chat_AgentEnterChatEntry]
  val eventType = "chat.agentEnterChat"
}

case class Chat_AgentExitedEntry(
                                      `type`: String,
                                      senderName: String,
                                      senderId: String,
                                      senderAlias: Option[String],
                                      owner: Boolean,
                                      disposition: Option[String],
                                      escalated: Option[Boolean],
                                      escalatedText: Option[String]
                                    )

object Chat_AgentExitedEntry {
  implicit val format: Format[Chat_AgentExitedEntry] = Json.format[Chat_AgentExitedEntry]
  val eventType = "chat.agentExited"
}

case class Chat_AgentLostConnection(
                                     `type`: String,
                                     senderId: Option[String],
                                     senderName: String,
                                     senderAlias: Option[String],
                                     content: String
                                   )

object Chat_AgentLostConnection {
  implicit val format: Format[Chat_AgentLostConnection] = Json.format[Chat_AgentLostConnection]
  val eventType = "chat.agentLostConnection"
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

case class Chat_CustomerExitedEntry(
                                     `type`: String,
                                     senderName: String,
                                     content: Option[String]
                                   )

object Chat_CustomerExitedEntry {
  implicit val format: Format[Chat_CustomerExitedEntry] = Json.format[Chat_CustomerExitedEntry]
  val eventType = "chat.customerExited"
}

case class Chat_CustomerLostConnection(
                                    `type`: String,
                                    senderName: String,
                                    senderAlias: Option[String],
                                    content: String
                                  )

object Chat_CustomerLostConnection {
  implicit val format: Format[Chat_CustomerLostConnection] = Json.format[Chat_CustomerLostConnection]
  val eventType = "chat.customerLostConnection"
}

case class Chat_DispositionStarted(
                                    `type`: String,
                                    senderId: String,
                                    senderAlias: Option[String]
                                  )

object Chat_DispositionStarted {
  implicit val format: Format[Chat_DispositionStarted] = Json.format[Chat_DispositionStarted]
  val eventType = "chat.dispositionStarted"
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

case class Chat_QueueWaitDisplayed(
                                `type`: String,
                                senderName: String,
                                senderAlias: Option[String],
                                content: String
                              )

object Chat_QueueWaitDisplayed {
  implicit val format: Format[Chat_QueueWaitDisplayed] = Json.format[Chat_QueueWaitDisplayed]
  val eventType = "chat.queueWaitDisplayed"
}

case class Chat_ScriptlineSentEntry(
                                        `type`: String,
                                        senderName: String,
                                        senderId: String,
                                        senderAlias: Option[String],
                                        content: String,
                                        lineType: Option[String]
                                      )

object Chat_ScriptlineSentEntry {
  implicit val format: Format[Chat_ScriptlineSentEntry] = Json.format[Chat_ScriptlineSentEntry]
  val eventType = "chat.scriptlineSent"
}

case class Chat_StatusDisplayed(
                                 `type`: String,
                                 senderName: String,
                                 senderAlias: Option[String],
                                 content: String,
                                 showedToCustomer: Option[Boolean]
                               )

object Chat_StatusDisplayed {
  implicit val format: Format[Chat_StatusDisplayed] = Json.format[Chat_StatusDisplayed]
  val eventType = "chat.statusDisplayed"
}

case class Chat_TransferredRequested(
                                      `type`: String,
                                      senderName: String,
                                      senderId: String,
                                      businessUnit: String,
                                      targetBusinessUnit: String,
                                      result: Option[String],
                                      newAgentID: Option[String],
                                      agentGroup: Option[String],
                                      targetAgentGroup: Option[String]
                                    )
object Chat_TransferredRequested {
  implicit val format: Format[Chat_TransferredRequested] = Json.format[Chat_TransferredRequested]
  val eventType = "chat.transferRequested"
}

case class Chat_VirtualAssistantSessionStarted(
                                                `type`: String,
                                                senderId: String,
                                                senderName: String,
                                                virtualAssistantSessionID: String
                                              )

object Chat_VirtualAssistantSessionStarted {
  implicit val format: Format[Chat_VirtualAssistantSessionStarted] = Json.format[Chat_VirtualAssistantSessionStarted]
  val eventType = "chat.virtualAssistantSessionStarted"
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

case class ConversionFunnel_Assisted(
                                      `type`: String,
                                      senderName: String
                                    )

object ConversionFunnel_Assisted {
  implicit val format: Format[ConversionFunnel_Assisted] = Json.format[ConversionFunnel_Assisted]
  val eventType = "conversionFunnel.assisted"
}

case class ConversionFunnel_Interacted(
                                        `type`: String,
                                        senderName: String
                                      )

object ConversionFunnel_Interacted {
  implicit val format: Format[ConversionFunnel_Interacted] = Json.format[ConversionFunnel_Interacted]
  val eventType = "conversionFunnel.interacted"
}

case class Queue_Abandoned(`type`: String)

object Queue_Abandoned {
  implicit val format: Format[Queue_Abandoned] = Json.format[Queue_Abandoned]
  val eventType = "queue.abandoned"
}

