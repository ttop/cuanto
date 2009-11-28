USE cuanto;

ALTER TABLE test_case ADD COLUMN parameters VARCHAR(255) DEFAULT NULL;

UPDATE test_case SET full_name = CONCAT(full_name, '()') WHERE full_name NOT LIKE "%)";
