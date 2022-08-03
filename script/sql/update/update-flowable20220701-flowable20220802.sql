CREATE TABLE `act_business_rule` (
 `id` BIGINT(20) NOT NULL COMMENT 'id',
 `bean_name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '全类名',
 `method` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '方法名',
 `param` VARCHAR(4000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '参数',
 `remark` VARCHAR(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
 `create_time` DATETIME NOT NULL COMMENT '创建时间',
 `update_time` DATETIME NOT NULL COMMENT '更新时间',
 `create_by` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
 `update_by` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '更新人',
 PRIMARY KEY (`id`) USING BTREE
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='业务规则';
INSERT INTO `act_business_rule` (`id`, `bean_name`, `method`, `param`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES(1471758168763731969,'workflowRuleUserComponent','queryUserById','[{\"paramType\":\"Long\",\"param\":\"userId\",\"remark\":\"用户id\",\"orderNo\":1}]','按id查询人员','2021-12-17 16:24:26','2022-07-15 21:54:29','admin','admin');
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES(1543513641015955457,'工作流程分类','act_category','0','admin','2022-07-03 16:35:04','admin','2022-07-03 16:35:04','工作流程分类');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES(1543513861028171778,'0','OA','oa','act_category','','primary','N','0','admin','2022-07-03 16:35:56','admin','2022-07-03 17:15:54',NULL);
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES(1543513955815247873,'1','财务','finance','act_category','','primary','N','0','admin','2022-07-03 16:36:19','admin','2022-07-03 16:36:19',NULL);

ALTER TABLE act_node_assignee ADD auto_complete TINYINT(1) NOT NULL DEFAULT 0 COMMENT '自动审批,0不自动办理,1自动办理,当前节点自动办理';

alter table act_node_assignee rename column full_class_id to business_rule_id;
