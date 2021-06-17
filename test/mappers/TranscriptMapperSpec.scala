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
import play.api.libs.json._

class TranscriptMapperSpec extends AnyWordSpec with Matchers {
  "mapTranscriptEntry" should {
    "process a transcript" in {
      val mapper = new TranscriptMapper
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
      val result = mapper.mapTranscriptEntry(Json.parse(input), "187286680131967188", 42)
      result.isSuccess mustBe true
      result.get mustBe Json.parse(
        """
          | {
          |    "auditSource": "digital-engagement-platform",
          |    "auditType": "EngagementTranscript",
          |    "eventId": "Transcript-187286680131967188-42",
          |    "generatedAt": "2020-09-30T13:23:38",
          |    "detail": {
          |        "engagementID": "187286680131967188",
          |        "transcriptIndex": 42,
          |        "type": "automaton.started",
          |        "senderId": "900020",
          |        "senderName": "businessRule"
          |    }
          | }
          |""".stripMargin
      )
    }
    "return error for a transcript without an iso time" in {
      val mapper = new TranscriptMapper
      val input =
        """
          | {
          |   "type": "automaton.started",
          |   "timestamp": 1614691418611,
          |   "senderId": "900020",
          |   "senderName": "businessRule"
          | }
          |""".stripMargin
      val result = mapper.mapTranscriptEntry(Json.parse(input), "187286680131967188", 42)
      result.isError mustBe true
    }
  }
  "mapTranscript" should {
    "handle no transcript" in {
      val mapper = new TranscriptMapper
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
      val mapper = new TranscriptMapper
      val input =
        """
          |{
          | "engagementID": "187286680131967188",
          | "transcript": [
          | {
          |   "type": "automaton.started",
          |   "iso": "2020-09-30T13:23:38+01:20",
          |   "timestamp": 1614691418611,
          |   "senderId": "900020",
          |   "senderName": "businessRule"
          | }
          | ]
          |}
          |""".stripMargin

      val result = mapper.mapTranscript(Json.parse(input))
      result mustBe Seq(
        Json.parse(
        """
          | {
          |    "auditSource": "digital-engagement-platform",
          |    "auditType": "EngagementTranscript",
          |    "eventId": "Transcript-187286680131967188-0",
          |    "generatedAt": "2020-09-30T13:23:38",
          |    "detail": {
          |        "engagementID": "187286680131967188",
          |        "transcriptIndex": 0,
          |        "type": "automaton.started",
          |        "senderId": "900020",
          |        "senderName": "businessRule"
          |    }
          | }
          |""".stripMargin)
      )
    }
    "handle bad data" in {
      val mapper = new TranscriptMapper
      val input =
        """
          |{
          |}
          |""".stripMargin

      val result = mapper.mapTranscript(Json.parse(input))
      result mustBe Seq()
    }
    "handle one transcript and one bad" in {
      val mapper = new TranscriptMapper
      val input =
        """
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
          | ]
          |}
          |""".stripMargin

      val result = mapper.mapTranscript(Json.parse(input))
      result mustBe Seq(
        Json.parse(
          """
            | {
            |    "auditSource": "digital-engagement-platform",
            |    "auditType": "EngagementTranscript",
            |    "eventId": "Transcript-187286680131967188-1",
            |    "generatedAt": "2020-09-30T13:23:38",
            |    "detail": {
            |        "engagementID": "187286680131967188",
            |        "transcriptIndex": 1,
            |        "type": "automaton.started",
            |        "senderId": "900020",
            |        "senderName": "businessRule"
            |    }
            | }
            |""".stripMargin)
      )
    }
    "create senderPID if senderID is @hmrc" in {
      val mapper = new TranscriptMapper
      val input =
        """
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
          | ]
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
            |    }
            | }
            |""".stripMargin)
      )
    }
  }
}