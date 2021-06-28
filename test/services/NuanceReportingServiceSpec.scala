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

package services

import java.time.LocalDateTime

import connectors.{NuanceAuthConnector, NuanceReportingConnector, NuanceReportingRequest}
import javax.inject.Inject
import models.{NuanceAuthInformation, NuanceReportingResponse, ValidNuanceReportingResponse}
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.JsArray

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

import scala.concurrent.ExecutionContext.Implicits.global

class NuanceReportingServiceSpec extends AnyWordSpec with Matchers with MockitoSugar {
  "NuanceReportingService" should {
    "be able to authenticate and read reporting data" in {
      val authConnector = mock[NuanceAuthConnector]
      val authInformation = NuanceAuthInformation("somecookie")
      when(authConnector.authenticate()).thenReturn(Future.successful(authInformation))

      val testStartDate = LocalDateTime.parse("2020-04-20T00:00:10")
      val testEndDate = LocalDateTime.parse("2020-07-17T00:00:20")

      val request = NuanceReportingRequest(start = 123, rows = 500, testStartDate, testEndDate)

      val response = ValidNuanceReportingResponse(500, 123, JsArray())

      val reportingConnector = mock[NuanceReportingConnector]
      when(reportingConnector.getHistoricData(authInformation, request)).thenReturn(Future.successful(response))

      val service = new NuanceReportingService(authConnector, reportingConnector)

      val result = Await.result(service.getHistoricData(request), Duration.Inf)
      result mustBe response
    }
  }
}
