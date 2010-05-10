ALTER TABLE test_outcome ADD COLUMN test_output_summary longtext null;

UPDATE test_outcome testout INNER JOIN test_result tresult ON (testout.test_result_id = tresult.id) 
  SET testout.test_output_summary = SUBSTRING(testout.test_output, 1, LOCATE('\n', testout.test_output) - 1)
  where tresult.is_failure = true and testout.test_output is not null and length(testout.test_output) > 0;
