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
