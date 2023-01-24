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
import io.jsonwebtoken.{Jwts, SignatureAlgorithm}
import models.NuanceAuthResponse.httpReads
import models.{NuanceAccessTokenResponse, NuanceAuthResponse}
import org.apache.commons.codec.binary.Base64
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

class NuanceAuthConnector @Inject()(http: ProxiedHttpClient, config: AppConfig)(implicit ec: ExecutionContext) {

  def createJwtString(): String = {
    val dateFormat = new SimpleDateFormat("YMMdHMS")

    val claims: Map[String, AnyRef] = Map(
      "iss" -> config.OAuthIssuer,
      "sub" -> config.OAuthSubject,
      "aud" -> config.OAuthAudience,
      "iat" -> dateFormat.format(Instant.now()),
      "exp" -> dateFormat.format(Instant.now().plus(5, ChronoUnit.MINUTES))
    )

    val header: Map[String, AnyRef] = Map(
      "kid" -> config.OAuthKeyId
    )

    Jwts.builder()
      .setHeader(header.asJava)
      .setClaims(claims.asJava)
      .signWith(SignatureAlgorithm.RS256, config.OAuthPrivateKey)
      .compact()
  }

  def getAccessToken: Unit = {

    val encodedAuthHeader =
      Base64.encodeBase64String(s"${config.OAuthClientId}:${config.OAuthClientSecret}".getBytes("UTF-8"))

    implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(s"Basic $encodedAuthHeader")))

    http.POSTForm[NuanceAccessTokenResponse](
      config.nuanceAuthUrl,
      Map(
        "grant_type" -> Seq("urn:ietf:params:oauth:grant-type:token-exchange"),
        "subject_token" -> Seq(createJwtString())
      )
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
