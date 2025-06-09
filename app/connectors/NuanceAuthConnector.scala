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
import models.NuanceAccessTokenResponse
import org.apache.commons.codec.binary.Base64
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim, JwtHeader}
import play.api.Logging
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import play.api.libs.ws.DefaultBodyWritables.writeableOf_urlEncodedSimpleForm
import java.security.spec.PKCS8EncodedKeySpec
import java.security.{KeyFactory, PrivateKey, SecureRandom}
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import java.util.Locale
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class NuanceAuthConnector @Inject()(http: HttpClientV2, config: AppConfig)
                                   (implicit ec: ExecutionContext) extends Logging {
  def requestAccessToken(): Future[NuanceAccessTokenResponse] = {

    createJwt() match {
      case Failure(exception) =>
        logger.warn("Error creating JWT:", exception)
        throw exception
      case Success(jwt) =>

        implicit val hc: HeaderCarrier = new HeaderCarrier

        val body = Map(
          "grant_type" -> "urn:ietf:params:oauth:grant-type:token-exchange",
          "subject_token" -> jwt
        )

        val encodedAuthHeader =
          Base64.encodeBase64String(s"${config.OAuthClientId}:${config.OAuthClientSecret}".getBytes("UTF-8"))

        http
          .post(url"${config.nuanceTokenAuthUrl}")
          .withBody(body)
          .setHeader("authorization" -> s"Basic $encodedAuthHeader", "content-type" -> "application/x-www-form-urlencoded")
          .withProxy
          .execute[NuanceAccessTokenResponse]
    }
  }

  def createJwt(): Try[String] = {

    val dateFormat = DateTimeFormatter
      .ofPattern("YMMdHms")
      .withLocale(Locale.UK)
      .withZone(ZoneId.of("GMT"))

    val now = Instant.now()
    val fiveMinutesInSeconds = 300
    val nowPlusFiveMinutes = now.plusSeconds(fiveMinutesInSeconds)
    val nowAsLong = dateFormat.format(now).toLong

    val jwtClaims = JwtClaim(
      issuer = Some(config.OAuthIssuer),
      subject = Some(config.OAuthSubject),
      audience = Some(Set(config.OAuthAudience)),
      expiration = Some(dateFormat.format(nowPlusFiveMinutes).toLong),
      issuedAt = Some(nowAsLong),
      jwtId = Some(generateRandomBase64Token())
    )

    val jwtHeader = JwtHeader(algorithm = Some(JwtAlgorithm.RS256), typ = Some("JWT"), keyId = Some(config.OAuthKeyId))

    for {
      privateKey <- readPrivateKey()
      jwt <- Try(Jwt.encode(jwtHeader, jwtClaims, privateKey))
    } yield jwt
  }

  private def readPrivateKey(): Try[PrivateKey] = Try {
    val keyFactory = KeyFactory.getInstance("RSA")
    val keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.decodeBase64(config.OAuthPrivateKey))

    keyFactory.generatePrivate(keySpecPKCS8)
  }

  def generateRandomBase64Token(): String = {
    val secureRandom = new SecureRandom
    val token = new Array[Byte](16)
    secureRandom.nextBytes(token)

    Base64.encodeBase64String(token)
  }

}
