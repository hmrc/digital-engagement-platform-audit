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

import java.time.{LocalDateTime, ZoneOffset}

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json._
import services.NuanceIdDecryptionService
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

class TranscriptMapperSpec extends AnyWordSpec with Matchers with MockitoSugar {
  private val nuanceIdDecryptionService = mock[NuanceIdDecryptionService]
  when(nuanceIdDecryptionService.decryptDeviceId(any())).thenReturn("DecryptedDeviceId")
  when(nuanceIdDecryptionService.decryptSessionId(any())).thenReturn("DecryptedSessionId")

  "mapTranscriptEntryEvent" should {
    "process a transcript" in {
      val mapper = new TranscriptMapper(nuanceIdDecryptionService)
      val input =
        """
          | {
          |   "type": "automaton.started",
          |   "iso": "2020-09-30T13:23:38+01:20",
          |   "timestamp": 1614691418611,
          |   "senderId": "900020",
          |   "senderName": "businessRule"
          | }
          |""".stripMargin
      val result = mapper.mapTranscriptEntryEvent(
        Json.parse(input),
        "187286680131967188",
        42,
        Map[String, String]("tag1" -> "value1", "tag2" -> "value2"))
      result mustBe Some(
        ExtendedDataEvent(
          "digital-engagement-platform",
          "EngagementTranscript",
          "Transcript-187286680131967188-42",
          Map[String, String]("tag1" -> "value1", "tag2" -> "value2"),
          Json.parse(
            """
              |
              | {
              |  "engagementID": "187286680131967188",
              |  "transcriptIndex": 42,
              |  "type": "automaton.started",
              |  "senderId": "900020",
              |  "senderName": "businessRule"
              | }
              |""".stripMargin),
          LocalDateTime.parse("2020-09-30T13:23:38").toInstant(ZoneOffset.UTC)
        )
      )
    }
    "return error for a transcript without an iso time" in {
      val mapper = new TranscriptMapper(nuanceIdDecryptionService)
      val input =
        """
          | {
          |   "type": "automaton.started",
          |   "timestamp": 1614691418611,
          |   "senderId": "900020",
          |   "senderName": "businessRule"
          | }
          |""".stripMargin

      val result = mapper.mapTranscriptEntryEvent(
        Json.parse(input),
        "187286680131967188",
        42,
        Map[String, String]())
      result mustBe None
    }

  }
  "mapTranscriptEntry" should {
    "process a transcript" in {
      val mapper = new TranscriptMapper(nuanceIdDecryptionService)
      val input =
        """
          | {
          |   "type": "automaton.started",
          |   "iso": "2020-09-30T13:23:38+01:20",
          |   "timestamp": 1614691418611,
          |   "senderId": "900020",
          |   "senderName": "businessRule"
          | }
          |""".stripMargin
      val result = mapper.mapTranscriptEntry(
        Json.parse(input),
        "187286680131967188",
        42,
        TagsReads(TestEngagementData.testEngagementJson, nuanceIdDecryptionService))
      result.isSuccess mustBe true
      result.get mustBe Json.parse(
        """
          | {
          |   "auditSource": "digital-engagement-platform",
          |   "auditType": "EngagementTranscript",
          |   "eventId": "Transcript-187286680131967188-42",
          |   "generatedAt": "2020-09-30T13:23:38",
          |   "detail": {
          |     "engagementID": "187286680131967188",
          |     "transcriptIndex": 42,
          |     "type": "automaton.started",
          |     "senderId": "900020",
          |     "senderName": "businessRule"
          |   },
          |   "tags": {
          |     "clientIP": "81.97.99.4",
          |     "path": "https://www.tax.service.gov.uk/account-recovery/lost-user-id-password/check-emails?ui_locales=en&nuance=2008HMRCSITTest",
          |     "deviceID": "DecryptedDeviceId",
          |     "X-Session-ID": "DecryptedSessionId"
          |   }
          | }
          |""".stripMargin
      )
    }
    "return error for a transcript without an iso time" in {
      val mapper = new TranscriptMapper(nuanceIdDecryptionService)
      val input =
        """
          | {
          |   "type": "automaton.started",
          |   "timestamp": 1614691418611,
          |   "senderId": "900020",
          |   "senderName": "businessRule"
          | }
          |""".stripMargin

      val result = mapper.mapTranscriptEntry(
        Json.parse(input),
        "187286680131967188",
        42,
        TagsReads(TestEngagementData.testEngagementJson, nuanceIdDecryptionService))
      result.isError mustBe true
    }
  }
  "mapTranscript" should {
    "handle no transcript" in {
      val mapper = new TranscriptMapper(nuanceIdDecryptionService)
      val input =
        """
          |{
          | "engagementID": "187286680131967188",
          | "transcript": []
          |}
          |""".stripMargin

      val result = mapper.mapTranscript(Json.parse(input))
      result mustBe Seq()
    }
    "handle one transcript" in {
      val mapper = new TranscriptMapper(nuanceIdDecryptionService)
      val input =
        s"""
          |{
          | "engagementID": "187286680131967188",
          | "transcript": [
          | {
          |   "type": "automaton.started",
          |   "iso": "2020-09-30T13:23:38+01:20",
          |   "timestamp": 1614691418611,
          |   "senderId": "900020",
          |   "senderName": "businessRule"
          | }],
          | ${TestEngagementData.tagsDataNeeds}
          |}
          |""".stripMargin

      val result = mapper.mapTranscript(Json.parse(input))
      result mustBe Seq(
        Json.parse(
        """
          | {
          |   "auditSource": "digital-engagement-platform",
          |   "auditType": "EngagementTranscript",
          |   "eventId": "Transcript-187286680131967188-0",
          |   "generatedAt": "2020-09-30T13:23:38",
          |   "detail": {
          |     "engagementID": "187286680131967188",
          |     "transcriptIndex": 0,
          |     "type": "automaton.started",
          |     "senderId": "900020",
          |     "senderName": "businessRule"
          |   },
          |   "tags": {
          |     "clientIP": "81.97.99.4",
          |     "path": "https://www.tax.service.gov.uk/account-recovery/lost-user-id-password/check-emails?ui_locales=en&nuance=2008HMRCSITTest",
          |     "deviceID": "DecryptedDeviceId",
          |     "X-Session-ID": "DecryptedSessionId"
          |   }
          | }
          |""".stripMargin)
      )
    }
    "handle bad data" in {
      val mapper = new TranscriptMapper(nuanceIdDecryptionService)
      val input =
        """
          |{
          |}
          |""".stripMargin

      val result = mapper.mapTranscript(Json.parse(input))
      result mustBe Seq()
    }
    "handle one transcript and one bad" in {
      val mapper = new TranscriptMapper(nuanceIdDecryptionService)
      val input =
        s"""
          |{
          | "engagementID": "187286680131967188",
          | "transcript": [
          | {},
          | {
          |   "type": "automaton.started",
          |   "iso": "2020-09-30T13:23:38+01:20",
          |   "timestamp": 1614691418611,
          |   "senderId": "900020",
          |   "senderName": "businessRule"
          | },
          | {}
          | ],
          | ${TestEngagementData.tagsDataNeeds}
          |}
          |""".stripMargin

      val result = mapper.mapTranscript(Json.parse(input))
      result mustBe Seq(
        Json.parse(
          """
            | {
            |   "auditSource": "digital-engagement-platform",
            |   "auditType": "EngagementTranscript",
            |   "eventId": "Transcript-187286680131967188-1",
            |   "generatedAt": "2020-09-30T13:23:38",
            |   "detail": {
            |     "engagementID": "187286680131967188",
            |     "transcriptIndex": 1,
            |     "type": "automaton.started",
            |     "senderId": "900020",
            |     "senderName": "businessRule"
            |   },
            |   "tags": {
            |     "clientIP": "81.97.99.4",
            |     "path": "https://www.tax.service.gov.uk/account-recovery/lost-user-id-password/check-emails?ui_locales=en&nuance=2008HMRCSITTest",
            |     "deviceID": "DecryptedDeviceId",
            |     "X-Session-ID": "DecryptedSessionId"
            |   }
            | }
            |""".stripMargin)
      )
    }
    "create senderPID if senderID is @hmrc" in {
      val mapper = new TranscriptMapper(nuanceIdDecryptionService)
      val input =
        s"""
          |{
          | "engagementID": "187286680131967188",
          | "transcript": [
          | {
          |   "type": "agent.requested",
          |   "senderName": "system",
          |   "iso": "2020-10-15T16:16:29+01:00",
          |   "timestamp": 1602774989274,
          |   "senderId": "6017420@hmrc",
          |   "result": "ASSIGNED",
          |   "businessUnit": "HMRC-Training",
          |   "agentGroup": "HMRC-Training"
          | }
          | ],
          | ${TestEngagementData.tagsDataNeeds}
          |}
          |""".stripMargin

      val result = mapper.mapTranscript(Json.parse(input))
      result mustBe Seq(
        Json.parse(
          """
            | {
            |   "auditSource": "digital-engagement-platform",
            |   "auditType": "EngagementTranscript",
            |   "eventId": "Transcript-187286680131967188-0",
            |   "generatedAt": "2020-10-15T16:16:29",
            |   "detail": {
            |     "engagementID": "187286680131967188",
            |     "transcriptIndex": 0,
            |     "type": "agent.requested",
            |     "senderName": "system",
            |     "senderId": "6017420@hmrc",
            |     "senderPID": "6017420",
            |     "result": "ASSIGNED",
            |     "businessUnit": "HMRC-Training",
            |     "agentGroup": "HMRC-Training"
            |   },
            |   "tags": {
            |     "clientIP": "81.97.99.4",
            |     "path": "https://www.tax.service.gov.uk/account-recovery/lost-user-id-password/check-emails?ui_locales=en&nuance=2008HMRCSITTest",
            |     "deviceID": "DecryptedDeviceId",
            |     "X-Session-ID": "DecryptedSessionId"
            |   }
            | }
            |""".stripMargin)
      )
    }
  }
}