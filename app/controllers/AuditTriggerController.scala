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

import play.api.Logging
import javax.inject.{Inject, Singleton}
import models.AuditJob
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repositories.AuditJobRepository
import services.LocalDateTimeService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

@Singleton()
class AuditTriggerController @Inject()(jobRepository: AuditJobRepository,
                                       localDateTimeService: LocalDateTimeService,
                                       cc: ControllerComponents)
                                      (implicit ec: ExecutionContext)
    extends BackendController(cc) with Logging {

  def trigger(startDateParam: String, endDateParam: String): Action[AnyContent] = Action.async { _ =>
    val startDate = LocalDateTime.parse(startDateParam)
    val endDate = LocalDateTime.parse(endDateParam)

    jobRepository.addJob(AuditJob(startDate, endDate, localDateTimeService.now)).map {
      results =>
        logger.info(s"[ProcessJob] Nuance auditing from $startDateParam to $endDateParam got results: $results")
        Ok(s"Nuance auditing from $startDateParam to $endDateParam got results: $results")
    }
  }
}
