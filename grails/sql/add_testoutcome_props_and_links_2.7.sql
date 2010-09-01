 CREATE TABLE `test_outcome_link` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `description` varchar(255) NOT NULL,
  `test_outcome_id` bigint(20) NOT NULL,
  `url` varchar(255) NOT NULL,
  `links_idx` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

 CREATE TABLE `test_outcome_property` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  `value` varchar(255) NOT NULL,
  `test_outcome_id` bigint(20) NOT NULL,
  `test_properties_idx` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

ALTER TABLE test_outcome_property ADD INDEX outcome_prop_name_index (name);
ALTER TABLE test_outcome_property ADD INDEX outcome_prop_value_index (value);
