package com.lapissea.datamanager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.stream.Stream;

public class SubDataManager implements IDataManager{
	
	private final IDataManager parent;
	private final String       addPath;
	
	
	public SubDataManager(IDataManager parent, String addPath){
		this.parent=parent;
		this.addPath=Paths.get(addPath).toString()+"/";
	}
	
	private String localToParent(String localPath){
		return addPath+localPath;
	}
	
	@Override
	public BufferedInputStream getInStream(String localPath){
		return parent.getInStream(localToParent(localPath));
	}
	
	@Override
	public BufferedReader getReader(String localPath){
		return parent.getReader(localToParent(localPath));
	}
	
	@Override
	public byte[] getBytes(String localPath){
		return parent.getBytes(localToParent(localPath));
	}
	
	@Override
	public char[] getChars(String localPath){
		return parent.getChars(localToParent(localPath));
	}
	
	@Override
	public String getAll(String localPath){
		return parent.getAll(localToParent(localPath));
	}
	
	@Override
	public List<String> getLines(String localPath){
		return parent.getLines(localToParent(localPath));
	}
	
	@Override
	public IDataManager getLines(String localPath, Consumer<String> lineConsumer){
		parent.getLines(localToParent(localPath), lineConsumer);
		return this;
	}
	
	@Override
	public IDataManager getLines(String localPath, ObjIntConsumer<String> lineConsumer){
		parent.getLines(localToParent(localPath), lineConsumer);
		return this;
	}
	
	@Override
	public String[] getDirNames(String localPath){
		return parent.getDirNames(localToParent(localPath));
	}
	
	
	@Override
	public Stream<String> getDirNamesS(String localPath){
		return parent.getDirNamesS(localToParent(localPath));
	}
	
	
	@Override
	public IDataManager subData(String localPath){
		return parent.subData(localToParent(localPath));
	}
	
	@Override
	public long getSize(String localPath){
		return parent.getSize(localToParent(localPath));
	}
}
