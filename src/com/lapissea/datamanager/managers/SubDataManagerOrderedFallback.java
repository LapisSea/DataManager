package com.lapissea.datamanager.managers;

import com.lapissea.datamanager.DataSignature;
import com.lapissea.datamanager.IDataManager;
import com.lapissea.util.NotNull;
import com.lapissea.util.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SubDataManagerOrderedFallback implements IDataManager{
	
	@NotNull
	private final IDataManager parent;
	@NotNull
	private final String[]     addPaths;
	
	
	public SubDataManagerOrderedFallback(@NotNull IDataManager parent, @NotNull String[] addPaths){
		if(addPaths.length<2) throw new IllegalArgumentException("Path count has to be at least 2!");
		
		this.parent=parent;
		
		this.addPaths=new String[addPaths.length];
		for(int i=0;i<addPaths.length;i++){
			this.addPaths[i]=Paths.get(addPaths[i]).toString()+"/";
		}
	}
	
	private String localToParent(String localPath, int id){
		return addPaths[id]+localPath;
	}
	
	@Nullable
	@Override
	public FileChannel getRandomAccess(@NotNull String localPath, @NotNull Mode mode){
		for(int i=0;i<addPaths.length;i++){
			FileChannel s=parent.getRandomAccess(localToParent(localPath, i), mode);
			if(s!=null) return s;
		}
		return null;
	}
	
	@Nullable
	@Override
	public BufferedInputStream getInStream(@NotNull String localPath){
		for(int i=0;i<addPaths.length;i++){
			BufferedInputStream s=parent.getInStream(localToParent(localPath, i));
			if(s!=null) return s;
		}
		return null;
	}
	
	@Nullable
	@Override
	public BufferedReader getReader(@NotNull String localPath){
		for(int i=0;i<addPaths.length;i++){
			BufferedReader s=parent.getReader(localToParent(localPath, i));
			if(s!=null) return s;
		}
		return null;
	}
	
	@Nullable
	@Override
	public byte[] readAllBytes(@NotNull String localPath){
		for(int i=0;i<addPaths.length;i++){
			byte[] s=parent.readAllBytes(localToParent(localPath, i));
			if(s!=null) return s;
		}
		return null;
	}
	
	@Nullable
	@Override
	public char[] readAllChars(@NotNull String localPath){
		for(int i=0;i<addPaths.length;i++){
			char[] s=parent.readAllChars(localToParent(localPath, i));
			if(s!=null) return s;
		}
		return null;
	}
	
	@Nullable
	@Override
	public String readAll(@NotNull String localPath){
		for(int i=0;i<addPaths.length;i++){
			String s=parent.readAll(localToParent(localPath, i));
			if(s!=null) return s;
		}
		return null;
	}
	
	@Nullable
	@Override
	public List<String> getLines(@NotNull String localPath){
		for(int i=0;i<addPaths.length;i++){
			List<String> s=parent.getLines(localToParent(localPath, i));
			if(s!=null) return s;
		}
		return null;
	}
	
	@Override
	public boolean getLines(@NotNull String localPath, @NotNull Consumer<String> lineConsumer){
		for(int i=0;i<addPaths.length;i++){
			if(parent.getLines(localToParent(localPath, i), lineConsumer)) return true;
		}
		return false;
	}
	
	@Override
	public boolean getLines(@NotNull String localPath, @NotNull ObjIntConsumer<String> lineConsumer){
		for(int i=0;i<addPaths.length;i++){
			if(parent.getLines(localToParent(localPath, i), lineConsumer)) return true;
		}
		return false;
	}
	
	@Nullable
	@Override
	public String[] getDirNames(@NotNull String localPath){
		for(int i=0;i<addPaths.length;i++){
			String[] s=parent.getDirNames(localToParent(localPath, i));
			if(s!=null) return s;
		}
		return null;
	}
	
	@Override
	public boolean exists(@NotNull String localPath){
		for(int i=0;i<addPaths.length;i++){
			if(parent.exists(localToParent(localPath, i))) return true;
		}
		return false;
	}
	
	@Nullable
	@Override
	public Stream<String> getDirNamesS(@NotNull String localPath){
		for(int i=0;i<addPaths.length;i++){
			Stream<String> s=parent.getDirNamesS(localToParent(localPath, i));
			if(s!=null) return s;
		}
		return null;
	}
	
	@NotNull
	@Override
	public IDataManager subData(@NotNull String localPath){
		String[] newPaths=new String[addPaths.length];
		for(int i=0;i<newPaths.length;i++){
			newPaths[i]=Paths.get(addPaths[i]).toString()+"/";
		}
		return new SubDataManagerOrderedFallback(parent, newPaths);
	}
	
	@NotNull
	@Override
	public IDataManager subData(@NotNull String... localPaths){
		String[] newPaths=new String[addPaths.length*localPaths.length];
		
		for(int i=0;i<localPaths.length;i++){
			for(int j=0;j<addPaths.length;j++){
				newPaths[i*localPaths.length+j]=Paths.get(addPaths[j], localPaths[i]).toString()+"/";
			}
		}
		
		return new SubDataManagerOrderedFallback(parent, newPaths);
	}
	
	@Override
	public long getSize(@NotNull String localPath){
		for(int i=0;i<addPaths.length;i++){
			long s=parent.getSize(localToParent(localPath, i));
			if(s >= 0) return s;
		}
		return -1;
	}
	
	@Nullable
	@Override
	public String[] getDirPaths(@NotNull String localPath){
		for(int i=0;i<addPaths.length;i++){
			String[] s=parent.getDirPaths(localToParent(localPath, i));
			if(s!=null) return s;
		}
		return null;
	}
	
	@Nullable
	@Override
	public String[] getDirPathsDeep(@NotNull String localPath){
		for(int i=0;i<addPaths.length;i++){
			String[] s=parent.getDirPathsDeep(localToParent(localPath, i));
			if(s!=null) return s;
		}
		return null;
	}
	
	@Nullable
	@Override
	public Stream<String> getDirPathsS(@NotNull String localPath){
		for(int i=0;i<addPaths.length;i++){
			Stream<String> s=parent.getDirPathsS(localToParent(localPath, i));
			if(s!=null) return s;
		}
		return null;
	}
	
	@Nullable
	@Override
	public Stream<String> getDirPathsDeepS(@NotNull String localPath){
		for(int i=0;i<addPaths.length;i++){
			Stream<String> s=parent.getDirPathsDeepS(localToParent(localPath, i));
			if(s!=null) return s;
		}
		return null;
	}
	
	@Override
	public boolean canEditCreate(@NotNull String localPath){
		for(int i=0;i<addPaths.length;i++){
			if(parent.canEditCreate(localToParent(localPath, i))) return true;
		}
		return false;
	}
	
	@NotNull
	@Override
	public BufferedOutputStream makeFile(@NotNull String localPath){
		for(int i=0;i<addPaths.length;i++){
			String path=localToParent(localPath, i);
			if(parent.canEditCreate(path)) return makeFile(path);
		}
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void makeFile(@NotNull String localPath, @NotNull byte[] data){
		for(int i=0;i<addPaths.length;i++){
			String path=localToParent(localPath, i);
			if(parent.canEditCreate(path)){
				parent.makeFile(path, data);
				return;
			}
		}
		throw new UnsupportedOperationException();
	}
	
	@NotNull
	@Override
	public DataSignature createSignature(@NotNull String localPath){
		return new DataSignature(localPath, this);
	}
	
	@Override
	public long getLastChange(@NotNull String localPath){
		for(int i=0;i<addPaths.length;i++){
			long s=parent.getLastChange(localToParent(localPath, i));
			if(s >= 0) return s;
		}
		return -1;
	}
	
	@NotNull
	@Override
	public String toString(){
		return parent+" ["+Arrays.stream(addPaths).collect(Collectors.joining(", "))+"]";
	}
}
