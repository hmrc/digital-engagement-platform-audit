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
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, containing, post, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models._
import org.apache.commons.codec.binary.Base64
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{Json, Reads}

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import java.util.Locale
import scala.util.Try

class NuanceAuthConnectorSpec extends BaseConnectorSpec {

  override def applicationBuilder(): GuiceApplicationBuilder = {
    super.applicationBuilder()
      .configure(
        Seq(
          "microservice.services.nuance-api.port" -> server.port(),
          "microservice.services.nuance-auth.host" ->  "localhost",
          "microservice.services.nuance-auth.port" -> server.port(),
          "microservice.services.nuance-auth.path" -> nuanceUrl,
          "nuance.oauth.private-key" -> dummyPrivateKey,
          "nuance.oauth.client-secret" -> "super-secret-squirrel",
          "nuance.oauth.client-id" -> "test-client-id",
          "nuance.oauth.issuer" -> "test-issuer",
          "nuance.oauth.subject" -> "test-subject",
          "nuance.oauth.audience" -> "test-audience",
          "nuance.oauth.key-id" -> "test-key-id",
          "nuance.site-id" -> "12345"
        ): _ *
      )
  }

  val nuanceUrl: String = "/some-auth-url"

  private def stubForPost(server: WireMockServer,
                          url: String,
                          requestBodyShouldContain: String,
                          returnStatus: Int,
                          responseBody: String,
                          delayResponse: Int = 0): StubMapping = {

    server.stubFor(
      post(urlEqualTo(url))
        .withRequestBody(containing(requestBodyShouldContain))
        .willReturn(
          aResponse()
            .withStatus(returnStatus)
            .withBody(responseBody).withFixedDelay(delayResponse)
        )
    )
  }

  private def wiremock(returnStatus: Int, responseBody: String = ""): StubMapping = {
    val requestBodyShouldContain = "grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Atoken-exchange&subject_token="

    stubForPost(server, nuanceUrl, requestBodyShouldContain, returnStatus, responseBody)
  }

  private lazy val connector = injector.instanceOf[NuanceAuthConnector]

  // helper case class to read decoded jwt content
  case class JwtContent(iss: String, aud: String, jti: String)
  implicit val jwtContentReads: Reads[JwtContent] = Json.reads[JwtContent]

  "NuanceAuthConnector" must {

    "create the expected JWT string" in {

      val dateFormat = DateTimeFormatter
        .ofPattern("YMMdHms")
        .withLocale(Locale.UK)
        .withZone(ZoneId.of("GMT"))

      val now = Instant.now()
      val fiveMinutesInSeconds = 300
      val nowAsLong = dateFormat.format(now).toLong
      val nowPlusFiveMinutesAsLong = dateFormat.format(now.plusSeconds(fiveMinutesInSeconds)).toLong

      val jwtString = connector.createJwtString()

      val decodedJwtTry: Try[JwtClaim] = Jwt.decode(jwtString, dummyPublicKey, Seq(JwtAlgorithm.RS256))

      assert(decodedJwtTry.isSuccess)

      val decodedJwt = decodedJwtTry.get
      val content: JwtContent = Json.parse(decodedJwt.content).as[JwtContent]

      assert(Base64.isBase64(content.jti))
      assert(content.iss == "test-issuer")
      assert(content.aud == "test-audience")

      assert(decodedJwt.subject.get == "test-subject")
      assert(decodedJwt.issuer.isEmpty)
      assert(decodedJwt.notBefore.isEmpty)
      assert(decodedJwt.jwtId.isEmpty)

      val secondsTolerance = 1

      decodedJwt.issuedAt.get shouldBe (nowAsLong +- secondsTolerance)
      decodedJwt.expiration.get shouldBe (nowPlusFiveMinutesAsLong +- secondsTolerance)
    }

    "return the expected TokenExchangeResponse, given a valid access token response from the server" in {

      val responseBody =
        """
          |{
          | "access_token":"lots-of-random-chars",
          | "token_type":"bearer",
          | "expires_in":100,
          | "scope":"read write",
          | "sites":["12345"],
          | "jti":"a-few-random-chars"
          |}
          |""".stripMargin

      wiremock(Status.OK, responseBody)

      val futureResult = connector.requestAccessToken()

      whenReady(futureResult) {
        response =>
          response mustBe TokenExchangeResponse(
            access_token = "lots-of-random-chars",
            token_type = "bearer",
            expires_in = 100,
            scope = "read write",
            sites = List("12345"),
            jti = "a-few-random-chars",
          )
      }
    }

    "return NuanceAccessTokenParseError, given an error in the JSON access token response from the Nuance server" in {

      val invalidResponseBody =
        """
          |{
          | "this_key_is_incorrect":"lots-of-random-chars",
          | "token_type":"bearer",
          | "expires_in":100,
          | "scope":"read write",
          | "sites":["12345"],
          | "jti":"a-few-random-chars"
          |}
          |""".stripMargin

      wiremock(Status.OK, invalidResponseBody)

      val futureResult = connector.requestAccessToken()

      whenReady(futureResult) { response => response mustBe NuanceAccessTokenParseError }
    }


    "return NuanceAccessTokenBadRequest given a BAD_REQUEST(400) status is returned from the Nuance server" in {
      wiremock(Status.BAD_REQUEST)
      val futureResult = connector.requestAccessToken()
      whenReady(futureResult) { response => response mustBe NuanceAccessTokenBadRequest }
    }

    "return NuanceAccessTokenUnauthorised given an UNAUTHORIZED(401) status is returned from the Nuance server" in {
      wiremock(Status.UNAUTHORIZED)
      val futureResult = connector.requestAccessToken()
      whenReady(futureResult) { response => response mustBe NuanceAccessTokenUnauthorised }
    }

    "return NuanceAccessTokenServerError given an INTERNAL_SERVER_ERROR(500) status is returned from the Nuance server" in {
      wiremock(Status.INTERNAL_SERVER_ERROR)
      val futureResult = connector.requestAccessToken()
      whenReady(futureResult) { response => response mustBe NuanceAccessTokenServerError }
    }

  }

