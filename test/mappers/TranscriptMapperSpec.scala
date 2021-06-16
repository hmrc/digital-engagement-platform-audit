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

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import javax.inject.Inject
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._
import services.LocalDateTimeService

class TranscriptMapper @Inject()(localDateTimeService: LocalDateTimeService) {
  private def transcriptPick = JsPath() \ 'transcripts
  private def isoPath = JsPath() \ 'iso
  def mapTranscriptEntry(transcript: JsValue, engagementId: String, index: Int): JsResult[JsValue] = {
    transcript.transform(isoPath.json.pick) match {
      case JsSuccess(JsString(datetime), _) =>
        val dt = LocalDateTime.parse(datetime, DateTimeFormatter.ISO_DATE_TIME)
        JsSuccess(Json.obj(
          "auditSource" -> "digital-engagement-platform",
          "auditType" -> "EngagementTranscript",
          "eventId" -> s"Transcript-$engagementId-$index",
          "generatedAt" -> dt
        ))
    }
  }

  def mapTranscript(engagement: JsValue): JsResult[JsArray] = {
    JsSuccess(JsArray())
  }
}

class TranscriptMapperSpec extends AnyWordSpec with Matchers {
  private val currentDateTime = LocalDateTime.of(1999, 3, 14, 13, 33)

  private object LocalDateTimeServiceStub extends LocalDateTimeService {
    override def now: LocalDateTime = currentDateTime
  }

  "mapTranscriptEntry" should {
    "process a transcript" in {
      val mapper = new TranscriptMapper(LocalDateTimeServiceStub)
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
          |    "generatedAt": "2020-09-30T13:23:38"
          | }
          |""".stripMargin

        //          |    "detail": {
        //          |        "engagementID": "187286680131967188",
        //          |        "transcriptIndex": 42,
        //          |        "type": "automaton.started",
        //          |        "senderId": "900020",
        //          |        "senderName": "businessRule"
        //          |    }

      )
    }
  }
  "mapTranscript" should {
    "handle no transcript" in {
      val mapper = new TranscriptMapper(LocalDateTimeServiceStub)
      val input =
        """
          |{
          | "engagementID": "187286680131967188",
          | "transcript": []
          |}
          |""".stripMargin

      val result = mapper.mapTranscript(Json.parse(input))
      result.isSuccess mustBe true
      result.get mustBe JsArray()
    }
    "handle one transcript" ignore {
      val mapper = new TranscriptMapper(LocalDateTimeServiceStub)
      val input =
        """
          |{
          | "engagementID": "187286680131967188",
          | "transcript": [
          | {
          |   "type": "automaton.started",
          |   "iso": "2021-03-02T13:23:38+00:00",
          |   "timestamp": 1614691418611,
          |   "senderId": "900020",
          |   "senderName": "businessRule"
          | }
          | ]
          |}
          |""".stripMargin

      val result = mapper.mapTranscript(Json.parse(input))
      result.isSuccess mustBe true
      result.get mustBe JsArray()
      result.get(0) mustBe Json.obj(
        "auditSource" -> "digital-engagement-platform",
        "auditType" -> "EngagementTranscript",
        "eventId" -> "Transcript-187286680131967188-0",
        "generatedAt" -> "1999-03-14T13:33:00",
      )
    }
  }
}