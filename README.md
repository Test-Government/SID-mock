# SID MOCK
[![Docker Image CI](https://github.com/Test-Government/SID-mock/actions/workflows/docker-image.yml/badge.svg)](https://github.com/Test-Government/SID-mock/actions/workflows/docker-image.yml) [![Run tests](https://github.com/Test-Government/SID-mock/actions/workflows/run-tests.yml/badge.svg)](https://github.com/Test-Government/SID-mock/actions/workflows/run-tests.yml)

Java application to act as local replacement for [SID DEMO](https://github.com/SK-EID/smart-id-documentation/wiki/Environment-technical-parameters#demo-parameters).
Built Docker images are pushed to [Docker Hub](https://hub.docker.com/repository/docker/nortal/sid-mock)

## Mocked endpoints

* **Certificate choice session**
  * POST /smart-id-rp/v2/certificatechoice/etsi/{identifier}
  * POST /smart-id-rp/v2/certificatechoice/document/{documentnumber}
* **Authentication session**
  * POST /smart-id-rp/v2/authentication/etsi/{identifier}
  * POST /smart-id-rp/v2/authentication/document/{documentnumber}
* **Signing session**
  * POST /smart-id-rp/v2/signature/etsi/{identifier}
  * POST /smart-id-rp/v2/signature/document/{documentnumber}
* **Session status**
  * GET /smart-id-rp/v2/session/{sessionId}

## Usage
### Requirements
* Docker
* Docker Compose

### Using image from Docker Hub
```
# running SID mock
docker compose up 
# or
docker compose up sid-mock-service 
```

## MockData (Users)
Predefined accounts list should mostly match [SID DEMO test accounts list](https://github.com/SK-EID/smart-id-documentation/wiki/Environment-technical-parameters#accounts).

As of 2024.01.18 the accounts list matches [SID DEMO test accounts @2024.01.18](https://github.com/SK-EID/smart-id-documentation/wiki/Environment-technical-parameters/55263761c9db0845e499fb776a7de1c9181b75ae)
with the following exceptions:

* Additional accounts:
  * Custom (have never existed in SID DEMO):
    * PNOBB-0303039903 (follows Barbados NRN format YYMMDDRRRR)
    * PNOIS-30303039903 (does not follow Icelandic identification number format)
  * Legacy (no longer exist in SID DEMO) 
    * PNOEE-39912319997 
    * PNOLT-39912319997 
    * PNOLV-010404-29990 
    * PNOLV-329999-99805
* Data mismatch:
  * Unlike DEMO, mocks signing certificates don't include users birthdate

All certificates for 'OK' response have been created with document number suffix "MOCK-Q".

### OCSP
In order to support [OCSP](https://en.wikipedia.org/wiki/Online_Certificate_Status_Protocol) validation
every new/updated user certificate needs to be uploaded to: https://demo.sk.ee/upload_cert/index.php

**Note:** only applies for user certificates with OCSP URL pointing at SK demo.

### Configuration

```yml
sid-mock:
  delay: 1s
  expiration: 5m
  store-session-init-requests: false
  override-document-number: false
```
| configuration key                    | description                                                                                                                                                                                                                                                                                                                                                                                                                             |
|--------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| sid-mock.delay                       | Minimum how long mock will wait before completing session.                                                                                                                                                                                                                                                                                                                                                                              |
| sid-mock.expiration                  | How long mock will retain a session and its generated response.                                                                                                                                                                                                                                                                                                                                                                         |
| sid-mock.store-session-init-requests | Whether mock will store session init request info (how long is defined by expiration key)                                                                                                                                                                                                                                                                                                                                               |
| sid-mock.override-document-number    | Applies to /documentnumber/ endpoints.<br/> If **true**, mock uses provided document number (with valid format) in response body. **Note! this will not change the document number in certificate and therefore creates a mismatch.**<br/> If **false**, mock permits only document numbers ending in "**MOCK-Q**" (matches embedded certificates).<br/> For other endpoints mock always returns document number with "MOCK-Q" suffix.  |


In addition, it is possible to edit or override predefined users list and their responses.

For this, there are the following endpoints:

#### /users/{identifier}
| Method | Action                                    |
|--------|-------------------------------------------|
| GET    | Get user.                                 |
| PUT    | Add new user. 500 if user already exists. |
| POST   | Edit **existing** user.                   |
| DELETE | Delete **existing** user.                 |

#### /users
| Method | Action                                          |
|--------|-------------------------------------------------|
| GET    | Get all users.                                  |
| PUT    | Add multiple new users. 500 if any duplicates.  |
| POST   | Edit multiple users. Add if user doesn't exist. |
| DELETE | Delete multiple users.                          |

#### /users/reset
| Method | Action                          |
|--------|---------------------------------|
| GET    | Reset users to predefined list. |


## Development
### Requirements
* Java  17
* Redis is running on default port (6379).
    ```
    docker run -p 6379:6379 redis 
    ```
### Continuous building during development
```
./gradlew run -t
```

### Build Docker image locally
1. Download repo
2. Build image
```
# building local SID mock image
docker build -t nortal/sid-mock:latest .
```
3. Run image
```
# running SID mock
docker compose up 
# or
docker compose up sid-mock-service 
```
**Note:** docker-compose is using "nortal/sid-mock:latest" as image name

## Improvements 
Listed under [GitHub issues](https://github.com/Test-Government/SID-mock/labels/enhancement)
