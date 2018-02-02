package com.lapissea.datamanager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.stream.Stream;

public abstract class Domain{
	
	public final File source;
	
	public Domain(File source){
		this.source=source;
	}
	
	public abstract BufferedInputStream getInStream(String localPath);
	
	public abstract BufferedReader getReader(String localPath);
	
	public byte[] getBytes(String localPath){
		try{
			return Files.readAllBytes(Paths.get(source.getPath(), localPath));
		}catch(IOException e){
			return null;
		}
	}
	
	public char[] getChars(String localPath){
		try(BufferedReader r=getReader(localPath)){
			if(r==null) return null;
			char[]        charArray=new char[128];
			StringBuilder result   =new StringBuilder();
			for(int rest;(rest=r.read(charArray))!=-1;){
				result.append(charArray, 0, rest);
			}
			if(result.length()!=charArray.length) charArray=new char[result.length()];
			result.getChars(0, result.length(), null, 0);
			return charArray;
		}catch(IOException e){}
		return null;
	}
	
	public String getAll(String localPath){
		return new String(getChars(localPath));
	}
	
	public List<String> getLines(String localPath){
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
	
	public boolean getLines(String localPath, Consumer<String> lineConsumer){
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
	
	public boolean getLines(String localPath, ObjIntConsumer<String> lineConsumer){
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
	
	public abstract String[] getDirNames(String localPath);
	
	public abstract String[] getDirPaths(String localPath);
	
	public abstract String[] getDirPathsDeep(String localPath);
	
	public Stream<String> getDirNamesS(String localPath){
		return Arrays.stream(getDirNames(localPath));
	}
	
	public Stream<String> getDirPathsS(String localPath){
		return Arrays.stream(getDirPaths(localPath));
	}
	
	public Stream<String> getDirPathsDeepS(String localPath){
		return Arrays.stream(getDirPathsDeep(localPath));
	}
	
	@Override
	public String toString(){
		return getClass().getSimpleName()+"{source="+source.getPath()+"}";
	}
	
	public abstract long getSize(String localPath);
}
