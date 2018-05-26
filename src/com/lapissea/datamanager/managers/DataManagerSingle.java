package com.lapissea.datamanager.managers;

import com.lapissea.datamanager.DataSignature;
import com.lapissea.datamanager.Domain;
import com.lapissea.datamanager.DomainRegistry;
import com.lapissea.datamanager.IDataManager;
import com.lapissea.util.NotNull;
import com.lapissea.util.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.stream.Stream;

@SuppressWarnings("CatchMayIgnoreException")
public class DataManagerSingle implements IDataManager{
	
	private final Domain domain;
	
	public DataManagerSingle(@NotNull String domainPath){
		domain=DomainRegistry.create(domainPath);
	}
	
	@Nullable
	@Override
	public FileChannel getRandomAccess(@NotNull String localPath, @NotNull Mode mode){
		return domain.getRandomAccess(localPath, mode);
	}
	
	@Nullable
	@Override
	public BufferedInputStream getInStream(@NotNull String localPath){
		
		try{
			BufferedInputStream t=domain.getInStream(localPath);
			if(t!=null) return t;
		}catch(Exception e){}
		
		return null;
	}
	
	@Nullable
	@Override
	public BufferedReader getReader(@NotNull String localPath){
		
		try{
			BufferedReader t=domain.getReader(localPath);
			if(t!=null) return t;
		}catch(Exception e){}
		return null;
	}
	
	@Override
	public long getSize(@NotNull String localPath){
		
		
		try{
			long t=domain.getSize(localPath);
			if(t!=-1) return t;
		}catch(Exception e){}
		return -1;
	}
	
	@Nullable
	@Override
	public byte[] readAllBytes(@NotNull String localPath){
		
		try{
			byte[] t=domain.getBytes(localPath);
			if(t!=null) return t;
		}catch(Exception e){}
		return null;
	}
	
	@Nullable
	@Override
	public char[] readAllChars(@NotNull String localPath){
		
		try{
			char[] t=domain.getChars(localPath);
			if(t!=null) return t;
		}catch(Exception e){}
		return null;
	}
	
	@Nullable
	@Override
	public String readAll(@NotNull String localPath){
		
		try{
			String t=domain.readAll(localPath);
			if(t!=null) return t;
		}catch(Exception e){}
		return null;
	}
	
	@Nullable
	@Override
	public List<String> getLines(@NotNull String localPath){
		
		try{
			List<String> t=domain.getLines(localPath);
			if(t!=null) return t;
		}catch(Exception e){}
		return null;
	}
	
	@Override
	public boolean getLines(@NotNull String localPath, @NotNull Consumer<String> lineConsumer){
		
		try{
			if(domain.getLines(localPath, lineConsumer)) return true;
		}catch(Exception e){}
		
		return false;
	}
	
	@Override
	public boolean getLines(@NotNull String localPath, @NotNull ObjIntConsumer<String> lineConsumer){
		
		try{
			if(domain.getLines(localPath, lineConsumer)) return true;
		}catch(Exception e){}
		
		return false;
	}
	
	@Nullable
	@Override
	public String[] getDirNames(@NotNull String localPath){
		
		try{
			String[] t=domain.getDirNames(localPath);
			if(t!=null) return t;
		}catch(Exception e){}
		return null;
	}
	
	@Override
	public boolean exists(@NotNull String localPath){
		
		try{
			if(domain.exists(localPath)) return true;
		}catch(Exception e){}
		return false;
	}
	
	@Nullable
	@Override
	public String[] getDirPaths(@NotNull String localPath){
		
		try{
			String[] t=domain.getDirPaths(localPath);
			if(t!=null) return t;
		}catch(Exception e){}
		return null;
	}
	
	@Nullable
	@Override
	public String[] getDirPathsDeep(@NotNull String localPath){
		
		try{
			String[] t=domain.getDirPathsDeep(localPath);
			if(t!=null) return t;
		}catch(Exception e){}
		return null;
	}
	
	@Nullable
	@Override
	public Stream<String> getDirPathsS(@NotNull String localPath){
		
		try{
			Stream<String> t=domain.getDirPathsS(localPath);
			if(t!=null) return t;
		}catch(Exception e){}
		return null;
	}
	
	@Nullable
	@Override
	public Stream<String> getDirPathsDeepS(@NotNull String localPath){
		
		try{
			Stream<String> t=domain.getDirPathsDeepS(localPath);
			if(t!=null) return t;
		}catch(Exception e){}
		return null;
	}
	
	@Override
	public boolean canEditCreate(@NotNull String localPath){
		return domain.canEditCreate(localPath);
	}
	
	@NotNull
	@Override
	public BufferedOutputStream makeFile(@NotNull String localPath){
		if(domain.canEditCreate(localPath)){
			return domain.makeFile(localPath);
		}
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void makeFile(@NotNull String localPath, @NotNull byte[] data){
		if(domain.canEditCreate(localPath)){
			domain.makeFile(localPath, data);
			return;
		}
		throw new UnsupportedOperationException();
	}
	
	@Nullable
	@Override
	public Stream<String> getDirNamesS(@NotNull String localPath){
		try{
			Stream<String> t=domain.getDirNamesS(localPath);
			if(t!=null) return t;
		}catch(Exception e){}
		return null;
	}
	
	@NotNull
	@Override
	public IDataManager subData(@NotNull String localPath){
		
		if(!exists(localPath)){
			throw new IllegalStateException(localPath+" does not exist in : "+domain.getSignature());
		}
		return new SubDataManager(this, localPath);
	}
	
	@NotNull
	@Override
	public IDataManager subData(@NotNull String... localPaths){
		return new SubDataManagerOrderedFallback(this, localPaths);
	}
	
	@NotNull
	@Override
	public DataSignature createSignature(@NotNull String localPath){
		return new DataSignature(localPath, this);
	}
	
	@Override
	public long getLastChange(@NotNull String localPath){
		
		long time=domain.getLastChange(localPath);
		if(time!=-1) return time;
		return -1;
	}
	
	@NotNull
	@Override
	public String toString(){
		return domain.getSignature();
	}
}
