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

import config.AppConfig
import javax.inject.Inject
import models.NuanceReportingResponse
import play.api.http.HeaderNames
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.{ExecutionContext, Future}

class NuanceReportingConnector @Inject()(http: HttpClient, config: AppConfig)(implicit ec: ExecutionContext) {

  def getHistoricData(start: Int, rows: Int, sessionId: String, filter: String = "totalConversions>=0"):
    Future[NuanceReportingResponse] = {

    val extraHeaders = Seq(HeaderNames.COOKIE -> s"JSESSIONID=$sessionId")
    implicit val hc : HeaderCarrier = new HeaderCarrier(extraHeaders = extraHeaders)

    http.GET[NuanceReportingResponse](
      url = config.nuanceReportingUrl,
      queryParams = Seq(
        "site" -> config.hmrcSiteId,
        "filter" -> filter,
        "returnFields" -> "ALL",
        "start" -> start.toString,
        "rows" -> rows.toString
      ),
      Seq()
    )
  }
}
