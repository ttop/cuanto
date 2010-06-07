ALTER TABLE test_outcome ADD COLUMN `is_failure_status_changed` bit(1) default NULL;
CREATE INDEX is_failure_status_changed_idx ON test_outcome(is_failure_status_changed);
