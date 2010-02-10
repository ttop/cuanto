ALTER TABLE test_case MODIFY full_name varchar(1024);
ALTER TABLE test_case ADD INDEX full_name_idx (full_name);
ALTER TABLE test_case MODIFY package_name varchar(1024);

ALTER TABLE test_outcome MODIFY test_run_id bigint(20) DEFAULT NULL;
