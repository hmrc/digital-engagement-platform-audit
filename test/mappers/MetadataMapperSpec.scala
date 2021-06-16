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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsPath, JsResult, JsString, JsSuccess, JsValue, Json}

class MetadataMapper {
  private val engagementIDPick = (JsPath() \ 'engagementID).json.pick
  def mapEngagement(engagement: JsValue): JsSuccess[JsValue] = {
    engagement.transform(engagementIDPick) match {
      case JsSuccess(JsString(engagementId), _) =>
        JsSuccess(Json.obj(
          "auditSource" -> "digital-engagement-platform",
          "auditType" -> "EngagementMetadata",
          "eventId" -> s"Metadata-${engagementId}"
        ))
    }
  }
}

class MetadataMapperSpec extends AnyWordSpec with Matchers {
  "something" should {
    "work with no engagements" in {
      val input =
        """
          |{
          | "engagementID": "187286680131967188",
          | "siteID": "10006719",
          | "saleQualified": false,
          | "businessRuleID": "900020",
          | "businessRuleName": "HMRC-LC-STD-SIT-STD-xtestx-C2C",
          | "pages": {
          |     "launchPageID": "-1",
          |     "launchPageMarker": "unrecognized_page",
          |     "launchPageURL": "https://www.tax.service.gov.uk/account-recovery/lost-user-id-password/check-emails?ui_locales=en&nuance=2008HMRCSITTest"
          | },
          | "engagementInitialAgentResponseTime": 0,
          | "initialTimeInQueue": 0,
          | "conferenceTime": 0,
          | "engagementMaxAgentResponseTime": 0,
          | "engagementAvgAgentResponseTime": 0,
          | "transferred": false,
          | "deviceType": "Standard",
          | "operatingSystem": "Windows",
          | "browserType": "CHROME",
          | "browserVersion": "88.0.4324.150",
          | "agents": [],
          | "businessUnits": [
          |     {
          |         "businessUnitID": "19001214",
          |         "businessUnitName": "HMRC-Training"
          |     }
          | ],
          | "agentGroups": [
          |     {
          |         "agentGroupID": "10006721",
          |         "agentGroupName": "HMRC-Training"
          |     }
          | ],
          | "engagementDuration": 6000,
          | "businessRuleAttribute": {},
          | "visitorAttribute": {
          |     "mdtpSessionId": [
          |         "ENCRYPTED-UU1USedMqML7Yj3XulYIHtNkOGpmoQzXx4X20+H3OfDeoIzzVoGbsVKY1rC8Z5LqUj2YtjkwaK9qFmxgACHK4u8TrGXi8hiKjo2X8rRBoT7YflRD9pJ25E9lEBT/ih8kA5NxReUSTABOhf+fkBPioYNTW1wOM4jBFg=="
          |     ],
          |     "mdtpdfSessionId": [
          |         "R0ruX1yE9Hz6gdw87YMFNnd5GbynBgVoEj3WOwehg54WrKGEryDV4OZRjaDBgy5P7Ooj5MTcq3NlkZwVuF0drAJJ"
          |     ],
          |     "clientIp": [
          |         "81.97.99.4"
          |     ],
          |     "deviceId": [
          |         "ENCRYPTED-7a0O8KdpAtAKQIbfo62FvLkdnvSTcYpWo++IvpAzx88DEzFJYGHRriq+w/bAwCv3wXQTZIyMtkvUrxz9pEQeflMi6gvenmBDQX8+Yl8jmVu3o48Pdbt4BzGKSE6/KMnwMsnVT/d7+qESnWbqHshXzMMvqMY+UrQMdQ=="
          |     ]
          | },
          | "endDate": {
          |     "iso": "2021-03-02T13:23:44+00:00",
          |     "timestamp": 1614691424868
          | },
          | "lastUpdateDate": {
          |     "iso": "2021-03-02T14:18:20+00:00",
          |     "timestamp": 1614694700682
          | },
          | "startDate": {
          |     "iso": "2021-03-02T13:23:38+00:00",
          |     "timestamp": 1614691418610
          | },
          | "customerID": "189819954921823295",
          | "language": "en",
          | "launchType": "C2C",
          | "cobrowse": false,
          | "escalated": false,
          | "wrapUpTime": 0,
          | "timeFromCustomerToAgentExit": 0,
          | "sessionID": "1898199549218232951",
          | "automatons": [
          |     {
          |         "automatonID": "2008",
          |         "automatonName": "HMRC_PreChat_CSG",
          |         "automatonType": "guide",
          |         "startedIn": "chat",
          |         "startedBy": "br,900020",
          |         "pageID": "unrecognized_page",
          |         "businessUnit": "HMRC-Training",
          |         "agentGroup": "HMRC-Training",
          |         "outcomeType": "NotCompleted",
          |         "outcomeMessage": "unknown_outcome",
          |         "launchDate": "2021-03-02 13:23:38",
          |         "launchDateMlSec": 1614691418611,
          |         "automatonAttribute": {
          |             "acif_version": "release-5.36.0-2 -- f91013f",
          |             "name": "HMRC_PreChat_CSG",
          |             "automaton_id": "2008"
          |         },
          |         "automatonTypes": [
          |             "guide"
          |         ],
          |         "nodes": [
          |             {
          |                 "automatonType": "guide",
          |                 "nodeID": "2024",
          |                 "nodeName": "HMRC_PreChat_CSG - Initial",
          |                 "outcomeType": "NotCompleted",
          |                 "nodeAttribute": {
          |                     "node_type": "survey",
          |                     "node_name": "HMRC_PreChat_CSG - Initial",
          |                     "next_node_id": "2038",
          |                     "next_node_name": "HMRC_PreChat - Busy",
          |                     "node_id": "2024"
          |                 },
          |                 "elementResponses": [
          |                     {
          |                         "elementID": "name",
          |                         "elementValue": "Name",
          |                         "responseNumericValue": 1.0,
          |                         "responseStringValue": "Carlos"
          |                     },
          |                     {
          |                         "elementID": "chat-reason",
          |                         "elementValue": "Your question",
          |                         "responseNumericValue": 1.0,
          |                         "responseStringValue": "Test chat"
          |                     }
          |                 ],
          |                 "nodeEnteredTime": {
          |                     "timestamp": 1614691424868,
          |                     "timeInClientTimezone": "2021-03-02 13:23:44"
          |                 }
          |             }
          |         ]
          |     }
          | ],
          | "transferAbandoned": false,
          | "conferenceConnected": false,
          | "callConnected": false,
          | "converted": false,
          | "transfersConnected": 0,
          | "callDuration": 0,
          | "totalActiveAgentTime": 0,
          | "totalActiveCallTime": 0,
          | "totalActiveCustomerTime": 0,
          | "totalAgentFreehandLines": 0,
          | "totalAgentScriptLines": 0,
          | "totalAgentLines": 0,
          | "totalCustomerLines": 0,
          | "totalEngagementLines": 0,
          | "totalAgentsInvolved": 0,
          | "totalCallsConnected": 0,
          | "totalFailedCalls": 0,
          | "totalConversions": 0,
          | "totalOrderValue": 0.0,
          | "engagementProductType": [
          |     "guide"
          | ],
          | "conversions": [],
          | "dispositions": [],
          | "escalationNotes": "[]",
          | "transcript": [],
          | "truncatedDueToEventLimit": false,
          | "initialEngagementInConversation": false,
          | "endType": "Customer Not Completed Automaton"
          |}
          |""".stripMargin

      val jsInput = Json.parse(input)
      val mapper = new MetadataMapper
      val result = mapper.mapEngagement(jsInput)
      result.isSuccess mustBe true
      result.get mustBe Json.obj(
        "auditSource" -> "digital-engagement-platform",
        "auditType" -> "EngagementMetadata",
        "eventId" -> "Metadata-187286680131967188"
      )
    }
  }
}