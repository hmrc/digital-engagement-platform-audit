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

package repositories

import java.time.LocalDateTime

import models.AuditJob
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class AuditJobRepositorySpec extends AnyWordSpec with Matchers with MockitoSugar with GuiceOneAppPerSuite {
  def applicationBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(Seq(
        "mongodb.uri" -> "mongodb://localhost:27017/digital-engagement-platform-audit-test",
        "metrics.enabled" -> false,
        "auditing.enabled" -> false
      ): _*)

  override lazy val app: Application = applicationBuilder.build()

  "AuditJobRepository" must {
    "add audit jobs to the database and get them back" in {
      val repository = app.injector.instanceOf[AuditJobRepository]

      val submissionDate = LocalDateTime.now()
      val job1 = AuditJob(
        LocalDateTime.parse("2020-06-20T13:15"),
        LocalDateTime.parse("2021-07-05T09:15"),
        submissionDate)
      val job2 = models.AuditJob(LocalDateTime.parse("2020-06-20T13:15"), LocalDateTime.parse("2021-07-05T13:54:44.75"), submissionDate.plusSeconds(1))

      val results = for {
        _ <- repository.drop()
        _ <- repository.addJob(job1)
        _ <- repository.addJob(job2)
        found <- repository.findAllJobs()
      } yield found

      val jobs = Await.result(results, Duration.Inf)

      jobs mustEqual Seq(job1, job2)
    }

    "add audit jobs to the database and find by date" in {
      val repository = app.injector.instanceOf[AuditJobRepository]

      val submissionDate = LocalDateTime.now()
      val job1 = models.AuditJob(LocalDateTime.parse("2020-06-20T13:15"), LocalDateTime.parse("2021-07-05T09:15"), submissionDate)
      val job2 = models.AuditJob(LocalDateTime.parse("2020-06-20T13:15"), LocalDateTime.parse("2021-07-05T09:16"), submissionDate.plusSeconds(1))

      val results = for {
        _ <- repository.drop()
        _ <- repository.addJob(job1)
        _ <- repository.addJob(job2)
        found <- repository.findJob(submissionDate)
      } yield found

      val jobs = Await.result(results, Duration.Inf)

      jobs mustEqual Seq(job1)
    }

    "update an audit job in the database" in {
      val repository = app.injector.instanceOf[AuditJobRepository]

      val submissionDate = LocalDateTime.now()
      val job1 = models.AuditJob(LocalDateTime.parse("2020-06-20T13:15"), LocalDateTime.parse("2021-07-05T09:15"), submissionDate)
      val job2 = models.AuditJob(LocalDateTime.parse("2020-06-20T13:15"), LocalDateTime.parse("2021-07-05T09:16"), submissionDate.plusSeconds(1))

      val results = for {
        _ <- repository.drop()
        _ <- repository.addJob(job1)
        _ <- repository.addJob(job2)
        found <- repository.findAllJobs()
      } yield found

      val jobs = Await.result(results, Duration.Inf)

      jobs mustEqual Seq(job1, job2)

      val modifiedResults = for {
        _ <- repository.setJobInProgress(jobs.head, inProgress = true)
        found <- repository.findAllJobs()
      } yield found

      val modifiedJobs = Await.result(modifiedResults, Duration.Inf)

      val modifiedJob1 = job1.copy(inProgress = true)

      modifiedJobs mustEqual Seq(modifiedJob1, job2)
    }

    "update an audit job in the database if not already updated" in {
      val repository = app.injector.instanceOf[AuditJobRepository]

      val submissionDate = LocalDateTime.now()
      val job1 = models.AuditJob(LocalDateTime.parse("2020-06-20T13:15"), LocalDateTime.parse("2021-07-05T09:15"), submissionDate)
      val job2 = models.AuditJob(LocalDateTime.parse("2020-06-20T13:15"), LocalDateTime.parse("2021-07-05T09:16"), submissionDate.plusSeconds(1))

      val results = for {
        _ <- repository.drop()
        _ <- repository.addJob(job1)
        _ <- repository.addJob(job2)
        found <- repository.findAllJobs()
      } yield found

      val jobs = Await.result(results, Duration.Inf)

      jobs mustEqual Seq(job1, job2)

      val jobToModify = jobs.head

      val modifiedResults = for {
        _ <- repository.setJobInProgress(jobToModify, inProgress = true)
        job <- repository.setJobInProgress(jobToModify, inProgress = true)
      } yield job

      val modifiedJob = Await.result(modifiedResults, Duration.Inf)

      modifiedJob mustBe None
    }

    "find next not in progress with no jobs" in {
      val repository = app.injector.instanceOf[AuditJobRepository]

      val results = for {
        _ <- repository.drop()
        found <- repository.findNextJobToProcess()
      } yield found

      val job = Await.result(results, Duration.Inf)

      job mustEqual None
    }

    "add audit jobs to the database and find next not in progress" in {
      val repository = app.injector.instanceOf[AuditJobRepository]

      val submissionDate = LocalDateTime.now()
      val job1 = models.AuditJob(LocalDateTime.parse("2020-06-20T13:15"), LocalDateTime.parse("2021-07-05T09:15"), submissionDate)
      val job2 = models.AuditJob(LocalDateTime.parse("2020-06-20T13:15"), LocalDateTime.parse("2021-07-05T09:16"), submissionDate.plusSeconds(1))

      val results = for {
        _ <- repository.drop()
        _ <- repository.addJob(job1)
        _ <- repository.addJob(job2)
        found1 <- repository.findNextJobToProcess()
        _ <- repository.setJobInProgress(found1.get, inProgress = true)
        found2 <- repository.findNextJobToProcess()
      } yield (found1, found2)

      val jobs = Await.result(results, Duration.Inf)

      jobs._1 mustEqual Option(job1)
      jobs._2 mustEqual Option(job2)
    }

    "delete audit jobs from the database" in {
      val repository = app.injector.instanceOf[AuditJobRepository]

      val submissionDate = LocalDateTime.now()
      val job1 = models.AuditJob(
        LocalDateTime.parse("2020-06-20T13:15"),
        LocalDateTime.parse("2021-07-05T09:15"),
        submissionDate)
      val job2 = models.AuditJob(LocalDateTime.parse("2020-06-20T13:15"), LocalDateTime.parse("2021-07-05T13:54:44.75"), submissionDate.plusSeconds(1))

      val futures = for {
        _ <- repository.drop()
        _ <- repository.addJob(job1)
        _ <- repository.addJob(job2)
        deleteResult <- repository.deleteJob(job1)
        found <- repository.findAllJobs()
      } yield (deleteResult, found)

      val results = Await.result(futures, Duration.Inf)

      results._1.wasAcknowledged() mustBe true
      results._1.getDeletedCount mustBe 1
      results._2 mustEqual Seq(job2)
    }
  }
}
