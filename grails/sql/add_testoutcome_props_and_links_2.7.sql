 CREATE TABLE `test_outcome_link` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `description` varchar(255) NOT NULL,
  `test_outcome_id` bigint(20) NOT NULL,
  `url` varchar(255) NOT NULL,
  `links_idx` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_LINK_OUTCOME` (`test_outcome_id`),
  CONSTRAINT `FK_LINK_OUTCOME` FOREIGN KEY (`test_outcome_id`) REFERENCES `test_outcome` (`id`)
);


 CREATE TABLE `test_outcome_property` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  `value` varchar(255) NOT NULL,
  `test_outcome_id` bigint(20) NOT NULL,
  `test_properties_idx` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_PROP_OUTCOME` (`test_outcome_id`),
  CONSTRAINT `FK_PROP_OUTCOME` FOREIGN KEY (`test_outcome_id`) REFERENCES `test_outcome` (`id`)
);