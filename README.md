# My Authenticationa Api With JWT Using Spring Boot Security

[Leia em Português](#minha-api-de-autenticacao-com-jwt-usando-spring-boot-security)

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

# Minha API de Autenticação com JWT Usando Spring Boot Security

[Read in English](#my-authentication-api-with-jwt-using-spring-boot-security)

Um projeto para estudar uma API de autenticação utilizando Jason Web Token e Spring Boot Security em Java.

## Visão Geral

Este projeto é uma API baseada em Spring Boot que implementa a autenticação JWT (JSON Web Token) para proteger endpoints. A API permite que os usuários se registrem, façam login e acessem recursos protegidos com base no token JWT fornecido. A implementação utiliza o Spring Security para lidar com a autenticação e autorização.

## Funcionalidades

- **Registro de Usuário**: Permite que novos usuários criem uma conta.
- **Login com JWT**: Gera um JWT após um login bem-sucedido.
- **Endpoints Protegidos**: Endpoints que requerem um JWT válido para serem acessados.
- **Autorização Baseada em Papéis (Roles)**: Controle de acesso baseado em papéis de usuário.
- **Expiração de Token**: Os tokens JWT possuem um mecanismo de expiração para melhorar a segurança.

## Estrutura do JWT

### Cabeçalho (Header)
```
{
  "alg": "HS384"
}
```
O Cabeçalho contém apenas o algoritmo de assinatura utilizado para assinar o JWT, que é o **HS384**.

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
O Payload contém o **sub** (assunto), que neste caso é o email do usuário (mas pode ser o nome de usuário ou qualquer outro dado utilizado como login), os **roles** (papéis) do usuário, o **iat** (data de emissão) e, finalmente, a **exp** (data de expiração).

### Como a Data de Expiração é Definida

A **data de expiração** é definida **3 dias** após a **data de emissão**.

Essa convenção é definida quando o token é gerado no **TokenService**, conforme o código abaixo:

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

Como pode ser visto na função `generateToken()`, ela recebe como parâmetros uma string (o email do usuário) e uma lista de strings (que são os papéis do usuário convertidos para string). Também é importante notar que, devido às convenções do próprio **Spring Security**, o prefixo `"ROLE_"` é adicionado.

### Chave Secreta

A **Chave Secreta** é configurada no `application.properties`, algo assim:

```
spring.application.name=MyJWTAuthenticationAPI

# Aqui estão as configurações do banco de dados...

# A Chave Secreta é basicamente uma string codificada em Base64

security.secretKey=TnX+frv3yMCVaQSf0Gr6STPHOPHRQe...
```

Depois, ela é injetada no **TokenService** como `secretKey`:

```
@Value("${security.secretKey}")
	String secretKey;
```

Após isso, no método `getSecretKey()`, a string `secretKey` é decodificada (em BASE64) para um array de bytes (`byte[]`). Isso é necessário porque a chave secreta geralmente é armazenada com segurança em Base64 no arquivo de configuração, mas precisa ser convertida para bytes antes de ser usada em operações criptográficas. O método `Keys.hmacShaKeyFor(keyBytes)` pega o array de bytes da chave secreta (já decodificado) e cria uma instância de `SecretKey` utilizando o algoritmo HMAC-SHA (Código de Autenticação de Mensagens Baseado em Hash). Esta chave será usada para assinar e validar os JWTs.

```
private SecretKey getSecretKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}
```

## Tecnologias

- Spring Boot 3
- Spring Security
- [JWT(JSON Web Token) - jjwt](https://github.com/jwtk/jjwt)
- Gradle
- PostgreSQL
- Java 21+

## Como Funciona

- **Fluxo de Registro**: Os usuários enviam uma requisição POST para `auth/register` com suas credenciais (nome de usuário, senha). Esses dados são armazenados de forma segura (com hash de senha).

- **Fluxo de Autenticação**: Os usuários enviam uma requisição POST para `auth/login` com suas credenciais. Se autenticados com sucesso, um JWT é gerado e retornado.

- **Endpoints Protegidos**: Uma vez que o usuário tenha o JWT, ele deve ser incluído no cabeçalho Authorization (como Bearer <token>) em requisições subsequentes para acessar endpoints protegidos.

## Endpoints

### Endpoints Públicos

- `POST /auth/register`: Registra um novo usuário.
  - Corpo da Requisição:
  
  ```
  {
     "email" : "youremail@gmail.com",
     "password" : "yourpassword123",
     "roles" : ["USER", "ADMIN"]
  }
  ```
  - Corpo da Resposta:
  
  ```
  {
    "id": 1,
    "email": "youremail@gmail.com",
    "password": "yourhashedpassword",
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
    "username": "youremail@gmail.com",
    "accountNonExpired": true,
    "accountNonLocked": true,
    "credentialsNonExpired": true
  }
  ```
  
- `POST /auth/login`: Autentica um usuário e gera um JWT.
  - Corpo da Requisição:
  ```
  {
     "email" : "youremail@gmail.com",
     "password" : "yourpassword123"
  }
  ```
  - Corpo da Resposta:
  ```
  {
     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
  ```

### Endpoints Protegidos (requer JWT)

Esses endpoints requerem um **Bearer Token** no cabeçalho, por exemplo:
`Authorization: Bearer <your-jwt-token>`
  
- `GET /ping`: Permitido apenas para usuários com a role **ADMIN**.
  
- `GET /user/{id}`: Acessa um usuário do banco de dados pelo seu **id**. (Não requer a role **ADMIN**)
