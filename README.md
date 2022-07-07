# SID MOCK

Java application to act as local replacement for [SID DEMO](https://github.com/SK-EID/smart-id-documentation/wiki/Environment-technical-parameters#demo-parameters).
Built Docker images are pushed to [Docker Hub](https://hub.docker.com/repository/docker/nortal/sid-mock)

## Mocked endpoints

* POST /smart-id-rp/v2/authentication/etsi/{identifier}
* GET /smart-id-rp/v2/session/{sessionId}


## Usage
1. Download repo
2. Build image
```
# building local SID mock image
docker build -t local:sid-mock .
```
3. Run image
```
# running SID mock
docker compose up 
# or
docker compose up sid-mock-service 
```
**Note:** docker-compose is using "local:sid-mock" as image name


## MockData (Users)
Predefined users list should mostly match [SID DEMO test accounts list](https://github.com/SK-EID/smart-id-documentation/wiki/Environment-technical-parameters#accounts) 
with the following exceptions:

Missing users:
* PNOLT-49912318881
* PNOLV-311299-18886

Additional users:
* PNOBB-30303039903
* PNOIS-30303039903

### Configuration
It is possible to edit or override predefined users list and their responses.

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
#### Continuous building during development

**Precondition:** Redis is running on default port (6379).
```
./gradlew run -t
```


## Improvements 
Listed under [GitHub issues](https://github.com/Test-Government/SID-mock/labels/enhancement)
