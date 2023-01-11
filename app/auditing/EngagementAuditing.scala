/*
 * Copyright 2023 HM Revenue & Customs
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

package auditing

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

import javax.inject.Inject
import mappers.EngagementMapper
import play.api.libs.json.{JsArray, JsValue}
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import play.api.Logging

class EngagementAuditing @Inject()(engagementMapper: EngagementMapper, auditConnector: AuditConnector)
                                  (implicit ec: ExecutionContext) extends Logging {
  def processEngagement(engagement: JsValue): Future[Seq[AuditResult]] = {
    Future.sequence {
      engagementMapper.mapEngagement(engagement).map { event: ExtendedDataEvent =>
        logger.info(s"[eventData]: sending extended event ${event.auditType}")
        val result = Await.result(auditConnector.sendExtendedEvent(event), Duration.Inf)
        Future.successful(result)
      }
    }
  }

  def processEngagements(engagements: JsArray): Future[Seq[Seq[AuditResult]]] = {
    Future.sequence {
      engagements.as[Seq[JsValue]].map(processEngagement)
    }
  }
}
