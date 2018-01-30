package com.lapissea.datamanager.domains;

import com.lapissea.datamanager.Domain;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DirectoryDomain extends Domain{
	
	public DirectoryDomain(File source){
		super(source);
	}
	
	@Override
	public BufferedInputStream getInStream(String localPath){
		try{
			return new BufferedInputStream(new FileInputStream(new File(source, localPath)));
		}catch(FileNotFoundException e){
			return null;
		}
	}
	
	@Override
	public BufferedReader getReader(String localPath){
		try{
			return new BufferedReader(new FileReader(new File(source, localPath)));
		}catch(FileNotFoundException e){
			return null;
		}
	}
	
	@Override
	public String[] getDirNames(String localPath){
		File f=new File(source, localPath);
		return f.list();
	}
	
	@Override
	public String[] getDirPaths(String localPath){
		String[] names=getDirNames(localPath);
		for(int i=0;i<names.length;i++){
			names[i]=new File(source, localPath+File.separator+names[i]).getPath();
		}
		return names;
	}
	
	
	private void listf(File dir, List<String> files){
		File[] fList=dir.listFiles();
		for(File file : fList){
			if(file.isFile()){
				files.add(file.getPath());
			}else if(file.isDirectory()){
				listf(file, files);
			}
		}
	}
	
	@Override
	public String[] getDirPathsDeep(String localPath){
		List<String> files=new ArrayList<>();
		listf(new File(source, localPath), files);
		return files.toArray(new String[files.size()]);
	}
}
