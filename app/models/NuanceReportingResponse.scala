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

package models

import play.api.Logging
import play.api.http.Status
import play.api.libs.json.{Format, JsArray, Json}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

trait NuanceReportingResponse

object NuanceServerError extends NuanceReportingResponse
object NuanceBadRequest extends NuanceReportingResponse
object NuanceUnauthorised extends NuanceReportingResponse

case class NuanceAuthFailure(authResponse: NuanceAuthResponse) extends NuanceReportingResponse

case class ValidNuanceReportingResponse(numFound: Int, start: Int, engagements: JsArray) extends NuanceReportingResponse
object ValidNuanceReportingResponse {
  implicit val format: Format[ValidNuanceReportingResponse] = Json.format[ValidNuanceReportingResponse]
}

object NuanceReportingResponse extends Logging {
  implicit lazy val httpReads: HttpReads[NuanceReportingResponse] =
    (_: String, _: String, response: HttpResponse) => {
      response.status match {
        case Status.OK =>
          response.json.as[ValidNuanceReportingResponse]
        case Status.BAD_REQUEST =>
          logger.warn("[NuanceReportingResponse] Got 'bad request' response from reporting API")
          NuanceBadRequest
        case Status.UNAUTHORIZED =>
          logger.warn("[NuanceReportingResponse] Got 'unauthorized' response from reporting API")
          NuanceUnauthorised
        case code =>
          logger.warn(s"[NuanceReportingResponse] Got error $code from reporting API")
          NuanceServerError
      }
    }
}

