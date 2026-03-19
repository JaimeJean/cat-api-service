# Deploy

## Objetivo

Este documento descreve como executar a aplicação:

- localmente, com Docker Compose;
- na AWS, com Terraform + ECS Fargate.

---

# 1. Execução local

## 1.1 Pré-requisitos

É necessário ter instalado:

- Java 21
- Maven
- Docker
- Docker Compose

---

## 1.2 Serviços locais utilizados

O ambiente local utiliza:

- aplicação Spring Boot
- PostgreSQL
- LocalStack para simulação de serviços AWS utilizados no fluxo assíncrono

---

## 1.3 Build da aplicação

Na raiz do projeto:

```bash
mvn clean package
```

Se necessário pular testes:

```bash
mvn clean package -DskipTests
```

---

## 1.4 Subir o ambiente local

```bash
docker compose up --build
```

Se quiser forçar rebuild completo sem cache da imagem:

```bash
docker compose build --no-cache
docker compose up
```

Se quiser limpar volumes e subir do zero:

```bash
docker compose down -v
docker compose build --no-cache
docker compose up
```

---

## 1.5 Profile utilizado localmente

No ambiente local, a aplicação roda com o profile:

```text
local
```

Isso faz com que o Spring carregue:

- `application.yml`
- `application-local.yml`

---

## 1.6 Validar execução local

### Health check
```text
GET http://localhost:8080/actuator/health
```

### Exemplos de endpoints síncronos
```text
GET http://localhost:8080/api/v1/breeds
GET http://localhost:8080/api/v1/breeds?origin=Egypt
GET http://localhost:8080/api/v1/breeds?temperament=Independent
GET http://localhost:8080/api/v1/breeds?temperament=Independent&origin=Egypt
```

### Exemplo de endpoint assíncrono
```text
POST http://localhost:8080/api/v1/async/breeds
Content-Type: application/json
```

Body:
```json
{
  "email": "jaime.jean@hotmail.com",
  "origin": "Egypt"
}
```

---

# 2. Deploy na AWS

## 2.1 Visão geral

O deploy na AWS foi dividido em etapas para garantir que a imagem da aplicação já exista no ECR antes da criação do ECS.

A estratégia adotada foi:

1. provisionar a base mínima necessária;
2. publicar a imagem da aplicação no ECR;
3. provisionar a infraestrutura completa.


---

# 3. Pré-requisitos AWS

É necessário ter:

- conta AWS
- AWS CLI configurada
- credenciais válidas
- Terraform instalado
- Docker instalado

---

# 4. Estrutura Terraform

A infraestrutura foi organizada em:

- `terraform/bootstrap`
- `terraform/envs/dev`
- `terraform/modules/*`

---

# 5. Backend remoto do Terraform

A solução utiliza backend remoto com:

- S3 para state
- `use_lockfile = true`

O bucket do backend precisa existir antes da inicialização do ambiente `dev`.

---

# 6. Inicialização do Terraform

## 6.1 Bootstrap
Na pasta `terraform/bootstrap` :

```bash
terraform init
terraform plan
terraform apply
```

## 6.2 Ambiente dev
Na pasta `terraform/envs/dev`:

```bash
terraform init
```

---

# 7. Fase 1 — criar base para publicar imagem

Na primeira etapa, são provisionados apenas os recursos mínimos para permitir o push da imagem e a preparação inicial do ambiente:

- rede
- security groups
- ECR
- secret da TheCatAPI

Comandos:

```bash
terraform fmt -recursive
terraform validate
terraform plan \
  -target=module.network \
  -target=module.security \
  -target=module.ecr \
  -target=module.thecatapi_secret

terraform apply \
  -target=module.network \
  -target=module.security \
  -target=module.ecr \
  -target=module.thecatapi_secret
```

---

# 8. Preencher o secret da TheCatAPI

Após a criação do secret, é necessário preencher o valor real da API key no AWS Secrets Manager.

