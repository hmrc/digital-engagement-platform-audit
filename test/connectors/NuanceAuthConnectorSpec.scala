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
import models.{NuanceAuthBadRequest, NuanceAuthInformation, NuanceAuthResponse, NuanceAuthServerError, NuanceAuthUnauthorised}
import play.api.http.{HeaderNames, Status}
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.Future

class NuanceAuthConnectorSpec extends BaseConnectorSpec
{
  override def applicationBuilder(): GuiceApplicationBuilder = {
    super.applicationBuilder()
      .configure(
        Seq(
          "nuance.auth-url" -> s"http://localhost:${server.port()}/auth-path",
          "nuance.auth-name" -> "AuthName",
          "nuance.auth-password" -> "AuthPassword"
        ): _*
      )
  }

  def nuanceUrl: String = "/auth-path"

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
          .withHeader(HeaderNames.SET_COOKIE, "SERVERID=api140")
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
            response mustBe NuanceAuthInformation("JSESSIONID=SomeNuanceCookie; SERVERID=api140")
        }
      }

      "returns 400 BAD_REQUEST" in {

        wiremock(Status.BAD_REQUEST)

        val futureResult: Future[NuanceAuthResponse] = connector.authenticate()
        whenReady(futureResult) {
          result => result mustBe NuanceAuthBadRequest
        }
      }
      "returns 401 UNAUTHORIZED" in {

        wiremock(Status.UNAUTHORIZED)

        val futureResult: Future[NuanceAuthResponse] = connector.authenticate()
        whenReady(futureResult) {
          result => result mustBe NuanceAuthUnauthorised
        }
      }

      "returns 500 INTERNAL_SERVER_ERROR" in {

        wiremock(Status.INTERNAL_SERVER_ERROR)

        val futureResult = connector.authenticate()
        whenReady(futureResult) {
          result => result mustBe NuanceAuthServerError
        }
      }
    }
  }
}
