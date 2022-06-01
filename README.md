# Digital Engagement Platform Audit

## Overview

The DEP Audit service schedules the capture of transcript and session details initially passed to 
Nuance when Chat/Digital Assistant launched.

This service also offers a trigger endpoint which provides the mechanism to call historical data
in the event a problem occurs with the scheduled data. This endpoint takes a start and end date as parameters.
This can only be done in preproduction environments and is used for testing purposes only.
For example, you can make a call to the trigger endpoint without waiting every 2 hours. 
This can be done using the curl-microservice in jenkins orchestrator job.

This service makes two calls to Nuance Historical API. 
The first call gets the number of engagements within a two hour period and saves that data along with the start and end time in MongoDB.
The second call gets a maximum number of 800 engagement chunks and processes them.

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
Every 2 hours a request is sent to Nuance Start time = now minus 7 hours / End time now - 5 hour to get the number of records.
This data is out in a mongoDB. If there are no engagements returned the number will be 0.
The Audit service queries then splits the number of engagements into 800 chunks. 
Every 15 seconds a worker queries the database for any chunks that have not been processes and processes them.
There is only one instance so no duplicate audit events will be created. The timings can be configered in app-configs.

### On the discovery page in Kibana ther are key works you can search for.

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

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
