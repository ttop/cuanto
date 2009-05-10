use cuanto;

-- add the test_type_id column to project
ALTER TABLE project ADD test_type_id BIGINT( 20 );

-- populate the new column with the test_type_id from the projects' test_case; grab the largest value available
UPDATE project p SET p.test_type_id = (
	SELECT tc.test_type_id 
	FROM  test_case tc 
	WHERE p.id = tc.project_id 
	ORDER BY tc.test_type_id ASC 
	LIMIT 1
);

-- if a project has no test cases, then assume that the test_type is 1
UPDATE project p SET P.test_type_id = 1 WHERE p.test_type_id = 0 OR p.test_type_id IS NULL;

-- make the new column NOT NULL
ALTER TABLE project CHANGE test_type_id test_type_id BIGINT( 20 ) NOT NULL;

-- add a foreign key on the new column
-- when you add a foreign key mysql will also create an index
-- so we'll give the index a name here
ALTER TABLE project
  ADD CONSTRAINT `FK_PROJECT_TEST_TYPE_ID`
    FOREIGN KEY `IDX_PROJECT_TEST_TYPE_ID` (test_type_id)
    REFERENCES test_type(id);

-- remove the tst_type_id foreign key on test_case
ALTER TABLE test_case DROP FOREIGN KEY FKB9A0FABDEFE6ED33;

-- drop unnecessary columns
ALTER TABLE test_case DROP test_type_id;
ALTER TABLE project DROP has_manual_test_cases;
