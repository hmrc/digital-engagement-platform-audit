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
import play.api.libs.json.{Format, JsArray, Json}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

import scala.util.{Failure, Success, Try}

trait NuanceReportingResponse

object NuanceServerError extends NuanceReportingResponse
object NuanceBadRequest extends NuanceReportingResponse
object NuanceUnauthorised extends NuanceReportingResponse
object NuanceParseError extends NuanceReportingResponse

case class NuanceAuthFailure(authResponse: NuanceAccessTokenResponse) extends NuanceReportingResponse


case class ValidNuanceReportingResponse(numFound: Int, start: Int, engagements: JsArray) extends NuanceReportingResponse
object ValidNuanceReportingResponse {
  implicit val format: Format[ValidNuanceReportingResponse] = Json.format[ValidNuanceReportingResponse]
}

object NuanceReportingResponse extends Logging {
  implicit lazy val httpReads: HttpReads[NuanceReportingResponse] =
    (_: String, _: String, response: HttpResponse) => {
      response.status match {
        case Status.OK =>

          Try(response.json.as[ValidNuanceReportingResponse]) match {
            case Failure(e) =>
              logger.warn(s"[NuanceReportingResponse] Error parsing response from reporting API: ${e.getMessage}")
              NuanceParseError
            case Success(response) =>
              logger.info("[NuanceReportingResponse] Got a successful response from reporting API and parsed response")
              response
          }

        case Status.BAD_REQUEST =>
          logger.warn("[NuanceReportingResponse] Got 'bad request' response from reporting API")
          NuanceBadRequest
        case Status.UNAUTHORIZED =>
          logger.warn("[NuanceReportingResponse] Got 'unauthorized' response from reporting API")
          NuanceUnauthorised
        case code =>
          logger.warn(s"[NuanceReportingResponse] Got error $code from reporting API, with body ${response.body}")
          NuanceServerError
      }
    }
}

