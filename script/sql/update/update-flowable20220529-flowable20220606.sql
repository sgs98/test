alter table act_business_status add create_by VARCHAR(255) not null comment '创建人';
alter table act_business_status add update_by VARCHAR(255) not null comment '更新人';
update act_business_status set create_by = 'admin' ,update_by = 'admin';

update act_business_status set create_time = SYSDATE() ,update_time = SYSDATE();
alter table act_business_status modify create_time datetime not null comment '创建时间';
alter table act_business_status modify update_time datetime not null comment '更新时间';



alter table act_task_node add create_by VARCHAR(255) not null comment '创建人';
alter table act_task_node add update_by VARCHAR(255) not null comment '更新人';
update act_task_node set create_by = 'admin' ,update_by = 'admin';

update act_task_node set create_time = SYSDATE() ,update_time = SYSDATE();
alter table act_task_node modify create_time datetime not null comment '创建时间';
alter table act_task_node modify update_time datetime not null comment '更新时间';

commit;





