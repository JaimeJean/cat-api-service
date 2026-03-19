# Monitoring

## Objetivo

Este documento descreve a estratégia de monitoramento e observabilidade da aplicação, com foco em:

- centralização de logs no AWS CloudWatch;
- investigação rápida de falhas da aplicação;
- acompanhamento do fluxo assíncrono;
- detecção de falhas recorrentes via alarmes.

---

# 1. Visão geral

A aplicação envia seus logs para o **AWS CloudWatch Logs** por meio da configuração `awslogs` no ECS.

Os logs permitem acompanhar:

- inicialização da aplicação;
- ingestão inicial da TheCatAPI;
- execução dos endpoints síncronos;
- fluxo assíncrono via SQS;
- envio de e-mails via SES;
- falhas de processamento e exceções.

Log group utilizado no ambiente AWS:

```text
/ecs/cat-api-case-dev-app
```

---

# 2. Queries do CloudWatch Logs Insights

As queries abaixo foram definidas para dar visibilidade operacional à aplicação sem excesso de complexidade.

## 2.1 Erros e falhas gerais

Utilizada para encontrar exceções, falhas de integração e erros inesperados.

```sql
fields @timestamp, @message
| filter @message like /ERROR|Exception|Failed/
| sort @timestamp desc
| limit 50
```

### Uso
- investigar erros do backend;
- encontrar stacktraces;
- identificar falhas em integrações externas.

---

## 2.2 Fluxo assíncrono completo

Utilizada para acompanhar o caminho da requisição assíncrona desde o recebimento do POST até o envio da notificação.

```sql
fields @timestamp, @message
| filter @message like /POST \/api\/v1\/async\/breeds|Message published to SQS|Message received from SQS|Processing async request|Query executed|Notification sent successfully via SES|Failed to send notification/
| sort @timestamp desc
| limit 100
```

### Uso
- validar se a request async entrou no sistema;
- confirmar publicação no SQS;
- confirmar consumo da mensagem;
- verificar resultado da busca;
- verificar envio bem-sucedido ou falha no SES.

---

## 2.3 Envio de notificações via SES

Utilizada para focar somente no envio de e-mails.

```sql
fields @timestamp, @message
| filter @message like /Sending notification via SES|Notification sent successfully via SES|Failed to send notification via SES|MessageRejectedException/
| sort @timestamp desc
| limit 50
```

### Uso
- validar que o SES foi chamado;
- diferenciar sucesso e falha de envio;
- identificar rejeição por destinatário não verificado.

---

# 3. Alarmes configurados

## 3.1 DLQ com mensagens visíveis

### Objetivo
Detectar falhas recorrentes no processamento assíncrono.

### Sinal monitorado
Fila DLQ com mensagens visíveis maior que zero.

### Interpretação
Se esse alarme disparar, significa que alguma mensagem excedeu o número máximo de tentativas na fila principal e foi desviada para a dead-letter queue.

---

## 3.2 Falha na ingestão inicial da TheCatAPI

### Objetivo
Detectar falha crítica no bootstrap de dados da aplicação.

### Sinal monitorado
Mensagem de log:

```text
TheCatAPI retornou lista vazia de raças — ingestão abortada
```

### Implementação
Esse cenário é monitorado por:
- **CloudWatch Log Metric Filter**
- **CloudWatch Alarm** baseado na métrica extraída do log

### Interpretação
Se esse alarme disparar, significa que a aplicação falhou ao carregar a base inicial de raças no startup.

---

# 4. Como validar os logs no console da AWS

## 4.1 Abrir o CloudWatch Logs Insights

1. Acessar o serviço **CloudWatch**
2. Abrir **Logs Insights**
3. Selecionar o log group:

```text
/ecs/cat-api-case-dev-app
```

4. Escolher o intervalo de tempo desejado
5. Colar a query
6. Executar em **Run query**

---

# 5. Como validar os alarmes

## 5.1 DLQ

### Forma prática de teste
Enviar uma request assíncrona para um e-mail não verificado no SES enquanto a conta estiver em sandbox.

### Comportamento esperado
- o envio via SES falha;
- a mensagem volta para a fila;
- após o número máximo de tentativas, vai para a DLQ;
- o alarme da DLQ pode disparar.

---

## 5.2 Falha de ingestão inicial

### Forma prática de teste
Esse cenário não foi forçado durante a validação final do case, mas o alarme está preparado para detectar a ocorrência caso a TheCatAPI retorne lista vazia no startup da aplicação.

---

# 6. Leitura operacional do ambiente

Com esse conjunto de logs e alarmes, a solução passa a ter visibilidade sobre:

- falhas gerais da aplicação;
- trilha do processamento assíncrono;
- envios e falhas de e-mail;
- mensagens problemáticas na DLQ;
- falha crítica na ingestão inicial de dados.

---

# 7. Melhorias futuras

Como evolução futura, a observabilidade pode ser expandida com:

- dashboard dedicado no CloudWatch;
- rastreamento de entrega/bounce/complaint do SES;
- métricas de negócio customizadas;
- notificações automáticas dos alarmes via SNS ou EventBridge.