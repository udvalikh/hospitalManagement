-- ********************************************************************************
-- This script creates the database users and grants them the necessary permissions
-- ********************************************************************************

GRANT ALL
ON ALL TABLES IN SCHEMA public
TO postgres;

GRANT ALL
ON ALL SEQUENCES IN SCHEMA public
TO postgres;

-- If needed, also grant privileges on functions
GRANT ALL
ON ALL FUNCTIONS IN SCHEMA public
TO postgres;
