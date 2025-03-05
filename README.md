# Spring Boot Authentication Server

The present Spring Boot application is meant to act as an independent authentication component as part of a larger microservices application. It's purpose is to manage users, their roles and issue authorization tokens to be used by other services. 

## Table of Contents
- [Features](#features)
- [Pending Features](#pending-features)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Running the Application](#running-the-application)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [License](#license)
- [Author](#author)

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
- Java 21 or higher
- Maven 3.x or higher
- PostgreSQL
- Docker is highly recommended but not strictly needed
- A Mailgun account 

### Installation
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

 For every execution, always run the `loadEnv` file and then the compiled .jar executable in the same shell  session. 

## Configuration

  The `credentials.json` file **MUST** contain the following environment variables:

  | Variable Name                     | Description                                                                 |
  |-----------------------------------|-----------------------------------------------------------------------------|
  | `KOMODO_DATASOURCE_URL`           | The database URL.                                                          |
  | `KOMODO_DOMAIN_URL`               | The domain in which the application will run (can be a domain name or IP). |
  | `KOMODO_DATASOURCE_USER`          | The database user.                                                         |
  | `KOMODO_DATASOURCE_PASSWORD`      | The database user password.                                                |
  | `KOMODO_MAILGUN_KEY`              | Your secret Mailgun key.                                                   |
  | `KOMODO_MAILGUN_DOMAIN`           | Your Mailgun domain.                                                       |
  | `KOMODO_JWT_SECRET_KEY`           | Your JWT secret key.                                                       |
  | `KOMODO_JWT_EXPIRATION_TIME`      | The time (in milliseconds) that JWT tokens should be valid before expiring.|
  | `KOMODO_JWT_EMAIL_VERIFICATION_TIME` | The time the email verification link should be valid for.               |

## API Documentation
  
  There is a Postman collection and an HTML file documenting the API and possible exceptions on the /docs directory.

## License

  This application is licensed under the MIT licence. See the [LICENSE](LICENSE) for details.

## Author
- **Kjistik**  
  GitHub: [Kjistik](https://github.com/kjistik)  
  Email: [coronelmartinernesto@gmail.com](mailto:coronelmartinernesto@gmail.com)

