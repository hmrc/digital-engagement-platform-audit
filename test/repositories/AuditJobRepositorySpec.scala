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

import org.mongodb.scala.{FindObservable, SingleObservable}
import org.mongodb.scala.result.InsertOneResult
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class AuditJobRepositorySpec extends AnyWordSpec with Matchers with MockitoSugar with GuiceOneAppPerSuite {
  "AuditJobRepository" must {
    "add an audit job to the database" in {
      val repository = app.injector.instanceOf[AuditJobRepository]

      val job = AuditJob(LocalDateTime.parse("2020-06-20T13:15"), LocalDateTime.now())
      val result: SingleObservable[InsertOneResult] = repository.add(job)
      result.subscribe(
        (result: InsertOneResult) => {
          println(s"====== result1: $result")

          val job = AuditJob(LocalDateTime.parse("2021-06-20T13:15"), LocalDateTime.now())
          val result2: SingleObservable[InsertOneResult] = repository.add(job)
          result2.subscribe(
            (result: InsertOneResult) => {

              println(s"====== result2: $result")
              val findResult: FindObservable[AuditJob] = repository.find()
              findResult.subscribe(
                (result: AuditJob) => println(s"------ found audit job: $result"),
                (e: Throwable) => println(s"Find: There was an error: $e")
              )
            })
        },
        (e: Throwable) => println(s"First insert: There was an error: $e")
      )

      Thread.sleep(4000)
    }
  }
}
