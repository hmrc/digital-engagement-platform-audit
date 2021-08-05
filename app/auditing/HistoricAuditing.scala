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
import play.api.Logging
import play.api.libs.json.JsValue
import services.NuanceReportingService

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

case class HistoricAuditingResult(start: Int, rows: Int)

class SuccessfulHistoricAuditingResult(request: NuanceReportingRequest)
  extends HistoricAuditingResult(request.start, request.rows)

class FailedHistoricAuditingResult(request: NuanceReportingRequest, val response: NuanceReportingResponse)
  extends HistoricAuditingResult(request.start, request.rows)

class HistoricAuditingExceptionResult(request: NuanceReportingRequest, val e: String)
  extends HistoricAuditingResult(request.start, request.rows)

class HistoricAuditing @Inject()(
                                  reportingService: NuanceReportingService,
                                  engagementAuditing: EngagementAuditing,
                                  appConfig: AppConfig)(
                                  implicit executionContext: ExecutionContext) extends Logging {
  def auditDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Future[Seq[HistoricAuditingResult]] = {
    val request = NuanceReportingRequest(start = 0, rows = 0, startDate, endDate)
    reportingService.getHistoricData(request) map {
      case response: ValidNuanceReportingResponse =>
        logger.info(s"[auditDateRange]: processing ${response.engagements.as[List[JsValue]].size} engagements")
        processAll(startDate, endDate, response.numFound)
      case response =>
        logger.warn(s"[auditDateRange] Got error reading number of engagements: $response")
        Seq(new FailedHistoricAuditingResult(request, response))
    }
  }

  private def processAll(startDate: LocalDateTime, endDate: LocalDateTime, numFound: Int): Seq[HistoricAuditingResult] = {
    val starts = 0 until numFound by appConfig.auditingChunkSize
    starts.map {
      start =>
        val f = {
          val request = NuanceReportingRequest(
            start,
            appConfig.auditingChunkSize.min(numFound - start),
            startDate,
            endDate)

          try {
            reportingService.getHistoricData(request).flatMap {
              case response: ValidNuanceReportingResponse =>
                logger.info(s"[processAll]: processing chunk of ${response.engagements.as[List[JsValue]].size} engagements")
                engagementAuditing.processEngagements(response.engagements).map {
                  _ => new SuccessfulHistoricAuditingResult(request)
                }
              case response =>
                logger.warn(s"[processAll] Got error getting data: $response")
                Future.successful(new FailedHistoricAuditingResult(request, response))
            }
          } catch {
            case e: Throwable =>
              logger.warn(s"[processAll] Got exception when getting historic data: ${e.getMessage}")
              Future.successful(new HistoricAuditingExceptionResult(request, e.getMessage))
          }
        }
        Await.result(f, Duration.Inf)
    }
  }
}
