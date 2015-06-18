-- Add TestCase.analysisCount
ALTER TABLE `test_case` ADD COLUMN `analysis_count` int(11);

-- Initialize TestCase.analysisCount
-- create temporary table
CREATE TABLE tmp_analysis_count
AS SELECT tout.test_case_id, count(*) as ct
FROM test_outcome tout, analysis_state anlz
WHERE anlz.is_analyzed IS TRUE AND tout.analysis_state_id = anlz.id
GROUP BY tout.test_case_id;

-- create temporary index
CREATE INDEX tmpidx_test_case_analysis_count ON tmp_analysis_count(test_case_id,ct);

-- initialize analysis_count
UPDATE test_case tc SET tc.analysis_count =
(select ct from tmp_analysis_count where tmp_analysis_count.test_case_id = tc.id);

-- clean up temporary table
DROP TABLE tmp_analysis_count;


-- Add FK for test_outcome.test_outcome_stats_id
alter table test_outcome add index FK715540A57DFC5DE8 (test_outcome_stats_id), add constraint FK715540A57DFC5DE8
foreign key (test_outcome_stats_id) references test_outcome_stats (id);


-- Add TestRun.allowPurge
ALTER TABLE `test_run` ADD COLUMN `allow_purge` bit(1) default true;

-- Add TestRunStats.successRateChange
ALTER TABLE `test_run_stats` ADD COLUMN `success_rate_change` decimal(19,2);
