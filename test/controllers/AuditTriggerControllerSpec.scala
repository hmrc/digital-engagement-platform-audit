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

package controllers

import java.time.LocalDateTime

import auditing.HistoricAuditing
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuditTriggerControllerSpec extends AnyWordSpec with Matchers with MockitoSugar {

  private val fakeRequest = FakeRequest("GET", "/")
  private val historicAuditing = mock[HistoricAuditing]
  when(historicAuditing.auditDateRange(any(), any())).thenReturn(Future.successful(Seq()))
  private val controller = new AuditTriggerController(
    historicAuditing,
    Helpers.stubControllerComponents()
  )

  "GET trigger" should {
    "return 200" in {
      val result = controller.trigger("2020-04-20T00:00", "2020-05-30T00:00")(fakeRequest)
      status(result) shouldBe Status.OK
      verify(historicAuditing).auditDateRange(
        LocalDateTime.parse("2020-04-20T00:00"),
        LocalDateTime.parse("2020-05-30T00:00")
      )
    }
  }
}
