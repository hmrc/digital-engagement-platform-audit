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

import akka.actor.ActorSystem
import config.AppConfig
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.ws.{WSClient, WSProxyServer}
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.play.http.ws.WSProxy

@Singleton
class ProxiedHttpClient @Inject()(configuration: Configuration,
                                  httpAuditing: HttpAuditing,
                                  wsClient: WSClient,
                                  actorSystem: ActorSystem,
                                  config: AppConfig)
  extends DefaultHttpClient(configuration, httpAuditing, wsClient, actorSystem) with WSProxy {

  override def wsProxyServer: Option[WSProxyServer] = config.wsProxyServer
}
