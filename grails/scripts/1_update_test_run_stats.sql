ALTER TABLE test_run_stats DROP COLUMN package_name;
DELETE FROM test_run_stats WHERE id NOT IN (SELECT test_run_statistics_id FROM test_run);
