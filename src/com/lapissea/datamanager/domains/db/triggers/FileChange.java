package com.lapissea.datamanager.domains.db.triggers;

import com.lapissea.datamanager.domains.db.FireOnlyTrigger;
import org.h2.api.Trigger;

import java.sql.*;

import static com.lapissea.datamanager.domains.db.SqlUtil.*;


public class FileChange implements FireOnlyTrigger{
	
	private Trigger reeee;
	
	@Override
	public void init(Connection con, String schemaName, String triggerName, String tableName, boolean before, int type) throws SQLException{
		FireOnlyTrigger[] TRIGGERS={
			//INSERT
			(conn, oldRow, newRow)->{
				fixPath(newRow);
				if(!before) connectFile(conn, (int)newRow[0], (String)newRow[2]);
			},
			//UPDATE
			(conn, oldRow, newRow)->{
				fixPath(newRow);
				
				Timestamp test=(Timestamp)newRow[3];
				newRow[3]=new Timestamp(System.currentTimeMillis());
				
				if(!((String)newRow[2]).equals(oldRow[2])){
					disconnectFile(conn, (int)oldRow[0], (String)oldRow[2]);
					connectFile(conn, (int)newRow[0], (String)newRow[2]);
				}
			},
			//DELETE
			(conn, oldRow, newRow)->{
				fixPath(oldRow);
				disconnectFile(conn, (int)oldRow[0], (String)oldRow[2]);
			}
		};
		
		reeee=TRIGGERS[32-Integer.numberOfLeadingZeros(type-1)];
	}
	
	@Override
	public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException{
		reeee.fire(conn, oldRow, newRow);
	}
	
	public static void connectFile(Connection con, int id, String path) throws SQLException{
		path=fixPath(path);
		
		String[] parts=path.split("[/|\\\\]");
		
		ResultSet root=con.prepareCall("SELECT top 1 id FROM Folder WHERE path=''").executeQuery();
		root.next();
		int topFolderId=root.getInt(1);
		
		StringBuilder folderPath=new StringBuilder();
		
		for(int i=0;i<parts.length-1;i++){
			
			folderPath.append("/").append(parts[i]);
			
			String st=fixPath(folderPath.toString());
			
			PreparedStatement getFileId=con.prepareStatement("SELECT top 1 f.id FROM ChildFolders as ch  inner join Folder as f on ch.pointer=f.id  where ch.folderId=? AND f.path=?");
			getFileId.setInt(1, topFolderId);
			getFileId.setString(2, st);
			ResultSet rs=getFileId.executeQuery();
			
			if(rs.next()){
				topFolderId=rs.getInt(1);
				continue;
			}
			
			PreparedStatement getFolderId=con.prepareStatement("SELECT top 1 id FROM Folder where path=?");
			getFolderId.setString(1, st);
			rs=getFolderId.executeQuery();
			
			if(rs.next()){
				topFolderId=rs.getInt(1);
				continue;
			}
			
			PreparedStatement s=con.prepareStatement("INSERT INTO Folder (path) VALUES (?)");
			s.setString(1, st);
			s.execute();
			
			rs=getFolderId.executeQuery();
			rs.next();
			int newId=rs.getInt(1);
			
			PreparedStatement g=con.prepareStatement("INSERT INTO ChildFolders (folderId, pointer) VALUES (?,?)");
			g.setInt(1, topFolderId);
			g.setInt(2, newId);
			g.execute();
			topFolderId=newId;
			
		}
		
		con.prepareCall("INSERT INTO ChildFiles (folderId, pointer) VALUES ("+topFolderId+","+id+")").execute();
	}
	
	public static void disconnectFile(Connection con, int id, String path) throws SQLException{
		con.prepareCall("DELETE TOP 1 FROM ChildFiles where pointer="+id).execute();
		boolean change=true;
		while(change){
			change=false;
			
			CallableStatement getBads =con.prepareCall("SELECT f.id FROM Folder as f where f.path!='' AND (select count(*) from ChildFiles as ch where f.id=ch.folderId)+(select count(*) from ChildFolders as ch where f.id=ch.folderId)=0");
			CallableStatement deletDis=con.prepareCall("SET @id1=?;DELETE FROM ChildFolders as ch WHERE ch.pointer=@id1;DELETE FROM Folder as f WHERE f.id=@id1;");
			
			ResultSet rs=getBads.executeQuery();
			while(rs.next()){
				change=true;
				int badId=rs.getInt(1);
				deletDis.setInt(1, badId);
				deletDis.execute();
			}
			
		}
	}
}
