package com.lapissea.datamanager.domains;

import com.lapissea.datamanager.Domain;
import com.lapissea.filechange.FileChageDetector;
import com.lapissea.filechange.FileChangeInfo;
import com.lapissea.util.LogUtil;
import com.lapissea.util.NotNull;
import com.lapissea.util.Nullable;
import com.lapissea.util.UtilL;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.lapissea.util.UtilL.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class ZipDomain extends Domain{
	
	public static final String[] EMPTY_STRING=new String[0];
	
	private class ZippedFile{
		@NotNull
		final Path             path;
		@NotNull
		final String           name;
		final boolean          isFolder;
		@NotNull
		final File             cache;
		final List<ZippedFile> children;
		@Nullable
		final FileChangeInfo   change;
		
		private ZippedFile(@NotNull Path path, long size, long lastModifiedTime){
			this.path=path;
			name=path.getName(path.getNameCount()-1).toString();
			isFolder=size==-1;
			change=new FileChangeInfo(lastModifiedTime, size);
			children=isFolder?new ArrayList<>(1):null;
			cache=new File(root.cache, path.toString()+".bin");
		}
		
		private ZippedFile(){
			path=Paths.get(UNCOMPRESSED_CACHE, source.getName()+"_"+Integer.toHexString(source.hashCode()));
			name="";
			isFolder=true;
			change=null;
			children=new ArrayList<>(1);
			cache=new File(path.toString());
		}
		
		@NotNull
		@Override
		public String toString(){
			return (isFolder?"folder: ":"File:   ")+path+", size: "+change.getSize();
		}
		
		@NotNull
		public InputStream getIn(){
			if(isFolder) throw new UnsupportedOperationException("Folders can not be read!");
			
			try{
				if(cache.exists()){
					InputStream in=new FileInputStream(cache);
					byte[]      b =new byte[Long.SIZE/Byte.SIZE];
					in.read(b);
					long l=UtilL.bytesToLong(b);
					if(FileChageDetector.checkChange(new FileChangeInfo(l, cache.length()-b.length), change)){
						in.close();
						cache.delete();
					}else return in;
				}
				
				extract();
				InputStream in=new FileInputStream(cache);
				in.read(new byte[Long.SIZE/Byte.SIZE]);
				return in;
				
			}catch(IOException e){
				throw uncheckedThrow(e);
			}
		}
		
		private void extract(){
			try{
				cache.getParentFile().mkdirs();
				cache.createNewFile();
				try(ZipFile zip=new ZipFile(source)){
					ZipEntry e=zip.getEntry(path.toString().replace('\\', '/'));
					try(InputStream is=zip.getInputStream(e)){
						try(OutputStream os=new FileOutputStream(cache)){
							int    read;
							byte[] bytes=new byte[1024];
							os.write(UtilL.longToBytes(e.getLastModifiedTime().toMillis()));
							while((read=is.read(bytes))!=-1){
								os.write(bytes, 0, read);
							}
						}
					}
				}
			}catch(IOException e){
				e.printStackTrace();
			}
			LogUtil.println("EXTRACTED", path, "TO", cache);
		}
	}
	
	private static final String UNCOMPRESSED_CACHE=".cache/zip/";
	
	private FileChangeInfo changeInfo;
	
	private final ZippedFile root=new ZippedFile();
	
	
	@NotNull
	public final File source;
	
	public ZipDomain(@NotNull String source){
		this.source=new File(source);
		async(this::updateDatabase);
	}
	
	@NotNull
	@Override
	public String toString(){
		return getClass().getSimpleName()+"{source="+source.getPath()+"}";
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj==this) return true;
		if(!(obj instanceof DirectoryDomain)) return true;
		DirectoryDomain other=(DirectoryDomain)obj;
		
		return this.source.equals(other.source);
	}
	
	protected synchronized void updateDatabase(){
		FileChangeInfo newInfo=FileChageDetector.getInfo(source);
		
		if(changeInfo!=null&&!FileChageDetector.checkChange(changeInfo, newInfo)) return;
		changeInfo=newInfo;
		
		
		LogUtil.println("Updating data table of "+source.getName());
		
		root.children.clear();
		
		try(ZipFile zip=new ZipFile(source)){
			zip.stream().sorted(Comparator.comparing(ZipEntry::getName)).forEach(e->{
				
				Path p=Paths.get(e.getName());
				
				ZippedFile last=root;
				
				for(int i=0, count=p.getNameCount();i<count;i++){
					String name=p.getName(i).toString();
					ZippedFile child=last.children.stream()
					                              .filter(f->f.name.equals(name))
					                              .sorted((a, b)->-Boolean.compare(a.isFolder, b.isFolder))
					                              .findFirst().orElse(null);
					if(child==null){
						Path path;
						if(i+1==count) path=p;
						else{
							LinkedList<String> l=new LinkedList<>();
							
							for(int j=0;j<=i;j++){
								l.add(p.getName(j).toString());
							}
							if(l.size()==1) path=Paths.get(l.getFirst());
							else path=Paths.get(l.removeFirst(), l.toArray(new String[l.size()]));
						}
						
						if(i+1==count) child=new ZippedFile(path, e.isDirectory()?-1:e.getSize(), Math.max(e.getLastModifiedTime().toMillis(), e.getCreationTime().toMillis()));
						else child=new ZippedFile(path, -1, -1);
						last.children.add(child);
					}
					
					last=child;
				}
			});
			LogUtil.println("Finished updating data table of "+source.getName());
		}catch(IOException e){
			throw uncheckedThrow(e);
		}
	}
	
	private synchronized ZippedFile get(@NotNull String path, boolean folder){
		ZippedFile last=root;
		Path       p   =Paths.get(path).normalize();
		
		if(p.getNameCount()==1&&p.getName(0).toString().isEmpty()){
			return folder?root:null;
		}
		
		for(int i=0, count=p.getNameCount();i<count;i++){
			String name=p.getName(i).toString();
			
			if(i+1==count){//last
				return last.children.stream()
				                    .filter(f->f.name.equals(name)&&f.isFolder==folder)
				                    .findFirst().orElse(null);
			}
			
			ZippedFile child=last.children.stream()
			                              .filter(f->f.name.equals(name)&&f.isFolder)
			                              .findAny().orElse(null);
			if(child==null) return null;
			last=child;
		}
		return null;
	}
	
	@Override
	public long getSize(@NotNull String localPath){
		ZippedFile cachedFile=get(localPath, false);
		return cachedFile==null?-1:cachedFile.change.getSize();
	}
	
	@Override
	public BufferedInputStream getInStream(@NotNull String localPath){
		ZippedFile cachedFile=get(localPath, false);
		return cachedFile==null?null:new BufferedInputStream(cachedFile.getIn());
	}
	
	@Override
	public BufferedReader getReader(@NotNull String localPath){
		ZippedFile cachedFile=get(localPath, false);
		return cachedFile==null?null:new BufferedReader(new InputStreamReader(cachedFile.getIn()));
	}
	
	@Override
	public boolean exists(@NotNull String localPath){
		ZippedFile cachedFile=get(localPath, true);
		return cachedFile==null&&get(localPath, false)!=null;
	}
	
	@Override
	public byte[] getBytes(@NotNull String localPath){
		try(BufferedInputStream in=getInStream(localPath)){
			if(in==null) return null;
			byte[] buffer=new byte[Math.toIntExact(getSize(localPath))];
			in.read(buffer);
			return buffer;
		}catch(IOException e){
			return null;
		}
	}
	
	@Override
	public String[] getDirNames(@NotNull String localPath){
		ZippedFile cachedFile=get(localPath, true);
		if(cachedFile==null) return EMPTY_STRING;
		
		return cachedFile.children.stream().map(f->f.name).toArray(String[]::new);
	}
	
	@Override
	public String[] getDirPaths(@NotNull String localPath){
		ZippedFile cachedFile=get(localPath, true);
		if(cachedFile==null) return EMPTY_STRING;
		
		return cachedFile.children.stream().map(f->f.path.toString()).toArray(String[]::new);
	}
	
	private void listAllEntries(@NotNull String dir, @NotNull List<ZippedFile> files){
		ZippedFile cachedFile=get(dir, true);
		if(cachedFile==null) return;
		for(ZippedFile child : cachedFile.children){
			files.add(child);
			if(child.isFolder) listAllEntries(child.path.toString(), files);
		}
	}
	
	@Override
	public String[] getDirPathsDeep(@NotNull String localPath){
		List<ZippedFile> files=new ArrayList<>();
		listAllEntries(localPath, files);
		return files.stream().map(e->e.path.toString()).toArray(String[]::new);
	}
	
	@NotNull
	@Override
	public String getSignature(){
		return "JAR:"+source.toString();
	}
	
	@Override
	public long getLastChange(@NotNull String localPath){
		ZippedFile cachedFile=get(localPath, false);
		return cachedFile==null?-1:cachedFile.change.getChangeTime();
	}
	
}
