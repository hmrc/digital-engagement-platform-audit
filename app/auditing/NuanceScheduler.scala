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

import akka.actor.Actor
import javax.inject.Inject
import models.AuditJob
import repositories.AuditJobRepository
import services.LocalDateTimeService

import scala.concurrent.ExecutionContext

object NuanceScheduler {
  case class ScheduleRecentPast(intervalInMinutes: Int)
  object NuanceJobScheduled
}

class NuanceScheduler @Inject()(
                                 repository: AuditJobRepository,
                                 localDateTimeService: LocalDateTimeService)(implicit ec: ExecutionContext) extends Actor {
  override def receive: Receive = {
    case message: NuanceScheduler.ScheduleRecentPast =>
      val endDateTime = localDateTimeService.now
      val startDateTime = endDateTime.minusMinutes(message.intervalInMinutes)
      val localSender = sender()
      repository.addJob(AuditJob(startDateTime, endDateTime, endDateTime)) map {
        _ => localSender ! NuanceScheduler.NuanceJobScheduled
      }
  }
}