#!/bin/bash
set -e

FUNCTION_NAME="weekly-report"
TOPIC_NAME="weekly-feedback-reports"
SCHEDULER_JOB_NAME="weekly-feedback-report-job"
REGION="us-central1"
ENTRY_POINT="fiap_adj8.feedback_platform.infra.adapter.in.WeeklyReportFunction"
TIME_ZONE="America/Sao_Paulo"
CRON_EXPRESSION="0 0 * * 0"  # Domingo às 00:00

echo "🔍 Verificando se o tópico '$TOPIC_NAME' existe..."

if ! gcloud pubsub topics describe "$TOPIC_NAME" >/dev/null 2>&1; then
  echo "📌 Tópico não encontrado. Criando..."
  gcloud pubsub topics create "$TOPIC_NAME"
  echo "✅ Tópico '$TOPIC_NAME' criado com sucesso."
else
  echo "✅ Tópico '$TOPIC_NAME' já existe."
fi

echo "🚀 Iniciando deploy da Cloud Function..."

gcloud functions deploy "$FUNCTION_NAME" \
  --runtime java17 \
  --trigger-topic "$TOPIC_NAME" \
  --entry-point "$ENTRY_POINT" \
  --region "$REGION"

echo "✅ Deploy da Cloud Function concluído!"

echo "🔍 Verificando se o Cloud Scheduler Job '$SCHEDULER_JOB_NAME' existe..."

if ! gcloud scheduler jobs describe "$SCHEDULER_JOB_NAME" --location="$REGION" >/dev/null 2>&1; then
  echo "📅 Job não encontrado. Criando agendamento semanal..."

  gcloud scheduler jobs create pubsub "$SCHEDULER_JOB_NAME" \
    --schedule="$CRON_EXPRESSION" \
    --time-zone="$TIME_ZONE" \
    --topic="$TOPIC_NAME" \
    --message-body='{"type":"WEEKLY_REPORT_TRIGGER"}' \
    --location="$REGION"

  echo "✅ Cloud Scheduler Job criado com sucesso!"
else
  echo "✅ Cloud Scheduler Job já existe."
fi

# 🔥 ENVIO DE MENSAGEM DE VALIDAÇÃO
echo "📨 Enviando mensagem de validação para testar a função..."

gcloud pubsub topics publish "$TOPIC_NAME" \
  --message="{\"type\":\"DEPLOY_VALIDATION\",\"source\":\"manual-deploy\",\"timestamp\":\"$(date -Iseconds)\"}"

echo "✅ Mensagem de validação enviada!"
echo "🔍 Verifique os logs com:"
echo "👉 gcloud functions logs read $FUNCTION_NAME --region $REGION --limit 50"

echo "🎉 Processo completo finalizado com sucesso!"
