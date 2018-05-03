package com.lapissea.datamanager;

import com.lapissea.datamanager.domains.DirectoryDomain;
import com.lapissea.datamanager.domains.ZipDomain;
import com.lapissea.datamanager.domains.db.DatabaseDomain;
import com.lapissea.util.NotNull;
import com.lapissea.util.UtilL;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.lapissea.util.UtilL.*;

public class DomainRegistry{
	
	private static class DomainTicket{
		Predicate<String>        whenToUse;
		Class<? extends Domain>  domainClass;
		Function<String, Domain> ctor;
		
		public DomainTicket(Predicate<String> whenToUse, Class<? extends Domain> domainClass){
			this.whenToUse=whenToUse;
			this.domainClass=domainClass;
			Constructor<? extends Domain> ctor;
			try{
				ctor=domainClass.getDeclaredConstructor(String.class);
			}catch(NoSuchMethodException e){
				throw new RuntimeException("Missing constructor "+domainClass.getSimpleName()+"(String) in "+domainClass.getName());
			}
			ctor.setAccessible(true);
			this.ctor=s->{
				try{
					return ctor.newInstance(s);
				}catch(Exception e){
					throw uncheckedThrow(e);
				}
			};
		}
		
		public boolean test(String path){
			return whenToUse.test(path);
		}
		
		public Domain create(String path){
			return ctor.apply(path);
		}
		
	}
	
	private static List<DomainTicket> DETECTORS=new ArrayList<>(3);
	
	static{
		List<String> compressedFileList=Arrays.asList("gz", "zip", "jar");
		
		registerDomainType(p->new File(p).isDirectory(), DirectoryDomain.class);
		registerDomainType(p->compressedFileList.contains(UtilL.fileExtension(p))&&new File(p).isFile(), ZipDomain.class);
		registerDomainType(p->{
			if(p.endsWith(".sql")) p=p.substring(0, p.length()-4);
			if(p.endsWith(".mv.db")) p=p.substring(0, p.length()-6);
			return new File(p+".mv.db").isFile()||new File(p+".sql").isFile();
		}, DatabaseDomain.class);
	}
	
	public static synchronized void registerDomainType(Predicate<String> whenToUse, Class<? extends Domain> domainClass){
		if(DETECTORS.stream().anyMatch(t->t.domainClass==domainClass)) throw new IllegalArgumentException(domainClass.getName()+" is already registered!");
		DETECTORS.add(new DomainTicket(whenToUse, domainClass));
	}
	
	public static Domain create(@NotNull String path){
		String safePath=path.isEmpty()?".":path;
		return DETECTORS.stream()
		                .filter(t->t.test(safePath))
		                .findFirst()
		                .map(t->t.create(safePath))
		                .orElseThrow(()->new RuntimeException("Unrecognised, unsupported or missing domain: "+path));
	}
	
}
