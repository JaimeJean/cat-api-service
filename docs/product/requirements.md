# ✅ Requisitos

## ⚙️ Requisitos Funcionais

### 📥 Ingestão de Dados

- **RF-01:** Na inicialização da aplicação, o sistema deve consumir a TheCatAPI e buscar todas as raças de gatos disponíveis.
- **RF-02:** Para cada raça, o sistema deve persistir as informações de origem, temperamento e descrição (quando disponíveis).
- **RF-03:** Para cada raça, o sistema deve buscar e persistir a URL de até 3 imagens (quando disponíveis).
- **RF-04:** O sistema deve buscar e persistir a URL de 3 imagens de gatos com chapéu (category_ids=1, sem vínculo com raça — a TheCatAPI não associa categorias com raças).
- **RF-05:** O sistema deve buscar e persistir a URL de 3 imagens de gatos com óculos (category_ids=4, sem vínculo com raça — mesma limitação da API).
- **RF-06:** A ingestão deve utilizar processamento paralelo (threads) para otimizar a coleta de dados.
- **RF-07:** A ingestão deve ser idempotente — se a aplicação reiniciar, os dados não devem ser duplicados. O sistema deve verificar a existência dos registros antes de inserir ou utilizar estratégia de upsert.
- **RF-08:** A comunicação com a TheCatAPI deve ser resiliente: configuração de timeout, retry com backoff exponencial (via Spring Retry ou Resilience4j) e tratamento adequado de falhas (API fora, lenta ou com rate limit). Estratégia escolhida por ser leve e adequada ao volume do case — em cenário produtivo de maior escala, Kafka com DLQ seria a abordagem preferida. *(ver ADR — Escolha do mecanismo de fila)*

### 🔌 APIs REST de Consulta

- **RF-09:** Todos os endpoints devem ser versionados (ex: `/api/v1/breeds`), permitindo evolução da API sem quebra de contrato com consumidores existentes.
- **RF-10:** `GET /api/v1/breeds` — Listar todas as raças cadastradas, com suporte a paginação.
- **RF-11:** `GET /api/v1/breeds/{id}` — Retornar as informações de uma raça específica (incluindo suas imagens).
- **RF-12:** `GET /api/v1/breeds/temperament/{temperament}` — Listar raças que possuam o temperamento informado, com suporte a paginação.
- **RF-13:** `GET /api/v1/breeds/origin/{origin}` — Listar raças a partir de uma origem informada, com suporte a paginação.
- **RF-14:** Pelo menos um dos endpoints acima deve utilizar processamento paralelo (threads).
- **RF-15:** Todos os endpoints devem retornar erros em formato padronizado seguindo RFC 7807 (Problem Details), garantindo consistência no contrato de erro da API.

### 🌟 Bônus — APIs Assíncronas

- **RF-16:** Endpoint único `POST /api/v1/async/breeds` com e-mail obrigatório e filtros opcionais (temperament, origin) combináveis. Sem filtros retorna todas as raças. Retorna 202 Accepted com requestId. Resultados enviados por e-mail via SES.
- **RF-17:** O processamento assíncrono utiliza SQS como fila, SES pra envio de e-mail e DynamoDB pra controle de idempotência do consumer (PutItem condicional com TTL de 7 dias). DLQ configurada com 3 tentativas. *(ver ADR — Escolha do mecanismo de fila)*

---

## 🏗️ Requisitos Não Funcionais

### 🧪 Código e Testes

- **RNF-01:** A aplicação deve ser desenvolvida em **Java 21** e **Spring Boot 3.x**.
- **RNF-02:** Cobertura mínima de 90% em testes unitários no código de negócio, verificada via JaCoCo com build falhando abaixo do threshold.
- **RNF-03:** Testes de integração para os principais fluxos da aplicação (comunicação entre camadas, contratos de APIs, persistência). *(ver ADR — Estratégia de testes)*
- **RNF-04:** Código organizado seguindo arquitetura hexagonal. *(ver ADR — Escolha da arquitetura)*
- **RNF-05:** Aplicação de princípios SOLID e Design Patterns onde apropriado.
- **RNF-06:** Padronização de formatação e estilo de código no projeto.

