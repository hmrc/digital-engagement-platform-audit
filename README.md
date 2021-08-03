# Digital Engagement Platform Audit

## Overview

The DEP Audit service schedules the capture of transcript and session details initially passed to 
Nuance when Chat/Digital Assistant launched.

This service also offers a trigger endpoint which provides the mechanism to call historical data
in the event a problem occurs with the scheduled data. This endpoint takes a start and end date as parameters.


## Running through service manager

*You need to be on the VPN*

Ensure your service manager config is up to date, and run the following command:

`sm --start DIGITAL_ENGAGEMENT_PLATFORM_ALL -r`

This will start all the required services

## Running from source
Clone the repository using SSH:

`git@github.com:hmrc/digital-engagement-platform-audit.git`

Run the code from source using

`sbt "run 9190"`

Dependencies will also need to be started from source or using service manager. If you started all the required services using DIGITAL_ENGAGEMENT_PLATFORM_ALL, 
you will need to 'sm --stop DIGITAL_ENGAGEMENT_PLATFORM_AUDIT' prior to running from source.

## Manually trigger using Postman locally

`GET http://localhost:9190/digital-engagement-platform-audit/trigger?startDate=2021-02-20T00:00:00&endDate=2021-02-20T12:00:00`

When running locally, the engagements will be taken from digital-engagement-platform-nuance-api-stub.

This will create a job in Mongo with the given start date and end date. Once the job has been processed it will be deleted.


## Technical information

The service uses MongoDB to store a collection of jobs.


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
