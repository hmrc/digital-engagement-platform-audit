# Digital Engagement Platform Audit

## Overview

This service periodically calls the Nuance Reporting API to retrieve customer engagement data, map the data to the required data structure and audit (CIP). The calls to the Nuance Reporting API are scheduled every 2 hours and retrieve data from 5 hours ago and 2 hours worth of data. The reason for going back 5 hours is to allow enough time for the real-time data to be populated on the Nuance side.

Two calls are made to Nuance. The first being the authentication call to retrieve an OAuth2 access token. If this is successful, a call to obtain engagement data is made.

There are 2 Pekko Actors in the service:

1) Nuance scheduler - Creates a scheduled job which adds a row in MongoDB with a startTime and endTime. These dates are calculated using the current datetime and app config values.
2) Jobs processor - Creates a scheduled job which processes the next row in MongoDB at a fixed rate (currently set at every 15 seconds and is configurable through app config).

The sequence of events is as follows:

1) job created in database with startTime & endTime
2) job read from database
3) call made to Nuance to get access token
4) call made to Nuance Reporting API using start and end times obtained from the job
5) data retrieved from Nuance for the period of time specified in the previous API call
6) data is mapped
7) data is audited (CIP)

The data retrieved from Nuance's API can be truncated into chunks. This helps with controlling the amount of processing the audit service needs to do. The chunk size is configurable in app config files and sets the number of engagements to be retrieved. By default, this is set to 800.

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
you will need to `sm --stop DIGITAL_ENGAGEMENT_PLATFORM_AUDIT` prior to running from source.

## Manually trigger using Postman locally
This service also offers a trigger endpoint which provides the mechanism to manually add a job to MongoDB (step 1 in flow above). This endpoint takes a start and end date as parameters. This can only be done in preproduction environments and is used for testing purposes only. For example, you can make a call to the trigger endpoint without waiting every 2 hours. This can be done using the curl-microservice in Jenkins orchestrator job.

`GET http://localhost:9190/digital-engagement-platform-audit/trigger?startDate=2021-02-20T00:00:00&endDate=2021-02-20T12:00:00`

NOTE: The digital-engagement-platform-nuance-api-stub service provides stub data for the Nuance Reporting API call. **This stub service should ALWAYS be used in all pre-prod environments. Calls to the real Nuance Reporting API should never occur in pre-prod environments as this will retrieve real people's data into pre-prod CIP environments**.

Once the job has been processed it will be deleted from Mongo.

## Technical information
The service uses MongoDB to store a collection of jobs.

Example MongoDB job:
```
{
    "_id" : ObjectId("61094e1756815b2d1e71d4fc"),
    "startDate" : "2021-08-03T10:09:27.599",
    "endDate" : "2021-08-03T12:09:27.599",
    "submissionDate" : "2021-08-03T15:09:27.599",
    "inProgress" : true
}
```

## Testing Notes

* When running locally, you must have MongoDB installed and running for all tests to pass successfully.  

* If testing local changes to the service, you must be on the HMRC VPN to receive responses from the Nuance API.

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
