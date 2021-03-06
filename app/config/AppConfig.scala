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

package config

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.ws.WSProxyServer
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.http.ws.WSProxyConfiguration

@Singleton
class AppConfig @Inject()
  (
    config: Configuration, servicesConfig: ServicesConfig
  )
{
  val nuanceAuthUrl: String = servicesConfig.baseUrl("nuance-api") + "/j_spring_security_check"
  val nuanceAuthName: String = config.get[String]("nuance.auth-name")
  val nuanceAuthPassword: String = config.get[String]("nuance.auth-password")
  val nuanceReportingUrl: String = servicesConfig.baseUrl("nuance-api") + "/v3/transcript/historic"
  val hmrcSiteId: String = config.get[String]("nuance.site-id")

  val auditingChunkSize: Int = config.get[Int]("nuance.auditing-chunk-size")

  val startJobProcessorWorker: Boolean = config.getOptional[Boolean](path = "workers.job-processor.enabled").getOrElse(true)
  val startNuanceSchedulerWorker: Boolean = config.getOptional[Boolean](path = "workers.nuance-scheduler.enabled").getOrElse(true)

  private val DefaultAuditJobWorkerInterval = 10      // 10 seconds
  val auditJobWorkerIntervalInSeconds: Int = config.getOptional[Int]("workers.job-processor.interval-in-seconds").getOrElse(DefaultAuditJobWorkerInterval)

  private val DefaultNuanceSchedulerOffset = 180      // 3 hours
  private val DefaultNuanceSchedulerInterval = 120    // 2 hours
  val nuanceSchedulerIntervalInMinutes: Int = config.getOptional[Int]("workers.nuance-scheduler.interval-in-minutes").getOrElse(DefaultNuanceSchedulerInterval)
  val nuanceSchedulerOffsetInMinutes: Int = config.getOptional[Int]("workers.nuance-scheduler.offset-in-minutes").getOrElse(DefaultNuanceSchedulerOffset)

  lazy val wsProxyServer: Option[WSProxyServer] = WSProxyConfiguration("proxy", config)
}
