package com.lapissea.filechange;

import com.lapissea.util.UtilL;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

public class FileChageDetector{
	
	/**
	 * returns if file has changed
	 */
	public static boolean checkChange(File file){
		return checkChange(file, genInfoFile(file));
	}
	
	
	/**
	 * returns if file has changed
	 */
	public static boolean checkChange(File file, File info){
		return read(info).chaged(getInfo(file));
	}
	
	/**
	 * returns if file has changed
	 */
	public static boolean checkChange(File file, FileChangeInfo info){
		return info.chaged(getInfo(file));
	}
	
	/**
	 * returns if file has changed
	 */
	public static boolean checkChange(FileChangeInfo file, File info){
		return read(info).chaged(file);
	}
	
	/**
	 * returns if file has changed
	 */
	public static boolean checkChange(FileChangeInfo file, FileChangeInfo info){
		return info.chaged(file);
	}
	
	/**
	 * returns last change info from file
	 */
	public static FileChangeInfo read(File info){
		try{
			byte[] b=Files.readAllBytes(info.toPath());
			if(b.length!=16) return FileChangeInfo.NULL;
			
			return new FileChangeInfo(UtilL.bytesToLong(0, b), UtilL.bytesToLong(8, b));
		}catch(IOException e){
			return FileChangeInfo.NULL;
		}
	}
	
	/**
	 * reads last change info of a file
	 */
	public static FileChangeInfo getInfo(File file){
		try{
			BasicFileAttributes attr=Files.readAttributes(file.toPath(), BasicFileAttributes.class);
			return new FileChangeInfo(Math.max(attr.lastModifiedTime().toMillis(), attr.creationTime().toMillis()), attr.size());
		}catch(IOException e){
			return FileChangeInfo.NULL;
		}
	}
	
	public static void update(File fileToUpdate){
		update(genInfoFile(fileToUpdate), getInfo(fileToUpdate));
	}
	
	public static void update(File infoFile, FileChangeInfo data){
		try{
			byte[] b=new byte[16];
			UtilL.longToBytes(b, 0, data.changeTime);
			UtilL.longToBytes(b, 8, data.size);
			Files.write(infoFile.toPath(), b);
		}catch(IOException e){
			throw UtilL.uncheckedThrow(e);
		}
	}
	
	public static File genInfoFile(File file){
		return new File(file.getParentFile(), file.getName()+".track");
	}
	
	public static void autoHandle(File file, Runnable onChange){
		autoHandle(file, genInfoFile(file), onChange);
	}
	
	public static void autoHandle(File file, File info, Runnable onChange){
		
		FileChangeInfo infoSrc=FileChageDetector.read(info), dataInfo=FileChageDetector.getInfo(file);
		
		if(checkChange(dataInfo, infoSrc)){
			onChange.run();
			update(info, dataInfo);
		}
	}
}
