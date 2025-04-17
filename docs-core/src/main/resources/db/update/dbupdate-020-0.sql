create table T_WEBHOOK ( WHK_ID_C varchar(36) not null, WHK_EVENT_C varchar(50) not null, WHK_URL_C varchar(1024) not null,  WHK_CREATEDATE_D datetime not null, WHK_DELETEDATE_D datetime, primary key (WHK_ID_C) );
create table T_GUEST_LOGIN_REQUEST ( GLR_ID_C varchar(36) not null, GLR_TOKEN_C varchar(100) not null, GLR_IP_C varchar(45) not null, GLR_TIMESTAMP_D datetime not null, GLR_STATUS_C varchar(20) not null, primary key (GLR_ID_C) );

update T_CONFIG set CFG_VALUE_C = '20' where CFG_ID_C = 'DB_VERSION';
