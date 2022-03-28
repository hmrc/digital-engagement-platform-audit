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

package services

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar

class GoodDecrypter extends Decrypter {
  override def decrypt(fieldName: String, encryptedValue: String): Option[String] = Some("HashValue-SuccessfulResult")

  override def verifyHash(plainText: String, hashedValue: String): Boolean = true
}

class UnencryptedFieldSpec extends AnyWordSpec with Matchers with MockitoSugar {
  "UnencryptedField" must {
    "successfully decrypt valid values" in {
      implicit val decrypter: Decrypter = mock[Decrypter]
      when(decrypter.decrypt(any(), any())).thenReturn(Some("HashValue-SuccessfulResult"))
      when(decrypter.verifyHash(any(), any())).thenReturn(true)

      UnencryptedField("FieldName", "ENCRYPTED-Value") mustBe "SuccessfulResult"
    }
    "not decrypt values with bad hash" in {
      implicit val decrypter: Decrypter = mock[Decrypter]
      when(decrypter.decrypt(any(), any())).thenReturn(Some("HashValue-SuccessfulResult"))
      when(decrypter.verifyHash(any(), any())).thenReturn(false)

      UnencryptedField("FieldName", "ENCRYPTED-Value") mustBe UnencryptedField.UNDECRYPTABLE_FIELD
    }
    "not decrypt values that don't start with proper prefix" in {
      implicit val decrypter: Decrypter = mock[Decrypter]

      UnencryptedField("FieldName", "Value") mustBe UnencryptedField.UNDECRYPTABLE_FIELD
    }
  }
}
