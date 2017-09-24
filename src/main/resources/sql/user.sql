/*
Navicat MySQL Data Transfer

Source Server         : localhost_3306
Source Server Version : 50540
Source Host           : localhost:3306
Source Database       : mylevel

Target Server Type    : MYSQL
Target Server Version : 50540
File Encoding         : 65001

Date: 2017-09-25 07:15:19
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(200) DEFAULT NULL,
  `password` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('1', 'admin', '1');
INSERT INTO `user` VALUES ('2', 'admin', '1');
INSERT INTO `user` VALUES ('3', 'admin', '1');
INSERT INTO `user` VALUES ('4', 'admin', '1');
INSERT INTO `user` VALUES ('5', 'admin', '1');
INSERT INTO `user` VALUES ('6', 'admin', '1');
INSERT INTO `user` VALUES ('7', 'admin', '1');