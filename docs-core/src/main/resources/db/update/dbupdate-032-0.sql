-- Add a table to track user activities for Gantt chart and admin dashboard

create cached table T_USER_ACTIVITY (
  UTA_ID_C varchar(36) not null,
  UTA_IDUSER_C varchar(36) not null,
  UTA_ACTIVITY_TYPE_C varchar(50) not null,
  UTA_ENTITY_ID_C varchar(36),
  UTA_PROGRESS_N int not null default 0,
  UTA_PLANNED_DATE_D datetime,
  UTA_COMPLETED_DATE_D datetime,
  UTA_CREATEDATE_D datetime not null,
  UTA_DELETEDATE_D datetime,
  primary key (UTA_ID_C)
);

alter table T_USER_ACTIVITY add constraint FK_UTA_IDUSER_C foreign key (UTA_IDUSER_C) references T_USER (USE_ID_C) on delete restrict on update restrict;

create index IDX_UTA_IDUSER_C on T_USER_ACTIVITY (UTA_IDUSER_C);
create index IDX_UTA_ACTIVITY_TYPE_C on T_USER_ACTIVITY (UTA_ACTIVITY_TYPE_C);
create index IDX_UTA_CREATEDATE_D on T_USER_ACTIVITY (UTA_CREATEDATE_D);

-- Update database version
update T_CONFIG set CFG_VALUE_C = '32' where CFG_ID_C = 'DB_VERSION'; 