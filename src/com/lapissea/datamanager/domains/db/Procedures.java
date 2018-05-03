package com.lapissea.datamanager.domains.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class Procedures{
	
	public static ResultSet getFileData(Connection con, String path) throws SQLException{
		path=SqlUtil.fixPath(path);
		
		PreparedStatement s=con.prepareStatement("SELECT top 1 data FROM v_File WHERE path=?");
		s.setString(1, path);
		return s.executeQuery();
	}
	
	/*
	 * ID, PATH, FILESIZE, LASTMODIFIED
	 */
	public static ResultSet getFileMeta(Connection con, String path, String what) throws SQLException{
		path=SqlUtil.fixPath(path);
		
		PreparedStatement s=con.prepareStatement("SELECT top 1 "+what+" FROM v_File WHERE path=?");
		s.setString(1, path);
		return s.executeQuery();
	}
	
	public static void makeFile(Connection con, String path, InputStream data, Integer length) throws SQLException{
		path=SqlUtil.fixPath(path);
		
		PreparedStatement getFileId=con.prepareStatement("SELECT top 1 id FROM v_File WHERE path=?");
		getFileId.setString(1, path);
		ResultSet rs=getFileId.executeQuery();
		
		PreparedStatement s;
		
		if(rs.next()){
			s=con.prepareStatement("update File set data=? where id=?");
			s.setInt(2, rs.getInt(1));
		}else{
			s=con.prepareStatement("INSERT INTO File (data, path) VALUES (?,?)");
			s.setString(2, path);
		}
		
		if(length!=null) s.setBlob(1, data, length);
		else s.setBlob(1, data);
		s.execute();
	}
	
	public static void deleteFile(Connection con, String path) throws SQLException{
		path=SqlUtil.fixPath(path);
		
		PreparedStatement s=con.prepareStatement("DELETE TOP 1 FROM File where path=?");
		s.setString(1, path);
		s.execute();
	}
	
	public static ResultSet getDirPaths(Connection con, String path) throws SQLException{
		path=SqlUtil.fixPath(path);
		
		PreparedStatement s=con.prepareStatement("SELECT top 1 id FROM Folder WHERE path=?");
		s.setString(1, path);
		ResultSet rs=s.executeQuery();
		if(!rs.next()) return null;
		
		int folderId=rs.getInt(1);
		
		PreparedStatement s1=con.prepareStatement("select concat ((SELECT path FROM ChildFolders inner join Folder on id=pointer where folderId=? ), (SELECT path FROM ChildFiles inner join File on id=pointer where folderId=?)) as paths");
		s1.setInt(1, folderId);
		s1.setInt(2, folderId);
		return s1.executeQuery();
	}
	
	public static ResultSet getDirPathsDeep(Connection con, String path, boolean f) throws SQLException{
		try{
			path=SqlUtil.fixPath(path);
			
			PreparedStatement s=con.prepareStatement("SELECT top 1 id FROM Folder WHERE path=?");
			s.setString(1, path);
			ResultSet rs=s.executeQuery();
			if(!rs.next()) return null;
			
			PreparedStatement s1=con.prepareStatement("SELECT path as P FROM "+(f?"Folder":"File")+" where path LIKE ?");
			s1.setString(1, path+"%");
			return s1.executeQuery();
			
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
}
