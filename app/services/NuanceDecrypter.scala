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

package services

import javax.inject.Inject
import play.api.{Configuration, Logger}
import uk.gov.hmrc.crypto.{Crypted, CryptoGCMWithKeysFromConfig, PlainText, Scrambled, Sha512Crypto}

import scala.util.{Failure, Success, Try}

class NuanceDecrypter @Inject()(
                         config: Configuration
                         ) extends Decrypter {

  val logger: Logger = Logger(this.getClass.getSimpleName)
  lazy val hashingKey: String = config.get[String]("request-body-encryption.hashing-key")

  lazy val crypto: CryptoGCMWithKeysFromConfig = new CryptoGCMWithKeysFromConfig("request-body-encryption", config.underlying)

  lazy val hasher: Sha512Crypto = new Sha512Crypto(hashingKey)

  def decrypt(fieldName: String, encryptedValue: String): Option[String] = Try {
    crypto.decrypt(Crypted(encryptedValue)).value
  } match {
    case Success(decryptedValue) => Some(decryptedValue)
    case Failure(exception: SecurityException) =>
      logger.warn(s"[decrypt] failed to decrypt value for field $fieldName: ${exception.getMessage}")
      None
    case Failure(exception) =>
      logger.warn(s"[decrypt] failed to decrypt value for field $fieldName with unexpected exception: ${exception.getMessage}")
      None
  }

  def verifyHash(plainText: String, hashedValue: String): Boolean = hasher.verify(PlainText(plainText), Scrambled(hashedValue))
}
