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

package workers

import akka.actor.{ActorRef, ActorSystem, Cancellable}
import auditing.AuditJobProcessor
import com.google.inject.name.Named
import config.AppConfig
import javax.inject.{Inject, Singleton}
import play.api.inject.ApplicationLifecycle

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait AuditJobProcessorWorker

@Singleton
class AuditJobProcessorWorkerImpl @Inject()(
                                     @Named("audit-job-processor") jobProcessor: ActorRef,
                                     actorSystem: ActorSystem,
                                     appConfig: AppConfig,
                                     applicationLifecycle: ApplicationLifecycle)
                                           (implicit ec: ExecutionContext) extends AuditJobProcessorWorker {

  private object NullJob extends Cancellable {
    def cancel(): Boolean = false
    def isCancelled: Boolean = false
  }

  val job: Cancellable = {
    if (appConfig.startJobProcessorWorker) {
      val scheduledJob = actorSystem.scheduler.scheduleAtFixedRate(
        0.seconds,
        appConfig.auditJobWorkerIntervalInSeconds.seconds,
        jobProcessor,
        AuditJobProcessor.ProcessNext)

      applicationLifecycle.addStopHook { () =>
        Future.successful(scheduledJob.cancel())
      }

      scheduledJob
    } else {
      NullJob
    }
  }
}
