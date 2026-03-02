-- FulfillFlow PostgreSQL initialization.
-- Creates one logical database and a dedicated role per service so that each
-- service owns its data. Runs once when the Postgres data volume is empty.
-- All values are development-only / synthetic.
-- Note: database creation must run outside a transaction block, so the actual
-- CREATE DATABASE statements are issued by the companion init-databases.sh
-- script. This file sets up roles and grants only.

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'order_service') THEN
    CREATE ROLE order_service LOGIN PASSWORD 'order-dev-password';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'inventory_service') THEN
    CREATE ROLE inventory_service LOGIN PASSWORD 'inventory-dev-password';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'delivery_service') THEN
    CREATE ROLE delivery_service LOGIN PASSWORD 'delivery-dev-password';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'notification_service') THEN
    CREATE ROLE notification_service LOGIN PASSWORD 'notification-dev-password';
  END IF;
END
$$;

-- Per-service schemas within the default DB are not used; each service gets its
-- own database (created by init-databases.sh). Grants are applied there, after
-- the databases exist.
