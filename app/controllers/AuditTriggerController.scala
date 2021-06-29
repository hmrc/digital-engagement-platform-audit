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
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class AuditTriggerController @Inject()(historicAuditing: HistoricAuditing, cc: ControllerComponents)
                                      (implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def trigger(startDateParam: String, endDateParam: String): Action[AnyContent] = Action.async { _ =>
    val startDate = LocalDateTime.parse(startDateParam)
    val endDate = LocalDateTime.parse(endDateParam)

    historicAuditing.auditDateRange(startDate, endDate).map {
      results =>
        Ok(s"TBH - Trigger for service: from $startDateParam to $endDateParam got results: $results")
    }
  }
}