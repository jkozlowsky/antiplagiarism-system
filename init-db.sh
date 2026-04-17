#!/bin/bash
set -e

MOK_PASS=$(cat /run/secrets/mok_password)
MPP_PASS=$(cat /run/secrets/mpp_password)

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER $MOK_USER WITH PASSWORD '$MOK_PASS';
    CREATE USER $MPP_USER WITH PASSWORD '$MPP_PASS';
EOSQL
