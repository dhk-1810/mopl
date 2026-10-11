#!/bin/bash
# ==============================================================================
# Debezium Outbox 커넥터 자동 등록 스크립트
# ==============================================================================

CONNECT_URL="http://localhost:8089"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONNECTOR_CONFIG="${SCRIPT_DIR}/content-outbox-connector.json"

echo "⏳ Kafka Connect (${CONNECT_URL}) 헬스체크 대기 중..."

# Kafka Connect 서비스가 응답(HTTP 200)할 때까지 대기
while true; do
  STATUS_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${CONNECT_URL}/connectors" 2>/dev/null)
  if [ "$STATUS_CODE" -eq 200 ]; then
    echo " Kafka Connect 서버가 정상적으로 실행 중입니다."
    break
  fi
  echo "  - Connect 서버 기동 대기 중 (HTTP 응답 코드: ${STATUS_CODE:-연결실패})... 3초 후 재시도"
  sleep 3
done

echo ""
echo "🚀 [content-outbox-connector] 등록 요청 시작..."

# JSON 파일에서 주석(// 로 시작하는 키) 제거 후 REST API 요청 전송
CLEANED_JSON=$(grep -v '"//' "${CONNECTOR_CONFIG}")

RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  --data "${CLEANED_JSON}" \
  "${CONNECT_URL}/connectors")

echo "📩 서버 응답:"
echo "${RESPONSE}" | grep -q "error_code" && echo "⚠️ 오류 발생: ${RESPONSE}" || echo " 커넥터 등록 완료!"
echo ""

echo "🔍 현재 등록된 커넥터 목록:"
curl -s "${CONNECT_URL}/connectors"
echo ""
