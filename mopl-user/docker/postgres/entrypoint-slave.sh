#!/bin/bash
set -e

# Master DB가 준비될 때까지 대기
PGDATA="${PGDATA:-/var/lib/postgresql/data}"
echo "Waiting for master database to be ready (PGDATA: $PGDATA)..."
until pg_isready -h ${MASTER_HOST:-user-db} -p 5432 -d "${POSTGRES_DB:-mopl_user}" -U "${POSTGRES_USER:-mopl_user_user}"; do
  echo "Master DB is not ready yet. Waiting..."
  sleep 2
done

# 데이터 디렉토리가 비어있거나 초기화되지 않은 경우 Master로부터 pg_basebackup 수행
if [ ! -s "$PGDATA/PG_VERSION" ]; then
  echo "Initializing standby from master via pg_basebackup..."
  mkdir -p "$PGDATA"
  chmod 700 "$PGDATA"
  rm -rf "$PGDATA"/*
  
  export PGPASSWORD='replicator_password'
  pg_basebackup -h ${MASTER_HOST:-user-db} -p 5432 -U replicator -D "$PGDATA" -Fp -Xs -P -R
  unset PGPASSWORD

  chmod 700 "$PGDATA"
  echo "Standby initialized successfully from master."
fi

# 기본 postgres 데몬 실행
exec docker-entrypoint.sh postgres
