package com.lapissea.datamanager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.stream.Stream;

public interface IDataManager{
	
	BufferedInputStream getInStream(String localPath);
	
	BufferedReader getReader(String localPath);
	
	byte[] getBytes(String localPath);
	
	char[] getChars(String localPath);
	
	String getAll(String localPath);
	
	List<String> getLines(String localPath);
	
	IDataManager getLines(String localPath, Consumer<String> lineConsumer);
	
	IDataManager getLines(String localPath, ObjIntConsumer<String> lineConsumer);
	
	String[] getDirNames(String localPath);
	
	Stream<String> getDirNamesS(String localPath);
	
	default IDataManager subData(String localPath){
		return new SubDataManager(this, localPath);
	}
}
