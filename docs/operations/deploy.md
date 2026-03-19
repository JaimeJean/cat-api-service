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

```powershell
mvn clean package
```

Se necessário pular testes:

```powershell
mvn clean package -DskipTests
```

---

## 1.4 Subir o ambiente local

```powershell
docker compose up --build
```

Se quiser forçar rebuild completo sem cache da imagem:

```powershell
docker compose build --no-cache
docker compose up
```

Se quiser limpar volumes e subir do zero:

```powershell
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
  "email": "usuario@dominio.com",
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

Essa abordagem evita comentar e descomentar módulos do Terraform e reduz risco de erro humano durante o processo.

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
Na pasta de bootstrap:

```powershell
terraform init
terraform plan
terraform apply
```

## 6.2 Ambiente dev
Na pasta `terraform/envs/dev`:

```powershell
terraform init
```

---

# 7. Fase 1 — criar base para publicar imagem

Na primeira etapa, são provisionados os recursos mínimos necessários para:

- preparar a rede;
- criar os security groups;
- criar o repositório ECR;
- criar o secret da TheCatAPI.

Essa etapa existe para garantir que:
- a imagem possa ser publicada no ECR antes da criação do ECS;
- o secret necessário pela aplicação já exista antes do deploy completo.

## 7.1 Quando a secret ainda não existe

Se a secret da TheCatAPI **ainda não existir**, ela deve ser criada já nesta fase.

### Plan da fase 1

```powershell
terraform plan -target="module.network" -target="module.security" -target="module.ecr" -target="module.thecatapi_secret"
```

### Apply da fase 1

```powershell
terraform apply -target="module.network" -target="module.security" -target="module.ecr" -target="module.thecatapi_secret"
```

## 7.2 Quando a secret já existe fora do state

Se a secret da TheCatAPI **já existir** na AWS, mas não estiver no state atual do Terraform, o ideal é:

1. importá-la para o state antes do apply;
2. ou, se necessário, mantê-la fora dessa execução específica.

Exemplo de import:

```powershell
terraform import module.thecatapi_secret.aws_secretsmanager_secret.this "<SECRET_ARN>"
```

Depois disso, o `terraform apply` pode ser executado normalmente.

---

# 8. Preencher o valor da API key no Secrets Manager

Após a criação da secret, é necessário preencher o valor real da API key da TheCatAPI no AWS Secrets Manager.

Isso pode ser feito:

- pelo console da AWS;
- ou pela CLI.

Exemplo via CLI:

```powershell
aws secretsmanager put-secret-value --secret-id <THECATAPI_SECRET_NAME> --secret-string "SUA_API_KEY_AQUI"
```

## 8.1 Observação importante

A criação do recurso Secret no Terraform **não preenche automaticamente** o valor da chave.  
Por isso, esse passo é obrigatório antes da fase completa do deploy, caso a aplicação dependa da variável `CAT_API_KEY`.

## 8.2 Resultado esperado ao final da fase 1

Ao concluir a fase 1, o ambiente já deve ter:

- rede criada;
- security groups criados;
- repositório ECR criado;
- secret da TheCatAPI criado;
- valor real da API key salvo no Secrets Manager.

Com isso, a imagem já pode ser publicada no ECR e o restante da infraestrutura pode ser provisionado na fase 2.

---

# 9. Build da imagem Docker

Na raiz do projeto:

```powershell
mvn clean package -DskipTests
docker build --no-cache -t cat-api-service .
```

---

# 10. Publicar imagem no ECR

## 10.1 Login no ECR

Utilize o domínio do registry da sua conta AWS na região onde o repositório foi criado.

```powershell
aws ecr get-login-password --region <AWS_REGION> | docker login --username AWS --password-stdin <ACCOUNT_ID>.dkr.ecr.<AWS_REGION>.amazonaws.com
```

---

## 10.2 Tag da imagem

A imagem local criada no build pode ter um nome como:

```text
cat-api-service:latest
```

Ela deve ser tagueada com a **URI real do repositório ECR**.

Essa URI pode ser obtida:
- pelo console da AWS;
- pelo output do Terraform, por exemplo `ecr_repository_url`.

Exemplo:

```powershell
docker tag cat-api-service:latest <ECR_REPOSITORY_URL>:latest
```

---

## 10.3 Push da imagem

Depois de taguear a imagem com a URI correta do repositório, faça o push:

```powershell
docker push <ECR_REPOSITORY_URL>:latest
```

---

## 10.4 Observação importante

O nome da imagem local e o nome do repositório remoto não precisam ser iguais.

Exemplo:
- imagem local: `cat-api-service:latest`
- repositório remoto: `<ECR_REPOSITORY_URL>:latest`

O que importa para o push é utilizar a **URI exata do repositório ECR**.

---

# 11. Fase 2 — criar restante da infraestrutura

Depois da imagem já estar publicada no ECR, provisionar a infraestrutura completa.

Se o secret da TheCatAPI já existir e não estiver sendo criado nessa execução, ele pode ficar fora do comando com `-target`.

## 11.1 Apply da fase 2 sem recriar o secret

```powershell
terraform apply -target="module.dynamodb" -target="module.sqs" -target="module.rds" -target="module.iam" -target="module.alb" -target="module.observability" -target="module.ses" -target="module.ecs"
```

## 11.2 Apply completo

Se todos os recursos necessários estiverem presentes no state, inclusive o secret, também é possível executar:

```powershell
terraform plan
terraform apply
```

Nesta etapa entram recursos como:

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

A task ECS também pode utilizar health check interno com `curl` na própria aplicação, desde que a imagem contenha esse binário.

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
/ecs/<project_name>-<environment>-app
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

Durante os testes, o SES foi utilizado com identidades verificadas.

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

```powershell
terraform destroy
```

Se houver recursos que devam ser preservados, como um secret já existente fora do state, eles não devem estar sob gerenciamento ativo do Terraform no momento do destroy.

---

# 19. Estratégia adotada no case

A estratégia de deploy escolhida para a entrega foi:

- infraestrutura provisionada com Terraform;
- criação inicial de recursos essenciais com `-target`;
- build e push da imagem realizados manualmente no ECR;
- deploy da aplicação em ECS Fargate;
- validação via ALB, CloudWatch, SQS, DynamoDB e SES.

Como evolução futura, esse fluxo pode ser automatizado com CI/CD.