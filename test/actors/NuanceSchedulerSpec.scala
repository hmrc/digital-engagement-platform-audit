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

package actors

import java.time.LocalDateTime

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.mongodb.client.result.InsertOneResult
import models.AuditJob
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.mongodb.scala.bson.BsonString
import org.scalatest.BeforeAndAfterAll
import repositories.AuditJobRepository
import services.LocalDateTimeService
import utils.BaseSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NuanceSchedulerSpec extends TestKit(ActorSystem("NuanceSchedulerSpec"))
  with BaseSpec
  with ImplicitSender
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "NuanceScheduler" should {
    "schedule a job for the past X minutes" in {

      val localDateTimeService = mock[LocalDateTimeService]
      val currentDateTime = LocalDateTime.parse("2021-07-08T13:45:00")
      when(localDateTimeService.now).thenReturn(currentDateTime)

      val auditJobRepository = mock[AuditJobRepository]

      when(auditJobRepository.addJob(any())).thenReturn(Future.successful(InsertOneResult.acknowledged(BsonString("Success"))))

      val interval = 120
      val offset = 150
      val nuanceScheduler = system.actorOf(Props(classOf[NuanceScheduler], auditJobRepository, localDateTimeService, global))
      nuanceScheduler ! NuanceScheduler.ScheduleIntervalInPast(interval, offset)
      expectMsg(NuanceScheduler.NuanceJobScheduled)

      val expectedStartDateTime = currentDateTime.minusMinutes(interval + offset)
      val expectedEndDateTime = currentDateTime.minusMinutes(offset)
      verify(auditJobRepository, times(1)).addJob(AuditJob(
        expectedStartDateTime,
        expectedEndDateTime,
        currentDateTime
      ))
    }
  }
}
