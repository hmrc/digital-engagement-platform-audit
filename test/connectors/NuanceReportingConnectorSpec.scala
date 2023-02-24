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

package connectors

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models._
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, Json}
import uk.gov.hmrc.http.StringContextOps

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NuanceReportingConnectorSpec extends BaseConnectorSpec {

  val nuanceUrl: String = "/v3/transcript/historic"

  override def applicationBuilder(): GuiceApplicationBuilder = {
    super.applicationBuilder()
      .configure(
        Seq(
          "microservice.services.nuance-api.port" -> server.port(),
          "nuance.site-id" -> "1234567",
          "nuance.api-base-url" -> server.url(nuanceUrl)
        ): _*
      )
  }

  private val testStartDate = LocalDateTime.now().minusHours(5)
  private val testEndDate = LocalDateTime.now().minusHours(3)

  private val dateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd'T'hh:mm:ss")
  private val formattedTestStartDate = dateTimeFormatter.format(testStartDate)
  private val formattedTestEndDate = dateTimeFormatter.format(testEndDate)

  val testFilter = s"""startDate>="$formattedTestStartDate" AND startDate<="$formattedTestEndDate""""
  val testStart = 100
  val testRows = 1234

  def stubForGet(server: WireMockServer,
                 url: String,
                 returnStatus: Int,
                 responseBody: String,
                 delayResponse: Int = 0): StubMapping = {

    val queryParams = Seq(
      "site" -> appConfig.hmrcSiteId,
      "filter" -> testFilter,
      "returnFields" -> "ALL",
      "start" -> testStart.toString,
      "rows" -> testRows.toString
    )

    val fullUrl = url"$url?$queryParams"

    val pathAndQueryParams = s"${fullUrl.getPath}?${fullUrl.getQuery}"

    server
      .stubFor(
        get(urlEqualTo(pathAndQueryParams))
          .willReturn(
            aResponse()
              .withStatus(returnStatus)
              .withBody(responseBody)
              .withFixedDelay(delayResponse)
          )
      )
  }

  private def wiremock(returnStatus: Int, responseBody: String = ""): StubMapping =
    stubForGet(server, server.url(nuanceUrl), returnStatus, responseBody)


  private lazy val connector = injector.instanceOf[NuanceReportingConnector]

  private val testAccessToken = "12345-once-i-caught-a-fish-alive"

  "NuanceReportingConnector" must {

    "GET historic reporting data" which {

      "returns 200 with valid result" in {

        val expectedResult = Json.obj(
          "numFound" -> 500,
          "start" -> testStart,
          "engagements" -> JsArray()
        )

        wiremock(Status.OK, expectedResult.toString)

        val request = NuanceReportingRequest(testStart, testRows, testStartDate, testEndDate)

        val futureResult = connector.getHistoricData(testAccessToken, request)
        whenReady(futureResult) {
          result => result mustBe ValidNuanceReportingResponse(500, testStart, JsArray())
        }
      }

      "returns 200 with invalid result, results in NuanceParseError" in {

        val emptyResponseBody = Json.obj()

        wiremock(Status.OK, emptyResponseBody.toString)

        val request = NuanceReportingRequest(testStart, testRows, testStartDate, testEndDate)

        val futureResult = connector.getHistoricData(testAccessToken, request)
        whenReady(futureResult) {
          result => result mustBe NuanceParseError
        }
      }

      "returns 400 BAD_REQUEST" in {

        wiremock(Status.BAD_REQUEST)

        val request = NuanceReportingRequest(testStart, testRows, testStartDate, testEndDate)

        val futureResult = connector.getHistoricData(testAccessToken, request)
        whenReady(futureResult) {
          result => result mustBe NuanceBadRequest
        }
      }

      "returns 401 UNAUTHORIZED" in {

        wiremock(Status.UNAUTHORIZED)

        val request = NuanceReportingRequest(testStart, testRows, testStartDate, testEndDate)

        val futureResult = connector.getHistoricData(testAccessToken, request)
        whenReady(futureResult) {
          result => result mustBe NuanceUnauthorised
        }
      }

      "returns 500 INTERNAL_SERVER_ERROR" in {

        wiremock(Status.INTERNAL_SERVER_ERROR)

        val request = NuanceReportingRequest(testStart, testRows, testStartDate, testEndDate)

        val futureResult = connector.getHistoricData(testAccessToken, request)
        whenReady(futureResult) {
          result => result mustBe NuanceServerError
        }
      }
    }
  }
}
