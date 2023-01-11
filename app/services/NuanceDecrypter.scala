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

package services

import javax.inject.Inject
import play.api.{Configuration, Logging}
import uk.gov.hmrc.crypto.{Crypted, PlainText, Scrambled, Sha512Crypto, SymmetricCryptoFactory}

import scala.util.{Failure, Success, Try}

class NuanceDecrypter @Inject()(
                         config: Configuration
                         ) extends Decrypter with Logging {

  lazy val hashingKey: String = config.get[String]("request-body-encryption.hashing-key")

  lazy val crypto = SymmetricCryptoFactory.aesGcmCryptoFromConfig("request-body-encryption", config.underlying)

  lazy val hasher: Sha512Crypto = new Sha512Crypto(hashingKey)

  def decrypt(fieldName: String, encryptedValue: String): Option[String] = Try {
    crypto.decrypt(Crypted(encryptedValue)).value
  } match {
    case Success(decryptedValue) => Some(decryptedValue)
    case Failure(exception) =>
      logger.warn(s"[decrypt] failed to decrypt value for field $fieldName with exception: ${exception.getMessage}")
      None
  }

  def verifyHash(plainText: String, hashedValue: String): Boolean = hasher.verify(PlainText(plainText), Scrambled(hashedValue))
}
