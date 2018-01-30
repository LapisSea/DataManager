package com.lapissea.datamanager;

import java.io.File;

public class Test{
	
	public static void main(String[] args){
		DataManager m=new DataManager();
		m.registerDomain(new File("").getAbsoluteFile());
		for(String s:m.getDirPathsDeep("")) {
			System.out.println(s);
		}
	}
}
