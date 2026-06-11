#!/bin/bash
set -e

MOK_PASS=$(cat /run/secrets/mok_password)
MPP_PASS=$(cat /run/secrets/mpp_password)

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER $MOK_USER WITH PASSWORD '$MOK_PASS';
    CREATE USER $MPP_USER WITH PASSWORD '$MPP_PASS';
    CREATE ROLE repuser WITH REPLICATION LOGIN;
    ALTER SYSTEM SET wal_level = 'replica';
    ALTER SYSTEM SET max_wal_senders = '5';
    ALTER SYSTEM SET wal_keep_size = '1GB';
    ALTER SYSTEM SET synchronous_commit = 'on';
    ALTER SYSTEM SET synchronous_standby_names = 'ANY 1 ("db-replica-1", "db-replica-2")';
    ALTER SYSTEM SET log_statement = 'mod';
    ALTER SYSTEM SET log_min_duration_statement = 0;
EOSQL

echo "host replication repuser 0.0.0.0/0 trust" >> "$PGDATA/pg_hba.conf"

psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT pg_reload_conf();"