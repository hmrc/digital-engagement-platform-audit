/*
 * Copyright 2023 HM Revenue & Customs
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

package auditing

import java.time.LocalDateTime

import config.AppConfig
import connectors.NuanceReportingRequest
import models.{NuanceBadRequest, ValidNuanceReportingResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsArray, JsValue}
import services.NuanceReportingService
import utils.JsonUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class HistoricAuditingSpec extends AnyWordSpec with Matchers with MockitoSugar {
  private val testStartDate = LocalDateTime.parse("2020-04-20T00:00:10")
  private val testEndDate = LocalDateTime.parse("2020-07-17T00:00:20")
  private val auditingChunkSize = 150

  // Expected requests generated internally.
  private val expectedInitialRequest = NuanceReportingRequest(start = auditingChunkSize * 0, rows = 0, testStartDate, testEndDate)
  private val expectedRequest0 = NuanceReportingRequest(start = auditingChunkSize * 0, rows = auditingChunkSize, testStartDate, testEndDate)
  private val expectedRequest1 = NuanceReportingRequest(start = auditingChunkSize * 1, rows = auditingChunkSize, testStartDate, testEndDate)
  private val expectedRequest2 = NuanceReportingRequest(start = auditingChunkSize * 2, rows = 30, testStartDate, testEndDate)

  "auditing.HistoricAuditingSpec" must {
    "read historic data and send audit events" in {

      val responseJson = JsonUtils.getJsonValueFromFile("HistoricSampleSmall.json")
      val nuanceReportingResponse = responseJson.as[ValidNuanceReportingResponse]
      nuanceReportingResponse.engagements.as[List[JsValue]].size mustBe 3

      val expectedRequest = NuanceReportingRequest(
        0,
        nuanceReportingResponse.numFound,
        testStartDate,
        testEndDate)

      val reportingService = mock[NuanceReportingService]
      when(reportingService.getHistoricData(any())).thenReturn(Future.successful(nuanceReportingResponse))

      val engagementAuditing = mock[EngagementAuditing]
      when(engagementAuditing.processEngagements(any())).thenReturn(Future.successful(Seq(Seq())))

      val appConfig = mock[AppConfig]
      when(appConfig.auditingChunkSize).thenReturn(auditingChunkSize)

      val auditing = new HistoricAuditing(reportingService, engagementAuditing, appConfig)

      val result = Await.result(auditing.auditDateRange(testStartDate, testEndDate), Duration.Inf)
      result mustBe Seq(new SuccessfulHistoricAuditingResult(expectedRequest))

      verify(reportingService).getHistoricData(expectedInitialRequest)
      verify(reportingService).getHistoricData(expectedRequest)
      verify(engagementAuditing, times(1)).processEngagements(any())
    }

    "return error for reporting failure" in {
      val reportingService = mock[NuanceReportingService]
      when(reportingService.getHistoricData(any())).thenReturn(Future.successful(NuanceBadRequest))

      val engagementAuditing = mock[EngagementAuditing]

      val appConfig = mock[AppConfig]
      when(appConfig.auditingChunkSize).thenReturn(auditingChunkSize)

      val auditing = new HistoricAuditing(reportingService, engagementAuditing, appConfig)

      val result = Await.result(auditing.auditDateRange(testStartDate, testEndDate), Duration.Inf)

      verify(reportingService).getHistoricData(expectedInitialRequest)
      result mustBe Seq(new FailedHistoricAuditingResult(expectedInitialRequest, NuanceBadRequest))
    }

    "read historic data in chunks and send audit events" in {

      val numFound = auditingChunkSize*2 + 30
      val reportingService = mock[NuanceReportingService]
      val response0 = ValidNuanceReportingResponse(numFound, auditingChunkSize * 0, JsArray())
      val response1 = ValidNuanceReportingResponse(numFound, auditingChunkSize * 1, JsArray())
      val response2 = ValidNuanceReportingResponse(numFound, auditingChunkSize * 2, JsArray())
      when(reportingService.getHistoricData(any()))
        .thenReturn(Future.successful(response0))
        .thenReturn(Future.successful(response0))
        .thenReturn(Future.successful(response1))
        .thenReturn(Future.successful(response2))

      val engagementAuditing = mock[EngagementAuditing]
      when(engagementAuditing.processEngagements(any())).thenReturn(Future.successful(Seq(Seq())))

      val appConfig = mock[AppConfig]
      when(appConfig.auditingChunkSize).thenReturn(auditingChunkSize)

      val auditing = new HistoricAuditing(reportingService, engagementAuditing, appConfig)

      val result = Await.result(auditing.auditDateRange(testStartDate, testEndDate), Duration.Inf)

      result mustBe Seq(
        new SuccessfulHistoricAuditingResult(expectedRequest0),
        new SuccessfulHistoricAuditingResult(expectedRequest1),
        new SuccessfulHistoricAuditingResult(expectedRequest2)
      )

      verify(reportingService).getHistoricData(expectedInitialRequest)
      verify(reportingService).getHistoricData(expectedRequest0)
      verify(reportingService).getHistoricData(expectedRequest1)
      verify(reportingService).getHistoricData(expectedRequest2)

      verify(engagementAuditing, times(3)).processEngagements(any())
    }

    "read historic data in chunks and send audit events and skip errors" in {

      val numFound = auditingChunkSize*2 + 30
      val reportingService = mock[NuanceReportingService]
      val response0 = ValidNuanceReportingResponse(numFound, auditingChunkSize * 0, JsArray())
      val response2 = ValidNuanceReportingResponse(numFound, auditingChunkSize * 2, JsArray())
      when(reportingService.getHistoricData(any()))
        .thenReturn(Future.successful(response0))
        .thenReturn(Future.successful(response0))
        .thenReturn(Future.successful(NuanceBadRequest))
        .thenReturn(Future.successful(response2))

      val engagementAuditing = mock[EngagementAuditing]
      when(engagementAuditing.processEngagements(any())).thenReturn(Future.successful(Seq(Seq())))

      val appConfig = mock[AppConfig]
      when(appConfig.auditingChunkSize).thenReturn(auditingChunkSize)

      val auditing = new HistoricAuditing(reportingService, engagementAuditing, appConfig)

      val result = Await.result(auditing.auditDateRange(testStartDate, testEndDate), Duration.Inf)

      result mustBe Seq(
        new SuccessfulHistoricAuditingResult(expectedRequest0),
        new FailedHistoricAuditingResult(expectedRequest1, NuanceBadRequest),
        new SuccessfulHistoricAuditingResult(expectedRequest2)
      )

      verify(reportingService).getHistoricData(expectedInitialRequest)
      verify(reportingService).getHistoricData(expectedRequest0)
      verify(reportingService).getHistoricData(expectedRequest1)
      verify(reportingService).getHistoricData(expectedRequest2)

      verify(engagementAuditing, times(2)).processEngagements(any())
    }

    "read historic data in chunks and send audit events and skip exceptions" in {

      val numFound = auditingChunkSize*2 + 30
      val reportingService = mock[NuanceReportingService]
      val response0 = ValidNuanceReportingResponse(numFound, auditingChunkSize * 0, JsArray())
      val response2 = ValidNuanceReportingResponse(numFound, auditingChunkSize * 2, JsArray())
      when(reportingService.getHistoricData(any()))
        .thenReturn(Future.successful(response0))
        .thenReturn(Future.successful(response0))
        .thenThrow(new IllegalArgumentException("Test Exception"))
        .thenReturn(Future.successful(response2))

      val engagementAuditing = mock[EngagementAuditing]
      when(engagementAuditing.processEngagements(any())).thenReturn(Future.successful(Seq(Seq())))

      val appConfig = mock[AppConfig]
      when(appConfig.auditingChunkSize).thenReturn(auditingChunkSize)

      val auditing = new HistoricAuditing(reportingService, engagementAuditing, appConfig)

      val result = Await.result(auditing.auditDateRange(testStartDate, testEndDate), Duration.Inf)

      result mustBe Seq(
        new SuccessfulHistoricAuditingResult(expectedRequest0),
        new HistoricAuditingExceptionResult(expectedRequest1, "Test Exception"),
        new SuccessfulHistoricAuditingResult(expectedRequest2)
      )

      verify(reportingService).getHistoricData(expectedInitialRequest)
      verify(reportingService).getHistoricData(expectedRequest0)
      verify(reportingService).getHistoricData(expectedRequest1)
      verify(reportingService).getHistoricData(expectedRequest2)

      verify(engagementAuditing, times(2)).processEngagements(any())
    }
  }
}
