-- drop the version columns on TestCase and TestRun, for which optimistic locking is disabled
ALTER TABLE `test_case` DROP COLUMN `version`;
ALTER TABLE `test_run` DROP COLUMN `version`;
ALTER TABLE `queued_test_run_stat` DROP COLUMN `version`;
ALTER TABLE `test_outcome` DROP COLUMN `version`;