Isso pode ser feito:

- pelo console da AWS;
- ou pela CLI.

Exemplo via CLI:

```bash
aws secretsmanager put-secret-value \
  --secret-id cat-api-case-dev-thecatapi-key \
  --secret-string "SUA_API_KEY_AQUI"
```

---

# 9. Build da imagem Docker

Na raiz do projeto:

```bash
mvn clean package -DskipTests
docker build --no-cache -t cat-api-service .
```

---

# 10. Publicar imagem no ECR

## 10.1 Login no ECR

Exemplo:

```bash
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com
```

---

## 10.2 Tag da imagem

```bash
docker tag cat-api-service:latest <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/<REPOSITORY_NAME>:latest
```

---

## 10.3 Push da imagem

```bash
docker push <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/<REPOSITORY_NAME>:latest
```

---

# 11. Fase 2 — criar restante da infraestrutura

Depois da imagem já estar publicada no ECR, provisionar a infraestrutura completa:

```bash
terraform fmt -recursive
terraform validate
terraform plan
terraform apply
```

Nesta etapa entram os recursos como:

- RDS
- DynamoDB
- SQS + DLQ
- IAM
- ALB
- CloudWatch
- SES
- ECS

---

# 12. Profile utilizado na AWS

No ECS, a aplicação roda com o profile:

```text
prod
```

Isso faz com que o Spring carregue:

- `application.yml`
- `application-prod.yml`

---

# 13. Variáveis e secrets utilizados no ECS

## Variáveis de ambiente
- `SPRING_PROFILES_ACTIVE=prod`
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `AWS_REGION`
- `SQS_QUEUE_NAME`
- `DYNAMODB_TABLE_NAME`
- `SES_SENDER_EMAIL`

## Secrets
- `DB_PASSWORD` vindo do secret do RDS
- `CAT_API_KEY` vindo do secret da TheCatAPI

---

# 14. Health check na AWS

O ALB utiliza como health check:

```text
/actuator/health
```

A task ECS também foi preparada para health check interno com `curl` na própria aplicação.

---

# 15. Validação após deploy

Após o `apply`, validar na seguinte ordem:

## 15.1 ECS
- cluster criado
- service criado
- task running

## 15.2 CloudWatch Logs
- logs no log group:

```text
/ecs/cat-api-case-dev-app
```

## 15.3 ALB / Target Group
- target saudável
- status `healthy`

## 15.4 Health check
```text
GET http://<ALB_DNS>/actuator/health
```

## 15.5 Endpoints síncronos
Validar listagens e filtros.

## 15.6 Endpoint assíncrono
Validar:
- publicação no SQS;
- consumo;
- envio via SES;
- registro no DynamoDB.

---

# 16. Observação sobre SES

Durante os testes, o SES foi utilizado com identidade verificada.

Se a conta estiver em sandbox:
- o remetente precisa estar verificado;
- os destinatários usados no teste também precisam estar verificados.

---

# 17. Collection Postman

A collection do Postman utiliza variáveis de ambiente para evitar fixar endpoints específicos no repositório.

Exemplo:
- ambiente `local` apontando para `http://localhost:8080`
- ambiente `prod` apontando para o DNS do ALB criado na AWS

Assim, o mesmo conjunto de requests pode ser usado tanto localmente quanto no ambiente publicado.

---

# 18. Destruição do ambiente

Para evitar custos desnecessários após os testes:

```bash
terraform destroy
```

---

# 19. Estratégia adotada no case

A estratégia de deploy escolhida para a entrega foi:

- infraestrutura provisionada com Terraform;
- criação inicial de recursos essenciais com `-target`;
- build e push da imagem realizados manualmente no ECR;
- deploy da aplicação em ECS Fargate;
- validação via ALB, CloudWatch, SQS, DynamoDB e SES.

Como evolução futura, esse fluxo pode ser automatizado com CI/CD.