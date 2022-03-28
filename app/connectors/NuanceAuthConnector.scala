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

package connectors

import config.AppConfig
import javax.inject.Inject
import models.NuanceAuthResponse
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class NuanceAuthConnector @Inject()(http: ProxiedHttpClient, config: AppConfig)(implicit ec: ExecutionContext) {

  def authenticate(): Future[NuanceAuthResponse] = {

    implicit val hc : HeaderCarrier = new HeaderCarrier

    http.POSTForm[NuanceAuthResponse](
      config.nuanceAuthUrl,
      Map(
        "j_username" -> Seq(config.nuanceAuthName),
        "j_password" -> Seq(config.nuanceAuthPassword),
        "submit" -> Seq("Login")
      ),
      Seq()
    )
  }
}
