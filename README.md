# gcp-weekly-report-function

## Descrição
A **Weekly Report Function** é uma **Cloud Function do GCP** que gera um **relatório semanal de feedbacks das aulas** e envia para os administradores cadastrados.

Ela é acionada **automaticamente pelo Cloud Scheduler** toda semana, publica uma mensagem no tópico **Pub/Sub** `weekly-feedback-reports`, que dispara a função. A função então:

1. Decodifica a mensagem recebida.
2. Busca os feedbacks da semana usando o serviço de Feedback.
3. Gera uma tabela HTML com as aulas mais avaliadas e mais populares.
4. Envia o relatório por email para todos os administradores.

---

## Fluxo da Função

![Weekly Report Function](weekly_report.png)

---

## Stack / Tecnologias Usadas
- **Linguagem:** Java 17
- **Frameworks:** Google Cloud Functions Framework
- **Bibliotecas adicionais:**
    - Gson (JSON parsing)
    - Jakarta Mail (envio de emails)
- **Serviços GCP:**
  - Cloud Functions (execução da função)
  - Pub/Sub (trigger de mensagens)
  - Cloud Scheduler (agendamento semanal)
  - IAM / Service Accounts (permissões)
    - Cloud Logging (logs de execução)

---

## Pré-requisitos
- Conta GCP ativa e projeto configurado (fiap-adj8-feedback-platform)
- Java 17 instalado
- Google Cloud SDK (gcloud) instalado e autenticado
- Maven 3+ para build da aplicação

---

## Estrutura de Arquivos

```text
gcp-weekly-report-function/
├── src/main/java/fiap_adj8/feedback_platform/application/port/out/client/FeedbackServiceClientPort.java
├── src/main/java/fiap_adj8/feedback_platform/application/port/out/email/EmailSender.java
├── src/main/java/fiap_adj8/feedback_platform/application/port/out/email/input/EmailInput.java
├── src/main/java/fiap_adj8/feedback_platform/application/port/out/template/TemplateProvider.java
├── src/main/java/fiap_adj8/feedback_platform/domain/model/PubSubMessage.java
├── src/main/java/fiap_adj8/feedback_platform/infra/adapter/in/WeeklyReportFunction.java
├── src/main/java/fiap_adj8/feedback_platform/infra/adapter/out/client/FeedbackServiceClientAdapter.java
├── src/main/java/fiap_adj8/feedback_platform/infra/adapter/out/client/dto/LessonSummary.java
├── src/main/java/fiap_adj8/feedback_platform/infra/adapter/out/email/JakartaMailSender.java
├── src/main/java/fiap_adj8/feedback_platform/infra/adapter/out/template/TemplateLoader.java
├── src/main/java/fiap_adj8/feedback_platform/infra/helper/LessonSummaryTableBuilder.java
├── src/main/resources/templates/weekly-report.html
├── pom.xml
├── deploy.sh
└── README.md
```

## Permissões Necessárias

### Service Account de Deploy: sa-deploy-weekly-report

- roles/cloudfunctions.developer
- roles/pubsub.admin
- roles/cloudscheduler.admin
- roles/logging.viewer
- roles/storage.admin

### Service Account de Runtime: sa-runtime-weekly-report

- roles/pubsub.subscriber
- roles/logging.logWriter

Essas permissões permitem que a função execute corretamente, leia mensagens do Pub/Sub, gere relatórios, envie emails e registre logs.

---

## Deploy

Para realizar o deploy da função semanal:

1. **Certifique-se que a Service Account de deploy está autenticada**:
```bash
gcloud auth activate-service-account sa-deploy-weekly-report@fiap-adj8-feedback-platform.iam.gserviceaccount.com --key-file=/path/to/key.json
gcloud config set project fiap-adj8-feedback-platform
```