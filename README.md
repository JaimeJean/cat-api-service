# cat-api-service
Case técnico — API REST que consome TheCatAPI, persiste dados em PostgreSQL e expõe consultas de raças de gatos. Java 21, Spring Boot 3.x, arquitetura hexagonal.

# The Cat API — Case Técnico

## Visão geral

Este projeto implementa uma API em **Java + Spring Boot** para ingestão, armazenamento e consulta de informações sobre raças de gatos a partir da **TheCatAPI**, além de disponibilizar um fluxo **assíncrono** com envio do resultado por e-mail.

A solução foi construída com foco em:

- organização e separação de responsabilidades;
- facilidade de execução local com Docker;
- deploy em nuvem com AWS;
- observabilidade básica com logs e alarmes;
- documentação de decisões arquiteturais.

---

# Funcionalidades implementadas

## Ingestão inicial de dados
Ao iniciar a aplicação, é executado um processo de ingestão que:

- busca as raças disponíveis na TheCatAPI;
- armazena origem, temperamento e descrição;
- armazena até 3 imagens por raça;
- armazena 3 imagens de gatos com chapéu;
- armazena 3 imagens de gatos com óculos.

## APIs síncronas
Foram implementadas APIs REST para:

- listar todas as raças;
- listar detalhes de uma raça;
- filtrar por temperamento;
- filtrar por origem;
- combinar filtros.

## Fluxo assíncrono
Também foi implementado um fluxo assíncrono em que:

- o usuário envia uma requisição com filtros e e-mail;
- a request é publicada em uma fila SQS;
- a mensagem é consumida pela aplicação;
- a busca é executada no banco;
- o resultado é enviado por e-mail via SES;
- o processamento é controlado no DynamoDB para evitar duplicidade;
- falhas repetidas são encaminhadas para uma DLQ.

---

# Bônus implementados

Foram implementados os seguintes itens bônus:

- deploy da aplicação na AWS;
- fluxo assíncrono com envio por e-mail;
- integração com AWS CloudWatch Logs;
- queries de observabilidade no CloudWatch Logs Insights;
- alarmes para monitoramento de falhas relevantes.

---

# Stack utilizada

## Backend
- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Flyway
- Bean Validation

## Banco de dados
- PostgreSQL

## Assíncrono / AWS
- Amazon SQS
- Amazon SES
- Amazon DynamoDB
- Amazon ECS Fargate
- Amazon ECR
- Amazon RDS
- Amazon ALB
- AWS Secrets Manager
- AWS CloudWatch

## Infra e execução
- Docker
- Docker Compose
- Terraform
- LocalStack (ambiente local)

---

# Arquitetura

A solução foi estruturada com uma abordagem inspirada em **arquitetura hexagonal**, separando:

- camada de entrada (controllers e consumers);
- camada de aplicação (serviços e casos de uso);
- camada de domínio;
- camada de saída (integrações com banco, SQS, SES, DynamoDB e TheCatAPI).

Os detalhes completos estão nos documentos de arquitetura e nos diagramas C4.

## Documentação de arquitetura
Consulte os arquivos em `docs/architecture/`.

Exemplos de conteúdo esperado nesse diretório:
- diagramas C4
- ADRs
- documentos de decisões técnicas

---

# Estrutura do repositório

```text
.
├── docs/
│   ├── architecture/
│   ├── operations/
│   └── ...
├── terraform/
│   ├── bootstrap/
│   ├── envs/
│   └── modules/
├── src/
├── Dockerfile
├── docker-compose.yml
└── README.md
```

## Organização dos principais diretórios

- `src/`  
  Código-fonte da aplicação.

- `terraform/`  
  Infraestrutura como código da solução AWS.

- `docs/architecture/`  
  Diagramas e decisões arquiteturais.

- `docs/operations/`  
  Documentação operacional, deploy e monitoramento.

---

# Endpoints principais

