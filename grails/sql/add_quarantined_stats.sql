ALTER TABLE test_run_stats ADD COLUMN quarantined bigint(20);
UPDATE test_run_stats SET quarantined = 0 WHERE quarantined IS NULL;