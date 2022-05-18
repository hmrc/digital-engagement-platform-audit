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

package controllers

import java.time.LocalDateTime

import com.mongodb.client.result.InsertOneResult
import models.AuditJob
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.mongodb.scala.bson.BsonString
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import repositories.AuditJobRepository
import services.LocalDateTimeService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuditTriggerControllerSpec extends AnyWordSpec with Matchers with MockitoSugar {

  private val fakeRequest = FakeRequest("GET", "/")

  private val localDateTime = LocalDateTime.parse("2021-07-09T13:24:26")
  private val localDateTimeService = mock[LocalDateTimeService]
  when(localDateTimeService.now).thenReturn(localDateTime)

  private val jobRepository = mock[AuditJobRepository]
  when(jobRepository.addJob(any())).thenReturn(Future.successful(InsertOneResult.acknowledged(BsonString("Success"))))

  private val controller = new AuditTriggerController(
    jobRepository,
    localDateTimeService,
    Helpers.stubControllerComponents()
  )

  "GET trigger" should {
    "add the audit job to the repository" in {
      val startDate = "2020-04-20T00:00"
      val endDate = "2020-05-30T00:00"
      val result = controller.trigger(startDate, endDate)(fakeRequest)
      status(result) shouldBe Status.OK
      verify(jobRepository).addJob(
        AuditJob(
          LocalDateTime.parse(startDate),
          LocalDateTime.parse(endDate),
          localDateTime
        )
      )
    }
  }
}
