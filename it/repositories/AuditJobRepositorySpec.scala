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
    "add an audit job to the database" in {
      val repository = app.injector.instanceOf[AuditJobRepository]

      val job1 = AuditJob(LocalDateTime.parse("2020-06-20T13:15"), LocalDateTime.parse("2021-07-05T09:15"), LocalDateTime.now())
      val job2 = AuditJob(LocalDateTime.parse("2020-06-20T13:15"), LocalDateTime.parse("2021-07-05T09:16"), LocalDateTime.now().plusSeconds(1))

      val results = for {
        drop <- repository.drop()
        add1 <- repository.add(job1)
        add2 <- repository.add(job2)
        found <- repository.find()
      } yield found

      val jobs = Await.result(results, Duration.Inf)

      jobs mustEqual Seq(job1, job2)
    }
  }
}
