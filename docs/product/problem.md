📋 Documento do Problema

## 🎯 Contexto

Case técnico para a vaga de Engenheiro de Software Pleno no Itaú Unibanco, comunidade Ativos PJ.

A proposta é criar uma aplicação em Java/Spring Boot que consome a [TheCatAPI](https://thecatapi.com/), salva os dados em um banco próprio e expõe endpoints REST pra consultar esses dados. O case avalia desde a escrita do código até o deploy, passando por testes, organização, logging e documentação.

---

## 🐱 Problema

A TheCatAPI é uma API pública que disponibiliza informações sobre raças de gatos, incluindo características como origem, temperamento e descrição, além de um acervo de imagens categorizadas.

O desafio é criar um serviço que:

1. **Ingira dados** da TheCatAPI de forma automatizada, selecionando e persistindo apenas as informações relevantes em um banco de dados próprio.
2. **Exponha APIs REST** que permitam consultar os dados já persistidos, sem dependência direta da API externa no momento da consulta.

Em resumo, a aplicação funciona como uma camada intermediária que coleta, armazena e serve dados de raças de gatos e suas imagens de forma independente da fonte original.

---

## 🔭 Escopo

### O que está dentro do escopo

- Consumo da TheCatAPI para coleta de dados de raças e imagens
- Persistência de dados em banco de dados com justificativa técnica da escolha
- Exposição de 4 APIs REST para consulta dos dados armazenados
- Aplicação de processamento paralelo (threads) em pelo menos um endpoint
- Logging estruturado com níveis adequados (INFO, WARN, ERROR, DEBUG)
- Testes unitários com cobertura mínima de 90% no código de negócio
- Containerização com Docker para execução local
- Documentação completa: projeto, APIs, arquitetura e instruções de execução
- Coleção Postman para consumo das APIs
- Desenho arquitetural da solução

### Itens bônus (dentro do escopo, mas opcionais)

- Deploy na AWS (ECS Fargate)
- Integração de logging com ferramenta externa (CloudWatch) com queries e alertas em tempo real
- Variação assíncrona das APIs: o usuário informa um e-mail e recebe os resultados via mecanismo de fila

### O que está fora do escopo

- Download ou armazenamento das imagens em si (apenas URLs são persistidas)
- Interface gráfica ou frontend
- Atualização contínua ou periódica dos dados (a ingestão é uma carga inicial única)

### Possíveis evoluções futuras

- **Autenticação/autorização nos endpoints:** implementaria um filtro com API Key para acesso geral e, em um cenário mais robusto, JWT com roles para diferenciar perfis (admin para disparar a carga, leitura para consultas). O Spring Security já suporta isso nativamente.
- **Carga incremental com agendamento periódico:** utilizaria `@Scheduled` do Spring para executar a ingestão em intervalos configuráveis. A lógica de upsert no banco evitaria duplicações, e um campo de controle (`last_updated`) permitiria processar apenas dados novos ou alterados.
- **Cache nas APIs de consulta:** adicionaria Redis como cache nos endpoints de listagem com TTL configurável (ex: 30 minutos). Como os dados mudam pouco depois da carga, o ganho de performance seria alto com baixa complexidade. A invalidação aconteceria no momento de uma nova carga de dados.

---

## 📊 Critérios de Avaliação

| Critério | Peso |
| --- | --- |
| Código e testes unitários | 20% |
| Estrutura do projeto e organização | 20% |
| Logging e mecanismos de monitoramento | 20% |
| Documentação | 20% |
| Facilidade de deploy | 20% |

> Para todos os critérios, a documentação é considerada como parte da entrega.
>

---

## 🚧 Restrições e Premissas

### Restrições

- A aplicação deve ser desenvolvida em **Java com Spring Boot**
- O código deve ser versionado no **GitHub**
- A entrega deve ocorrer até **20/03/2026**, independente do estágio de desenvolvimento

### Premissas

- A TheCatAPI estará disponível e acessível durante o desenvolvimento e a avaliação
- A chave de API (api_key) da TheCatAPI será utilizada para acesso aos endpoints que a exigem
- A carga de dados é executada uma única vez na inicialização da aplicação
- O volume de dados é pequeno (~67 raças, ~200 URLs de imagens + 6 imagens extras)