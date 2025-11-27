#!/bin/bash
set -e

########################################
# CONFIGURAÇÕES
########################################
FUNCTION_NAME="weekly-report"
TOPIC_NAME="weekly-feedback-reports"
SCHEDULER_JOB_NAME="weekly-feedback-report-job"
REGION="us-central1"
ENTRY_POINT="fiap_adj8.feedback_platform.infra.adapter.in.WeeklyReportFunction"
TIME_ZONE="America/Sao_Paulo"
CRON_EXPRESSION="0 0 * * 0"  # Domingo às 00:00
SERVICE_ACCOUNT="sa-deploy-weekly-report@fiap-adj8-feedback-platform.iam.gserviceaccount.com"
RUNTIME="java17"
MEMORY="512MB"
TIMEOUT="60s"

SA_KEY_PATH="$HOME/gcp-keys/sa-deploy-weekly-report-key.json"
PROJECT_ID="fiap-adj8-feedback-platform"
echo "🔐 Autenticando com Service Account de Infra..."
gcloud auth activate-service-account --key-file="$SA_KEY_PATH"
gcloud config set project "$PROJECT_ID"

########################################
# FUNÇÕES AUXILIARES
########################################
log() {
  echo -e "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

gcloud auth activate-service-account \
  sa-deploy-weekly-report@fiap-adj8-feedback-platform.iam.gserviceaccount.com \
  --key-file="$HOME/gcp-keys/sa-deploy-weekly-report-key.json"

########################################
# 1. Criar tópico Pub/Sub se não existir
########################################
log "🔍 Verificando se o tópico '$TOPIC_NAME' existe..."
if ! gcloud pubsub topics describe "$TOPIC_NAME" >/dev/null 2>&1; then
  log "📌 Tópico não encontrado. Criando..."
  gcloud pubsub topics create "$TOPIC_NAME" --quiet
  log "✅ Tópico '$TOPIC_NAME' criado com sucesso."
else
  log "✅ Tópico '$TOPIC_NAME' já existe."
fi

########################################
# CARREGAR VARIÁVEIS DO .env
########################################

ENV_FILE="$(dirname "$0")/.env"

if [ ! -f "$ENV_FILE" ]; then
  echo "❌ Arquivo .env não encontrado em $ENV_FILE"
  exit 1
fi

set -a
source "$ENV_FILE"
set +a

log "🔄 Gerando arquivo env.yaml para Cloud Function..."

cat > env.yaml <<EOF
FEEDBACK_SERVICE_BASE_URL: "$FEEDBACK_SERVICE_BASE_URL"
FEEDBACK_SERVICE_AUTH: "$FEEDBACK_SERVICE_AUTH"

EMAIL_SMTP_FROM: "$EMAIL_SMTP_FROM"
EMAIL_SMTP_PASSWORD: "$EMAIL_SMTP_PASSWORD"
EMAIL_SMTP_HOST: "$EMAIL_SMTP_HOST"
EMAIL_SMTP_PORT: "$EMAIL_SMTP_PORT"
EOF


########################################
# 2. Deploy / Update da Cloud Function
########################################
if gcloud functions describe "$FUNCTION_NAME" --region "$REGION" >/dev/null 2>&1; then
  log "🔄 Função $FUNCTION_NAME já existe - atualizando..."
else
  log "🚀 Criando função $FUNCTION_NAME..."
fi

gcloud functions deploy "$FUNCTION_NAME" \
  --runtime "$RUNTIME" \
  --trigger-topic "$TOPIC_NAME" \
  --entry-point "$ENTRY_POINT" \
  --region "$REGION" \
  --service-account "$SERVICE_ACCOUNT" \
  --memory "$MEMORY" \
  --timeout "$TIMEOUT" \
  --env-vars-file env.yaml \
  --quiet

log "✅ Deploy da Cloud Function concluído!"

########################################
# 3. Criar Cloud Scheduler Job se não existir
########################################
log "🔍 Verificando se o Cloud Scheduler Job '$SCHEDULER_JOB_NAME' existe..."
if ! gcloud scheduler jobs describe "$SCHEDULER_JOB_NAME" --location="$REGION" >/dev/null 2>&1; then
  log "📅 Job não encontrado. Criando agendamento semanal..."
  gcloud scheduler jobs create pubsub "$SCHEDULER_JOB_NAME" \
    --schedule="$CRON_EXPRESSION" \
    --time-zone="$TIME_ZONE" \
    --topic="$TOPIC_NAME" \
    --message-body='{"type":"WEEKLY_REPORT_TRIGGER"}' \
    --location="$REGION" \
    --env-vars-file env.yaml \
    --quiet
  log "✅ Cloud Scheduler Job criado com sucesso!"
else
  log "✅ Cloud Scheduler Job já existe."
fi

########################################
# 4. Mensagem de validação
########################################
log "📨 Enviando mensagem de validação para testar a função..."
gcloud pubsub topics publish "$TOPIC_NAME" \
  --message="{\"type\":\"DEPLOY_VALIDATION\",\"source\":\"manual-deploy\",\"timestamp\":\"$(date -Iseconds)\"}" \
  --quiet

rm -f env.yaml

log "✅ Mensagem de validação enviada!"
log "🔍 Verifique os logs com:"
echo "👉 gcloud functions logs read $FUNCTION_NAME --region $REGION --limit 50"

log "🎉 Processo completo finalizado com sucesso!"
