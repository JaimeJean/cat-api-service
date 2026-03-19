> Abordagem BDD (Behavior-Driven Development) como prática de especificação de comportamento.
Cenários escritos em Dado/Quando/Então, executados manualmente via Postman contra Docker Compose completo.
>

---

## 1. Ingestão de Dados (Startup)

### Cenário 1.1 — Ingestão completa no primeiro startup

**Dado** que o banco de dados está vazio
**E** a aplicação está configurada com TheCatAPI key válida
**Quando** a aplicação inicia via `docker-compose up`**Então** todas as raças da TheCatAPI são persistidas na tabela `breeds`**E** até 3 imagens por raça são persistidas na tabela `images` com `category = BREED`**E** 3 imagens de gatos com chapéu são persistidas com `category = HAT` e `breed_id = null`**E** 3 imagens de gatos com óculos são persistidas com `category = GLASSES` e `breed_id = null`**E** os logs registram início e fim da ingestão com contagem de raças e imagens

**Validação:**

```sql
SELECT COUNT(*) FROM breeds;                                    -- ~67 raças
SELECT COUNT(*) FROM images WHERE category = 'BREED';           -- até 201 (67 × 3)
SELECT COUNT(*) FROM images WHERE category = 'HAT';             -- 3
SELECT COUNT(*) FROM images WHERE category = 'GLASSES';         -- 3
```

### Cenário 1.2 — Idempotência na reinicialização

**Dado** que a aplicação já executou a ingestão com sucesso
**Quando** a aplicação é reiniciada (`docker-compose restart app`)
**Então** nenhum dado é duplicado no banco
**E** os logs registram "já existem, pulando" para raças e categorias já ingeridas

**Validação:**

```sql
-- Contar antes e depois do restart — valores devem ser idênticos
SELECT COUNT(*) FROM breeds;
SELECT COUNT(*) FROM images;
```

### Cenário 1.3 — Resiliência a falhas parciais na API

**Dado** que a TheCatAPI está instável (timeout em algumas requisições)
**Quando** a ingestão é executada
**Então** as raças que obtiveram resposta com sucesso são persistidas normalmente
**E** as raças que falharam são registradas nos logs com nível ERROR
**E** a ingestão não é interrompida por falhas individuais

---

## 2. Consulta de Raças

### Cenário 2.1 — Listar todas as raças (paginação padrão)

**Dado** que existem raças no banco de dados
**Quando** faço `GET /api/v1/breeds`**Então** recebo status `200 OK`**E** o body contém uma página com até 20 raças (size padrão)
**E** cada raça possui `id`, `name`, `origin`, `temperament`, `description` e `images`**E** a página contém metadados: `totalElements`, `totalPages`, `number`, `size`

**Request Postman:**

```
GET http://localhost:8080/api/v1/breeds
```

**Response esperada (estrutura):**

```json
{
  "content": [
    {
      "id": 1,
      "name": "Abyssinian",
      "origin": "Ethiopia",
      "temperament": "Active, Energetic, Independent, Intelligent, Gentle",
      "description": "...",
      "images": [
        { "id": 1, "url": "https://cdn2.thecatapi.com/images/...", "category": "BREED" }
      ]
    }
  ],
  "totalElements": 67,
  "totalPages": 4,
  "number": 0,
  "size": 20
}
```

### Cenário 2.2 — Paginação customizada

**Dado** que existem raças no banco de dados
**Quando** faço `GET /api/v1/breeds?page=1&size=5&sort=name,desc`**Então** recebo status `200 OK`**E** o body contém no máximo 5 raças
**E** a página indica `number: 1` e `size: 5`**E** as raças estão ordenadas por nome em ordem decrescente

**Request Postman:**

```
GET http://localhost:8080/api/v1/breeds?page=1&size=5&sort=name,desc
```

### Cenário 2.3 — Filtrar por temperamento

**Dado** que existem raças com temperamento "Playful" no banco
**Quando** faço `GET /api/v1/breeds?temperament=Playful`**Então** recebo status `200 OK`**E** todas as raças retornadas possuem "Playful" no campo `temperament`

**Request Postman:**

```
GET http://localhost:8080/api/v1/breeds?temperament=Playful
```

### Cenário 2.4 — Filtrar por origem

**Dado** que existem raças com origem "Egypt" no banco
**Quando** faço `GET /api/v1/breeds?origin=Egypt`**Então** recebo status `200 OK`**E** todas as raças retornadas possuem "Egypt" no campo `origin`

**Request Postman:**

```
GET http://localhost:8080/api/v1/breeds?origin=Egypt
```

### Cenário 2.5 — Filtrar por temperamento e origem combinados

