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

package auditing

import java.time.LocalDateTime

import config.AppConfig
import connectors.NuanceReportingRequest
import javax.inject.Inject
import models.{NuanceReportingResponse, ValidNuanceReportingResponse}
import services.NuanceReportingService

import scala.concurrent.{ExecutionContext, Future}

class HistoricAuditing @Inject()(
                                  reportingService: NuanceReportingService,
                                  engagementAuditing: EngagementAuditing,
                                  appConfig: AppConfig)(
                                  implicit executionContext: ExecutionContext) {
  def auditDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Future[NuanceReportingResponse] = {
    val request = NuanceReportingRequest(0, appConfig.auditingChunkSize, startDate, endDate)
    reportingService.getHistoricData(request) map {
      case response: ValidNuanceReportingResponse =>
        engagementAuditing.processEngagements(response.engagements)
        response
      case response => response
    }
  }
}
