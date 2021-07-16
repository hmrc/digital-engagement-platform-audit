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

package connectors

import java.time.LocalDateTime

import config.AppConfig
import javax.inject.Inject
import models.{NuanceAuthInformation, NuanceReportingResponse}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.{ExecutionContext, Future}

case class NuanceReportingRequest(start: Int, rows: Int, startDate: LocalDateTime, endDate: LocalDateTime)

class NuanceReportingConnector @Inject()(http: ProxiedHttpClient, config: AppConfig)(implicit ec: ExecutionContext) {

  def getHistoricData(authInfo: NuanceAuthInformation, request: NuanceReportingRequest):
    Future[NuanceReportingResponse] = {

    implicit val hc : HeaderCarrier = new HeaderCarrier(extraHeaders = authInfo.toHeaders)

    http.GET[NuanceReportingResponse](
      url = config.nuanceReportingUrl,
      queryParams = Seq(
        "site" -> config.hmrcSiteId,
        "filter" -> s"""endDate>="${request.startDate}" and endDate<="${request.endDate}"""",
        "returnFields" -> "ALL",
        "start" -> request.start.toString,
        "rows" -> request.rows.toString
      ),
      Seq()
    )
  }
}
