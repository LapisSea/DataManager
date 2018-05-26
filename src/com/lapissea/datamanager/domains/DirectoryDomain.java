package com.lapissea.datamanager.domains;

import com.lapissea.datamanager.Domain;
import com.lapissea.datamanager.IDataManager;
import com.lapissea.util.NotNull;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static com.lapissea.util.UtilL.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class DirectoryDomain extends Domain{
	
	
	@NotNull
	public final File source;
	
	public DirectoryDomain(@NotNull String source){
		this.source=new File(source);
	}
	
	@NotNull
	@Override
	public String toString(){
		return getClass().getSimpleName()+"{source="+source.getPath()+"}";
	}
	
	private File local(@NotNull String local){
		return new File(source, local.isEmpty()?".":local);
	}
	
	private Path path(String local){
		return Paths.get(source.getPath(), local.isEmpty()?".":local);
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj==this) return true;
		if(!(obj instanceof DirectoryDomain)) return false;
		DirectoryDomain other=(DirectoryDomain)obj;
		
		return this.source.equals(other.source);
	}
	
	@Override
	public byte[] getBytes(@NotNull String localPath){
		try{
			return Files.readAllBytes(path(localPath));
		}catch(IOException e){
			return null;
		}
	}
	
	@Override
	public BufferedInputStream getInStream(@NotNull String localPath){
		try{
			return new BufferedInputStream(new FileInputStream(local(localPath)));
		}catch(FileNotFoundException e){
			return null;
		}
	}
	
	@Override
	public BufferedReader getReader(@NotNull String localPath){
		try{
			return new BufferedReader(new FileReader(local(localPath)));
		}catch(FileNotFoundException e){
			return null;
		}
	}
	
	@Override
	public boolean exists(@NotNull String localPath){
		return local(localPath).exists();
	}
	
	@Override
	public String[] getDirNames(@NotNull String localPath){
		File f=local(localPath);
		return f.list();
	}
	
	@Override
	public String[] getDirPaths(@NotNull String localPath){
		String[] names=getDirNames(localPath);
		if(names==null) return null;
		
		for(int i=0;i<names.length;i++){
			names[i]=localPath+File.separator+names[i];
		}
		return names;
	}
	
	
	private void listf(@NotNull File dir, @NotNull List<String> files){
		File[] fList=dir.listFiles();
		if(fList==null) return;
		
		for(File file : fList){
			if(file.isFile()){
				files.add(file.getPath());
			}else if(file.isDirectory()){
				listf(file, files);
			}
		}
	}
	
	@Override
	public String[] getDirPathsDeep(@NotNull String localPath){
		List<String> files=new ArrayList<>();
		listf(local(localPath), files);
		return files.toArray(new String[files.size()]);
	}
	
	@Override
	public long getSize(@NotNull String localPath){
		return local(localPath).length();
	}
	
	@NotNull
	@Override
	public String getSignature(){
		return source.getPath();
	}
	
	@Override
	public boolean canEditCreate(@NotNull String localPath){
		return true;
	}
	
	@NotNull
	@Override
	public BufferedOutputStream makeFile(@NotNull String localPath){
		File f=local(localPath);
		f.getParentFile().mkdirs();
		try{
			f.createNewFile();
			return new BufferedOutputStream(new FileOutputStream(f));
		}catch(IOException e){
			throw uncheckedThrow(e);
		}
	}
	
	@Override
	public void makeFile(@NotNull String localPath, byte[] data){
		try(OutputStream os=makeFile(localPath)){
			os.write(data);
			os.flush();
		}catch(IOException e){
			throw uncheckedThrow(e);
		}
	}
	
	@Override
	public long getLastChange(@NotNull String localPath){
		try{
			BasicFileAttributes attr=Files.readAttributes(path(localPath), BasicFileAttributes.class);
			return Math.max(attr.lastModifiedTime().toMillis(), attr.creationTime().toMillis());
		}catch(IOException e){
			return -1;
		}
	}
	
	
	@Override
	public FileChannel getRandomAccess(String localPath, IDataManager.Mode mode){
		try{
			return new RandomAccessFile(local(localPath), mode.handle).getChannel();
		}catch(FileNotFoundException e){
			return null;
		}
	}
}
