ALTER TABLE test_outcome ADD COLUMN test_output_summary varchar(255) null;

UPDATE test_outcome testout INNER JOIN test_result tresult ON (testout.test_result_id = tresult.id)
  SET testout.test_output_summary = CONVERT(SUBSTRING(testout.test_output, 1, LOCATE('\n', testout.test_output) - 1), char(255))
  WHERE tresult.is_failure = TRUE AND testout.test_output IS NOT NULL AND LENGTH(testout.test_output) > 0;
