# My Authenticationa Api With JWT Using Spring Boot Security

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)

[Leia em Português](READEM.pt_br.md)

A project to study an authentication API using Jason Web Token and Spring Boot Security in Java.

## Overview

This project is a Spring Boot-based API that implements JWT (JSON Web Token) authentication for securing endpoints. The API allows users to register, login, and access protected resources based on the provided JWT token. The implementation leverages Spring Security to handle authentication and authorization.

## Features

- **User Registration**: Allows new users to create an account.
- **Login with JWT**: Issues a JWT upon successful login.
- **Protected Endpoints**: Endpoints that require a valid JWT to be accessed.
- **Role-based Authorization**: Access control based on user roles.
- **Token Expiration**: JWT tokens have an expiration mechanism to improve security.

## JWT Structure

### Header
```
{
  "alg": "HS384"
}
```
The Header contains only the signature algorithm used to sign the JWT, which is **HS384**.

### Payload
```
{
  "sub": "youremail@gmail.com",
  "roles": [
    "ROLE_ADMIN"
  ],
  "iat": 1726437119,
  "exp": 1726696319
}
```
The payload contains the **sub** (subject), which in this case is the user's email (but could be the username or any other data that can be used as a login), our user's **roles**, the **iat** (issued date), and finally, the **exp** (expiration date).

### How The Expiration Date is Defined

The **expiration date** is defined **3 days** before the **issued date**.

This convention is defined when the token is generated in the **TokenService**, following the code below:

```
[...]
public String generateToken(String email, List<String> roles) {
		return Jwts.builder()
				.subject(email)
				.claim("roles", roles.stream().map(role -> "ROLE_" + role).collect(Collectors.toList()))
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000))
				.signWith(getSecretKey())
				.compact();
	}
```

As you can see in the `generateToken()` function, it receives as parameters, a string (the user's email) and a list of strings (Which are our user's Roles converted to string), also note that due to the conventions given by **Spring Security** itself, the prefix `"ROLE_"` is added.

### Secret Key

The **Secret Key** is configured in `application.properties`, something like this:

```
spring.application.name=MyJWTAuthenticationAPI

# Here are database configurations...

# The Secret Key is basically a Base64 encoded string 

security.secretKey=TnX+frv3yMCVaQSf0Gr6STPHOPHRQe...
```
Then it is injected into **TokenService** as `secretKey`:

```
@Value("${security.secretKey}")
	String secretKey;
```

After that, in the `getSecretKey()` method the string `secretKey` is decoded (in BASE64) into a byte array (`byte[]`). This is necessary because the secret key is usually stored securely in Base64 in the configuration file, but needs to be converted to bytes before being used for cryptographic operations.
The `Keys.hmacShaKeyFor(keyBytes)` method takes the secret key byte array (already decoded) and creates an instance of `SecretKey` using the HMAC-SHA (Hash-based Message Authentication Code) algorithm. This key will be used to sign and validate the JWTs.

```
private SecretKey getSecretKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}
```

## Technologies

- Spring Boot 3
- Spring Security
- [JWT(JSON Web Token) - jjwt](https://github.com/jwtk/jjwt)
- Gradle
- PostgreeSQL
- Java 21+

## How it Works

- **Registration Flow**: Users send a POST request to auth/register with their credentials (username, password). This data is stored securely (with password hashing).

- **Authentication Flow**: Users send a POST request to auth/login with their credentials. If authenticated successfully, a JWT is generated and returned.

- **Protected Endpoints**: Once the user has the JWT, it must be included in the Authorization header (as Bearer <token>) in subsequent requests to access protected endpoints.

## Endpoints

### Public Endpoints

- `POST /auth/register`: Register a new user.
  - Request Body:
  
  ```
  {
     "email" : "youremail@gmail.com",
     "password" : "yourpassword123",
     "roles" : ["USER","ADMIN"]
  }
  
  ```
  - Reponse Body:
  
  ```
  {
    "id": 1,
    "email": "youremail@gmail.com",
    "password": "yourhashedpassowrd"",
    "roles": [
        "USER",
        "ADMIN"
    ],
    "enabled": true,
    "authorities": [
        {
            "authority": "ADMIN"
        }
    ],
    "username": "myemail@gmail.com",
    "accountNonExpired": true,
    "accountNonLocked": true,
    "credentialsNonExpired": true
    }
  ```
- `POST /auth/login`: Authenticate a user and generate a JWT.
  - Request Body:
  ```
  {
     "email" : "youremail@gmail.com",
     "password" : "yourpassword123"
  }
  ```
  - Response Body:
  ```
  {
     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
  ```
### Secured Endpoints (require JWT)

These endpoints require a **Bearer Token** in their header, for example:
`Authorization: Bearer <your-jwt-token>`
  
- `GET \ping`: Permited only for users with the **ADMIN** role.
  
- `GET \user\{id}`: Access a database user by their **id**. (Not require the **ADMIN** role)

---


