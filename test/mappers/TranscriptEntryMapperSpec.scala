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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json

class TranscriptEntryMapperSpec extends AnyWordSpec with Matchers with MockitoSugar {
  "mapTranscriptDetail" should {
    "process unknown entries" in {
      TranscriptEntryMapper.mapTranscriptDetail(Json.obj(), "187286680131967188", 42) mustBe None
    }

    "process automaton.started" in {
      val input = Json.parse("""
                               | {
                               |   "type": "automaton.started",
                               |   "iso": "2020-09-30T13:23:38+01:20",
                               |   "timestamp": 1614691418611,
                               |   "senderId": "900020",
                               |   "senderName": "businessRule",
                               |   "someOtherField": "someOtherValue"
                               | }
                               |""".stripMargin)

      val expected = Json.parse("""
                                  | {
                                  |  "engagementID": "187286680131967188",
                                  |  "transcriptIndex": 42,
                                  |  "type": "automaton.started",
                                  |  "senderId": "900020",
                                  |  "senderName": "businessRule"
                                  | }
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, "187286680131967188", 42) mustBe Some(expected)
    }

    "process automaton.contentSentToCustomer" in {
      val input = Json.parse("""
                               |{
                               |  "type": "automaton.contentSentToCustomer",
                               |  "iso": "2020-07-06T15:47:33+01:00",
                               |  "timestamp": 1594046853613,
                               |  "custom.decisiontree.nodeID": "HMRC_PreChat_CSG - Initial",
                               |  "custom.decisiontree.questions": [
                               |    "Name",
                               |    "Your question"
                               |  ]
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  | {
                                  |  "engagementID": "187286680131967188",
                                  |  "transcriptIndex": 42,
                                  |  "type": "automaton.contentSentToCustomer",
                                  |  "custom.decisiontree.nodeID": "HMRC_PreChat_CSG - Initial",
                                  |  "custom.decisiontree.questions": [
                                  |    "Name",
                                  |    "Your question"
                                  |  ]
                                  | }
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, "187286680131967188", 42) mustBe Some(expected)
    }

    "process automaton.contentSentToCustomer with missing optional fields" in {
      val input = Json.parse("""
                               |{
                               |  "type": "automaton.contentSentToCustomer",
                               |  "iso": "2020-07-06T15:47:33+01:00",
                               |  "timestamp": 1594046853613
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  | {
                                  |  "engagementID": "187286680131967188",
                                  |  "transcriptIndex": 42,
                                  |  "type": "automaton.contentSentToCustomer"
                                  | }
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, "187286680131967188", 42) mustBe Some(expected)
    }

    "process engagement.requested" in {
      val input = Json.parse("""
                               |{
                               |  "type": "engagement.requested",
                               |  "senderName": "system",
                               |  "iso": "2020-07-06T15:47:33+01:00",
                               |  "timestamp": 1594046853613,
                               |  "resourceNeeded": "automaton",
                               |  "automaton.automatonID": "2008",
                               |  "businessUnit": "HMRC-Training",
                               |  "agentGroup": "HMRC-Training"
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  |{
                                  | "engagementID": "187286680131967188",
                                  | "transcriptIndex": 42,
                                  | "type": "engagement.requested",
                                  | "senderName": "system",
                                  | "resourceNeeded": "automaton",
                                  | "automaton.automatonID": "2008",
                                  | "businessUnit": "HMRC-Training",
                                  | "agentGroup": "HMRC-Training"
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, "187286680131967188", 42) mustBe Some(expected)
    }

    "process automaton.customerResponded" in {
      val input = Json.parse("""
                               |{
                               |  "type": "automaton.customerResponded",
                               |  "senderName": "customer",
                               |  "iso": "2020-07-06T15:47:37+01:00",
                               |  "timestamp": 1594046857592,
                               |  "custom.decisiontree.nodeID": "HMRC_PreChat_CSG - Initial",
                               |  "custom.decisiontree.questions": [
                               |    "Name=John",
                               |    "Your question=This is a test"
                               |  ]
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  |{
                                  | "engagementID": "187286680131967188",
                                  | "transcriptIndex": 42,
                                  | "type": "automaton.customerResponded",
                                  | "senderName": "customer",
                                  | "custom.decisiontree.nodeID": "HMRC_PreChat_CSG - Initial",
                                  | "custom.decisiontree.questions": [
                                  |   "Name=John",
                                  |   "Your question=This is a test"
                                  | ]
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, "187286680131967188", 42) mustBe Some(expected)
    }

    "process automaton.ended" in {
      val input = Json.parse("""
                               |{
                               |  "type": "automaton.ended",
                               |  "iso": "2020-07-06T15:47:37+01:00",
                               |  "timestamp": 1594046857592
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  |{
                                  | "engagementID": "187286680131967188",
                                  | "transcriptIndex": 42,
                                  | "type": "automaton.ended"
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, "187286680131967188", 42) mustBe Some(expected)
    }

    "process chat.customerChatlineSent" in {
      val input = Json.parse("""
                               |{
                               |  "type": "chat.customerChatlineSent",
                               |  "content": "hello",
                               |  "senderName": "customer",
                               |  "senderAlias": "You",
                               |  "iso": "2020-06-04T13:21:31+01:00",
                               |  "timestamp": 1591273291761
                               |
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  |{
                                  | "engagementID": "187286680131967188",
                                  | "transcriptIndex": 42,
                                  | "type": "chat.customerChatlineSent",
                                  | "content": "hello",
                                  | "senderName": "customer"
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, "187286680131967188", 42) mustBe Some(expected)
    }

    "process agent.requested" in {
      val input = Json.parse("""
                               |{
                               |  "type": "agent.requested",
                               |  "senderName": "system",
                               |  "iso": "2020-06-02T13:01:20+01:00",
                               |  "timestamp": 1591099280061,
                               |  "senderId": "12345@hmrc",
                               |  "result": "ASSIGNED",
                               |  "businessUnit": "HMRC-CSG",
                               |  "agentGroup": "HMRC-CSG-AUTO"
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  |{
                                  | "engagementID": "187286680131967188",
                                  | "transcriptIndex": 42,
                                  | "type": "agent.requested",
                                  | "senderName": "system",
                                  | "senderId": "12345@hmrc",
                                  | "senderPID": "12345",
                                  | "result": "ASSIGNED",
                                  | "businessUnit": "HMRC-CSG",
                                  | "agentGroup": "HMRC-CSG-AUTO"
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, "187286680131967188", 42) mustBe Some(expected)
    }

    "process chat.agentEnterChat" in {
      val input = Json.parse("""
                               |{
                               |  "type": "chat.agentEnterChat",
                               |  "senderName": "system",
                               |  "senderAlias": "HMRC",
                               |  "iso": "2020-06-02T12:54:26+01:00",
                               |  "timestamp": 1591098866433,
                               |  "content": "Agent '12345@hmrc' enters chat (as HMRC)",
                               |  "senderId": "12345@hmrc",
                               |  "enterType": "TRANSFER",
                               |  "virtualAssistantSessionID": "@9360416e-e378-2b67-c0e7-a1c2d3ad3664@c365f18e-4f53-4604-ad92-391795882fce"
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  |{
                                  | "engagementID": "187286680131967188",
                                  | "transcriptIndex": 42,
                                  | "type": "chat.agentEnterChat",
                                  | "content": "Agent '12345@hmrc' enters chat (as HMRC)",
                                  | "senderName": "system",
                                  | "senderAlias": "HMRC",
                                  | "senderId": "12345@hmrc",
                                  | "senderPID": "12345",
                                  | "enterType": "TRANSFER"
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, "187286680131967188", 42) mustBe Some(expected)
    }
  }
}