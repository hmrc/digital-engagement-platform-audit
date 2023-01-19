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

import javax.inject.Inject
import models.NuanceAuthResponse
import uk.gov.hmrc.http.HeaderCarrier
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException

import java.text.SimpleDateFormat
import java.util.Date
import java.time.Instant
import java.time.temporal.{ChronoUnit, TemporalUnit}
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.util.Try


class NuanceAuthConnector @Inject()(http: ProxiedHttpClient, config: AppConfig)(implicit ec: ExecutionContext) {

  def createJwt(): Unit = {

    Try {
      val dateFormat = new SimpleDateFormat("YMMdHMS")

      val payload: Map[String, String] = Map(
        "iss" -> config.OAuthIssuer,
        "sub" -> config.OAuthSubject,
        "aud" -> config.OAuthAudience,
        "iat" -> dateFormat.format(Instant.now()),
        "exp" -> dateFormat.format(Instant.now().plus(5, ChronoUnit.MINUTES))
      )

      val token = JWT
        .create
        .withPayload(payload.asJava)
      
    }

    ???
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
