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

import scala.concurrent.{ExecutionContext, Future}

object AuditJobProcessor {
  object ProcessNext
  object DoneProcessing
}

class AuditJobProcessor @Inject()(repository: AuditJobRepository, historicAuditing: HistoricAuditing)
                                 (implicit ec: ExecutionContext) extends Actor {
  override def receive: Receive = {
    case AuditJobProcessor.ProcessNext =>
      val localSender = sender()
      processNext().map { _ =>
        localSender ! AuditJobProcessor.DoneProcessing
      }
  }

  def processNext(): Future[Boolean] = {
    repository.findNextJobToProcess() flatMap {
      case Some(auditJob: AuditJob) =>
        // A job to process
        repository.setJobInProgress(auditJob, inProgress = true) flatMap {
          case Some(auditJob: AuditJob) =>
            // Job successfully set to "in progress"
            processJob(auditJob) flatMap { _ =>
              repository.deleteJob(auditJob) map { _ => true }
            }
          case None =>
            // Could not set job to "in progress"
            Future.successful(true)
        }
      case None =>
        // No job to process
        Future.successful(true)
    }
  }

  private def processJob(auditJob: AuditJob) = {
    historicAuditing.auditDateRange(auditJob.startDate, auditJob.endDate) map {
      _: Seq[HistoricAuditingResult] => true
    }
  }
}
