ALTER TABLE test_result ADD COLUMN is_skip BIT(1) NOT NULL;
UPDATE test_result SET is_skip = TRUE, is_failure = FALSE WHERE name LIKE 'Skip';

ALTER TABLE test_run_stats ADD COLUMN skipped INT(11) DEFAULT NULL;
UPDATE test_run_stats SET skipped = 0;

INSERT INTO queued_test_run_stat (version, date_created, test_run_id) SELECT 0, CURDATE() + INTERVAL 2 DAY, id FROM test_run tr;
