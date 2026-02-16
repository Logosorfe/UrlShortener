# URL Shortener Service

A production‑ready URL shortening platform built with Spring Boot 3, Spring Security (JWT),
PostgreSQL, and OpenAPI/Swagger. The system supports user management, subscriptions for
custom path prefixes, URL shortening, redirect handling, and detailed usage statistics.

------------------------------------------------------------
FEATURES
------------------------------------------------------------

1. Authentication & Authorization
    - JWT-based authentication
    - Role-based access control (USER, ADMIN)
    - Secure password hashing (BCrypt)
    - Access restrictions enforced at controller and service layers

2. User Management
    - Registration and login
    - Fetch user by ID or email
    - Update user roles (ADMIN only)
    - Delete user (self-delete or ADMIN)
    - Extensive unit tests for service logic

3. URL Shortening
    - Create short URLs with or without a prefix
    - SHA-256 based suffix generation
    - Reuse existing URL bindings when possible
    - Reset redirect counter
    - Delete URL bindings
    - Redirect endpoint (hidden from Swagger)

4. Subscriptions
    - Users can purchase subscriptions for custom prefixes
    - Expired subscriptions can be reassigned
    - Active subscriptions are exclusive
    - Admin-controlled payment processing
    - Subscription expiration logic

5. Statistics
    - Total redirect count per user
    - Redirect count per URL binding
    - Top 10 most used URLs (ADMIN only)

6. URL Validation
    - URL format validation
    - UID format validation
    - Safe redirect validation (blocks localhost, private networks)

7. API Documentation
    - Fully documented with OpenAPI 3
    - Swagger UI: http://localhost:8080/swagger-ui.html
    - Redirect controller is hidden from Swagger

------------------------------------------------------------
ARCHITECTURE OVERVIEW
------------------------------------------------------------

src/main/java/com/telran/org/urlshortener
│
├── controller        # REST controllers
├── dto               # Data transfer objects
├── entity            # JPA entities
├── exception         # Custom exceptions
├── mapper            # DTO <-> Entity converters
├── model             # Enums (RoleType, StatusState)
├── repository        # Spring Data JPA repositories
├── security          # JWT, filters, UserDetailsService
├── service           # Business logic
└── utility           # Helpers (URL validation, masking)

------------------------------------------------------------
DATABASE SCHEMA (PostgreSQL)
------------------------------------------------------------

TABLE: users
- id (BIGINT PK)
- email (TEXT, unique)
- password (TEXT, BCrypt hash)
- role (TEXT: USER / ADMIN)

TABLE: subscriptions
- id (BIGINT PK)
- path_prefix (TEXT)
- creation_date (DATE)
- expiration_date (DATE)
- status (TEXT: PAID / UNPAID)
- user_id (BIGINT FK)

TABLE: url_bindings
- id (BIGINT PK)
- uid (TEXT)
- original_url (TEXT)
- count (BIGINT)
- user_id (BIGINT FK)

------------------------------------------------------------
INSTALLATION & SETUP
------------------------------------------------------------

Prerequisites:
- Java 21+
- Maven 3.9+
- PostgreSQL 14+

1. Clone the repository:
   git clone https://github.com/your-repo/url-shortener.git
   cd url-shortener

2. Configure environment variables:
   export DB_PASSWORD=postgres

3. Create database:
   createdb urlshortener

4. Run the application:
   mvn spring-boot:run

------------------------------------------------------------
AUTHENTICATION (JWT)
------------------------------------------------------------

Login:
POST /users/login

Response:
{
"token": "jwt-token-here"
}

Use in protected endpoints:
Authorization: Bearer <token>

------------------------------------------------------------
API ENDPOINTS OVERVIEW
------------------------------------------------------------

USERS:
POST   /users                 (public)       Register
POST   /users/login           (public)       Login
GET    /users                 (admin)        List all users
GET    /users/{id}            (admin)        Get user by ID
GET    /users/by-email        (user/admin)   Get user by email
PATCH  /users/{id}/role       (admin)        Update role
DELETE /users/{id}            (user/admin)   Delete user

SUBSCRIPTIONS:
POST   /subscriptions                 (user)        Create subscription
GET    /subscriptions/by-user/{id}    (user/admin)  List subscriptions
GET    /subscriptions/{id}            (user/admin)  Get subscription
DELETE /subscriptions/{id}            (user)        Delete subscription
POST   /subscriptions/{id}/pay        (admin)       Process payment

URL BINDINGS:
POST   /url_bindings                  (user)        Create short URL
GET    /url_bindings/by-user/{id}     (user/admin)  List bindings
GET    /url_bindings?uId=...          (user/admin)  Get binding
PATCH  /url_bindings/{id}/reset       (user)        Reset counter
DELETE /url_bindings/{id}             (user)        Delete binding

REDIRECT (hidden from Swagger):
GET /{shortPath}

------------------------------------------------------------
TESTING
------------------------------------------------------------

Unit tests included for:
- UserServiceImplTest
- UrlBindingServiceImplTest
- SubscriptionServiceImplTest
- StatisticsServiceImplTest
- UrlValidationServiceTest
- JwtServiceTest
- JwtAuthenticationFilterTest (MockMvc)

Run tests:
mvn test

------------------------------------------------------------
TECHNOLOGIES USED
------------------------------------------------------------

- Java 21
- Spring Boot 3.3
- Spring Security (JWT)
- Spring Data JPA
- PostgreSQL
- Lombok
- Springdoc OpenAPI 3
- JUnit 5 / Mockito

------------------------------------------------------------
LICENSE
------------------------------------------------------------

This project is licensed under the MIT License.

------------------------------------------------------------
AUTHOR
------------------------------------------------------------

Aleksandrs Lapickis
URL Shortener Project
2025-2026
