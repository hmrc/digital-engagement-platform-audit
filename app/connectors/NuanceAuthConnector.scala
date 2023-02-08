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
import models.{NuanceAccessTokenResponse, NuanceAuthResponse, TokenExchangeResponse}
import org.apache.commons.codec.binary.Base64
import pdi.jwt.{Jwt, JwtAlgorithm}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, StringContextOps}

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import java.util.Locale
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NuanceAuthConnector @Inject()(http: ProxiedHttpClient, config: AppConfig)(implicit ec: ExecutionContext) {

  def createJwtString(): String = {

    val dateFormat = DateTimeFormatter
      .ofPattern("YMMdHms")
      .withLocale(Locale.UK)
      .withZone(ZoneId.of("GMT"))

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
  */

    val header =
      s"""
         |{
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

    Jwt.encode(header = header, claim = claims, key = config.OAuthPrivateKey, JwtAlgorithm.RS256)
  }


  def requestAccessToken(): Future[NuanceAccessTokenResponse] = {

    val jwtString = createJwtString()

    val encodedAuthHeader =
      Base64.encodeBase64String(s"${config.OAuthClientId}:${config.OAuthClientSecret}".getBytes("UTF-8"))

    implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(s"Basic $encodedAuthHeader")))

    val body = Map(
      "grant_type" -> "urn:ietf:params:oauth:grant-type:token-exchange",
      "subject_token" -> jwtString
    )

    // TODO check if we need to set the auth header in the setHeader fn
    http.get()
      .post(url"${config.nuanceTokenAuthUrl}")
      .withBody(body)
      .setHeader("authorization" -> s"Basic $encodedAuthHeader", "content-type" -> "application/x-www-form-urlencoded")
      .execute[NuanceAccessTokenResponse]
  }

  def authenticate(): Future[NuanceAuthResponse] = {

    implicit val hc: HeaderCarrier = new HeaderCarrier

    val body = Map(
      "j_username" -> Seq(config.nuanceAuthName),
      "j_password" -> Seq(config.nuanceAuthPassword),
      "submit" -> Seq("Login")
    )

    http.get()
      .post(url"${config.nuanceAuthUrl}")
      .withBody(body)
      .execute[NuanceAuthResponse]
  }

}
