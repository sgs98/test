UPDATE `ruoyi-flowable6`.`sys_menu` SET `menu_name` = '报表管理', `parent_id` = 0, `order_num` = 9, `path` = 'report', `component` = NULL, `query_param` = NULL, `is_frame` = 1, `is_cache` = 0, `menu_type` = 'M', `visible` = '0', `status` = '0', `perms` = NULL, `icon` = 'chart', `create_by` = 'admin', `create_time` = '2022-06-18 17:24:41', `update_by` = 'admin', `update_time` = '2022-06-18 17:24:41', `remark` = '' WHERE `menu_id` = 1538090311987884033;
UPDATE `ruoyi-flowable6`.`sys_menu` SET `menu_name` = '设计报表', `parent_id` = 1538090311987884033, `order_num` = 3, `path` = 'jmreport', `component` = 'jmreport/index', `query_param` = NULL, `is_frame` = 1, `is_cache` = 0, `menu_type` = 'C', `visible` = '0', `status` = '0', `perms` = NULL, `icon` = '#', `create_by` = 'admin', `create_time` = '2022-06-18 17:25:33', `update_by` = 'admin', `update_time` = '2022-08-04 10:34:54', `remark` = '' WHERE `menu_id` = 1538090530318184449;
UPDATE `ruoyi-flowable6`.`sys_menu` SET `menu_name` = '系统日志', `parent_id` = 1538090311987884033, `order_num` = 2, `path` = 'sysLogReport', `component` = 'jmreport/view', `query_param` = '{\"id\":\"715009479168196608\"}', `is_frame` = 1, `is_cache` = 0, `menu_type` = 'C', `visible` = '0', `status` = '0', `perms` = NULL, `icon` = '#', `create_by` = 'admin', `create_time` = '2022-06-18 17:41:12', `update_by` = 'admin', `update_time` = '2022-08-04 11:41:04', `remark` = '' WHERE `menu_id` = 1538094466311778306;