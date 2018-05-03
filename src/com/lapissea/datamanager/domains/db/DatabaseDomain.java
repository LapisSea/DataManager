package com.lapissea.datamanager.domains.db;

import com.lapissea.datamanager.Domain;
import com.lapissea.util.NotNull;
import com.lapissea.util.Nullable;
import com.lapissea.util.function.UnsafeConsumer;
import gnu.trove.list.array.TByteArrayList;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.jdbcx.JdbcDataSource;

import java.io.*;
import java.lang.ref.SoftReference;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static com.lapissea.util.UtilL.*;

public class DatabaseDomain extends Domain{
	
	private final JdbcConnectionPool sqlConnectionPool;
	private final String             path;
	
	private static final String DATABASE_DESIGN_VERSION="1.03";
	
	public DatabaseDomain(String dbFilePath){
		path=dbFilePath;
		
		String setupCode=null;
		
		if(dbFilePath.endsWith(".sql")) dbFilePath=dbFilePath.substring(0, dbFilePath.length()-4);
		else if(dbFilePath.endsWith(".mv.db")) dbFilePath=dbFilePath.substring(0, dbFilePath.length()-6);
		
		String dbFile=new File(dbFilePath).getAbsolutePath();
		
		if(!new File(dbFilePath+".mv.db").isFile()){
			try{
				File f=new File(dbFilePath+".sql");
				try(Reader r=new InputStreamReader(new BufferedInputStream(new FileInputStream(f)))){
					StringBuilder sb=new StringBuilder();
					
					int c;
					while((c=r.read())!=-1){
						sb.append((char)c);
					}
					setupCode=sb.toString();
				}
				Files.move(f.toPath(), Paths.get(dbFilePath+".sql0"));
			}catch(IOException e){
				throw uncheckedThrow(e);
			}
		}
		
		JdbcDataSource ds=new JdbcDataSource();
		ds.setURL("jdbc:h2:"+dbFile+";TRACE_LEVEL_FILE=0");
		ds.setUser("sa");
		ds.setPassword("LAPIS_H2_DATA_MANAGER");
		
		setupDatabase(ds, setupCode);
		
		sqlConnectionPool=JdbcConnectionPool.create(ds);
		
	}
	
	private Connection connect() throws SQLException{
		return sqlConnectionPool.getConnection();
	}
	
	@SuppressWarnings("SameParameterValue")
	private void setupDatabase(JdbcDataSource ds, String setupCode){
		
		UnsafeConsumer<Connection, Exception> setup=c->{
			StringBuilder sb=new StringBuilder();
			
			Reader r=new InputStreamReader(DatabaseDomain.class.getResourceAsStream("/datamanager/db/ServerSetup.sql"));
			int    i;
			while((i=r.read())!=-1){
				sb.append((char)i);
			}
			sb.append("DELETE FROM DATABASE_DESIGN_VERSION;\nINSERT INTO DATABASE_DESIGN_VERSION VALUES("+DATABASE_DESIGN_VERSION+");\n");
			if(setupCode!=null) sb.append(';').append(setupCode).append(';');
			c.prepareCall(sb.toString()).execute();
			
		};
		
		try(Connection c=ds.getConnection()){
			boolean needsSetup;
			
			try(CallableStatement cl=c.prepareCall("SELECT ? NOT IN (SELECT TOP 1 id FROM DATABASE_DESIGN_VERSION)")){
				cl.setString(1, DATABASE_DESIGN_VERSION);
				try(ResultSet rs=cl.executeQuery()){
					rs.next();
					needsSetup=rs.getBoolean(1);
				}
			}catch(SQLException e1){//if it fails than the db is a bad version
				needsSetup=true;
			}
			
			if(needsSetup) setup.accept(c);
		}catch(Exception e1){
			throw uncheckedThrow(e1);
		}
	}
	
	
	@Nullable
	@Override
	public BufferedInputStream getInStream(@NotNull String localPath){
		try(Connection con=connect();
		    PreparedStatement st=con.prepareStatement("call getFileData(?)")){
			st.setString(1, localPath);
			try(ResultSet rs=st.executeQuery()){
				rs.next();
				return new BufferedInputStream(rs.getBinaryStream(1));
			}
		}catch(SQLException e){
			return null;
		}
	}
	
	@Nullable
	@Override
	public BufferedReader getReader(@NotNull String localPath){
		InputStream is=getInStream(localPath);
		return is==null?null:new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
	}
	
	@Override
	public boolean exists(@NotNull String localPath){
		try(Connection con=connect();
		    PreparedStatement st=con.prepareStatement("call getFileMeta(?,'count(*)')>0")){
			st.setString(1, localPath);
			try(ResultSet rs=st.executeQuery()){
				rs.next();
				return rs.getBoolean(1);
			}
		}catch(SQLException e){
			return false;
		}
	}
	
