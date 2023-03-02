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

package models


import play.api.Logging
import play.api.http.Status
import play.api.libs.json.{Json, Reads}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

import scala.util.{Failure, Success, Try}

trait NuanceAccessTokenResponse

object NuanceAccessTokenBadRequest extends NuanceAccessTokenResponse
object NuanceAccessTokenUnauthorised extends NuanceAccessTokenResponse
object NuanceAccessTokenServerError extends NuanceAccessTokenResponse
object NuanceAccessTokenParseError extends NuanceAccessTokenResponse

case class TokenExchangeResponse(access_token: String,
                                 token_type: String,
                                 expires_in: Int,
                                 scope: String,
                                 sites: List[String],
                                 jti: String) extends NuanceAccessTokenResponse

object TokenExchangeResponse {
  implicit val reads: Reads[TokenExchangeResponse] = Json.format[TokenExchangeResponse]
}

object NuanceAccessTokenResponse extends Logging {

  implicit lazy val httpReads: HttpReads[NuanceAccessTokenResponse] =
    (_: String, _: String, response: HttpResponse) => {
      response.status match {
        case Status.OK | Status.FOUND =>

           Try(response.json.as[TokenExchangeResponse]) match {
            case Success(tokenExchangeResponse) =>
              logger.info("[NuanceAuthResponse] Got a successful response from auth API and parsed response")
              tokenExchangeResponse
            case Failure(e) =>
              logger.info(s"[NuanceAuthResponse] Could not parse response from auth API: ${e.getMessage}")
              NuanceAccessTokenParseError
          }

        case Status.BAD_REQUEST =>
          logger.warn("[NuanceAuthResponse] Got 'bad request' response from auth API")
          NuanceAccessTokenBadRequest
        case Status.UNAUTHORIZED =>
          logger.error("[NuanceAuthResponse] Got 'unauthorized' response from auth API")
          NuanceAccessTokenUnauthorised
        case code =>
          logger.warn(s"[NuanceAuthResponse] Got error $code from auth API")
          NuanceAccessTokenServerError
      }
    }
}
