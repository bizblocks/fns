create table FNS_TAXPAYER (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    INN varchar(12),
    TYPE_ varchar(50),
    DESCRIPTION varchar(1024),
    FIO varchar(255),
    OKVED text,
    ADD_OKVED text,
    --
    primary key (ID)
);
