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

import javax.inject.Inject
import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes}
import org.mongodb.scala.result.InsertOneResult
import org.mongodb.scala.{FindObservable, Observable, SingleObservable}
import org.reactivestreams.Publisher
import play.api.libs.json._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import scala.concurrent.{ExecutionContext, Future}

case class AuditJob(
                     startDate: LocalDateTime,
                     endDate: LocalDateTime,
                     submissionDate: LocalDateTime,
                     inProgress: Boolean = false)

object AuditJob {
  implicit val format: Format[AuditJob] = Json.format[AuditJob]

//  val mongoFormat: Format[AuditJob] = new Format[AuditJob] {
//    override def writes(job: AuditJob): JsValue = Json.obj(
//      "startDate" ->  Json.toJson(job.startDate),
//      "endDate" ->  Json.toJson(job.endDate),
//      "id" -> s"${job.startDate}-${job.endDate}"
//    )
//
//    override def reads(json: JsValue): JsResult[AuditJob] = {
//      JsSuccess(
//        AuditJob(
//          (json \ "startDate").as[LocalDateTime],
//          (json \ "endDate").as[LocalDateTime],
//          (json \ "submissionDate").as[LocalDateTime]
//        )
//      )
//    }
//  }
}

class AuditJobRepository @Inject()(mongo: MongoComponent)(implicit ec: ExecutionContext
) extends PlayMongoRepository[AuditJob](
  mongoComponent = mongo,
  collectionName = "jobs",
  domainFormat   = AuditJob.format,
  indexes        = Seq(IndexModel(Indexes.ascending("submissionDate"), IndexOptions().name("submissionDateIdx").unique(true)))
) {

  def add(job: AuditJob): Future[InsertOneResult] = {
    collection.insertOne(job).toFuture()
  }

  def find(): Future[Seq[AuditJob]] = {
    collection.find().toFuture()
  }

  def drop(): Future[Void] = {
    collection.drop().toFuture()
  }

}
