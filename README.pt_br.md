# Minha API de Autenticação com JWT Usando Spring Boot Security

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)


[Read in English](README.md)

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

## Rodando o projeto

Para rodar esse projeto sem mais complicações, utilizaremos **Docker**. 


### Via Docker CLI

Primeiramente, crie uma imagem para o banco dados **PostgreSQL**.

```
docker pull postgres

```

Agora, crie uma rede para a conexão do container do banco dados e o container da API.


```
docker network create auth_ne

```

Após isso, crie os containers e conecte-os a rede:


- Container PostgreSQL:


```
docker run --name auth_db --network auth_net -e POSTGRES_PASSWORD=everybodywannabeapassword -e POSTGRES_DB=test_db -v /tmp/database:/var/lib/postgresql/data -p 5432:5432 -d postgres

```

- Container da API:

```
docker run --name jwt_api --network auth_net -p 8080:8080 -e DB_URL=jdbc:postgresql://auth_db:5432/test_db -e DB_PASSWORD=everybodywannabeapassword my-jwt-ap
```

### Via Docker Compose

```
version: '3.8'

services:
  auth_db:
    image: postgres
    container_name: auth_db
    environment:
      POSTGRES_PASSWORD: everybodywannabeapassword
      POSTGRES_DB: test_db
    volumes:
      - /tmp/database:/var/lib/postgresql/data
    networks:
      - auth_net
    ports:
      - "5432:5432"

  jwt_api:
    image: my-jwt-api
    container_name: jwt_api
    environment:
      DB_URL: jdbc:postgresql://auth_db:5432/test_db
      DB_PASSWORD: everybodywannabeapassword
    depends_on:
      - auth_db
    networks:
      - auth_net
    ports:
      - "8080:8080"

networks:
  auth_net:
    driver: bridge

```

---
