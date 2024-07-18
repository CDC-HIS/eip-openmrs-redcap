CREATE DATABASE IF NOT EXISTS `eip_openmrs_redcap_mgt` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `eip_openmrs_redcap_mgt`;

CREATE USER 'eip_openmrs_redcap'@'localhost' IDENTIFIED BY 'password';
CREATE USER 'eip_openmrs_redcap'@'%' IDENTIFIED BY 'password';

GRANT ALL PRIVILEGES ON eip_openmrs_redcap_mgt.* TO 'eip_openmrs_redcap'@'localhost';
GRANT ALL PRIVILEGES ON eip_openmrs_redcap_mgt.* TO 'eip_openmrs_redcap'@'%';
FLUSH PRIVILEGES;
