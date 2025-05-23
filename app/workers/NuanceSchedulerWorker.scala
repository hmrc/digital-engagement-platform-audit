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

package workers

import actors.NuanceScheduler
import org.apache.pekko.actor.{ActorRef, ActorSystem, Cancellable}
import com.google.inject.name.Named
import config.AppConfig
import javax.inject.{Inject, Singleton}
import play.api.inject.ApplicationLifecycle

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait NuanceSchedulerWorker

@Singleton
class NuanceSchedulerWorkerImpl @Inject() (
                                            @Named("nuance-scheduler") nuanceScheduler: ActorRef,
                                            actorSystem: ActorSystem,
                                            appConfig: AppConfig,
                                            applicationLifecycle: ApplicationLifecycle)
                                   (implicit ec: ExecutionContext) extends NuanceSchedulerWorker {

  private object NullJob extends Cancellable {
    def cancel(): Boolean = false
    def isCancelled: Boolean = false
  }

  val job: Cancellable = {
    if (appConfig.startNuanceSchedulerWorker) {
      val scheduledJob = actorSystem.scheduler.scheduleAtFixedRate(
        0.seconds,
        appConfig.nuanceSchedulerIntervalInMinutes.minutes,
        nuanceScheduler,
        NuanceScheduler.ScheduleIntervalInPast(
          appConfig.nuanceSchedulerIntervalInMinutes,
          appConfig.nuanceSchedulerOffsetInMinutes))

      applicationLifecycle.addStopHook { () =>
        Future.successful(scheduledJob.cancel())
      }

      scheduledJob
    } else {
      NullJob
    }
  }
}
