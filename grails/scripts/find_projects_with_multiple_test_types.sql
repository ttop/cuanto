-- Find projects whose test cases have more than one test_type.
-- NOTE: this should be ran before move_test_type_to_project.sql to avoid any unexpected data.

SELECT * FROM project p WHERE 1 < (
    SELECT COUNT(DISTINCT tc.test_type_id) FROM test_case tc
    WHERE tc.project_id = p.id
)