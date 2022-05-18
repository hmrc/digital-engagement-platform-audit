/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.http.{HeaderNames, Status}
import play.api.libs.json.{Format, Json}
import play.api.mvc.{Cookie, DefaultCookieHeaderEncoding}
import uk.gov.hmrc.http.{HttpReads, HttpResponse, UnauthorizedException}

trait NuanceAuthResponse

object NuanceAuthBadRequest extends NuanceAuthResponse
object NuanceAuthUnauthorised extends NuanceAuthResponse
object NuanceAuthServerError extends NuanceAuthResponse

case class NuanceAuthInformation(cookieHeader: String) extends NuanceAuthResponse {
  def toHeaders: Seq[(String, String)] = Seq(HeaderNames.COOKIE -> cookieHeader)
}
object NuanceAuthInformation {
  implicit val format: Format[NuanceAuthInformation] = Json.format[NuanceAuthInformation]
}

object NuanceAuthResponse extends Logging {
  private val cookieHeaderEncoding = new DefaultCookieHeaderEncoding

  implicit lazy val httpReads: HttpReads[NuanceAuthResponse] =
    (_: String, _: String, response: HttpResponse) => {
      response.status match {
        case Status.OK | Status.FOUND =>
          val cookieHeaders: Seq[String] = response.headers(HeaderNames.SET_COOKIE)
          val cookieHeadersAsString = cookieHeaders.mkString(cookieHeaderEncoding.SetCookieHeaderSeparator)
          val cookies: Seq[Cookie] = cookieHeaderEncoding.decodeSetCookieHeader(cookieHeadersAsString)
          logger.info("[NuanceAuthResponse] Got a successful reaponce from auth API")
          NuanceAuthInformation(cookieHeaderEncoding.encodeCookieHeader(cookies))
        case Status.BAD_REQUEST =>
          logger.warn("[NuanceAuthResponse] Got 'bad request' response from auth API")
          NuanceAuthBadRequest
        case Status.UNAUTHORIZED =>
          logger.error("[NuanceAuthResponse] Got 'unauthorized' response from auth API")
          NuanceAuthUnauthorised
        case code =>
          logger.warn(s"[NuanceAuthResponse] Got error $code from auth API")
          NuanceAuthServerError
      }
    }
}
