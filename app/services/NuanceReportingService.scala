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

package services

import connectors.{NuanceAuthConnector, NuanceReportingConnector, NuanceReportingRequest}
import javax.inject.Inject
import models.{NuanceAuthFailure, NuanceAuthInformation, NuanceReportingResponse}
import play.api.Logging

import scala.concurrent.{ExecutionContext, Future}

class NuanceReportingService @Inject()(
                                        authConnector: NuanceAuthConnector,
                                        reportingConnector: NuanceReportingConnector)
                                      (implicit ec: ExecutionContext) extends Logging {
  def getHistoricData(request: NuanceReportingRequest): Future[NuanceReportingResponse] = {
    authConnector.authenticate() flatMap {
      case authInfo: NuanceAuthInformation => {
        logger.info(s"[getHistoricData] Authentication request success with: - $request")
        reportingConnector.getHistoricData(authInfo, request)
      }
      case authError =>
        logger.warn("[getHistoricData] Unable to authenticate with Nuance server.")
        Future.successful(NuanceAuthFailure(authError))
    }
  }
}
