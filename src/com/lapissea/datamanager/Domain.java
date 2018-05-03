package com.lapissea.datamanager;

import com.lapissea.util.NotNull;
import com.lapissea.util.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.stream.Stream;

public abstract class Domain{
	
	@Nullable
	public abstract BufferedInputStream getInStream(@NotNull String localPath);
	
	@Nullable
	public abstract BufferedReader getReader(@NotNull String localPath);
	
	public abstract boolean exists(@NotNull String localPath);
	
	@Nullable
	public abstract byte[] getBytes(@NotNull String localPath);
	
	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Nullable
	public char[] getChars(@NotNull String localPath){
		try(BufferedReader r=getReader(localPath)){
			if(r==null) return null;
			long size=getSize(localPath);
			if(size>Integer.MAX_VALUE) throw new IOException("Trying to read extremely large file");
			
			char[] charArray=new char[(int)size];
			r.read(charArray);
			return charArray;
		}catch(IOException e){}
		return null;
	}
	
	@Nullable
	public String readAll(@NotNull String localPath){
		char[] ch=getChars(localPath);
		if(ch==null) return null;
		return new String(ch);
	}
	
	@Nullable
	public List<String> getLines(@NotNull String localPath){
		try(BufferedReader r=getReader(localPath)){
			if(r==null) return null;
			BufferedReader    b=new BufferedReader(r);
			ArrayList<String> l=new ArrayList<>();
			for(String line;(line=b.readLine())!=null;){
				l.add(line);
			}
			return l;
		}catch(IOException e){}
		return null;
	}
	
	public boolean getLines(@NotNull String localPath, @NotNull Consumer<String> lineConsumer){
		try(BufferedReader r=getReader(localPath)){
			if(r==null) return false;
			BufferedReader b=new BufferedReader(r);
			for(String line;(line=b.readLine())!=null;){
				lineConsumer.accept(line);
			}
			return true;
		}catch(IOException e){}
		return false;
	}
	
	public boolean getLines(@NotNull String localPath, @NotNull ObjIntConsumer<String> lineConsumer){
		try(BufferedReader r=getReader(localPath)){
			if(r==null) return false;
			BufferedReader b=new BufferedReader(r);
			int            i=0;
			for(String line;(line=b.readLine())!=null;){
				lineConsumer.accept(line, i++);
			}
			return true;
		}catch(IOException e){}
		return false;
	}
	
	@Nullable
	public abstract String[] getDirNames(@NotNull String localPath);
	
	@Nullable
	public abstract String[] getDirPaths(@NotNull String localPath);
	
	@Nullable
	public abstract String[] getDirPathsDeep(@NotNull String localPath);
	
	@Nullable
	public Stream<String> getDirNamesS(@NotNull String localPath){
		String[] r=getDirNames(localPath);
		if(r==null) return null;
		return Arrays.stream(r);
	}
	
	@Nullable
	public Stream<String> getDirPathsS(@NotNull String localPath){
		String[] r=getDirPaths(localPath);
		if(r==null) return null;
		return Arrays.stream(r);
	}
	
	@Nullable
	public Stream<String> getDirPathsDeepS(@NotNull String localPath){
		String[] r=getDirPathsDeep(localPath);
		if(r==null) return null;
		return Arrays.stream(r);
	}
	
	
	public abstract long getSize(@NotNull String localPath);
	
	@NotNull
	public abstract String getSignature();
	
	public boolean canEditCreate(@NotNull String localPath){
		return false;
	}
	
	@NotNull
	public BufferedOutputStream makeFile(@NotNull String localPath){
		throw new UnsupportedOperationException();
	}
	
	public void makeFile(@NotNull String localPath, byte[] data){
		throw new UnsupportedOperationException();
	}
	
	public abstract long getLastChange(@NotNull String localPath);
}
