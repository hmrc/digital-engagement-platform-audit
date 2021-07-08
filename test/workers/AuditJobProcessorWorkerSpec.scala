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

package workers

import akka.actor.{Actor, ActorRef, ActorSystem, Cancellable, Scheduler}
import akka.testkit.{TestActorRef, TestKit}
import auditing.AuditJobProcessor
import com.typesafe.config.{Config, ConfigFactory}
import config.AppConfig
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{times, verify, when}
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import play.api.inject.guice.GuiceApplicationBuilder
import utils.BaseSpec

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.{FiniteDuration, _}

class AuditJobProcessorWorkerSpec extends TestKit(ActorSystem("AuditJobProcessorSpec"))
  with BaseSpec {

  private def createTestConfig(enabled: Boolean, fallback: Configuration) = {
    val contents = s"workers.job-processor.enabled = $enabled"
    val config: Config = ConfigFactory.parseString(contents)
    new AppConfig(Configuration(config).withFallback(fallback))
  }

  private object TestJob extends Cancellable {
    def cancel(): Boolean = false
    def isCancelled: Boolean = false
  }

  override def applicationBuilder(): GuiceApplicationBuilder = {
    super.applicationBuilder()
      .configure(
        Seq(
          "workers.job-processor.interval-in-seconds" -> 15
        ): _*
      )
  }

  private object TestAuditJobProcessor extends Actor {
    override def receive: Receive = {
      case _ =>
    }
  }

  "auditJobWorker" should {
    "schedule the job processor actor" in {
      val actorSystem = mock[ActorSystem]
      val scheduler = mock[Scheduler]
      when(actorSystem.scheduler).thenReturn(scheduler)

      when(scheduler.scheduleAtFixedRate(
        any[FiniteDuration],
        any[FiniteDuration],
        any[ActorRef],
        any[Any])(any[ExecutionContext], any[ActorRef])).thenReturn(TestJob)

      val configuration = injector.instanceOf[Configuration]

      val applicationLifecycle = mock[ApplicationLifecycle]
      val appConfig = createTestConfig(enabled = true, configuration)

      implicit val ec: ExecutionContext = injector.instanceOf[ExecutionContext]

      val auditJobProcessor = TestActorRef(TestAuditJobProcessor)
      new AuditJobWorkerImpl(auditJobProcessor, actorSystem, appConfig, applicationLifecycle)

      verify(scheduler).scheduleAtFixedRate(
        meq(0.seconds),
        meq(15.seconds),
        meq(auditJobProcessor),
        meq(AuditJobProcessor.ProcessNext))(any(), any())

      verify(applicationLifecycle).addStopHook(any[() => Future[_]])
    }

    "not schedule the job processor actor if workers.job-processor.enabled is false" in {
      val actorSystem = mock[ActorSystem]

      val configuration = injector.instanceOf[Configuration]

      val applicationLifecycle = mock[ApplicationLifecycle]
      val appConfig = createTestConfig(enabled = false, configuration)

      implicit val ec: ExecutionContext = injector.instanceOf[ExecutionContext]

      val auditJobProcessor = TestActorRef(TestAuditJobProcessor)
      new AuditJobWorkerImpl(auditJobProcessor, actorSystem, appConfig, applicationLifecycle)

      verify(actorSystem, times(0)).scheduler
      verify(applicationLifecycle, times(0)).addStopHook(any[() => Future[_]])
    }

  }
}
