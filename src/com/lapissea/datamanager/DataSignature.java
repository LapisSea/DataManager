package com.lapissea.datamanager;

import com.lapissea.util.NotNull;
import com.lapissea.util.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

//TODO: implement all IDataManager functions
public class DataSignature{
	
	@NotNull
	public final String       path;
	@NotNull
	public final IDataManager source;
	
	public DataSignature(@NotNull String path, @NotNull IDataManager source){
		this.path=path;
		this.source=source;
	}
	
	
	@NotNull
	public DataSignature derive(@NotNull Function<String, String> pathChange){
		return source.createSignature(pathChange.apply(path));
	}
	
	@NotNull
	public DataSignature migrate(@NotNull IDataManager dest){
		return dest.createSignature(path);
	}
	
	@Nullable
	public String readAll(){
		return source.readAll(path);
	}
	
	public boolean exists(){
		return source.exists(path);
	}
	
	public boolean canEditCreate(){
		return source.canEditCreate(path);
	}
	
	@NotNull
	public CompletableFuture<byte[]> readAllBytesAsync(){
		return source.readAllBytesAsync(path);
	}
	
	@Nullable
	public byte[] readAllBytes(){
		return source.readAllBytes(path);
	}
	
	public long getLastChange(){
		return source.getLastChange(path);
	}
	
	@NotNull
	@Override
	public String toString(){
		return "DataSignature{"+
		       "path='"+path+'\''+
		       ", source="+source+
		       '}';
	}
	
	@NotNull
	public String getPath(){
		return path;
	}
	
	public boolean newerThan(@NotNull DataSignature other){
		long thisTim=getLastChange(), otherTim=other.getLastChange();
		if(thisTim==-1) throw new RuntimeException("Missing resource: "+this);
		if(otherTim==-1) throw new RuntimeException("Missing resource: "+other);
		return thisTim>otherTim;
	}
	
	public boolean olderThan(@NotNull DataSignature other){
		long thisTim=getLastChange(), otherTim=other.getLastChange();
		if(thisTim==-1) throw new RuntimeException("Missing resource: "+this);
		if(otherTim==-1) throw new RuntimeException("Missing resource: "+other);
		return thisTim<otherTim;
	}
	
	@NotNull
	public BufferedOutputStream makeFile(){
		return source.makeFile(path);
	}
	
	public void makeFile(@NotNull byte[] data){
		source.makeFile(path, data);
	}
	
	public long getSize(){
		return source.getSize(path);
	}
	
	@Nullable
	public BufferedInputStream getInStream(){
		return source.getInStream(path);
	}
}
