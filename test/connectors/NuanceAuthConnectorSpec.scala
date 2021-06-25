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

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalTo, post, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import config.AppConfig
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.{HeaderNames, Status}
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HttpResponse
import utils.WireMockHelper

import scala.concurrent.Future

class NuanceAuthConnectorSpec extends AnyWordSpec
  with Matchers
  with MockitoSugar
  with WireMockHelper
  with GuiceOneServerPerSuite
  with ScalaFutures
  with IntegrationPatience {

  private lazy val application = applicationBuilder().build()

  private def injector = application.injector

  def appConfig : AppConfig = injector.instanceOf[AppConfig]

  def nuanceUrl: String = "/auth-path"

  def applicationBuilder(): GuiceApplicationBuilder = {
    new GuiceApplicationBuilder()
      .configure(
        Seq(
          "metrics.enabled" -> false,
          "auditing.enabled" -> false,
          "nuance.auth-url" -> s"http://localhost:${server.port()}/auth-path",
          "nuance.auth-name" -> "AuthName",
          "nuance.auth-password" -> "AuthPassword"
        ): _*
      )
  }

  private def stubForPost(server: WireMockServer,
                  url: String,
                  requestBody: String,
                  returnStatus: Int,
                  responseBody: String,
                  delayResponse: Int = 0): StubMapping = {

    server.stubFor(post(urlEqualTo(url))
      .withRequestBody(equalTo(requestBody))
      .willReturn(
        aResponse()
          .withStatus(returnStatus)
          .withBody(responseBody).withFixedDelay(delayResponse)
          .withHeader(HeaderNames.SET_COOKIE, "JSESSIONID=SomeNuanceCookie")
      )
    )
  }

  private def wiremock(returnStatus: Int): StubMapping = {
    stubForPost(server,
      nuanceUrl,
      "j_username=AuthName&j_password=AuthPassword&submit=Login",
      returnStatus,
      "" )
  }

  private lazy val connector = injector.instanceOf[NuanceAuthConnector]

  "NuanceAuthConnector" must {

    "POST login details" which {

      "returns 302 with cookie" in {

        wiremock(Status.FOUND)

        val futureResult = connector.authenticate()
        whenReady(futureResult) {
          response =>
            response.status mustBe Status.FOUND
            response.headers(HeaderNames.SET_COOKIE) mustBe Seq("JSESSIONID=SomeNuanceCookie")
        }
      }

      "returns 400 BAD_REQUEST" in {

        wiremock(Status.BAD_REQUEST)

        val futureResult: Future[HttpResponse] = connector.authenticate()
        whenReady(futureResult) {
          result => result.status mustBe Status.BAD_REQUEST
        }
      }

      "returns 500 INTERNAL_SERVER_ERROR" in {

        wiremock(Status.INTERNAL_SERVER_ERROR)

        val futureResult = connector.authenticate()
        whenReady(futureResult) {
          result => result.status mustBe Status.INTERNAL_SERVER_ERROR
        }
      }
    }
  }
}