### 🗄️ Banco de Dados

- **RNF-07:** Os dados devem ser persistidos em PostgreSQL 16, com justificativa técnica da escolha. Gerenciamento de schema via Flyway, com migrations versionadas no repositório. *(ver ADR — Escolha do banco de dados)*

### 📡 API e Contratos

- **RNF-08:** Documentação interativa das APIs via Swagger/OpenAPI, gerada automaticamente pelo Spring, complementando a coleção Postman.
- **RNF-09:** Health check endpoint (`/actuator/health`) exposto para monitoramento de disponibilidade da aplicação e integração com checks do ALB/ECS.
- **RNF-10:** Configuração por ambiente via Spring Profiles (local e prod), separando configurações específicas de cada ambiente.

### 📊 Logging e Observabilidade

- **RNF-11:** A aplicação deve utilizar logging estruturado com níveis padronizados (INFO, WARN, ERROR, DEBUG).
- **RNF-12:** Os logs devem permitir rastreabilidade das operações de ingestão, consulta e fluxo assíncrono.
- **RNF-13:** A solução deve expor sinais mínimos de observabilidade para operação, incluindo health check, logs e alarmes operacionais básicos.
- **RNF-14 (Bônus):** Integrar logs e sinais operacionais com o AWS CloudWatch, incluindo alarmes úteis para disponibilidade da aplicação e fila assíncrona.

### ☁️ Infraestrutura e Deploy

- **RNF-15:** A aplicação deve rodar localmente via Docker/Docker Compose.
- **RNF-16:** O código deve ser versionado no GitHub.
- **RNF-17:** Estratégia de FinOps: infraestrutura provisionada e destruída sob demanda via Terraform, evitando custos desnecessários em ambiente de desenvolvimento/demonstração. *(mencionado na descrição da vaga como “Visão de custo/FinOps”)*
- **RNF-18 (Bônus):** Deploy na AWS utilizando ECS Fargate, provisionado via Terraform.
- **RNF-19 (Evolução futura):** Automatizar build, testes e deploy com GitHub Actions em uma versão posterior.

### 📄 Documentação

- **RNF-20:** README com documentação do projeto, das APIs, da arquitetura e instruções de execução local.
- **RNF-21:** Desenho arquitetural utilizando C4 Model (níveis de Contexto, Container e Componente).
- **RNF-22:** ADRs (Architecture Decision Records) para justificar decisões técnicas.
- **RNF-23:** Coleção Postman ou Insomnia para consumo das APIs.

---

## 🗺️ Mapeamento: Requisitos → Critérios de Avaliação

| Critério (20% cada) | Requisitos relacionados |
| --- | --- |
| Código e testes unitários | RNF-01, RNF-02, RNF-03, RNF-04, RNF-05, RNF-06 |
| Estrutura do projeto e organização | RNF-04, RNF-05, RNF-07, RNF-08, RNF-10, RNF-16 |
| Logging e mecanismos de monitoramento | RNF-09, RNF-11, RNF-12, RNF-13, RNF-14 |
| Documentação | RNF-08, RNF-20, RNF-21, RNF-22, RNF-23 |
| Facilidade de deploy | RNF-15, RNF-17, RNF-18 |

---

## 📝 ADRs previstos

| ADR | Decisão | Status |
| --- | --- | --- |
| ADR-001 | Escolha do banco de dados | ✅ Concluído |
| ADR-002 | Escolha da arquitetura | ✅ Concluído |
| ADR-003 | Escolha do mecanismo de fila | ✅ Concluído |
| ADR-004 | Estratégia de deploy | ✅ Concluído |
| ADR-005 | Estratégia de testes | ✅ Concluído |