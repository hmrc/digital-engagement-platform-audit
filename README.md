# Digital Engagement Platform Audit

## Overview

The DEP Audit service schedules the capture of transcript and session details initially passed to 
Nuance when Chat/Digital Assistant launched.

This service also offers a trigger endpoint which provides the mechanism to call historical data
in the event a problem occurs with the scheduled data. This endpoint takes a start and end date as parameters.

## Running through service manager

*You need to be on the VPN*

Ensure your service manager config is up to date, and run the following command:

`sm --start DIGITAL_ENGAGEMENT_PLATFORM_AUDIT_ALL -r`

This will start all the required services

## Running from source
Clone the repository using SSH:

`git@github.com:hmrc/digital-engagement-platform-audit.git`

Run the code from source using

`sbt run`

Dependencies will also need to be started from source or using service manager. If you started all the required services using DIGITAL_ENGAGEMENT_PLATFORM_AUDIT_ALL, 
you will need to 'sm --stop DIGITAL_ENGAGEMENT_PLATFORM_AUDIT' prior to running from source.

## Manually trigger using Postman locally

`GET http://localhost:9190/digital-engagement-platform-audit/trigger?startDate=2021-02-20T00:00:00&endDate=2021-02-20T12:00:00`

When running locally, the engagements will be taken from digital-engagement-platform-nuance-api-stub.

This will create a job in Mongo with the given start date and end date. Once the job has been processed it will be deleted.

## Technical information
The service uses MongoDB to store a collection of jobs.

## kibana logs and how to see audit service is running in production
###How the audit service workers
Every 2 hours a request is sent to Nuance Start time = now minus 3 hours / End time now - 1 hour to get the number of records.
This data is out in a mongoDB. If there are no engagements returned the number will be 0.
The Audit service queries then splits the number of engagements into 800 chunks. 
Every 15 seconds a worker queries the database for any chunks that have not been processes and processes them.
There is only one instance so no duplicate audit events will be created. The timings can be configered in app-configs.

### On the discovery page in Kibana there are key words you can search for.

### processAll 
Will give you the requests to the proxy that goes out to the Nuance Historic API service.
###auditDateRange
Will give the number of records found. This is requested every two hours.
###getHistoricData
Will give you the number of records completed in a chunk (split into 800 data sizes), 
the start time and the end time of the data chuck.

### NuanceAuthResponse
Will give you the responce from Nuance on the requests every 2 hours. 
Here is where you will be any issues for logging in of in nuance is down.

### processNext
Will give you the information on when a chunk of work has been started and completed.


##How to analise engagements from the Portal that are sent to Splunk
To compare engagements that are in the Nuance Portal to what has been sent to Splunk you
will need access to Production Nuance Portal, Kibana and Splunk.

We'll use an example of getting a full day's engagements for one Agent Group.

You need to be aware that any request from the Audit microservice to the Nuance Historic API is
7 hours behind and uses a query of start date and time. For example a request for data sent at 16:00:00
will retrieve engagements with a start time of 09:00:00 to 11:00:00.

###Getting times in Kibana
First go into Kibana to get the start date times that you'll use in the Nuance Portal.
Login and go into the Discover tab. Enter "getHistoricData" in the search criteria and put in the
time and  date you are looking for. You may have to tweak the time to get the data you require.
To get a full day's engagements take the first start time closed to midnight at the start of the day
and the closest time to midnight at the end of the day. As most webchats start at 8am and finish a 8pm
the first and last requests will have no engagements in.
This will not be the case where Virtual Assistant is involved as Virtual Assistant is open 24/7.

In this scenario we'll use 2021-12-22 07:00:00.000 to 2021-12-23 07:00:00.000 in the Kibana query to
get a full day. This will give us a startDate>="2021-12-22T01:06:05.000" and startDate<="2021-12-23T01:06:05.000".
These times we'll use in Splunk. We'll also just Agent Group HMRC-BTAC-VRS.

###Sarching for the engagements in the Nuance Portal
Go into the Production Nuance Portal.
Click on Transcript > Transcript Query Builder > RightTouch

Select
Filter based on = Engagement
Date Type = Start Date
Date Range = 12-22-2021 01:06:05 and 12-23-2021 01:06:05
Add a Condition Agent "Group Name" "is equal to" "HMRC-BTAC-VRS"
In Select fields to display section click "Select All"
And click Search

In this search we found 19 engagements.

###Searching for the engagements in Splunk
Login to Production Right Splunk and go to DEP "Audit Service Monitoring Dashboard".

Go to the "Engagement Events" section.

Select the times you got from Kibana "Data time range" from "2021-12-22T01:06:05.000" to 2021-12-23T01:06:05.000
Select the Agent Group = "HMRC-BTAC-VRS"
The result should be the same as what was in the Nuance Portal = 19


## License
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
