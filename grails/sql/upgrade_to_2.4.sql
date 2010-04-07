
-- normalize duration to milliseconds

UPDATE test_outcome tout INNER JOIN test_case tc
   ON tout.test_case_id = tc.id
   INNER JOIN project
   ON tc.project_id = project.id
   INNER JOIN test_type tt
   ON project.test_type_id = tt.id
SET tout.duration = tout.duration * 1000
WHERE tt.name like 'JUnit';

ALTER TABLE test_outcome MODIFY duration bigint(20);

UPDATE test_run trun INNER JOIN project
   ON trun.project_id = project.id
   INNER JOIN test_type tt
   ON project.test_type_id = tt.id
   INNER JOIN test_run_stats trs
   ON trun.test_run_statistics_id
SET trs.total_duration = trs.total_duration * 1000,
    trs.average_duration = trs.average_duration * 1000
WHERE tt.name LIKE 'JUnit';

ALTER TABLE test_run_stats MODIFY total_duration bigint(20);
ALTER TABLE test_run_stats MODIFY average_duration bigint(20);
ALTER TABLE test_case MODIFY parameters varchar(1024);
UPDATE test_result SET include_in_calculations=true, is_failure=true WHERE name like 'Skip';

