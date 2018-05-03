package com.lapissea.filechange;

public class FileChangeInfo{
	
	public static final FileChangeInfo NULL=new FileChangeInfo(-1, -1){
		@Override
		boolean chaged(FileChangeInfo file){
			return true;
		}
	};
	
	final long changeTime, size;
	
	public FileChangeInfo(long changeTime, long size){
		this.changeTime=changeTime;
		this.size=size;
	}
	
	@Override
	public int hashCode(){
		return (int)(size+changeTime);
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj==this) return true;
		if(!(obj instanceof FileChangeInfo)) return false;
		FileChangeInfo o=(FileChangeInfo)obj;
		return changeTime==o.changeTime&&size==o.size;
	}
	
	boolean chaged(FileChangeInfo file){
		if(file==NULL) return true;
		if(size!=file.size) return true;
		return changeTime<file.changeTime;
	}
	
	public long getSize(){
		return size;
	}
	
	public long getChangeTime(){
		return changeTime;
	}
}
