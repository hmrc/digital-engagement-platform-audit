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

package services

import connectors.{NuanceAuthConnector, NuanceReportingConnector, NuanceReportingRequest}

import javax.inject.Inject
import models._
import play.api.Logging

import scala.concurrent.{ExecutionContext, Future}

class NuanceReportingService @Inject()(
                                        authConnector: NuanceAuthConnector,
                                        reportingConnector: NuanceReportingConnector)
                                      (implicit ec: ExecutionContext) extends Logging {

  def getHistoricDataV3(request: NuanceReportingRequest): Future[NuanceReportingResponse] = {
    authConnector.requestAccessToken() flatMap {
      case tokenExchangeResponse: TokenExchangeResponse =>
        logger.info(s"[getHistoricDataV3] Authentication request success with: - $request")
         reportingConnector.getHistoricDataV3Api(tokenExchangeResponse, request)

      case authError: NuanceAccessTokenResponse =>
        logger.warn("[getHistoricDataV3] Unable to authenticate with Nuance server.")
       Future.successful(NuanceAccessTokenFailure(authError))
    }
  }

  def getHistoricData(request: NuanceReportingRequest): Future[NuanceReportingResponse] = {
    authConnector.authenticate() flatMap {
      case authInfo: NuanceAuthInformation =>
        logger.info(s"[getHistoricData] Authentication request success with: - $request")
        reportingConnector.getHistoricData(authInfo, request)
      case authError: NuanceAuthResponse =>
        logger.warn("[getHistoricData] Unable to authenticate with Nuance server.")
        Future.successful(NuanceAuthFailure(authError))
    }
  }
}
