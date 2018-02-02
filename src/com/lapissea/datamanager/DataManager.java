package com.lapissea.datamanager;

import com.lapissea.datamanager.domains.DirectoryDomain;
import com.lapissea.datamanager.domains.ZipDomain;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.stream.Stream;

public class DataManager implements IDataManager{
	
	private final List<Function<File, Domain>> detectors=new ArrayList<>(List.of(
		path->path.isDirectory()?new DirectoryDomain(path):null,
		path->{
			if(path.isFile()){
				String extension=path.getName().substring(path.getName().lastIndexOf(".")+1);
				
				if(extension.equals("zip")||extension.equals("jar")){
					return new ZipDomain(path);
				}
			}
			return null;
		}
	
	                                                                            ));
	
	private final List<Domain> domains=new ArrayList<>();
	
	public DataManager registerDomain(File path){
		if(!domains.isEmpty()&&domains.stream().anyMatch(d->d.source.equals(path))) return this;
		domains.add(detectors.stream()
		                     .map(f->f.apply(path))
		                     .filter(Objects::nonNull)
		                     .findAny()
		                     .orElseThrow(()->new RuntimeException("Unrecognised or missing domain: "+path))
		           );
		return this;
	}
	
	@Override
	public BufferedInputStream getInStream(String localPath){
		for(Domain domain : domains){
			try{
				BufferedInputStream t=domain.getInStream(localPath);
				if(t!=null) return t;
			}catch(Exception e){}
		}
		return null;
	}
	
	@Override
	public BufferedReader getReader(String localPath){
		for(Domain domain : domains){
			try{
				BufferedReader t=domain.getReader(localPath);
				if(t!=null) return t;
			}catch(Exception e){}
		}
		return null;
	}
	
	@Override
	public long getSize(String localPath){
		for(Domain domain : domains){
			try{
				long t=domain.getSize(localPath);
				if(t!=-1) return t;
			}catch(Exception e){}
		}
		return -1;
	}
	
	@Override
	public byte[] getBytes(String localPath){
		for(Domain domain : domains){
			try{
				byte[] t=domain.getBytes(localPath);
				if(t!=null) return t;
			}catch(Exception e){}
		}
		return null;
	}
	
	@Override
	public char[] getChars(String localPath){
		for(Domain domain : domains){
			try{
				char[] t=domain.getChars(localPath);
				if(t!=null) return t;
			}catch(Exception e){}
		}
		return null;
	}
	
	@Override
	public String getAll(String localPath){
		for(Domain domain : domains){
			try{
				String t=domain.getAll(localPath);
				if(t!=null) return t;
			}catch(Exception e){}
		}
		return null;
	}
	
	@Override
	public List<String> getLines(String localPath){
		
		for(Domain domain : domains){
			try{
				List<String> t=domain.getLines(localPath);
				if(t!=null) return t;
			}catch(Exception e){}
		}
		return null;
	}
	
	@Override
	public DataManager getLines(String localPath, Consumer<String> lineConsumer){
		for(Domain domain : domains){
			try{
				if(domain.getLines(localPath, lineConsumer)) return this;
			}catch(Exception e){}
		}
		return this;
	}
	
	@Override
	public DataManager getLines(String localPath, ObjIntConsumer<String> lineConsumer){
		for(Domain domain : domains){
			try{
				if(domain.getLines(localPath, lineConsumer)) return this;
			}catch(Exception e){}
		}
		return this;
	}
	
	@Override
	public String[] getDirNames(String localPath){
		for(Domain domain : domains){
			try{
				String[] t=domain.getDirNames(localPath);
				if(t!=null) return t;
			}catch(Exception e){}
		}
		return null;
	}
	
	public String[] getDirPaths(String localPath){
		for(Domain domain : domains){
			try{
				String[] t=domain.getDirPaths(localPath);
				if(t!=null) return t;
			}catch(Exception e){}
		}
		return null;
	}
	
	public String[] getDirPathsDeep(String localPath){
		for(Domain domain : domains){
			try{
				String[] t=domain.getDirPathsDeep(localPath);
				if(t!=null) return t;
			}catch(Exception e){}
		}
		return null;
	}
	
	@Override
	public Stream<String> getDirNamesS(String localPath){
		for(Domain domain : domains){
			try{
				Stream<String> t=domain.getDirNamesS(localPath);
				if(t!=null) return t;
			}catch(Exception e){}
		}
		return null;
	}
	
	public Stream<String> getDirPathsS(String localPath){
		for(Domain domain : domains){
			try{
				Stream<String> t=domain.getDirPathsS(localPath);
				if(t!=null) return t;
			}catch(Exception e){}
		}
		return null;
	}
	
	public Stream<String> getDirPathsDeepS(String localPath){
		for(Domain domain : domains){
			try{
				Stream<String> t=domain.getDirPathsDeepS(localPath);
				if(t!=null) return t;
			}catch(Exception e){}
		}
		return null;
	}
}
