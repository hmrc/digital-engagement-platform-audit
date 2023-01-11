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

package actors

import java.time.LocalDateTime

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import auditing.HistoricAuditing
import com.mongodb.client.result.DeleteResult
import models.AuditJob
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import repositories.AuditJobRepository
import utils.BaseSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuditJobProcessorSpec extends TestKit(ActorSystem("AuditJobProcessorSpec"))
  with BaseSpec
  with ImplicitSender
  with BeforeAndAfterEach
  with BeforeAndAfterAll {

  private val auditJobRepository = mock[AuditJobRepository]
  private val historicAuditing = mock[HistoricAuditing]

  override def beforeEach(): Unit = {
    reset(auditJobRepository)
    reset(historicAuditing)
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "JobProcessorImpl" should {
    "be happy if there are no jobs to process" in {

      when(auditJobRepository.findNextJobToProcess()).thenReturn(Future.successful(None))

      val jobProcessor = system.actorOf(Props(classOf[AuditJobProcessor], auditJobRepository, historicAuditing, global))
      jobProcessor ! AuditJobProcessor.ProcessNext
      expectMsg(AuditJobProcessor.DoneProcessing)

      verify(auditJobRepository, times(1)).findNextJobToProcess()
    }

    "should process job if it finds one" in {

      val startTime = LocalDateTime.parse("2020-04-20T00:00:10")
      val endTime = LocalDateTime.parse("2020-05-20T00:00:10")
      val auditJob = AuditJob(
        startTime,
        endTime,
        LocalDateTime.now())

      when(auditJobRepository.findNextJobToProcess()).thenReturn(Future.successful(Some(auditJob)))
      when(auditJobRepository.setJobInProgress(any(), any())).thenReturn(Future.successful(Some(auditJob)))
      when(auditJobRepository.deleteJob(any())).thenReturn(Future.successful(DeleteResult.acknowledged(1)))

      when(historicAuditing.auditDateRange(any(), any())).thenReturn(Future.successful(Seq()))

      val jobProcessor = system.actorOf(Props(classOf[AuditJobProcessor], auditJobRepository, historicAuditing, global))
      jobProcessor ! AuditJobProcessor.ProcessNext
      expectMsg(AuditJobProcessor.DoneProcessing)

      verify(auditJobRepository, times(1)).findNextJobToProcess()
      verify(auditJobRepository, times(1)).setJobInProgress(auditJob, inProgress = true)
      verify(historicAuditing, times(1)).auditDateRange(startTime, endTime)
      verify(auditJobRepository, times(1)).deleteJob(auditJob)
    }

    "should ignore the job if it can't set to in progress" in {

      val startTime = LocalDateTime.parse("2020-04-20T00:00:10")
      val endTime = LocalDateTime.parse("2020-05-20T00:00:10")
      val auditJob = models.AuditJob(
        startTime,
        endTime,
        LocalDateTime.now())

      when(auditJobRepository.findNextJobToProcess()).thenReturn(Future.successful(Some(auditJob)))
      when(auditJobRepository.setJobInProgress(any(), any())).thenReturn(Future.successful(None))

      val jobProcessor = system.actorOf(Props(classOf[AuditJobProcessor], auditJobRepository, historicAuditing, global))
      jobProcessor ! AuditJobProcessor.ProcessNext
      expectMsg(AuditJobProcessor.DoneProcessing)

      verify(auditJobRepository, times(1)).findNextJobToProcess()
      verify(auditJobRepository, times(1)).setJobInProgress(auditJob, inProgress = true)
      verify(historicAuditing, times(0)).auditDateRange(startTime, endTime)
      verify(auditJobRepository, times(0)).deleteJob(auditJob)
    }
  }
}
