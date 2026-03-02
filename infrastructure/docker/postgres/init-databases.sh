#!/bin/bash
# Creates one logical database per FulfillFlow service, owned by its dedicated
# role. Runs after init.sql (which creates the roles). PostgreSQL init scripts
# run in alphabetical order, so this (init-databases.sh) runs after init.sql.
# CREATE DATABASE cannot run inside a transaction, so we use psql directly.
set -eu

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
  SELECT 'CREATE DATABASE fulfillflow_order OWNER order_service'
  WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'fulfillflow_order')\gexec
  SELECT 'CREATE DATABASE fulfillflow_inventory OWNER inventory_service'
  WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'fulfillflow_inventory')\gexec
  SELECT 'CREATE DATABASE fulfillflow_delivery OWNER delivery_service'
  WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'fulfillflow_delivery')\gexec
  SELECT 'CREATE DATABASE fulfillflow_notification OWNER notification_service'
  WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'fulfillflow_notification')\gexec
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
  GRANT ALL PRIVILEGES ON DATABASE fulfillflow_order TO order_service;
  GRANT ALL PRIVILEGES ON DATABASE fulfillflow_inventory TO inventory_service;
  GRANT ALL PRIVILEGES ON DATABASE fulfillflow_delivery TO delivery_service;
  GRANT ALL PRIVILEGES ON DATABASE fulfillflow_notification TO notification_service;
EOSQL

echo "[init-databases] created service databases:"
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" -lqt | cut -d '|' -f 1 | grep fulfillflow | sed 's/^/  /'
