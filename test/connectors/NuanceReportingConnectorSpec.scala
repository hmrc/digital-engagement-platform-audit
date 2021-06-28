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

package connectors

import java.time.LocalDateTime

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalTo, get, urlPathEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.{NuanceAuthInformation, NuanceBadRequest, NuanceServerError, NuanceUnauthorised, ValidNuanceReportingResponse}
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, Json}

import scala.collection.JavaConverters._

class NuanceReportingConnectorSpec extends BaseConnectorSpec
{
  override def applicationBuilder(): GuiceApplicationBuilder = {
    super.applicationBuilder()
      .configure(
        Seq(
          "nuance.reporting-url" -> s"http://localhost:${server.port()}/reporting",
          "nuance.site-id" -> "1234567"
        ): _*
      )
  }

  def nuanceUrl: String = "/reporting"
  val testSessionId = "xxxSESSIONIDxxx"
  val testServerId = "api130"
  val testFilter = """endDate>="2020-04-20T00:00:10" and endDate<="2020-07-17T00:00:20""""
  val testStart = 100
  val testRows = 1234

  private val testStartDate = LocalDateTime.parse("2020-04-20T00:00:10")
  private val testEndDate = LocalDateTime.parse("2020-07-17T00:00:20")

  def stubForGet(server: WireMockServer,
                 url: String, returnStatus: Int,
                 responseBody: String,
                 delayResponse: Int = 0): StubMapping = {
    server.stubFor(get(urlPathEqualTo(url))
        .withQueryParams(Map(
          "site" -> equalTo(appConfig.hmrcSiteId),
          "filter" -> equalTo(testFilter),
          "returnFields" -> equalTo("ALL"),
          "start" -> equalTo(testStart.toString),
          "rows" -> equalTo(testRows.toString)
        ).asJava)
      .withCookie("JSESSIONID", equalTo(testSessionId))
      .withCookie("SERVERID", equalTo(testServerId))
      .willReturn(
        aResponse()
          .withStatus(returnStatus)
          .withBody(responseBody).withFixedDelay(delayResponse)))
  }

  private def wiremock(returnStatus: Int, responseBody: String = ""): StubMapping = {
    stubForGet(server,
      nuanceUrl,
      returnStatus,
      responseBody )
  }

  private lazy val connector = injector.instanceOf[NuanceReportingConnector]

  private val testAuthInfo = NuanceAuthInformation(s"JSESSIONID=$testSessionId; SERVERID=$testServerId")

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

        val futureResult = connector.getHistoricData(testAuthInfo, request)
        whenReady(futureResult) {
          response => response mustBe ValidNuanceReportingResponse(500, testStart, JsArray())
        }
      }

      "returns 400 BAD_REQUEST" in {

        wiremock(Status.BAD_REQUEST)

        val request = NuanceReportingRequest(testStart, testRows, testStartDate, testEndDate)

        val futureResult = connector.getHistoricData(testAuthInfo, request)
        whenReady(futureResult) {
          result => result mustBe NuanceBadRequest
        }
      }

      "returns 401 UNAUTHORIZED" in {

        wiremock(Status.UNAUTHORIZED)

        val request = NuanceReportingRequest(testStart, testRows, testStartDate, testEndDate)

        val futureResult = connector.getHistoricData(testAuthInfo, request)
        whenReady(futureResult) {
          result => result mustBe NuanceUnauthorised
        }
      }

      "returns 500 INTERNAL_SERVER_ERROR" in {

        wiremock(Status.INTERNAL_SERVER_ERROR)

        val request = NuanceReportingRequest(testStart, testRows, testStartDate, testEndDate)

        val futureResult = connector.getHistoricData(testAuthInfo, request)
        whenReady(futureResult) {
          result => result mustBe NuanceServerError
        }
      }
    }
  }
}
