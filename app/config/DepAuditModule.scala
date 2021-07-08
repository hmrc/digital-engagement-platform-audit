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

import auditing.AuditJobProcessorImpl
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import workers.{AuditJobWorker, AuditJobWorkerImpl}


class DepAuditModule extends AbstractModule with AkkaGuiceSupport {

  override protected def configure(): Unit = {
    bindActor[AuditJobProcessorImpl]("audit-job-processor")
    bind(classOf[AuditJobWorker]).to(classOf[AuditJobWorkerImpl]).asEagerSingleton()
  }
}
