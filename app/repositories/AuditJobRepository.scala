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

package repositories

import java.time.LocalDateTime

import javax.inject.Inject
import javax.inject.Singleton
import models.AuditJob
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes}
import org.mongodb.scala.result.{DeleteResult, InsertOneResult}
import org.mongodb.scala.{ObservableFuture, SingleObservableFuture}
import play.api.libs.json._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuditJobRepository @Inject()(mongo: MongoComponent)(implicit ec: ExecutionContext
) extends PlayMongoRepository[AuditJob] (
  mongoComponent = mongo,
  collectionName = "jobs",
  domainFormat   = AuditJob.format,
  indexes        = Seq(IndexModel(Indexes.ascending("submissionDate"), IndexOptions().name("submissionDateIdx").unique(true)))
) {

  def addJob(job: AuditJob): Future[InsertOneResult] = {
    collection.insertOne(job).toFuture()
  }

  def findAllJobs(): Future[Seq[AuditJob]] = {
    collection.find().toFuture()
  }

  def findJob(submissionDate: LocalDateTime): Future[Seq[AuditJob]] = {
    collection.find(submissionDateEquals(submissionDate)).toFuture()
  }

  def findNextJobToProcess(): Future[Option[AuditJob]] = {
    collection.find(inProgressEquals(false)).headOption()
  }

  def drop(): Future[Unit] = {
    collection.drop().toFuture()
  }

  def setJobInProgress(job: AuditJob, inProgress: Boolean): Future[Option[AuditJob]] = {
    collection.findOneAndUpdate(
      and(submissionDateEquals(job.submissionDate), inProgressEquals(!inProgress)),
      set("inProgress", inProgress)
    ).toFutureOption()
  }

  def deleteJob(job: AuditJob): Future[DeleteResult] = {
    collection.deleteOne(submissionDateEquals(job.submissionDate)).toFuture()
  }

  private def submissionDateEquals(submissionDate: LocalDateTime) = {
    equal("submissionDate", Json.toJson(submissionDate).as[String])
  }

  private def inProgressEquals(inProgress: Boolean) = {
    equal("inProgress", inProgress)
  }
}
