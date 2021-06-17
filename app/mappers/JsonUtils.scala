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

package mappers

import play.api.libs.json._

object JsonUtils {
  def deleteValue(path: JsPath): Reads[JsObject] = path.json.prune

  def putValue(path: JsPath, value: JsValue): Reads[JsObject] = {
    __.json.update(path.json.put(value))
  }

  def putString(path: JsPath, value: String): Reads[JsObject] = putValue(path, Json.toJson(value))
}
