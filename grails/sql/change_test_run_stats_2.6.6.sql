ALTER TABLE test_run_stats ADD COLUMN test_run_id bigint(20);
UPDATE test_run_stats trs, test_run tr SET trs.test_run_id = tr.id WHERE tr.test_run_statistics_id = trs.id;

DELETE FROM analysis_statistic USING test_run_stats INNER JOIN analysis_statistic
	WHERE test_run_stats.id = analysis_statistic.test_run_stats_id AND test_run_stats.test_run_id IS NULL;
DELETE FROM test_run_stats WHERE test_run_id IS NULL; 

ALTER TABLE test_run_stats ADD INDEX test_run_idx (test_run_id);

-- replace FKNAME with fkname shown for 'test_run_statistics_id' when executing 'show create table test_run;'
ALTER TABLE test_run DROP FOREIGN KEY FKNAME;
ALTER TABLE test_run DROP COLUMN test_run_statistics_id;
