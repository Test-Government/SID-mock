# SID MOCK

Java application to act as local replacement for [SID DEMO](https://github.com/SK-EID/smart-id-documentation/wiki/Environment-technical-parameters#demo-parameters)

## Mocked endpoints

* POST /smart-id-rp/v2/authentication/etsi/{identifier}
* GET /smart-id-rp/v2/session/{sessionId}

## Usage


## Users
Predefined users list should match [SID DEMO test accounts list](https://github.com/SK-EID/smart-id-documentation/wiki/Environment-technical-parameters#accounts)

**NOTE!** Currently all "OK" users are replaced with PNOLV-010404-29982

### Configuration
It is possible to edit or override predefined users list and their responses.

For this, there are the following endpoints:

#### /user/{identifier}
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

## TODO list:
* Logging
* Add tests
* Updating/overriding certificates without the need to rebuild application
* Option to overload default users.json via file on application start
* Private identifier endpoint support (in addition to ETSI)
* Documentnumber endpoint support (in addition to ETSI)
* Signing support (in addition to authentication)
* Relying Party restriction capability
* On TIMEOUT use actual(configurable) timeout time instead of common delay