## Síncronos
- `GET /api/v1/breeds`
- `GET /api/v1/breeds/{id}`
- `GET /api/v1/breeds?temperament=...`
- `GET /api/v1/breeds?origin=...`
- `GET /api/v1/breeds?temperament=...&origin=...`

## Assíncrono
- `POST /api/v1/async/breeds`

Exemplo de body:

```json
{
  "email": "usuario@dominio.com",
  "temperament": "Independent",
  "origin": "Egypt"
}
```

## Health check
- `GET /actuator/health`

---

# Collection Postman

A collection do Postman foi preparada com uso de **variáveis de ambiente**, permitindo alternar entre:

- ambiente local
- ambiente publicado na AWS

A ideia é evitar hardcode de endpoints específicos no repositório.

Exemplo:
- `{{baseUrl}}/api/v1/breeds`
- `{{baseUrl}}/api/v1/async/breeds`

---

# Como executar localmente

As instruções detalhadas estão em:

```text
docs/operations/deploy.md
```

## Resumo rápido

### Pré-requisitos
- Java 21
- Maven
- Docker
- Docker Compose

### Build
```bash
mvn clean package -DskipTests
```

### Subir ambiente
```bash
docker compose up --build
```

### Health check local
```text
GET http://localhost:8080/actuator/health
```

---

# Como executar na AWS

As instruções completas estão em:

```text
docs/operations/deploy.md
```

## Resumo rápido

O deploy foi feito em etapas:

1. criar recursos essenciais com Terraform;
2. buildar a imagem;
3. publicar a imagem no ECR;
4. aplicar a infraestrutura completa;
5. validar ECS, ALB, RDS, SQS, SES e logs.

---

# Logging e monitoramento

A aplicação utiliza logging com níveis adequados (`INFO`, `WARN`, `ERROR`) e foi integrada ao **AWS CloudWatch Logs**.

Também foram definidos:

- queries no CloudWatch Logs Insights;
- alarme para DLQ;
- alarme para falha na ingestão inicial da TheCatAPI.

Documentação detalhada em:

```text
docs/operations/monitoring.md
```

---

# Testes

O projeto possui testes automatizados para partes relevantes da aplicação, incluindo cenários do fluxo assíncrono.

Para executar os testes:

```bash
mvn test
```

---

# Documentação complementar

## Problema e requisitos
- Documento do problema
- Requisitos funcionais e não funcionais

## Arquitetura
- diagramas C4
- ADRs
- decisões de banco
- decisões de fila
- estratégia de deploy

## Operação
- deploy local e AWS
- monitoramento e observabilidade

Todos esses materiais estão organizados na pasta `docs/`.

---

# Decisões técnicas relevantes

## Banco principal: PostgreSQL
Foi escolhido para armazenar o domínio principal da aplicação, devido ao modelo relacional das entidades e à necessidade de consultas por filtros.

## Controle assíncrono: DynamoDB
Foi utilizado para controle de processamento de mensagens, com foco em idempotência no fluxo assíncrono.

## Fila: SQS + DLQ
Foi adotado SQS com dead-letter queue para desacoplar o processamento assíncrono e tratar falhas recorrentes.

## E-mail: SES
Foi utilizado o Amazon SES para envio do resultado das consultas assíncronas.

## Infraestrutura: Terraform
Toda a infraestrutura da solução foi modelada como código, com separação entre bootstrap, ambiente e módulos reutilizáveis.

---

# Limitações e melhorias futuras

Como evoluções futuras, seria possível adicionar:

- pipeline CI/CD para build, push e deploy automatizados;
- dashboards mais completos no CloudWatch;
- rastreamento avançado de entrega de e-mails via SES;
- endurecimento adicional de permissões IAM;
- melhoria da estratégia de startup para reduzir tempo da ingestão inicial.

---

# Observações finais

A solução foi construída buscando equilíbrio entre:

- qualidade técnica;
- clareza arquitetural;
- simplicidade operacional;
- e capacidade de validação pela avaliadora.

O projeto inclui documentação complementar para facilitar a leitura e a validação da entrega sem concentrar todos os detalhes em um único arquivo.