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

import play.api.Logging

object UnencryptedField extends Logging {
  val UNDECRYPTABLE_FIELD: String = "could not decrypt value"
  private val FIELD_PREFIX: String = "ENCRYPTED-"

  private def decrypt(fieldName: String, value: String)(implicit decrypter: Decrypter): Option[String] = if (value.startsWith(FIELD_PREFIX)){
    decrypter.decrypt(fieldName, value.replace(FIELD_PREFIX, "")).flatMap {
      decryptedValue =>
        decryptedValue.split("-").toList match {
          case hashedValue :: rawValue if decrypter.verifyHash(rawValue.mkString("-"), hashedValue) =>
            Some(rawValue.mkString("-"))
          case _ => logger.warn(s"[decrypt] invalid decrypted value for field $fieldName")
            None
        }
    }
  } else {
    logger.warn(s"[decrypt] dual running mode for encryption is disabled and plain text field is passed in for field $fieldName.")
    None
  }

  def apply(fieldName: String, value: String)(implicit decryptor: Decrypter): String = {
    decrypt(fieldName, value)
      .fold[String](
        UnencryptedField.UNDECRYPTABLE_FIELD)(
        decryptedField => decryptedField)
  }
}
