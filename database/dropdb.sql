-- **************************************************************
-- This script destroys the database so it can be recreated.
-- **************************************************************

-- Terminate any other active connections to the database so that it can be dropped
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'hospital_db' AND pid <> pg_backend_pid();

-- ⚠️ This deletes all data in hospital_db
DROP DATABASE IF EXISTS hospital_db;

-- NOTE: never drop the 'postgres' superuser here - the application and this script connect with it.