**Dado** que existem raças no banco
**Quando** faço `GET /api/v1/breeds?temperament=Active&origin=Ethiopia`**Então** recebo status `200 OK`**E** todas as raças retornadas possuem "Active" no temperamento **e** "Ethiopia" na origem

**Request Postman:**

```
GET http://localhost:8080/api/v1/breeds?temperament=Active&origin=Ethiopia
```

### Cenário 2.6 — Filtro sem resultados

**Dado** que não existem raças com origem "Atlantis"
**Quando** faço `GET /api/v1/breeds?origin=Atlantis`**Então** recebo status `200 OK`**E** o body contém uma página vazia (`content: []`, `totalElements: 0`)

**Request Postman:**

```
GET http://localhost:8080/api/v1/breeds?origin=Atlantis
```

---

## 3. Busca por ID

### Cenário 3.1 — Buscar raça existente

**Dado** que existe uma raça com `id = 1`**Quando** faço `GET /api/v1/breeds/1`**Então** recebo status `200 OK`**E** o body contém a raça com suas imagens associadas

**Request Postman:**

```
GET http://localhost:8080/api/v1/breeds/1
```

### Cenário 3.2 — Buscar raça inexistente (404)

**Dado** que não existe raça com `id = 9999`**Quando** faço `GET /api/v1/breeds/9999`**Então** recebo status `404 Not Found`**E** o body segue o padrão RFC 7807 (Problem Details):

```json
{
  "type": "https://api.catapi.com/errors/breed-not-found",
  "title": "Breed Not Found",
  "status": 404,
  "detail": "Breed not found with id: 9999"
}
```

**Request Postman:**

```
GET http://localhost:8080/api/v1/breeds/9999
```

### Cenário 3.3 — ID com formato inválido (400)

**Dado** que o endpoint espera um ID numérico
**Quando** faço `GET /api/v1/breeds/abc`**Então** recebo status `400 Bad Request`**E** o body segue o padrão RFC 7807:

```json
{
  "type": "https://api.catapi.com/errors/invalid-parameter",
  "title": "Invalid Parameter",
  "status": 400,
  "detail": "Parameter 'id' must be of type Long"
}
```

**Request Postman:**

```
GET http://localhost:8080/api/v1/breeds/abc
```

---

## 4. Consulta de Imagens por Categoria

### Cenário 4.1 — Listar imagens de gatos com chapéu

**Dado** que existem imagens com `category = HAT` no banco
**Quando** faço `GET /api/v1/images?category=HAT`**Então** recebo status `200 OK`**E** o body contém uma lista de imagens com `category: "HAT"`**E** nenhuma imagem possui associação com raça

**Request Postman:**

```
GET http://localhost:8080/api/v1/images?category=HAT
```

**Response esperada:**

```json
[
  { "id": 1, "url": "https://cdn2.thecatapi.com/images/...", "category": "HAT" },
  { "id": 2, "url": "https://cdn2.thecatapi.com/images/...", "category": "HAT" },
  { "id": 3, "url": "https://cdn2.thecatapi.com/images/...", "category": "HAT" }
]
```

### Cenário 4.2 — Listar imagens de gatos com óculos

**Dado** que existem imagens com `category = GLASSES` no banco
**Quando** faço `GET /api/v1/images?category=GLASSES`**Então** recebo status `200 OK`**E** o body contém uma lista de imagens com `category: "GLASSES"`

**Request Postman:**

```
GET http://localhost:8080/api/v1/images?category=GLASSES
```

### Cenário 4.3 — Categoria inválida (400)

**Dado** que o endpoint aceita apenas HAT ou GLASSES
**Quando** faço `GET /api/v1/images?category=INVALID`**Então** recebo status `400 Bad Request`**E** o body segue o padrão RFC 7807

**Request Postman:**

```
GET http://localhost:8080/api/v1/images?category=INVALID
```

---

## 5. Health Check

### Cenário 5.1 — Aplicação saudável

**Dado** que a aplicação está rodando e o banco está acessível
**Quando** faço `GET /actuator/health`**Então** recebo status `200 OK`**E** o body contém `{ "status": "UP" }`

**Request Postman:**

```
GET http://localhost:8080/actuator/health
```

---

## 6. Endpoint inexistente

### Cenário 6.1 — Rota não mapeada (404)

**Dado** que o endpoint `/api/v1/dogs` não existe
**Quando** faço `GET /api/v1/dogs`**Então** recebo status `404 Not Found`**E** o body segue o padrão RFC 7807:

```json
{
  "type": "https://api.catapi.com/errors/endpoint-not-found",
  "title": "Endpoint Not Found",
  "status": 404,
  "detail": "The requested endpoint does not exist"
}
```

**Request Postman:**

```
GET http://localhost:8080/api/v1/dogs
```

---

## Execução

