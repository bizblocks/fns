-- begin FNS_TAXPAYER
create table FNS_TAXPAYER (
    INN varchar(12),
    --
    TYPE_ varchar(50),
    DESCRIPTION varchar(1024),
    FIO varchar(255),
    OKVED text,
    ADD_OKVED text,
    ADDRESS varchar(1024),
    --
    primary key (INN)
)^
-- end FNS_TAXPAYER
