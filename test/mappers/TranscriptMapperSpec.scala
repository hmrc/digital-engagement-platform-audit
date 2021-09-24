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

import java.time.{Instant, LocalDateTime, ZoneOffset}
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
          |   "senderName": "businessRule",
          |   "someOtherField": "someOtherValue"
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
          "18bc0957-7fc0-3fb7-aadd-d49dc6df4bb8",
          Map[String, String]("tag1" -> "value1", "tag2" -> "value2"),
          Json.parse(
            """
              | {
              |  "engagementID": "187286680131967188",
              |  "transcriptIndex": 42,
              |  "type": "automaton.started",
              |  "senderId": "900020",
              |  "senderName": "businessRule"
              | }
              |""".stripMargin),
            Instant.parse("2021-03-02T13:23:38.611Z")
        )
      )
    }

    "return None if unknown type" in {
      val mapper = new TranscriptMapper(nuanceIdDecryptionService)
      val input =
        """
          | {
          |   "iso": "2020-09-30T13:23:38+01:20",
          |   "type": "wacky races"
          | }
          |""".stripMargin

      val result = mapper.mapTranscriptEntryEvent(
        Json.parse(input),
        "187286680131967188",
        42,
        Map[String, String]())
      result mustBe None
    }

    "return error for a transcript without a timestamp" in {
      val mapper = new TranscriptMapper(nuanceIdDecryptionService)
      val input =
        """
          | {
          |   "type": "automaton.started",
          |   "iso": "2020-09-30T13:23:38+01:20",
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

    "process a transcript with an HMRC sender ID" in {
      val mapper = new TranscriptMapper(nuanceIdDecryptionService)
      val input =
        """
          | {
          |   "type": "automaton.started",
          |   "iso": "2020-09-30T13:23:38+01:20",
          |   "timestamp": 1614691418611,
          |   "senderId": "900020@hmrc",
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
          "18bc0957-7fc0-3fb7-aadd-d49dc6df4bb8",
          Map[String, String]("tag1" -> "value1", "tag2" -> "value2"),
          Json.parse(
            """
              |
              | {
              |  "engagementID": "187286680131967188",
              |  "transcriptIndex": 42,
              |  "type": "automaton.started",
              |  "senderId": "900020@hmrc",
              |  "senderPID": "900020",
              |  "senderName": "businessRule"
              | }
              |""".stripMargin),
          Instant.parse("2021-03-02T13:23:38.611Z")
        )
      )
    }
  }

  "mapTranscriptEvents" should {
    "handle no transcript" in {
      val mapper = new TranscriptMapper(nuanceIdDecryptionService)
      val input =
        """
          |{
          | "engagementID": "187286680131967188",
          | "transcript": []
          |}
          |""".stripMargin

      val result = mapper.mapTranscriptEvents(Json.parse(input))
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

      val result = mapper.mapTranscriptEvents(Json.parse(input))
      result mustBe Seq(
        ExtendedDataEvent(
          "digital-engagement-platform",
          "EngagementTranscript",
          "9231d778-77b0-3c2f-9731-fd578627b84f",
          Map[String, String](
            "clientIP" -> "81.97.99.4",
            "path" -> "https://www.tax.service.gov.uk/account-recovery/lost-user-id-password/check-emails?ui_locales=en&nuance=2008HMRCSITTest",
            "deviceID" -> "DecryptedDeviceId",
            "X-Session-ID" -> "DecryptedSessionId"
          ),
          Json.parse(
            """
              |
              | {
              |  "engagementID": "187286680131967188",
              |  "transcriptIndex": 0,
              |  "type": "automaton.started",
              |  "senderId": "900020",
              |  "senderName": "businessRule"
              | }
              |""".stripMargin),
          Instant.parse("2021-03-02T13:23:38.611Z")
        )
      )
    }

    "handle bad data" in {
      val mapper = new TranscriptMapper(nuanceIdDecryptionService)
      val input =
        """
          |{
          |}
          |""".stripMargin

      val result = mapper.mapTranscriptEvents(Json.parse(input))
      result mustBe Seq()
    }

    "handle transcripts with bad entries" in {
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
           | {}],
           | ${TestEngagementData.tagsDataNeeds}
           |}
           |""".stripMargin

      val result = mapper.mapTranscriptEvents(Json.parse(input))
      result mustBe Seq(
        ExtendedDataEvent(
          "digital-engagement-platform",
          "EngagementTranscript",
          "2965eda8-6a77-3f2a-b51b-2499fe39a69b",
          Map[String, String](
            "clientIP" -> "81.97.99.4",
            "path" -> "https://www.tax.service.gov.uk/account-recovery/lost-user-id-password/check-emails?ui_locales=en&nuance=2008HMRCSITTest",
            "deviceID" -> "DecryptedDeviceId",
            "X-Session-ID" -> "DecryptedSessionId"
          ),
          Json.parse(
            """
              |
              | {
              |  "engagementID": "187286680131967188",
              |  "transcriptIndex": 1,
              |  "type": "automaton.started",
              |  "senderId": "900020",
              |  "senderName": "businessRule"
              | }
              |""".stripMargin),
          Instant.parse("2021-03-02T13:23:38.611Z")
        )
      )
    }
  }
}