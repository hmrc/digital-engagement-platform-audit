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

import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import uk.gov.hmrc.crypto.PlainText

class DecrypterSpec extends PlaySpec with ScalaFutures {
  val defaultConfig = Configuration(
    "request-body-encryption.key" -> "j+RAVSn+P6IJvuApL121ofM7Uh85p9C0nJwNoa2tuc4=",
    "request-body-encryption.hashing-key" -> "xVcxKBRUhKqca953LJ9k8O2huZhIoBRUDQEbqYs3d6/TbHo1f7iTco7Ae+sS5W0u23v5HYkARIAnKCK72C5gTQ==",
    "request-body-encryption.previousKeys" -> Seq.empty[String],
  )

  class Setup(config: Configuration = defaultConfig) {
    val decrypter = new NuanceDecrypter(config)
  }

  "decrypt" must {
    "decrypt an encrypted string" in new Setup {
      decrypter.decrypt("fieldName", decrypter.crypto.encrypt(PlainText("some-plain-text")).value) mustBe Some("some-plain-text")
    }
    "fail to decrypt a  malformed string" in new Setup {
      decrypter.decrypt("fieldName", "HELLO-NO-ENCRYPTION") mustBe None
    }
  }

  "hasher" must {
    "validate a hashed string and a raw string that matches" in new Setup {
      decrypter.verifyHash("hello", decrypter.hasher.hash(PlainText("hello")).value) mustBe true
    }
    "not validate a hashed string and a raw string that doesnt match" in new Setup {
      decrypter.verifyHash("hello:", decrypter.hasher.hash(PlainText("hello")).value) mustBe false
    }
    "hashed value must not contain dashes (-)" in new Setup {
      decrypter.hasher.hash(PlainText("some-very-long-text/with\\char")).value.contains("-") mustBe false
    }
  }
}
