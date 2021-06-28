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

package auditing

import java.time.LocalDateTime

import connectors.NuanceReportingRequest
import models.ValidNuanceReportingResponse
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.JsValue
import services.NuanceReportingService
import utils.JsonUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class HistoricAuditingSpec extends AnyWordSpec with Matchers with MockitoSugar {

  "auditing.HistoricAuditingSpec" must {
    "read historic data and send audit events" in {

      val startDate = LocalDateTime.parse("2020-04-20T00:00:10")
      val endDate = LocalDateTime.parse("2020-07-17T00:00:20")

      val responseJson = JsonUtils.getJsonValueFromFile("HistoricSampleSmall.json")
      val nuanceReportingResponse = responseJson.as[ValidNuanceReportingResponse]
      nuanceReportingResponse.engagements.as[List[JsValue]].size mustBe 3

      val testRequest = NuanceReportingRequest(0, 100, startDate, endDate)

      val reportingService = mock[NuanceReportingService]
      when(reportingService.getHistoricData(testRequest)).thenReturn(Future.successful(nuanceReportingResponse))

      val engagementAuditing = mock[EngagementAuditing]
      val auditing = new HistoricAuditing(reportingService, engagementAuditing)

      Await.result(auditing.auditDateRange(startDate, endDate), Duration.Inf)

      verify(engagementAuditing).processEngagements(any())
    }
  }
}