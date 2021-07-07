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

import akka.actor.{Actor, ActorRef, ActorSystem, Cancellable}
import auditing.AuditJobProcessor
import com.google.inject.name.Named
import config.AppConfig
import javax.inject.{Inject, Singleton}
import play.api.inject.ApplicationLifecycle

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait AuditJobWorker

@Singleton
class AuditJobWorkerImpl @Inject() (
                                 @Named("audit-job-processor") jobProcessor: ActorRef,
                                 schedulerActorSystem: ActorSystem,
                                 appConfig: AppConfig,
                                 applicationLifecycle: ApplicationLifecycle)
                               (implicit ec: ExecutionContext) extends AuditJobWorker {

  private object NullJob extends Cancellable {
    println("AuditJobWorker is not starting up")
    def cancel(): Boolean = false
    def isCancelled: Boolean = false
  }

  val job: Cancellable = {
    if (appConfig.startWorkers) {
      val scheduler = schedulerActorSystem.scheduler
      println("AuditJobWorker is started up")

      val scheduledJob = schedulerActorSystem.scheduler.scheduleAtFixedRate(0.seconds, 10.seconds, jobProcessor, AuditJobProcessor.ProcessNext)

      applicationLifecycle.addStopHook { () =>
        println("Shutting down AuditJobWorker...")
        Future.successful(scheduledJob.cancel())
      }

      scheduledJob
    } else {
      NullJob
    }
  }
}
