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

package config

import auditing.{AuditJobProcessor, NuanceScheduler}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import workers.{AuditJobProcessorWorker, AuditJobProcessorWorkerImpl, NuanceSchedulerWorker, NuanceSchedulerWorkerImpl}


class DepAuditModule extends AbstractModule with AkkaGuiceSupport {

  override protected def configure(): Unit = {
    bindActor[AuditJobProcessor]("audit-job-processor")
    bindActor[NuanceScheduler]("nuance-scheduler")
    bind(classOf[AuditJobProcessorWorker]).to(classOf[AuditJobProcessorWorkerImpl]).asEagerSingleton()
    bind(classOf[NuanceSchedulerWorker]).to(classOf[NuanceSchedulerWorkerImpl]).asEagerSingleton()
  }
}
