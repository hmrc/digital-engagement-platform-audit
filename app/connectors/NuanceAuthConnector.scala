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

import config.AppConfig
import models.NuanceAuthResponse.httpReads
import models.{NuanceAccessTokenResponse, NuanceAuthResponse}
import org.apache.commons.codec.binary.Base64
import pdi.jwt.{Jwt, JwtAlgorithm}
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import java.util.Locale
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NuanceAuthConnector @Inject()(http: ProxiedHttpClient, config: AppConfig)(implicit ec: ExecutionContext) {

  def createJwtString(): String = {

    val dateFormat = DateTimeFormatter
      .ofPattern("YMMdHms")
      .withLocale( Locale.UK )
      .withZone( ZoneId.of("GMT"))

    val fiveMinutesInSeconds = 300

    val now = Instant.now()
    val nowPlusFiveMinutes = now.plusSeconds(fiveMinutesInSeconds)

    /* TODO use the following type safe code when the nuance backend is fixed

        val claim = JwtClaim(
          issuer = Some(config.OAuthIssuer),
          subject = Some(config.OAuthSubject),
          audience = Some(Set(config.OAuthAudience)),
          issuedAt = Some(dateFormat.format(now).toLong),
          expiration = Some(dateFormat.format(nowPlusFiveMinutes).toLong).toLong)
      )

      val header = JwtHeader(
        algorithm = Some(JwtAlgorithm.RS256),
        typ = Some("JWT"),
        keyId = Some(config.OAuthKeyId)
      )
     */

    val header2 = s"""
      |{
      | "alg": "RS256",
      | "typ": "JWT",
      | "kid": "${config.OAuthKeyId}"
      |}
      |""".stripMargin

    val claims =
      s"""
         |{
         |  "iss": "${config.OAuthIssuer}",
         |  "aud": "${config.OAuthAudience}",
         |  "sub": "${config.OAuthSubject}",
         |  "iat": ${dateFormat.format(now).toLong},
         |  "exp": ${dateFormat.format(nowPlusFiveMinutes).toLong}
         |}
         |""".stripMargin

    Jwt.encode(header = header2, claim = claims, config.OAuthPrivateKey, JwtAlgorithm.HS256)
  }

  case class AccessTokenRequest(grant_type: String,
                                subject_token: String)

  object AccessTokenRequest {
    implicit val format: Writes[AccessTokenRequest] = Json.format[AccessTokenRequest]
  }

  def requestAccessToken(): Future[NuanceAccessTokenResponse] = {

    val jwtString = createJwtString()

    val encodedAuthHeader =
      Base64.encodeBase64String(s"${config.OAuthClientId}:${config.OAuthClientSecret}".getBytes("UTF-8"))

    implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(s"Basic $encodedAuthHeader")))

    http.POST[AccessTokenRequest, NuanceAccessTokenResponse](
      url = config.nuanceTokenAuthUrl,
      AccessTokenRequest("urn:ietf:params:oauth:grant-type:token-exchange", jwtString )
    )
  }

  def authenticate(): Future[NuanceAuthResponse] = {

    implicit val hc: HeaderCarrier = new HeaderCarrier

    http.POSTForm[NuanceAuthResponse](
      config.nuanceAuthUrl,
      Map(
        "j_username" -> Seq(config.nuanceAuthName),
        "j_password" -> Seq(config.nuanceAuthPassword),
        "submit" -> Seq("Login")
      ),
      Seq()
    )
  }
}
