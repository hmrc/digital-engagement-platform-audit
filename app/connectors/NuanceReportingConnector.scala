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
import models.{NuanceAuthInformation, NuanceReportingResponse, TokenExchangeResponse}
import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}
import java.util.Locale
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class NuanceReportingRequest(start: Int, rows: Int, startDate: LocalDateTime, endDate: LocalDateTime)

class NuanceReportingConnector @Inject()(http: ProxiedHttpClient, config: AppConfig)(implicit ec: ExecutionContext) extends Logging {

  def getHistoricData(authInfo: NuanceAuthInformation, request: NuanceReportingRequest):
  Future[NuanceReportingResponse] = {

    implicit val hc: HeaderCarrier = new HeaderCarrier(extraHeaders = authInfo.toHeaders)

    val queryParams = Seq(
      "site" -> config.hmrcSiteId,
      "filter" -> s"""startDate>="${request.startDate}" and startDate<="${request.endDate}"""",
      "returnFields" -> "ALL",
      "start" -> request.start.toString,
      "rows" -> request.rows.toString
    )

    logger.info(s"[getHistoricData] read from url ${config.nuanceReportingUrl} with params $queryParams")

    http.get()
      .get(url"${config.nuanceReportingUrl}?$queryParams")
      .execute[NuanceReportingResponse]
  }

  def getHistoricDataV3Api(tokenExchangeResponse: TokenExchangeResponse, request: NuanceReportingRequest)
  : Future[NuanceReportingResponse] = {

    implicit val hc: HeaderCarrier = new HeaderCarrier()

    val dateTimeFormatter =
      DateTimeFormatter.ofPattern("YYYY-MM-DD'T'hh:mm:ss")
        .withLocale(Locale.UK)
        .withZone(ZoneId.of("GMT"))

    val formattedStartDate = request.startDate.format(dateTimeFormatter)
    val formattedEndDate = request.endDate.format(dateTimeFormatter)

    val queryParams = Seq(
      "site" -> config.hmrcSiteId,
      "filter" -> s"""startDate>="$formattedStartDate" AND startDate<="$formattedEndDate"""",
      "returnFields" -> "ALL",
      "start" -> request.start.toString,
      "rows" -> request.rows.toString,
    )

    http.get()
      .get(url"${config.nuanceTokenApiUrl}/v3/transcript/historic?$queryParams")
      .setHeader("Authorization" -> s"Bearer ${tokenExchangeResponse.access_token}")
      .execute[NuanceReportingResponse]
  }
}
