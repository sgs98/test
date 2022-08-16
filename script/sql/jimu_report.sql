/*
 Navicat Premium Data Transfer

 Source Server         : 阿里云
 Source Server Type    : MySQL
 Source Server Version : 80018
 Source Host           : rm-2ze36x32el4x85854uo.mysql.rds.aliyuncs.com:3306
 Source Schema         : ruoyi-flowable6

 Target Server Type    : MySQL
 Target Server Version : 80018
 File Encoding         : 65001

 Date: 16/08/2022 19:59:22
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for jimu_dict
-- ----------------------------
DROP TABLE IF EXISTS `jimu_dict`;
CREATE TABLE `jimu_dict`  (
  `id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `dict_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '字典名称',
  `dict_code` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '字典编码',
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
  `del_flag` int(1) NULL DEFAULT NULL COMMENT '删除状态',
  `create_by` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `type` int(1) UNSIGNED ZEROFILL NULL DEFAULT 0 COMMENT '字典类型0为string,1为number',
  `tenant_id` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '多租户标识',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sd_dict_code`(`dict_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of jimu_dict
-- ----------------------------
INSERT INTO `jimu_dict` VALUES ('716978883681226752', '操作类型', 'sys_oper_type', '操作类型', 0, 'admin', '2022-08-09 21:44:57', NULL, NULL, NULL, NULL);
INSERT INTO `jimu_dict` VALUES ('716981848714416128', '系统状态', 'sys_common_status', '系统状态', 0, 'admin', '2022-08-09 21:56:44', NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for jimu_dict_item
-- ----------------------------
DROP TABLE IF EXISTS `jimu_dict_item`;
CREATE TABLE `jimu_dict_item`  (
  `id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `dict_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '字典id',
  `item_text` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '字典项文本',
  `item_value` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '字典项值',
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
  `sort_order` int(10) NULL DEFAULT NULL COMMENT '排序',
  `status` int(11) NULL DEFAULT NULL COMMENT '状态（1启用 0不启用）',
  `create_by` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_by` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sdi_role_dict_id`(`dict_id`) USING BTREE,
  INDEX `idx_sdi_role_sort_order`(`sort_order`) USING BTREE,
  INDEX `idx_sdi_status`(`status`) USING BTREE,
  INDEX `idx_sdi_dict_val`(`dict_id`, `item_value`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of jimu_dict_item
-- ----------------------------
INSERT INTO `jimu_dict_item` VALUES ('716979000140271616', '716978883681226752', '新增', '1', '新增', 1, 1, 'admin', '2022-08-09 21:45:25', NULL, NULL);
INSERT INTO `jimu_dict_item` VALUES ('716979073167298560', '716978883681226752', '修改', '2', '修改', 2, 1, 'admin', '2022-08-09 21:45:42', NULL, NULL);
INSERT INTO `jimu_dict_item` VALUES ('716979132135018496', '716978883681226752', '删除', '3', '删除', 3, 1, 'admin', '2022-08-09 21:45:56', 'admin', '2022-08-09 21:46:25');
INSERT INTO `jimu_dict_item` VALUES ('716979205132685312', '716978883681226752', '授权', '4', '授权', 4, 1, 'admin', '2022-08-09 21:46:14', NULL, NULL);
INSERT INTO `jimu_dict_item` VALUES ('716979349697761280', '716978883681226752', '导出', '5', '导出', 5, 1, 'admin', '2022-08-09 21:46:48', NULL, NULL);
INSERT INTO `jimu_dict_item` VALUES ('716979399651921920', '716978883681226752', '导入', '6', '导入', 6, 1, 'admin', '2022-08-09 21:47:00', NULL, NULL);
INSERT INTO `jimu_dict_item` VALUES ('716979451501907968', '716978883681226752', '强退', '7', '强退', 7, 1, 'admin', '2022-08-09 21:47:12', NULL, NULL);
INSERT INTO `jimu_dict_item` VALUES ('716979520410128384', '716978883681226752', '生成代码', '8', '生成代码', 8, 1, 'admin', '2022-08-09 21:47:29', NULL, NULL);
INSERT INTO `jimu_dict_item` VALUES ('716979587271528448', '716978883681226752', '清空数据', '9', '清空数据', 9, 1, 'admin', '2022-08-09 21:47:45', NULL, NULL);
INSERT INTO `jimu_dict_item` VALUES ('716987753640136704', '716981848714416128', '成功', '0', '成功', 1, 1, 'admin', '2022-08-09 22:20:12', 'admin', '2022-08-09 22:22:43');
INSERT INTO `jimu_dict_item` VALUES ('716987799416770560', '716981848714416128', '失败', '1', '失败', 1, 1, 'admin', '2022-08-09 22:20:23', 'admin', '2022-08-09 22:22:40');

-- ----------------------------
-- Table structure for jimu_report
-- ----------------------------
DROP TABLE IF EXISTS `jimu_report`;
CREATE TABLE `jimu_report`  (
  `id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '主键',
  `code` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '编码',
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '名称',
  `note` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '说明',
  `status` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '状态',
  `type` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '类型',
  `json_str` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT 'json字符串',
  `api_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求地址',
  `thumb` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '缩略图',
  `create_by` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  `del_flag` tinyint(1) NULL DEFAULT NULL COMMENT '删除标识0-正常,1-已删除',
  `api_method` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求方法0-get,1-post',
  `api_code` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求编码',
  `template` tinyint(1) NULL DEFAULT NULL COMMENT '是否是模板 0-是,1-不是',
  `view_count` bigint(15) NULL DEFAULT 0 COMMENT '浏览次数',
  `css_str` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT 'css增强',
  `js_str` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT 'js增强',
  `tenant_id` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '多租户标识',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniq_jmreport_code`(`code`) USING BTREE,
  INDEX `uniq_jmreport_createby`(`create_by`) USING BTREE,
  INDEX `uniq_jmreport_delflag`(`del_flag`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '在线excel设计器' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of jimu_report
-- ----------------------------
INSERT INTO `jimu_report` VALUES ('716974933112426496', '20220809213843', '操作日志', NULL, NULL, 'datainfo', '{\"loopBlockList\":[],\"area\":false,\"excel_config_id\":\"716974933112426496\",\"printConfig\":{\"paper\":\"A4\",\"width\":210,\"height\":297,\"definition\":1,\"isBackend\":false,\"marginX\":10,\"marginY\":10,\"layout\":\"portrait\"},\"zonedEditionList\":[],\"rows\":{\"0\":{\"cells\":{\"0\":{\"style\":11,\"text\":\"系统模块\"},\"1\":{\"style\":11,\"text\":\"操作类型\"},\"2\":{\"style\":11,\"text\":\"方法名称\"},\"3\":{\"style\":11,\"text\":\"请求方式\"},\"4\":{\"style\":11,\"text\":\"操作人员\"},\"5\":{\"style\":11,\"text\":\"请求URL\"},\"6\":{\"style\":11,\"text\":\"主机地址\"},\"7\":{\"style\":11,\"text\":\"操作地点\"},\"8\":{\"rendered\":\"\",\"config\":\"\",\"display\":\"normal\",\"style\":11,\"text\":\"请求参数\"},\"9\":{\"rendered\":\"\",\"config\":\"\",\"style\":11,\"text\":\"返回参数\"},\"10\":{\"style\":11,\"rendered\":\"\",\"config\":\"\",\"text\":\"操作状态\"},\"11\":{\"style\":11,\"text\":\"错误消息\"},\"12\":{\"style\":11,\"text\":\"操作时间\"},\"13\":{\"style\":12,\"text\":\" \"},\"14\":{\"style\":12,\"text\":\" \"},\"15\":{\"style\":12,\"text\":\" \"},\"16\":{\"style\":12,\"text\":\" \"},\"17\":{\"style\":12,\"text\":\" \"},\"18\":{\"style\":12,\"text\":\" \"},\"19\":{\"style\":12,\"text\":\" \"},\"20\":{\"style\":12,\"text\":\" \"},\"21\":{\"style\":12,\"text\":\" \"},\"22\":{\"style\":12,\"text\":\" \"},\"23\":{\"style\":12,\"text\":\" \"},\"24\":{\"style\":12,\"text\":\" \"},\"25\":{\"style\":12,\"text\":\" \"},\"26\":{\"style\":12,\"text\":\" \"},\"27\":{\"style\":12,\"text\":\" \"},\"28\":{\"style\":12,\"text\":\" \"},\"29\":{\"style\":12,\"text\":\" \"},\"30\":{\"style\":12,\"text\":\" \"},\"31\":{\"style\":12,\"text\":\" \"},\"32\":{\"style\":12,\"text\":\" \"},\"33\":{\"style\":12,\"text\":\" \"},\"34\":{\"style\":12,\"text\":\" \"},\"35\":{\"style\":12,\"text\":\" \"},\"36\":{\"style\":12,\"text\":\" \"},\"37\":{\"style\":12,\"text\":\" \"},\"38\":{\"style\":12,\"text\":\" \"},\"39\":{\"style\":12,\"text\":\" \"},\"40\":{\"style\":12,\"text\":\" \"},\"41\":{\"style\":12,\"text\":\" \"},\"42\":{\"style\":12,\"text\":\" \"},\"43\":{\"style\":12,\"text\":\" \"},\"44\":{\"style\":12,\"text\":\" \"},\"45\":{\"style\":12,\"text\":\" \"},\"46\":{\"style\":12,\"text\":\" \"},\"47\":{\"style\":12,\"text\":\" \"},\"48\":{\"style\":12,\"text\":\" \"},\"49\":{\"style\":12,\"text\":\" \"},\"50\":{\"style\":12,\"text\":\" \"},\"51\":{\"style\":12,\"text\":\" \"},\"52\":{\"style\":12,\"text\":\" \"},\"53\":{\"style\":12,\"text\":\" \"},\"54\":{\"style\":12,\"text\":\" \"},\"55\":{\"style\":12,\"text\":\" \"},\"56\":{\"style\":12,\"text\":\" \"},\"57\":{\"style\":12,\"text\":\" \"},\"58\":{\"style\":12,\"text\":\" \"},\"59\":{\"style\":12,\"text\":\" \"},\"60\":{\"style\":12,\"text\":\" \"},\"61\":{\"style\":12,\"text\":\" \"},\"62\":{\"style\":12,\"text\":\" \"},\"63\":{\"style\":12,\"text\":\" \"},\"64\":{\"style\":12,\"text\":\" \"},\"65\":{\"style\":12,\"text\":\" \"},\"66\":{\"style\":12,\"text\":\" \"},\"67\":{\"style\":12,\"text\":\" \"},\"68\":{\"style\":12,\"text\":\" \"},\"69\":{\"style\":12,\"text\":\" \"},\"70\":{\"style\":12,\"text\":\" \"},\"71\":{\"style\":12,\"text\":\" \"},\"72\":{\"style\":12,\"text\":\" \"},\"73\":{\"style\":12,\"text\":\" \"},\"74\":{\"style\":12,\"text\":\" \"},\"75\":{\"style\":12,\"text\":\" \"},\"76\":{\"style\":12,\"text\":\" \"},\"77\":{\"style\":12,\"text\":\" \"},\"78\":{\"style\":12,\"text\":\" \"},\"79\":{\"style\":12,\"text\":\" \"},\"80\":{\"style\":12,\"text\":\" \"},\"81\":{\"style\":12,\"text\":\" \"},\"82\":{\"style\":12,\"text\":\" \"},\"83\":{\"style\":12,\"text\":\" \"},\"84\":{\"style\":12,\"text\":\" \"},\"85\":{\"style\":12,\"text\":\" \"},\"86\":{\"style\":12,\"text\":\" \"},\"87\":{\"style\":12,\"text\":\" \"},\"88\":{\"style\":12,\"text\":\" \"},\"89\":{\"style\":12,\"text\":\" \"},\"90\":{\"style\":12,\"text\":\" \"},\"91\":{\"style\":12,\"text\":\" \"},\"92\":{\"style\":12,\"text\":\" \"},\"93\":{\"style\":12,\"text\":\" \"},\"94\":{\"style\":12,\"text\":\" \"},\"95\":{\"style\":12,\"text\":\" \"},\"96\":{\"style\":12,\"text\":\" \"},\"97\":{\"style\":12,\"text\":\" \"}}},\"1\":{\"cells\":{\"0\":{\"text\":\"#{sys_oper_log.系统模块}\",\"style\":0},\"1\":{\"text\":\"#{sys_oper_log.操作类型}\",\"style\":0},\"2\":{\"text\":\"#{sys_oper_log.方法名称}\",\"style\":0},\"3\":{\"text\":\"#{sys_oper_log.请求方式}\",\"style\":0},\"4\":{\"text\":\"#{sys_oper_log.操作人员}\",\"style\":0},\"5\":{\"text\":\"#{sys_oper_log.请求url}\",\"style\":0},\"6\":{\"text\":\"#{sys_oper_log.主机地址}\",\"style\":0},\"7\":{\"text\":\"#{sys_oper_log.操作地点}\",\"style\":0},\"8\":{\"text\":\"#{sys_oper_log.请求参数}\",\"rendered\":\"\",\"config\":\"\",\"display\":\"normal\",\"style\":0},\"9\":{\"text\":\"#{sys_oper_log.返回参数}\",\"rendered\":\"\",\"config\":\"\",\"style\":0},\"10\":{\"text\":\"#{sys_oper_log.操作状态}\",\"style\":0},\"11\":{\"text\":\"#{sys_oper_log.错误消息}\",\"style\":0},\"12\":{\"text\":\"#{sys_oper_log.操作时间}\",\"style\":0},\"13\":{},\"14\":{},\"15\":{},\"16\":{},\"17\":{},\"18\":{},\"19\":{},\"20\":{},\"21\":{},\"22\":{},\"23\":{},\"24\":{},\"25\":{},\"26\":{},\"27\":{},\"28\":{},\"29\":{},\"30\":{},\"31\":{},\"32\":{},\"33\":{},\"34\":{},\"35\":{},\"36\":{},\"37\":{},\"38\":{},\"39\":{},\"40\":{},\"41\":{},\"42\":{},\"43\":{},\"44\":{},\"45\":{},\"46\":{},\"47\":{},\"48\":{},\"49\":{},\"50\":{},\"51\":{},\"52\":{},\"53\":{},\"54\":{},\"55\":{},\"56\":{},\"57\":{},\"58\":{},\"59\":{},\"60\":{},\"61\":{},\"62\":{},\"63\":{},\"64\":{},\"65\":{},\"66\":{},\"67\":{},\"68\":{},\"69\":{},\"70\":{},\"71\":{},\"72\":{},\"73\":{},\"74\":{},\"75\":{},\"76\":{},\"77\":{},\"78\":{},\"79\":{},\"80\":{},\"81\":{},\"82\":{},\"83\":{},\"84\":{},\"85\":{},\"86\":{},\"87\":{},\"88\":{},\"89\":{},\"90\":{},\"91\":{},\"92\":{},\"93\":{},\"94\":{},\"95\":{},\"96\":{},\"97\":{}}},\"len\":98},\"dbexps\":[],\"dicts\":[],\"rpbar\":{\"show\":true,\"pageSize\":\"\",\"btnList\":[]},\"freeze\":\"A1\",\"displayConfig\":{},\"background\":false,\"name\":\"sheet1\",\"autofilter\":{},\"styles\":[{\"align\":\"center\"},{\"align\":\"center\",\"font\":{\"bold\":true}},{\"font\":{\"bold\":true}},{\"align\":\"center\",\"font\":{\"bold\":true},\"bgcolor\":\"#a5a5a5\"},{\"font\":{\"bold\":true},\"bgcolor\":\"#a5a5a5\"},{\"align\":\"center\",\"font\":{\"bold\":true},\"bgcolor\":\"#ffc001\"},{\"font\":{\"bold\":true},\"bgcolor\":\"#ffc001\"},{\"align\":\"center\",\"font\":{\"bold\":true},\"bgcolor\":\"#4371c6\"},{\"font\":{\"bold\":true},\"bgcolor\":\"#4371c6\"},{\"align\":\"center\",\"font\":{\"bold\":true},\"bgcolor\":\"#ed7d31\"},{\"font\":{\"bold\":true},\"bgcolor\":\"#ed7d31\"},{\"align\":\"center\",\"font\":{\"bold\":true},\"bgcolor\":\"#e7e5e6\"},{\"font\":{\"bold\":true},\"bgcolor\":\"#e7e5e6\"}],\"validations\":[],\"cols\":{\"0\":{\"width\":162},\"1\":{\"width\":140},\"2\":{\"width\":153},\"3\":{\"width\":133},\"4\":{\"width\":122},\"5\":{\"width\":148},\"6\":{\"width\":154},\"7\":{\"width\":149},\"8\":{\"width\":173},\"9\":{\"width\":175},\"10\":{\"width\":200},\"11\":{\"width\":207},\"12\":{\"width\":192},\"13\":{\"width\":188},\"14\":{\"width\":176},\"len\":100},\"merges\":[]}', NULL, NULL, 'admin', '2022-08-09 21:38:43', 'admin', '2022-08-10 22:14:53', 0, NULL, NULL, 0, 34, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for jimu_report_data_source
-- ----------------------------
DROP TABLE IF EXISTS `jimu_report_data_source`;
CREATE TABLE `jimu_report_data_source`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据源名称',
  `report_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '报表_id',
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '编码',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `db_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据库类型',
  `db_driver` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '驱动类',
  `db_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据源地址',
  `db_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `db_password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '密码',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建日期',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新日期',
  `connect_times` int(1) UNSIGNED NULL DEFAULT 0 COMMENT '连接失败次数',
  `tenant_id` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '多租户标识',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_jmdatasource_report_id`(`report_id`) USING BTREE,
  INDEX `idx_jmdatasource_code`(`code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of jimu_report_data_source
-- ----------------------------
INSERT INTO `jimu_report_data_source` VALUES ('26d21fe4f27920d2f56abc8d90a8e527', 'oracle', '1308645288868712448', '', NULL, 'ORACLE', 'oracle.jdbc.OracleDriver', 'jdbc:oracle:thin:@192.168.1.199:1521:helowin', 'jeecgbootbpm', 'jeecg196283', 'admin', '2021-01-05 19:26:24', NULL, '2021-01-05 19:26:24', 1, NULL);
INSERT INTO `jimu_report_data_source` VALUES ('8f90daf47d15d35ca6cf420748b8b9ba', 'localhost', '1338744112815411200', '', NULL, 'MYSQL5.7', 'com.mysql.cj.jdbc.Driver', 'jdbc:mysql://127.0.0.1:3306/jeecg-boot?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT%2B8', 'root', '@JimuReportSMy3f94QGFM=', 'admin', '2022-08-06 19:47:16', 'admin', '2022-08-06 19:47:16', 13, NULL);

-- ----------------------------
-- Table structure for jimu_report_db
-- ----------------------------
DROP TABLE IF EXISTS `jimu_report_db`;
CREATE TABLE `jimu_report_db`  (
  `id` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'id',
  `jimu_report_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '主键字段',
  `create_by` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建人登录名称',
  `update_by` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '更新人登录名称',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建日期',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新日期',
  `db_code` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '数据集编码',
  `db_ch_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '数据集名字',
  `db_type` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '数据源类型',
  `db_table_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '数据库表名',
  `db_dyn_sql` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '动态查询SQL',
  `db_key` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '数据源KEY',
  `tb_db_key` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '填报数据源',
  `tb_db_table_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '填报数据表',
  `java_type` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'java类数据集  类型（spring:springkey,class:java类名）',
  `java_value` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'java类数据源  数值（bean key/java类名）',
  `api_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求地址',
  `api_method` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求方法0-get,1-post',
  `is_list` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '是否是列表0否1是 默认0',
  `is_page` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '是否作为分页,0:不分页，1:分页',
  `db_source` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '数据源',
  `db_source_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '数据库类型 MYSQL ORACLE SQLSERVER',
  `json_data` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT 'json数据，直接解析json内容',
  `api_convert` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'api转换器',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_jmreportdb_db_key`(`db_key`) USING BTREE,
  INDEX `idx_jimu_report_id`(`jimu_report_id`) USING BTREE,
  INDEX `idx_db_source_id`(`db_source`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of jimu_report_db
-- ----------------------------
INSERT INTO `jimu_report_db` VALUES ('716975799424946176', '716974933112426496', 'admin', 'admin', '2022-08-10 21:35:11', '2022-08-10 21:35:11', 'sys_oper_log', '操作日志', '0', NULL, 'SELECT\n	title 系统模块,\n	business_type 操作类型,\n	method 方法名称,\n	request_method 请求方式,\n	operator_type 操作类别,\n	oper_name 操作人员,\n	dept_name 部门名称,\n	oper_url 请求URL,\n	oper_ip 主机地址,\n	oper_location 操作地点,\n	oper_param 请求参数,\n	json_result 返回参数,\n	`status` 操作状态,\n	error_msg 错误消息,\n	oper_time 操作时间 \nFROM\n	sys_oper_log', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '1', '1', '8f90daf47d15d35ca6cf420748b8b9ba', 'mysql', '', '');

-- ----------------------------
-- Table structure for jimu_report_db_field
-- ----------------------------
DROP TABLE IF EXISTS `jimu_report_db_field`;
CREATE TABLE `jimu_report_db_field`  (
  `id` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'id',
  `create_by` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建人登录名称',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建日期',
  `update_by` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '更新人登录名称',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新日期',
  `jimu_report_db_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '数据源ID',
  `field_name` varchar(80) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '字段名',
  `field_text` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '字段文本',
  `widget_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '控件类型',
  `widget_width` int(10) NULL DEFAULT NULL COMMENT '控件宽度',
  `order_num` int(3) NULL DEFAULT NULL COMMENT '排序',
  `search_flag` int(3) NULL DEFAULT 0 COMMENT '查询标识0否1是 默认0',
  `search_mode` int(3) NULL DEFAULT NULL COMMENT '查询模式1简单2范围',
  `dict_code` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '字典编码支持从表中取数据',
  `search_value` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '查询默认值',
  `search_format` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '查询时间格式化表达式',
  `ext_json` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '参数配置',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_jrdf_jimu_report_db_id`(`jimu_report_db_id`) USING BTREE,
  INDEX `idx_dbfield_order_num`(`order_num`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of jimu_report_db_field
-- ----------------------------
INSERT INTO `jimu_report_db_field` VALUES ('716983467761897472', 'admin', '2022-08-10 21:35:11', NULL, NULL, '716975799424946176', '系统模块', '系统模块', 'String', NULL, 0, NULL, NULL, NULL, NULL, NULL, '');
INSERT INTO `jimu_report_db_field` VALUES ('716983467870949376', 'admin', '2022-08-10 21:35:11', NULL, NULL, '716975799424946176', '操作类型', '操作类型', 'String', NULL, 1, NULL, NULL, 'sys_oper_type', NULL, NULL, '');
INSERT INTO `jimu_report_db_field` VALUES ('716983467938058240', 'admin', '2022-08-10 21:35:11', NULL, NULL, '716975799424946176', '方法名称', '方法名称', 'String', NULL, 2, NULL, NULL, NULL, NULL, NULL, '');
INSERT INTO `jimu_report_db_field` VALUES ('716983468013555712', 'admin', '2022-08-10 21:35:11', NULL, NULL, '716975799424946176', '请求方式', '请求方式', 'String', NULL, 3, NULL, NULL, NULL, NULL, NULL, '');
INSERT INTO `jimu_report_db_field` VALUES ('716983468101636096', 'admin', '2022-08-10 21:35:11', NULL, NULL, '716975799424946176', '操作类别', '操作类别', 'String', NULL, 4, NULL, NULL, '', NULL, NULL, '');
INSERT INTO `jimu_report_db_field` VALUES ('716983468181327872', 'admin', '2022-08-10 21:35:11', NULL, NULL, '716975799424946176', '操作人员', '操作人员', 'String', NULL, 5, NULL, NULL, NULL, NULL, NULL, '');
INSERT INTO `jimu_report_db_field` VALUES ('716983468252631040', 'admin', '2022-08-10 21:35:11', NULL, NULL, '716975799424946176', '部门名称', '部门名称', 'String', NULL, 6, NULL, NULL, NULL, NULL, NULL, '');
INSERT INTO `jimu_report_db_field` VALUES ('716983468336517120', 'admin', '2022-08-10 21:35:11', NULL, NULL, '716975799424946176', '请求url', '请求url', 'String', NULL, 7, NULL, NULL, NULL, NULL, NULL, '');
INSERT INTO `jimu_report_db_field` VALUES ('716983468424597504', 'admin', '2022-08-10 21:35:11', NULL, NULL, '716975799424946176', '主机地址', '主机地址', 'String', NULL, 8, NULL, NULL, NULL, NULL, NULL, '');
INSERT INTO `jimu_report_db_field` VALUES ('716983468563009536', 'admin', '2022-08-10 21:35:11', NULL, NULL, '716975799424946176', '操作地点', '操作地点', 'String', NULL, 9, NULL, NULL, NULL, NULL, NULL, '');
INSERT INTO `jimu_report_db_field` VALUES ('716983468760141824', 'admin', '2022-08-10 21:35:11', NULL, NULL, '716975799424946176', '请求参数', '请求参数', 'String', NULL, 10, NULL, NULL, NULL, NULL, NULL, '');
INSERT INTO `jimu_report_db_field` VALUES ('716983468978245632', 'admin', '2022-08-10 21:35:12', NULL, NULL, '716975799424946176', '返回参数', '返回参数', 'String', NULL, 11, NULL, NULL, NULL, NULL, NULL, '');
INSERT INTO `jimu_report_db_field` VALUES ('716983469095686144', 'admin', '2022-08-10 21:35:12', NULL, NULL, '716975799424946176', '操作状态', '操作状态', 'String', NULL, 12, NULL, NULL, 'sys_common_status', NULL, NULL, '');
INSERT INTO `jimu_report_db_field` VALUES ('716983469204738048', 'admin', '2022-08-10 21:35:12', NULL, NULL, '716975799424946176', '错误消息', '错误消息', 'String', NULL, 13, NULL, NULL, '', NULL, NULL, '');
INSERT INTO `jimu_report_db_field` VALUES ('716983469301207040', 'admin', '2022-08-10 21:35:12', NULL, NULL, '716975799424946176', '操作时间', '操作时间', 'String', NULL, 14, NULL, NULL, NULL, NULL, NULL, '');

-- ----------------------------
-- Table structure for jimu_report_db_param
-- ----------------------------
DROP TABLE IF EXISTS `jimu_report_db_param`;
CREATE TABLE `jimu_report_db_param`  (
  `id` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `jimu_report_head_id` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '动态报表ID',
  `param_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '参数字段',
  `param_txt` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '参数文本',
  `param_value` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '参数默认值',
  `order_num` int(11) NULL DEFAULT NULL COMMENT '排序',
  `create_by` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建人登录名称',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建日期',
  `update_by` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '更新人登录名称',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新日期',
  `search_flag` int(1) NULL DEFAULT NULL COMMENT '查询标识0否1是 默认0',
  `widget_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '查询控件类型',
  `search_mode` int(1) NULL DEFAULT NULL COMMENT '查询模式1简单2范围',
  `dict_code` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '字典',
  `search_format` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '查询时间格式化表达式',
  `ext_json` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '参数配置',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_jmrheadid`(`jimu_report_head_id`) USING BTREE,
  INDEX `idx_jrdp_jimu_report_head_id`(`jimu_report_head_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jimu_report_link
-- ----------------------------
DROP TABLE IF EXISTS `jimu_report_link`;
CREATE TABLE `jimu_report_link`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键id',
  `report_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '积木设计器id',
  `parameter` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '参数',
  `eject_type` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '弹出方式（0 当前页面 1 新窗口）',
  `link_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '链接名称',
  `api_method` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求方法0-get,1-post',
  `link_type` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '链接方式(0 网络报表 1 网络连接 2 图表联动)',
  `api_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '外网api',
  `link_chart_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联动图表的ID',
  `expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '表达式',
  `requirement` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '条件',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `uniq_link_reportid`(`report_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '超链接配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jimu_report_map
-- ----------------------------
DROP TABLE IF EXISTS `jimu_report_map`;
CREATE TABLE `jimu_report_map`  (
  `id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '主键',
  `label` varchar(125) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '地图名称',
  `name` varchar(125) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '地图编码',
  `data` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '地图数据',
  `create_by` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  `del_flag` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '0表示未删除,1表示删除',
  `sys_org_code` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所属部门',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniq_jmreport_map_name`(`name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '地图配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jimu_report_share
-- ----------------------------
DROP TABLE IF EXISTS `jimu_report_share`;
CREATE TABLE `jimu_report_share`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `report_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '在线excel设计器id',
  `preview_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '预览地址',
  `preview_lock` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '密码锁',
  `last_update_time` datetime(0) NULL DEFAULT NULL COMMENT '最后更新时间',
  `term_of_validity` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '有效期(0:永久有效，1:1天，2:7天)',
  `status` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否过期(0未过期，1已过期)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '积木报表预览权限表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
