drop table if exists DATABASE_DESIGN_VERSION;
create table DATABASE_DESIGN_VERSION(
	id varchar not null
);

SET	TRACE_LEVEL_FILE 0;
SET TRACE_LEVEL_SYSTEM_OUT 0;
SET CACHE_SIZE 8192;
SET MULTI_THREADED 1;

drop view if exists v_File;

drop table if exists ChildFiles;
drop table if exists ChildFolders;
drop table if exists Folder;
drop table if exists File;

drop index if exists folderNames;
drop index if exists fileNames;

create table Folder(
	id int not null auto_increment,
	path nvarchar(256) not null,
	PRIMARY KEY (ID)
);

create table File(
	id int not null auto_increment,
	data BLOB not null,
	path nvarchar(256) not null,
	PRIMARY KEY (ID),
        lastModified datetime default CURRENT_TIMESTAMP
);

create unique index folderNames on Folder(path);
create unique index fileNames on File(path);

drop trigger if exists TR_ONFILEDELETE;
drop trigger if exists TR_ONFILEINSERT_B;
drop trigger if exists TR_ONFILEINSERT_A;
drop trigger if exists TR_ONFILEUPDATE;
CREATE TRIGGER TR_ONFILEDELETE BEFORE DELETE ON FILE FOR EACH ROW CALL "com.lapissea.datamanager.domains.db.triggers.FileChange";
CREATE TRIGGER TR_ONFILEINSERT_B BEFORE INSERT ON FILE FOR EACH ROW CALL "com.lapissea.datamanager.domains.db.triggers.FileChange";
CREATE TRIGGER TR_ONFILEINSERT_A AFTER INSERT ON FILE FOR EACH ROW CALL "com.lapissea.datamanager.domains.db.triggers.FileChange";
CREATE TRIGGER TR_ONFILEUPDATE BEFORE UPDATE ON FILE FOR EACH ROW CALL "com.lapissea.datamanager.domains.db.triggers.FileChange";

create view v_File as select id, path, OCTET_LENGTH(Data) as FileSize, lastModified, data  from File;

create table ChildFolders(
	folderId int not null,
	pointer int not null unique,
        FOREIGN KEY (folderId) references Folder (id),
        FOREIGN KEY (pointer) references Folder (id)
);

create table ChildFiles(
	folderId int not null,
	pointer int not null unique,
        FOREIGN KEY (folderId) references Folder (id),
        FOREIGN KEY (pointer) references File (id)
);
-- root
INSERT INTO Folder (path) VALUES('');


drop alias if exists getFileData;
drop alias if exists getFileMeta;
drop alias if exists makeFile;
drop alias if exists deleteFile;
drop alias if exists getDirPaths;
drop alias if exists getDirPathsDeep;

CREATE ALIAS getFileData FOR "com.lapissea.datamanager.domains.db.Procedures.getFileData";
CREATE ALIAS getFileMeta FOR "com.lapissea.datamanager.domains.db.Procedures.getFileMeta";
CREATE ALIAS makeFile FOR "com.lapissea.datamanager.domains.db.Procedures.makeFile";
CREATE ALIAS deleteFile FOR "com.lapissea.datamanager.domains.db.Procedures.deleteFile";
CREATE ALIAS getDirPaths FOR "com.lapissea.datamanager.domains.db.Procedures.getDirPaths";
CREATE ALIAS getDirPathsDeep FOR "com.lapissea.datamanager.domains.db.Procedures.getDirPathsDeep";

