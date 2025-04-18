-- DBUPDATE-031-0.SQL

-- Insert a new setting for OCR recognition
insert into T_CONFIG (CFG_ID_C, CFG_VALUE_C) values ('OCR_ENABLED', 'true');

-- Add a table to track user activities for Gantt chart and admin dashboard
create table T_USER_ACTIVITY (UTA_ID_C varchar(36) not null, UTA_IDUSER_C varchar(36) not null, UTA_ACTIVITY_TYPE_C varchar(50) not null, UTA_ENTITY_ID_C varchar(36), UTA_PROGRESS_N int not null default 0, UTA_PLANNED_DATE_D timestamp, UTA_COMPLETED_DATE_D timestamp, UTA_CREATEDATE_D timestamp not null, UTA_DELETEDATE_D timestamp, primary key (UTA_ID_C));

alter table T_USER_ACTIVITY add constraint FK_UTA_IDUSER_C foreign key (UTA_IDUSER_C) references T_USER (USE_ID_C) on delete restrict on update restrict;

create index IDX_UTA_IDUSER_C on T_USER_ACTIVITY (UTA_IDUSER_C);
create index IDX_UTA_ACTIVITY_TYPE_C on T_USER_ACTIVITY (UTA_ACTIVITY_TYPE_C);
create index IDX_UTA_CREATEDATE_D on T_USER_ACTIVITY (UTA_CREATEDATE_D);

-- Update the database version
update T_CONFIG set CFG_VALUE_C = '31' where CFG_ID_C = 'DB_VERSION';