**Pré-requisitos:**

1. Docker e Docker Compose instalados
2. TheCatAPI key configurada no `.env` ou `application-local.yml`

**Passos:**

1. Subir o ambiente: `docker-compose up -d`
2. Aguardar logs de ingestão finalizar (verificar com `docker-compose logs -f app`)
3. Executar a coleção Postman na ordem dos cenários acima
4. Validar cenários de banco via `psql` ou DBeaver conectado ao PostgreSQL local
5. Registrar evidências (screenshots dos responses no Postman)

**Resultado esperado:** todos os cenários passam conforme descrito acima.

## 7. Processamento assíncrono

### 7.1 Requisição assíncrona sem filtros

**Dado** que a aplicação está em execução e o fluxo assíncrono está configurado corretamente  
**Quando** faço uma requisição:

```http
POST /api/v1/async/breeds
Content-Type: application/json
```

Body:
```json
{
  "email": "jaime.jean@hotmail.com"
}
```

**Então** a API deve responder com:

- status `202 Accepted`
- um `requestId` no corpo da resposta

**E** a mensagem deve ser publicada na fila SQS  
**E** deve ser consumida pela aplicação  
**E** a busca deve ser executada sem filtros  
**E** o resultado deve ser enviado por e-mail

---

### 7.2 Requisição assíncrona com filtros válidos

**Dado** que existem raças cadastradas compatíveis com os filtros informados  
**Quando** faço uma requisição:

```http
POST /api/v1/async/breeds
Content-Type: application/json
```

Body:
```json
{
  "email": "jaime.jean@hotmail.com",
  "temperament": "Independent",
  "origin": "Egypt"
}
```

**Então** a API deve responder com:

- status `202 Accepted`
- um `requestId` no corpo da resposta

**E** a mensagem deve ser publicada na fila SQS  
**E** a mensagem deve ser consumida com sucesso  
**E** a busca deve retornar resultados compatíveis  
**E** o envio por e-mail deve ser realizado com sucesso

---

### 7.3 Requisição assíncrona com filtros sem resultado

**Dado** que os filtros informados não possuem correspondência na base  
**Quando** faço uma requisição:

```http
POST /api/v1/async/breeds
Content-Type: application/json
```

Body:
```json
{
  "email": "jaime.jean@hotmail.com",
  "temperament": "NonExistentTemperament",
  "origin": "UnknownOrigin"
}
```

**Então** a API deve responder com:

- status `202 Accepted`
- um `requestId` no corpo da resposta

**E** a mensagem deve ser publicada na fila SQS  
**E** a mensagem deve ser consumida com sucesso  
**E** a busca deve retornar `0` resultados  
**E** a aplicação deve tentar enviar um e-mail informando que nenhum resultado foi encontrado

---

### 7.4 Requisição assíncrona com e-mail não verificado no SES

**Dado** que o ambiente SES esteja em sandbox e o destinatário não esteja verificado  
**Quando** faço uma requisição:

```http
POST /api/v1/async/breeds
Content-Type: application/json
```

Body:
```json
{
  "email": "email-nao-verificado@dominio.com",
  "origin": "Egypt"
}
```

**Então** a API deve responder inicialmente com:

- status `202 Accepted`
- um `requestId` no corpo da resposta

**Mas** durante o processamento assíncrono:
- o envio via SES deve falhar
- o erro deve ser registrado em log
- a mensagem deve ser reprocessada conforme política da fila
- após exceder o número máximo de tentativas, a mensagem pode ser encaminhada para a DLQ

---

### 7.5 Requisição assíncrona com payload inválido

**Dado** que o campo `email` é obrigatório  
**Quando** faço uma requisição com body vazio:

```http
POST /api/v1/async/breeds
Content-Type: application/json
```

Body:
```json
{}
```

**Então** a API deve responder com:

- status `400 Bad Request`

**E** o corpo da resposta deve indicar erro de validação

---

### 7.6 Requisição assíncrona com e-mail ausente e filtros preenchidos

**Dado** que o campo `email` é obrigatório mesmo quando filtros são informados  
**Quando** faço uma requisição:

```http
POST /api/v1/async/breeds
Content-Type: application/json
```

Body:
```json
{
  "temperament": "Independent",
  "origin": "Egypt"
}
```

**Então** a API deve responder com:

- status `400 Bad Request`

**E** o corpo da resposta deve indicar que o campo `email` é obrigatório

---

### 7.7 Evidências complementares

A validação do fluxo assíncrono pode ser complementada com:

- logs da aplicação no console ou CloudWatch;
- verificação de mensagens na fila SQS;
- verificação de processamento no DynamoDB;
- verificação de envio/falha de e-mail via SES;
- verificação de mensagens na DLQ em caso de erro recorrente.