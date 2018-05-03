package com.lapissea.datamanager.managers;

import com.lapissea.datamanager.DataSignature;
import com.lapissea.datamanager.IDataManager;
import com.lapissea.util.NotNull;
import com.lapissea.util.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.stream.Stream;

public class SubDataManager implements IDataManager{
	
	@NotNull
	private final IDataManager parent;
	@NotNull
	private final String       addPath;
	
	
	public SubDataManager(@NotNull IDataManager parent, @NotNull String addPath){
		this.parent=parent;
		this.addPath=Paths.get(addPath).toString()+"/";
	}
	
	private String localToParent(String localPath){
		return addPath+localPath;
	}
	
	@Nullable
	@Override
	public BufferedInputStream getInStream(@NotNull String localPath){
		return parent.getInStream(localToParent(localPath));
	}
	
	@Nullable
	@Override
	public BufferedReader getReader(@NotNull String localPath){
		return parent.getReader(localToParent(localPath));
	}
	
	@Nullable
	@Override
	public byte[] readAllBytes(@NotNull String localPath){
		return parent.readAllBytes(localToParent(localPath));
	}
	
	@Nullable
	@Override
	public char[] readAllChars(@NotNull String localPath){
		return parent.readAllChars(localToParent(localPath));
	}
	
	@Nullable
	@Override
	public String readAll(@NotNull String localPath){
		return parent.readAll(localToParent(localPath));
	}
	
	@Nullable
	@Override
	public List<String> getLines(@NotNull String localPath){
		return parent.getLines(localToParent(localPath));
	}
	
	@NotNull
	@Override
	public IDataManager getLines(@NotNull String localPath, @NotNull Consumer<String> lineConsumer){
		parent.getLines(localToParent(localPath), lineConsumer);
		return this;
	}
	
	@NotNull
	@Override
	public IDataManager getLines(@NotNull String localPath, @NotNull ObjIntConsumer<String> lineConsumer){
		parent.getLines(localToParent(localPath), lineConsumer);
		return this;
	}
	
	@Nullable
	@Override
	public String[] getDirNames(@NotNull String localPath){
		return parent.getDirNames(localToParent(localPath));
	}
	
	@Override
	public boolean exists(@NotNull String localPath){
		return parent.exists(localToParent(localPath));
	}
	
	@Nullable
	@Override
	public Stream<String> getDirNamesS(@NotNull String localPath){
		return parent.getDirNamesS(localToParent(localPath));
	}
	
	@NotNull
	@Override
	public IDataManager subData(@NotNull String localPath){
		return parent.subData(localToParent(localPath));
	}
	
	@Override
	public long getSize(@NotNull String localPath){
		return parent.getSize(localToParent(localPath));
	}
	
	@Nullable
	@Override
	public String[] getDirPaths(@NotNull String localPath){
		return parent.getDirPaths(localToParent(localPath));
	}
	
	@Nullable
	@Override
	public String[] getDirPathsDeep(@NotNull String localPath){
		return parent.getDirPathsDeep(localToParent(localPath));
	}
	
	@Nullable
	@Override
	public Stream<String> getDirPathsS(@NotNull String localPath){
		return parent.getDirPathsS(localToParent(localPath));
	}
	
	@Nullable
	@Override
	public Stream<String> getDirPathsDeepS(@NotNull String localPath){
		return parent.getDirPathsDeepS(localToParent(localPath));
	}
	
	@Override
	public boolean canEditCreate(@NotNull String localPath){
		return parent.canEditCreate(localToParent(localPath));
	}
	
	@NotNull
	@Override
	public BufferedOutputStream makeFile(@NotNull String localPath){
		return parent.makeFile(localToParent(localPath));
	}
	
	@Override
	public void makeFile(@NotNull String localPath, byte[] data){
		parent.makeFile(localToParent(localPath), data);
	}
	
	@NotNull
	@Override
	public DataSignature createSignature(@NotNull String localPath){
		return new DataSignature(localPath, this);
	}
	
	@Override
	public long getLastChange(@NotNull String localPath){
		return parent.getLastChange(localToParent(localPath));
	}
	
	@NotNull
	@Override
	public String toString(){
		return addPath+parent;
	}
}