	@Nullable
	@Override
	public byte[] getBytes(@NotNull String localPath){
		try(Connection con=connect();
		    PreparedStatement st=con.prepareStatement("call getFileData(?)")){
			st.setString(1, localPath);
			try(ResultSet rs=st.executeQuery()){
				rs.next();
				return rs.getBytes(1);
			}
		}catch(SQLException e){
			return null;
		}
	}
	
	@Nullable
	@Override
	public String[] getDirNames(@NotNull String localPath){
		String[] paths=getDirPaths(localPath);
		if(paths==null) return null;
		
		for(int i=0;i<paths.length;i++){
			int id=paths[i].lastIndexOf('/');
			if(id==-1) id=paths[i].lastIndexOf('\\');
			if(id!=-1) paths[i]=paths[i].substring(id+1);
		}
		return paths;
	}
	
	@Nullable
	@Override
	public String[] getDirPaths(@NotNull String localPath){
		try(Connection con=connect();
		    PreparedStatement st=con.prepareStatement("call getDirPaths(?)")){
			st.setString(1, localPath);
			try(ResultSet rs=st.executeQuery()){
				List<String> data=new ArrayList<>();
				while(rs.next()) data.add(rs.getString(1));
				return data.toArray(new String[data.size()]);
			}
		}catch(SQLException e){
			e.printStackTrace();
			return null;
		}
	}
	
	@Nullable
	@Override
	public String[] getDirPathsDeep(@NotNull String localPath){
		try(Connection con=connect()){
			PreparedStatement st=con.prepareStatement("call getDirPathsDeep(?,?)");
			st.setString(1, localPath);
			
			List<String> data=new ArrayList<>();
			
			st.setBoolean(2, true);
			ResultSet rs=st.executeQuery();
			while(rs.next()) data.add(rs.getString(1));
			
			st.setBoolean(2, false);
			rs=st.executeQuery();
			while(rs.next()) data.add(rs.getString(1));
			
			return data.toArray(new String[data.size()]);
		}catch(SQLException e){
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public long getSize(@NotNull String localPath){
		try(Connection con=connect();
		    PreparedStatement st=con.prepareStatement("call getFileMeta(?,'FileSize')")){
			st.setString(1, localPath);
			try(ResultSet rs=st.executeQuery()){
				rs.next();
				return rs.getInt(1);
			}
		}catch(SQLException e){
			return -1;
		}
	}
	
	@NotNull
	@Override
	public String getSignature(){
		return "H2:"+path;
	}
	
	@Override
	public long getLastChange(@NotNull String localPath){
		try(Connection con=connect();
		    PreparedStatement st=con.prepareStatement("call getFileMeta(?,'LastModified')")){
			st.setString(1, localPath);
			try(ResultSet rs=st.executeQuery()){
				rs.next();
				return rs.getInt(1);
			}
		}catch(SQLException e){
			return -1;
		}
	}
	
	@Override
	public boolean canEditCreate(@NotNull String localPath){
		return true;
	}
	
	@Override
	public void makeFile(@NotNull String localPath, byte[] data){
		
		try(Connection con=connect();
		    PreparedStatement st=con.prepareStatement("call makeFile(?,?,?)")){
			st.setString(1, localPath);
			st.setBinaryStream(2, new ByteArrayInputStream(data), data.length);
			st.setObject(3, data.length);
			st.execute();
		}catch(SQLException e){
			e.printStackTrace();
		}
		
	}
	
	private static class Exposed extends TByteArrayList{
		public Exposed(){}
		
		byte[] getData(){
			return this._data;
		}
	}
	
	private static final Stack<SoftReference<Exposed>> CACHE=new Stack<>();
	
	private static Exposed pop(){
		synchronized(CACHE){
			while(true){
				if(CACHE.empty()) return new Exposed();
				Exposed e=CACHE.pop().get();
				if(e!=null) return e;
			}
		}
	}
	
	@Override
	@NotNull
	public BufferedOutputStream makeFile(@NotNull String localPath){
		return new BufferedOutputStream(new OutputStream(){
			
			Exposed data=pop();
			
			@Override
			public void write(@NotNull byte[] b, int off, int len){
				data.add(b, off, len);
			}
			
			@Override
			public void write(@NotNull byte[] b){
				data.add(b);
			}
			
			@Override
			public void write(int b){
				data.add((byte)b);
			}
			
			@Override
			public void close(){
				try(Connection con=connect();
				    PreparedStatement st=con.prepareStatement("call makeFile(?,?,?)")){
					st.setString(1, localPath);
					st.setBinaryStream(2, new ByteArrayInputStream(data.getData(), 0, data.size()));
					st.setObject(3, null);
					st.execute();
					
					synchronized(CACHE){
						CACHE.push(new SoftReference<>(data));
					}
				}catch(SQLException e){
					e.printStackTrace();
				}
			}
		});
	}
}
