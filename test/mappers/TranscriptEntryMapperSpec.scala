/*
 * Copyright 2022 HM Revenue & Customs
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
  private val testEngagementId = "187286680131967188"
  private val testIndex = 42

  "mapTranscriptDetail" should {
    "process unknown entries" in {
      TranscriptEntryMapper.mapTranscriptDetail(Json.obj(), testEngagementId, testIndex) mustBe None
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

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
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

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
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

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
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

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
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

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
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

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
    }

    "process chat.automatonAgentOutcome" in {
      val input = Json.parse("""
                               |{
                               |  "type": "chat.automatonAgentOutcome",
                               |  "content": "<!-- Data Pass -->\n- Selections:\n    1) HMRC_PreChat_CSG - Initial\n- name: Fred Flintstone\n- chat-reason: I've started a new job and its more pay a year and I need to update my tax credits claim.\n",
                               |  "senderName": "system",
                               |  "iso": "2021-06-29T14:37:34+01:00",
                               |  "timestamp": 1624973854204
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  |{
                                  | "engagementID": "187286680131967188",
                                  | "transcriptIndex": 42,
                                  | "type": "chat.automatonAgentOutcome",
                                  | "content": "<!-- Data Pass -->\n- Selections:\n    1) HMRC_PreChat_CSG - Initial\n- name: Fred Flintstone\n- chat-reason: I've started a new job and its more pay a year and I need to update my tax credits claim.\n",
                                  | "senderName": "system"
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
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

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
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

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
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
                                  | "enterType": "TRANSFER",
                                  | "virtualAssistantSessionID": "@9360416e-e378-2b67-c0e7-a1c2d3ad3664@c365f18e-4f53-4604-ad92-391795882fce"
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
    }

    "process chat.clickStream" in {
      val input = Json.parse("""
                               |{
                               |  "type": "chat.clickstream",
                               |  "senderName": "system",
                               |  "iso": "2020-06-02T13:01:20+01:00",
                               |  "timestamp": 1591099280437,
                               |  "pageMarker": "HMRC-O-COVID_Support",
                               |  "historicPageMarkers": "HMRC-O-COVID_Support",
                               |  "systemInfo": "OS: Windows 8,Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.61 Safari/537.36,Browser: Chrome 83.0.4103.61"
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  |{
                                  | "engagementID": "187286680131967188",
                                  | "transcriptIndex": 42,
                                  | "type": "chat.clickstream",
                                  | "senderName": "system",
                                  | "pageMarker": "HMRC-O-COVID_Support",
                                  | "historicPageMarkers": "HMRC-O-COVID_Support",
                                  | "systemInfo": "OS: Windows 8,Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.61 Safari/537.36,Browser: Chrome 83.0.4103.61"
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
    }

    "process chat.agentAutoOpenerScriptSent" in {
      val input = Json.parse("""
                               |{
                               |  "type": "chat.agentAutoOpenerScriptSent",
                               |  "content": "I'm Amanda. I will be happy to assist you today.",
                               |  "senderName": "agent",
                               |  "senderAlias": "HMRC",
                               |  "iso": "2020-10-15T15:41:19+01:00",
                               |  "timestamp": 1602772879568,
                               |  "senderId": "6017420@hmrc"
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  |{
                                  | "engagementID": "187286680131967188",
                                  | "transcriptIndex": 42,
                                  | "type": "chat.agentAutoOpenerScriptSent",
                                  | "content": "I'm Amanda. I will be happy to assist you today.",
                                  | "senderName": "agent",
                                  | "senderAlias": "HMRC",
                                  | "senderId": "6017420@hmrc",
                                  | "senderPID": "6017420"
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
    }

    "process chat.openerDisplayed" in {
      val input = Json.parse("""
                               |{
                               |  "type": "chat.openerDisplayed",
                               |  "content": "Hello, I’m HMRC’s digital assistant.<br/><br/>If you are registered to sign in to Self Assessment, I can recover your Government Gateway user ID and email it to you.  You can then use it to reset your password.<br/><br/>Do you want to continue?<br/><ul><li><a href=\"#\" data-vtz-link-type=\"Dialog\" data-vtz-jump=\"9438f60c-8792-4751-bbc5-7ee4ed9cc051\" onclick=\"inqFrame.Application.sendVALinkClicked(event)\">Yes</a></li><li><a href=\"#\" data-vtz-link-type=\"Dialog\" data-vtz-jump=\"d66545f2-c0c7-46a8-9c38-a27dd0c18e8b\" onclick=\"inqFrame.Application.sendVALinkClicked(event)\">No</a></li></ul>",
                               |  "senderName": "opener",
                               |  "senderAlias": "HMRC",
                               |  "iso": "2020-07-29T11:58:04+01:00",
                               |  "timestamp": 1596020284488
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  |{
                                  | "engagementID": "187286680131967188",
                                  | "transcriptIndex": 42,
                                  | "type": "chat.openerDisplayed",
                                  | "content": "Hello, I’m HMRC’s digital assistant.<br/><br/>If you are registered to sign in to Self Assessment, I can recover your Government Gateway user ID and email it to you.  You can then use it to reset your password.<br/><br/>Do you want to continue?<br/><ul><li><a href=\"#\" data-vtz-link-type=\"Dialog\" data-vtz-jump=\"9438f60c-8792-4751-bbc5-7ee4ed9cc051\" onclick=\"inqFrame.Application.sendVALinkClicked(event)\">Yes</a></li><li><a href=\"#\" data-vtz-link-type=\"Dialog\" data-vtz-jump=\"d66545f2-c0c7-46a8-9c38-a27dd0c18e8b\" onclick=\"inqFrame.Application.sendVALinkClicked(event)\">No</a></li></ul>",
                                  | "senderName": "opener",
                                  | "senderAlias": "HMRC"
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
    }

    "process chat.agentChatlineSent" in {
      val input = Json.parse("""
                               |{
                               |  "type": "chat.agentChatlineSent",
                               |  "content": "hello",
                               |  "senderName": "agent",
                               |  "senderAlias": "HMRC",
                               |  "iso": "2020-10-01T16:12:17+01:00",
                               |  "timestamp": 1601565137401,
                               |  "senderId": "12345@hmrc",
                               |  "lineType": "freehand"
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  |{
                                  | "engagementID": "187286680131967188",
                                  | "transcriptIndex": 42,
                                  | "type": "chat.agentChatlineSent",
                                  | "content": "hello",
                                  | "senderName": "agent",
                                  | "senderAlias": "HMRC",
                                  | "senderId": "12345@hmrc",
                                  | "senderPID": "12345",
                                  | "lineType": "freehand"
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
    }

    "process conversionFunnel.assisted" in {
      val input = Json.parse("""
                               |{
                               |  "type": "conversionFunnel.assisted",
                               |  "senderName": "system",
                               |  "iso": "2020-10-01T16:12:17+01:00",
                               |  "timestamp": 1601565137540
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  |{
                                  | "engagementID": "187286680131967188",
                                  | "transcriptIndex": 42,
                                  | "type": "conversionFunnel.assisted",
                                  | "senderName": "system"
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
    }

    "process conversionFunnel.interacted" in {
      val input = Json.parse("""
                               |{
                               |  "type": "conversionFunnel.interacted",
                               |  "senderName": "system",
                               |  "iso": "2020-10-01T16:12:17+01:00",
                               |  "timestamp": 1601565137540
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  |{
                                  | "engagementID": "187286680131967188",
                                  | "transcriptIndex": 42,
                                  | "type": "conversionFunnel.interacted",
                                  | "senderName": "system"
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
    }

    "process chat.dispositionStarted" in {
      val input = Json.parse("""
                               |{
                               |  "type": "chat.dispositionStarted",
                               |  "iso": "2020-10-01T16:20:35+01:00",
                               |  "timestamp": 1601565635783,
                               |  "senderId": "12345@hmrc"
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  |{
                                  | "engagementID": "187286680131967188",
                                  | "transcriptIndex": 42,
                                  | "type": "chat.dispositionStarted",
                                  | "senderId": "12345@hmrc",
                                  | "senderPID": "12345"
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
    }

    "process chat.agentExited" in {
      val input = Json.parse("""
                               |{
                               |  "type": "chat.agentExited",
                               |  "senderName": "agent",
                               |  "senderAlias": "HMRC",
                               |  "iso": "2020-10-15T16:16:11+01:00",
                               |  "timestamp": 1602774971936,
                               |  "senderId": "12345@hmrc",
                               |  "disposition": "Enquiry Handled - Customer Question:No answer given by customer or Not asked as chat terminated",
                               |  "owner": true,
                               |  "escalated": false
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  |{
                                  | "engagementID": "187286680131967188",
                                  | "transcriptIndex": 42,
                                  | "type": "chat.agentExited",
                                  | "senderName": "agent",
                                  | "senderAlias": "HMRC",
                                  | "senderId": "12345@hmrc",
                                  | "senderPID": "12345",
                                  | "disposition": "Enquiry Handled - Customer Question:No answer given by customer or Not asked as chat terminated",
                                  | "owner": true,
                                  | "escalated": false
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
    }

    "process chat.statusDisplayed" in {
      val input = Json.parse("""
                               |{
                               |  "type": "chat.statusDisplayed",
                               |  "content": "The customer was disconnected for 2:26 minutes but has now reestablished connection",
                               |  "senderName": "system",
                               |  "iso": "2020-12-02T15:17:34+00:00",
                               |  "timestamp": 1606922254025,
                               |  "showedToCustomer": false
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  |{
                                  | "engagementID": "187286680131967188",
                                  | "transcriptIndex": 42,
                                  | "type": "chat.statusDisplayed",
                                  | "content": "The customer was disconnected for 2:26 minutes but has now reestablished connection",
                                  | "senderName": "system",
                                  | "showedToCustomer": false
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
    }

    "process chat.virtualAssistantSessionStarted" in {
      val input = Json.parse("""
                               |{
                               |  "type": "chat.virtualAssistantSessionStarted",
                               |  "senderName": "system",
                               |  "iso": "2021-02-15T16:21:20+00:00",
                               |  "timestamp": 1613406080874,
                               |  "senderId": "virtualAssistant.nina",
                               |  "virtualAssistantSessionID": "@30517c78-dfe8-0663-cb87-f145ecd69bf0@d20f339a-13ec-4ee9-b206-3a8f7a27661d"
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  |{
                                  | "engagementID": "187286680131967188",
                                  | "transcriptIndex": 42,
                                  | "type": "chat.virtualAssistantSessionStarted",
                                  | "senderName": "system",
                                  | "senderId": "virtualAssistant.nina",
                                  | "virtualAssistantSessionID": "@30517c78-dfe8-0663-cb87-f145ecd69bf0@d20f339a-13ec-4ee9-b206-3a8f7a27661d"
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
    }

    "process chat.scriptlineSent" in {
      val input = Json.parse("""
                               |{
                               |  "type": "chat.scriptlineSent",
                               |  "content": "<div onclick=\"window.inqFrame.Application.sendVALinkClicked(event);\" >Try asking me in a few words what you'd like help with and I may be able to assist you.</div>",
                               |  "senderName": "agent",
                               |  "senderAlias": "HMRC",
                               |  "iso": "2021-02-15T16:21:20+00:00",
                               |  "timestamp": 1613406080876,
                               |  "senderId": "virtualAssistant.nina",
                               |  "lineType": "script"
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  |{
                                  | "engagementID": "187286680131967188",
                                  | "transcriptIndex": 42,
                                  | "type": "chat.scriptlineSent",
                                  | "content": "<div onclick=\"window.inqFrame.Application.sendVALinkClicked(event);\" >Try asking me in a few words what you'd like help with and I may be able to assist you.</div>",
                                  | "senderName": "agent",
                                  | "senderAlias": "HMRC",
                                  | "senderId": "virtualAssistant.nina",
                                  | "lineType": "script"
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
    }

    "process chat.customerExited" in {
      val input = Json.parse("""
                               |{
                               |  "type": "chat.customerExited",
                               |  "content": "Customer Closed Chat Window",
                               |  "senderName": "customer",
                               |  "senderAlias": "HMRC",
                               |  "iso": "2021-02-15T16:21:32+00:00",
                               |  "timestamp": 1613406092549
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  |{
                                  | "engagementID": "187286680131967188",
                                  | "transcriptIndex": 42,
                                  | "type": "chat.customerExited",
                                  | "content": "Customer Closed Chat Window",
                                  | "senderName": "customer",
                                  | "senderAlias": "HMRC"
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
    }

    "process chat.transferRequested" in {
      val input = Json.parse("""
                               |{
                               |  "type": "chat.transferRequested",
                               |  "senderName": "system",
                               |  "iso": "2020-12-02T15:17:34+00:00",
                               |  "timestamp": 1606922254028,
                               |  "senderId": "virtualAssistant.nina",
                               |  "result": "QUEUED",
                               |  "businessUnit": "HMRC-Training",
                               |  "targetBusinessUnit": "HMRC-CSG",
                               |  "agentGroup": "HMRC-Training",
                               |  "targetAgentGroup": "HMRC-PTO-SA"
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  |{
                                  | "engagementID": "187286680131967188",
                                  | "transcriptIndex": 42,
                                  | "type": "chat.transferRequested",
                                  | "senderName": "system",
                                  | "senderId": "virtualAssistant.nina",
                                  | "result": "QUEUED",
                                  | "businessUnit": "HMRC-Training",
                                  | "targetBusinessUnit": "HMRC-CSG",
                                  | "agentGroup": "HMRC-Training",
                                  | "targetAgentGroup": "HMRC-PTO-SA"
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
    }

    "process chat.queueWaitDisplayed" in {
      val input = Json.parse("""
                               |{
                               |  "type": "chat.queueWaitDisplayed",
                               |  "content": "Thank you for your patience, the next available adviser will be with you shortly. You are 8 in the queue.",
                               |  "senderName": "system",
                               |  "iso": "2020-12-02T15:17:38+00:00",
                               |  "timestamp": 1606922258773
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  |{
                                  | "engagementID": "187286680131967188",
                                  | "transcriptIndex": 42,
                                  | "type": "chat.queueWaitDisplayed",
                                  | "content": "Thank you for your patience, the next available adviser will be with you shortly. You are 8 in the queue.",
                                  | "senderName": "system"
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
    }

    "process chat.customerLostConnection" in {
      val input = Json.parse("""
                               |{
                               |  "type": "chat.customerLostConnection",
                               |  "content": "The customer closed the browser window",
                               |  "senderName": "customer",
                               |  "iso": "2020-12-02T15:23:38+00:00",
                               |  "timestamp": 1606922618365,
                               |  "senderAlias": "You"
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  |{
                                  | "engagementID": "187286680131967188",
                                  | "transcriptIndex": 42,
                                  | "type": "chat.customerLostConnection",
                                  | "content": "The customer closed the browser window",
                                  | "senderName": "customer",
                                  | "senderAlias": "You"
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
    }

    "process chat.agentLostConnection" in {
      val input = Json.parse("""
                               |{
                               |  "type": "chat.agentLostConnection",
                               |  "content": "Agent '12345@hmrc' loses connection",
                               |  "senderName": "agent",
                               |  "senderAlias": "HMRC",
                               |  "iso": "2020-10-15T16:33:25+01:00",
                               |  "timestamp": 1602776005290,
                               |  "senderId": "12345@hmrc"
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  |{
                                  | "engagementID": "187286680131967188",
                                  | "transcriptIndex": 42,
                                  | "type": "chat.agentLostConnection",
                                  | "content": "Agent '12345@hmrc' loses connection",
                                  | "senderName": "agent",
                                  | "senderAlias": "HMRC",
                                  | "senderId": "12345@hmrc",
                                  | "senderPID": "12345"
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
    }

    "process queue.abandoned" in {
      val input = Json.parse("""
                               |{
                               |  "type": "queue.abandoned",
                               |  "iso": "2020-12-02T15:23:38+00:00",
                               |  "timestamp": 1606922618366
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  |{
                                  | "engagementID": "187286680131967188",
                                  | "transcriptIndex": 42,
                                  | "type": "queue.abandoned"
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
    }

    "process queue.removed" in {
      val input = Json.parse("""
                               |{
                               |  "type": "queue.removed",
                               |  "iso": "2020-12-02T15:23:38+00:00",
                               |  "timestamp": 1606922618366
                               |}
                               |""".stripMargin)

      val expected = Json.parse("""
                                  |{
                                  | "engagementID": "187286680131967188",
                                  | "transcriptIndex": 42,
                                  | "type": "queue.removed"
                                  |}
                                  |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe Some(expected)
    }

    "process entry with missing fields" in {
      val input = Json.parse("""
                               |{
                               |  "type": "chat.customerLostConnection",
                               |  "iso": "2020-12-02T15:23:38+00:00",
                               |  "timestamp": 1606922618365
                               |}
                               |""".stripMargin)

      TranscriptEntryMapper.mapTranscriptDetail(input, testEngagementId, testIndex) mustBe None
    }
  }
}