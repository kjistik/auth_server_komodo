# Spring Boot Authentication Server

The present Spring Boot application is meant to act as an independent authentication component as part of a larger microservices application. It's purpose is to manage users, their roles and issue authorization tokens to be used by other services. 

## Table of Contents
- [Features](#features)
- [Pending Features](#pending-features)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
    - [With Docker](#with-docker)
    - [Without Docker](#without-docker)
  - [Running the Application](#running-the-application)
    - [Running with Docker](#running-with-docker)
    - [Running without Docker](#running-without-docker)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [License](#license)
- [Author](#author)
- [Credits](#credits)

## Features
- User authentication and authorization.
- JWT (JSON Web Token) support for secure communication.
- Role-based access control (RBAC).
- Email verification for new users.
- Integration with Mailgun for email services.

## Pending Features

- Logout mechanism.
- User deactivation.
- Refresh tokens for added security and ease of use.

## Getting Started

### Prerequisites
- A Mailgun account 
- Docker and the docker-compose plugin are highly recommended but not strictly needed. (If present, these are the only requirements)
- Java 21 or higher
- Maven 3.x or higher
- PostgreSQL

### Installation 

## With Docker

1. Clone the repository:
   ```bash
   git clone https://github.com/kjistik/auth_server_komodo.git
    ```

2. Create `credentials.json` file on the /setup folder (see [Configuration](#configuration) for details).

4. Run the `start` file corresponding to your OS from the /setup folder.

## Without Docker
1. Clone the repository:
   ```bash
   git clone https://github.com/kjistik/auth_server_komodo.git
    ```

2. Provide a PostgreSQL database and run the `/setup/setup.sql` script in it.

3. Create `credentials.json` file on the /setup folder (see [Configuration](#configuration) for details).

4. Run the `loadEnv` file corresponding to your OS in a shell.

5. Compile the application with maven.
   ```bash
   mvn package
    ```
### Running the Application

## Running with Docker

Run the `start` file corresponding to your OS from the /setup folder.

## Running without Docker
 For every execution, always run the `loadEnv` file and then the compiled .jar executable in the same shell  session. 

## Configuration

  The `credentials.json` file **MUST** contain the following environment variables:

  | Variable Name                     | Description                                                                 |
  |-----------------------------------|-----------------------------------------------------------------------------|
  | `KOMODO_DATASOURCE_URL`           | The database URL.                                                          |
  | `KOMODO_DOMAIN_URL`               | The domain in which the application will run (can be a domain name or IP). |
  | `KOMODO_DATASOURCE_NAME`          | The name of the database. Must match the one specified in `KOMODO_DATASOURCE_URL`  |
  | `KOMODO_DATASOURCE_USER`          | The database user.                                                         |
  | `KOMODO_DATASOURCE_PASSWORD`      | The database user password.                                                |
  | `KOMODO_MAILGUN_KEY`              | Your secret Mailgun key.                                                   |
  | `KOMODO_MAILGUN_DOMAIN`           | Your Mailgun domain.                                                       |
  | `KOMODO_JWT_SECRET_KEY`           | Your JWT secret key.                                                       |
  | `KOMODO_JWT_EXPIRATION_TIME`      | The time (in milliseconds) that JWT tokens should be valid before expiring.|
  | `KOMODO_REFRESH_TOKEN_EXPIRATION_TIME`| The time (in miliseconds) that refresh tokens should be valid for      |
  | `KOMODO_JWT_EMAIL_VERIFICATION_TIME` | The time the email verification link should be valid for.               |
  | `KOMODO_ADMIN_PASSWORD`           | The password of the OWNER user.                                            |
  | `KOMODO_ADMIN_EMAIL`              | The email of the OWNER user. It will always be assumed to exist.           |
  | `KOMODO_ADMIN_GIVENNAME`          | The given name of the OWNER user.                                          |
  | `KOMODO_ADMIN_LASTNAME`           | The last name of the OWNER user.                                           |

## API Documentation
  
  There is a Postman collection and an HTML file documenting the API and possible exceptions on the /docs directory.

## License

  This application is licensed under the MIT licence. See the [LICENSE](LICENSE) for details.

## Author
- **Kjistik**  
  GitHub: [Kjistik](https://github.com/kjistik)  
  Email: [coronelmartinernesto@gmail.com](mailto:coronelmartinernesto@gmail.com)

## Credits
- The `wait-for-it.sh` script is used from [vishnubob/wait-for-it](https://github.com/vishnubob/wait-for-it), licensed under the MIT License.