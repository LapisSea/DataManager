package com.lapissea.datamanager.domains.db;

import java.nio.file.Paths;

public class SqlUtil{
	
	public static void fixPath(Object[] newRow){
		newRow[2]=Paths.get((String)newRow[2]).normalize().toString();
	}
	
	public static String fixPath(String path){
		String correctPath=Paths.get(path).normalize().toString();
		while(correctPath.startsWith("\\")||correctPath.startsWith("/")) correctPath=correctPath.substring(1);
		return correctPath;
	}
}
