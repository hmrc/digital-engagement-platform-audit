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

package config

import javax.inject.{Inject, Singleton}
import play.api.Configuration

@Singleton
class AppConfig @Inject()
  (
    config: Configuration
  )
{
  val nuanceAuthUrl: String = config.get[String]("nuance.auth-url")
  val nuanceAuthName: String = config.get[String]("nuance.auth-name")
  val nuanceAuthPassword: String = config.get[String]("nuance.auth-password")
  val nuanceReportingUrl: String = config.get[String]("nuance.reporting-url")
  val hmrcSiteId: String = config.get[String]("nuance.site-id")

  val auditingChunkSize: Int = config.get[Int]("nuance.auditing-chunk-size")

  val ttlInSeconds: Int = config.get[Int]("mongodb.ttlSeconds")

  val startJobProcessorWorker: Boolean = config.getOptional[Boolean](path = "workers.job-processor.start").getOrElse(true)
  val startNuanceSchedulerWorker: Boolean = config.getOptional[Boolean](path = "workers.nuance-scheduler.start").getOrElse(true)

  val DefaultAuditJobWorkerInitialDelay = 0
  val DefaultAuditJobWorkerInterval = 10
  val auditJobWorkerInitialDelayInSeconds: Int = config.getOptional[Int]("workers.job-processor.initial-delay-in-seconds").getOrElse(DefaultAuditJobWorkerInitialDelay)
  val auditJobWorkerIntervalInSeconds: Int = config.getOptional[Int]("workers.job-processor.interval-in-seconds").getOrElse(DefaultAuditJobWorkerInterval)

  val DefaultNuanceSchedulerInterval = 120    // 2 hours
  val nuanceSchedulerIntervalInMinutes: Int = config.getOptional[Int]("workers.nuance-scheduler.interval-in-minutes").getOrElse(DefaultNuanceSchedulerInterval)
}