  private lazy val dummyPrivateKey =
    "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDz2sEA8KuPowRZjbq7kXdbdi/Y052kvlF5A65E0E69cEcC/CfYD6" +
      "PasEkR71FWCv2FOYY3O8ZFdJPtFDBTzjpZGuYyolIqds84VDX2ydm2ieo2PQR4hmypacO1Lo41a2Z8X3Zt2MB1oegiotn3BDOzYP" +
      "YUVQgof6HsZnm1dtLd9AWY0LNhYYZ8sLfPvrd18GbAteAF1mqPkBvB/s+o2m2Hh6Sar/KxmqPsZcyN6Y+A2seW3ly/GTPfdjSFczz" +
      "GdNym37/FmtLSiCY6gPK0Bfa97f6yk/BfG1Tn74yHkD3UQklPa+Eb/28OK5cNbXTjfwoComybzKy+MhhzjUbpY8mpAgMBAAECggEA" +
      "QRi5gx28Prje1WU/XwkDGthfB3veTnc72pk/8UDGdE2/ty1HGad7L0r2BqKjTNvcN69Wg/IiCLKJNrW3/PdFnjnPD99DHfDfeoMIc" +
      "NwLkW4Zpub9BYulAEiqpPhLoDkOf88gF0zQe9Z/2JcupoFpp84Pgvf8GIuR10C35thUjEktux1YnuJGEj/cLLLGEsYGCboE9iNbIl" +
      "sBMYfl5AtXLu+2YClOIGa9HLyPMzEQzoHNa3DE2plP8GGczUMINfq1Yp/4OdROpYnX+Akua/ioniQ3spy7BUfHg86HHR2j1wKPS5m" +
      "lp/5ABEOQZWJ0RbRFtVaNtgKE3H02HASb2pQpXQKBgQD66jceJXKklgM1vjOnaRQxzFwJpIr86clFDb1yD/PWCbf90Ql64Hvvu7Ei" +
      "ZUCtUO85HopnbZCwZsO/LPlZ+L1a2E6LgPAvkLvYTdaZHNwYwbtlTW+vDRVrtVRiLRkacuo8pnky8uoYYV/vuIWCCE0oh2id0JQ2a" +
      "DtatDsaPKn8GwKBgQD4y+iAUg808u0ng0yKUEHZRPKBZ8W10g2q9YBeOjG2hgHLG6gpRcYyjT+ATFSZUoSHRGTAmx5yj3yi7bBQGG" +
      "7OY80r3zl5xoKMQwLXl0PJqcYVxqXkX3z3S4op0TL6wA5PgAGhmKFUePjybX3DiBZk+hpCRGuXSeQIIwwdYXgliwKBgQD0tHVarjT" +
      "XdQHeaQfhTiaGxoJAWTu/mpVTBiprUkmAxeut2y7+qm3UiN0g6VShctxLfZCHfCBvGVnwWYE5kM/DCE/Z177m9KVuN8OBgdbZQh9r" +
      "lZDFTOJGMAsC0G66D7aHvedXm/ZYHqc8mF2ESmpNi3mo56GUSiUQudMmLGGvXwKBgBi8jV1Uy1ZEYPfwwgdue12S0Vm0a5/sRpec2" +
      "SqFNrQjFsKO+WVBHref+5RSAodMAQlB75KG2bwiMA4y+7i69SloGtol050b85bmgr3UuFSwoJVBrvTJ2a7sv8vwlNUBi2q0G3Vpq4" +
      "CulnHkGQD74t4fT8UO8HOWUSnN8kJpknyJAoGBAMZK9LjCPXMfWTfba8UyvM+XEMpeDvIzDc0WitnznqfRXwku9xuuoU2OIy+txK4" +
      "D/2EDb6KCAIirzyT2jLQFO3Nw1Ho384EUz6pcXTtQQ8K7tzQvWRWxZEnyXF7gF+KL3nombeC82cFjmLLlVLp4Blxg1h6jFuj1/bs6GceBE234"

  private lazy val dummyPublicKey =
    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA89rBAPCrj6MEWY26u5F3W3Yv2NOdpL5ReQOuRNBOvXBHAvwn2A+j2rBJEe9" +
      "RVgr9hTmGNzvGRXST7RQwU846WRrmMqJSKnbPOFQ19snZtonqNj0EeIZsqWnDtS6ONWtmfF92bdjAdaHoIqLZ9wQzs2D2FFUIKH+h" +
      "7GZ5tXbS3fQFmNCzYWGGfLC3z763dfBmwLXgBdZqj5Abwf7PqNpth4ekmq/ysZqj7GXMjemPgNrHlt5cvxkz33Y0hXM8xnTcpt+/x" +
      "ZrS0ogmOoDytAX2ve3+spPwXxtU5++Mh5A91EJJT2vhG/9vDiuXDW10438KAqJsm8ysvjIYc41G6WPJqQIDAQAB"
}
