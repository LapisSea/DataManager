package com.lapissea.datamanager;

import com.lapissea.datamanager.managers.DataManagerMulti;
import com.lapissea.util.NotNull;
import com.lapissea.util.Nullable;
import com.lapissea.util.UtilL;
import com.lapissea.util.function.UnsafeFunction;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ObjIntConsumer;
import java.util.stream.Stream;

import static com.lapissea.util.UtilL.*;

/**
 * <h2>Concepts</h2>
 * <b>domain</b>
 * <p>
 * A path and all its contents handled by an implementation of {@link Domain}.<br>
 * Can be interchangeable with a folder or compressed file, depending on context.
 * </p>
 * <b>local path</b>
 * <p>
 * Path that represents the end portion of any domain. <br><br>
 * EG:<br>
 * registered domain = <code>resources/gamedata.zip</code><br>
 * localPath = <code>textures/sky.hdr</code><br>
 * <br>
 * AKA: <code>./resources/gamedata.zip/textures/sky.hdr</code> in an example format<br>
 * Opens gamedata zip and in it searches for textures/sky.hdr
 * </p>
 */
public interface IDataManager{
	/**
	 * A manager for the directory where it was ran
	 */
	IDataManager APP_RUN_DIR=new DataManagerMulti(".");
	/**
	 * A manager for appdata
	 */
	IDataManager APPDATA_DIR=new DataManagerMulti(UtilL.getAppData());
	
	/**
	 * <p>Function used to get a raw {@link InputStream} of some resource provided by registered {@link Domain}.</p>
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @return {@link BufferedInputStream} that is a wrapper of specific {@link Domain} implementation.<br>
	 * <ul>
	 * <li>System file: {@link java.io.FileInputStream}</li>
	 * <li>File inside compressed folder: {@link java.util.jar.JarInputStream} (without caching)</li>
	 * <li>...</li>
	 * </ul>
	 */
	@Nullable
	BufferedInputStream getInStream(@NotNull String localPath);
	
	/**
	 * Runs {@link #getInStream(String)} in a {@link CompletableFuture} where if result fails (aka is null),
	 * throws exception so it cen be handled properly with functions like {@link CompletableFuture#exceptionally}.
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @param onRead    Callback to process {@link BufferedInputStream} and return a "parsed" result
	 * @param <Out>     resulting output type defined by {@code onRead}
	 * @return {@link CompletableFuture} that will return object provided by {@code onRead}
	 * @see #getInStream(String)
	 */
	@NotNull
	default <Out> CompletableFuture<Out> readBytesAsync(@NotNull String localPath, @NotNull UnsafeFunction<BufferedInputStream, Out, Exception> onRead){
		return async(()->{
			try(BufferedInputStream s=getInStream(localPath)){
				return onRead.apply(Objects.requireNonNull(s));
			}catch(Exception e){
				throw uncheckedThrow(e);
			}
		});
	}
	
	/**
	 * <p>Function used to get a raw {@link Reader} of some resource provided by registered {@link Domain}.</p>
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @return {@link java.io.BufferedReader} that is a wrapper of specific {@link Domain} implementation.<br>
	 * <ul>
	 * <li>System file: {@link java.io.FileReader}</li>
	 * <li>File inside compressed folder: {@link java.io.InputStreamReader}</li>
	 * <li>...</li>
	 * </ul>
	 */
	@Nullable
	BufferedReader getReader(@NotNull String localPath);
	
	/**
	 * <p>Runs {@link #getReader(String)} in a {@link CompletableFuture} where if result fails (aka is null),
	 * throws exception so it cen be handled properly with functions like {@link CompletableFuture#exceptionally}.</p>
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @param onRead    Callback to process {@link BufferedReader} and return a "parsed" result
	 * @param <Out>     resulting output type defined by {@code onRead}
	 * @return {@link CompletableFuture} that will return object provided by {@code onRead}
	 * @see #getReader(String)
	 */
	@NotNull
	default <Out> CompletableFuture<Out> readCharsAsync(@NotNull String localPath, @NotNull Function<BufferedReader, Out> onRead){
		return async(()->{
			try(BufferedReader s=getReader(localPath)){
				return onRead.apply(Objects.requireNonNull(s));
			}catch(IOException e){
				throw uncheckedThrow(e);
			}
		});
	}
	
	/**
	 * <p>Function used to get all bytes of some resource provided by registered {@link Domain}.</p>
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @return {@code byte[]} with contents of the resource
	 */
	@Nullable
	byte[] readAllBytes(@NotNull String localPath);
	
	/**
	 * <p>Runs {@link #readAllBytes(String)} in a {@link CompletableFuture} where if result fails (aka is null),
	 * throws exception so it cen be handled properly with functions like {@link CompletableFuture#exceptionally}.</p>
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @return {@link CompletableFuture} that will return {@code byte[]} with contents of the resource
	 * @see #readAllBytes(String)
	 */
	@NotNull
	default CompletableFuture<byte[]> readAllBytesAsync(@NotNull String localPath){
		return async(()->Objects.requireNonNull(readAllBytes(localPath)));
	}
	
	/**
	 * <p>Function used to get all characters of some resource provided by registered {@link Domain}.</p>
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @return {@code char[]} with contents of the resource
	 */
	@Nullable
	char[] readAllChars(@NotNull String localPath);
	
	/**
	 * <p>Runs {@link #readAllChars(String)} in a {@link CompletableFuture} where if result fails (aka is null),
	 * throws exception so it cen be handled properly with functions like {@link CompletableFuture#exceptionally}.</p>
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @return {@link CompletableFuture} that will return {@code char[]} with contents of the resource
	 * @see #readAllChars(String)
	 */
	@NotNull
	default CompletableFuture<char[]> readAllCharsAsync(@NotNull String localPath){
		return async(()->Objects.requireNonNull(readAllChars(localPath)));
	}
	
	/**
	 * <p>Function used to get all contents of some resource in a string provided by registered {@link Domain}.</p>
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @return {@link String} with contents of the resource
	 */
	@Nullable
	String readAll(@NotNull String localPath);
	
	/**
	 * <p>Runs {@link #readAll(String)} in a {@link CompletableFuture} where if result fails (aka is null),
	 * throws exception so it cen be handled properly with functions like {@link CompletableFuture#exceptionally}.</p>
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @return {@link CompletableFuture} that will return {@link String} with contents of the resource
	 * @see #readAll(String)
	 */
	@NotNull
	default CompletableFuture<String> readAllAsync(@NotNull String localPath){
		return async(()->Objects.requireNonNull(readAll(localPath)));
	}
	
	/**
	 * <p>Function used to get all lines of a text based resource provided by registered {@link Domain}.</p>
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @return {@link List} with all lines of the resource
	 */
	@Nullable
	List<String> getLines(@NotNull String localPath);
	
	/**
	 * <p>Runs {@link #getLines(String)} in a {@link CompletableFuture} where if result fails (aka is null),
	 * throws exception so it cen be handled properly with functions like {@link CompletableFuture#exceptionally}.</p>
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @return {@link CompletableFuture} that will return {@link List} with all lines of the resource
	 * @see #readAll(String)
	 */
	@NotNull
	default CompletableFuture<List<String>> getLinesAsync(@NotNull String localPath){
		return async(()->Objects.requireNonNull(getLines(localPath)));
	}
	
	/**
	 * <p>Function used to get all lines of a text based resource provided in a callback fashion by registered {@link Domain}.</p>
	 *
	 * @param localPath    see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @param lineConsumer {@link Consumer} that will be given individual line by line of a resource provided by registered {@link Domain}.
	 * @return this object (for chaining)
	 */
	@NotNull
	IDataManager getLines(@NotNull String localPath, @NotNull Consumer<String> lineConsumer);
	
	/**
	 * <p>Function used to get all lines of a text with its index (line number) based resource provided in a callback fashion by registered {@link Domain}.</p>
	 *
	 * @param localPath    see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @param lineConsumer {@link Consumer} that will be given individual line by line of a resource provided by registered {@link Domain}.
	 * @return this object (for chaining)
	 */
	@NotNull
	IDataManager getLines(@NotNull String localPath, @NotNull ObjIntConsumer<String> lineConsumer);
	
	/**
	 * <p>Function used to get all names of files/directories in a directory defined by {@code localPath}.</p>
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @return {@code String[]} containing all names
	 */
	@Nullable
	String[] getDirNames(@NotNull String localPath);
	
	/**
	 * <p>Function used to get all names of files/directories in a directory defined by {@code localPath}.</p>
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @return {@link Stream} of all names
	 */
	@Nullable
	Stream<String> getDirNamesS(@NotNull String localPath);
	
	/**
	 * <p>Function used to check if a resource exists at {@code localPath}.</p>
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @return flag representing if resource was found
	 */
	boolean exists(@NotNull String localPath);
	
	/**
	 * <p>Function that creates a view of current {@link IDataManager} with localPath<br><br>
	 * EG:<br>
	 * Domain contains:<br>
	 * <code>
	 * textures/sky.hdr<br>
	 * textures/...<br>
	 * ...
	 * </code><br>
	 * <br>
	 * So a to localize textures something like this would be done:<br>
	 * <code>
	 * {@link IDataManager} assets=...;<br>
	 * {@link IDataManager} tx=assets.subData("textures");
	 * </code><br><br>
	 * And to get something from it would be done like:<br>
	 * <code>{@link InputStream} skyStream=tx.getInStream("sky.hdr");</code>
	 * </p>
	 *
	 * @param localPath The string that will limit the view (aka: localize local paths called from created manager)
	 * @return A new instance of {@link com.lapissea.datamanager.managers.SubDataManager SubDataManager} with calling object as the parent
	 */
	@NotNull
	IDataManager subData(@NotNull String localPath);
	
	/**
	 * <p>Function used to get size of a readable resource in bytes. If it is not readable (directory/missing) then this will return -1.</p>
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @return size of resource in bytes
	 */
	long getSize(@NotNull String localPath);
	
	/**
	 * <p>
	 * Function used to get all bytes of some resource provided by registered {@link Domain} and put them in to a {@link ByteBuffer}.<br>
	 * Instead of passing in the {@link ByteBuffer} this function requests a ByteBuffer with a speified size- (resource byte size)
	 * </p>
	 *
	 * @param localPath     see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @param newByteBuffer Function to create/supply a {@link ByteBuffer} where to write bytes
	 * @return {@link ByteBuffer} that was given in {@code byteBuffer}.
	 */
	@Nullable
	default ByteBuffer readAllBytes(@NotNull String localPath, @NotNull IntFunction<ByteBuffer> newByteBuffer){
		long size=getSize(localPath);
		if(size<=0) return null;
		if(size>Integer.MAX_VALUE) throw new OutOfMemoryError("Trying to load extremely large file in to memory");
		
		return readAllBytes(localPath, newByteBuffer.apply((int)size));
	}
	
	/**
	 * Runs {@link #readAllBytes(String, IntFunction)} in a {@link CompletableFuture} where if result fails (aka is null),
	 * throws exception so it cen be handled properly with functions like {@link CompletableFuture#exceptionally}.
	 *
	 * @param localPath     see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @param newByteBuffer Function to create/supply a {@link ByteBuffer}
	 * @return {@link CompletableFuture} that will return object provided by {@code onRead}
	 * @see #getInStream(String)
	 */
	@NotNull
	default CompletableFuture<ByteBuffer> readAllBytesAsync(@NotNull String localPath, @NotNull IntFunction<ByteBuffer> newByteBuffer){
		return async(()->Objects.requireNonNull(readAllBytes(localPath, newByteBuffer)));
	}
	
	/**
	 * <p>Function used to get all bytes of some resource provided by registered {@link Domain} and put them in to a {@link ByteBuffer}.</p>
	 *
	 * @param localPath  see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @param byteBuffer destination where to write bytes
	 * @return {@link ByteBuffer} that was given in {@code byteBuffer}.
	 */
	@NotNull
	default ByteBuffer readAllBytes(@NotNull String localPath, @NotNull ByteBuffer byteBuffer){
		
		try(BufferedInputStream in=getInStream(localPath)){
			int b;
			while((b=in.read())!=-1) byteBuffer.put((byte)b);
			byteBuffer.flip();
			return byteBuffer;
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Runs {@link #readAllBytes(String, ByteBuffer)} in a {@link CompletableFuture} where if result fails (aka is null),
	 * throws exception so it cen be handled properly with functions like {@link CompletableFuture#exceptionally}.
	 *
	 * @param localPath  see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @param byteBuffer destination og where to store bytes in
	 * @return {@link CompletableFuture} that will return object provided by {@code onRead}
	 * @see #getInStream(String)
	 */
	@NotNull
	default CompletableFuture<ByteBuffer> readAllBytesAsync(@NotNull String localPath, @NotNull ByteBuffer byteBuffer){
		return async(()->Objects.requireNonNull(readAllBytes(localPath, byteBuffer)));
	}
	
	/**
	 * <p>Function used to get all text of some resource provided by registered {@link Domain} and put it in a {@link StringBuilder}.</p>
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @param dest      String builder where to put all the contents in to.
	 * @return returns {@link StringBuilder} that was given in {@code dest}.
	 */
	@NotNull
	default StringBuilder readAllTo(@NotNull String localPath, @NotNull StringBuilder dest){
		int size=(int)getSize(localPath);
		dest.ensureCapacity(dest.length()+size);
		
		try(BufferedReader in=getReader(localPath)){
			int b;
			while((b=in.read())!=-1) dest.append((char)b);
		}catch(IOException e){}
		
		return dest;
	}
	
	/**
	 * <p>Function used to get all paths of files/directories in a directory defined by {@code localPath}.</p>
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @return {@code String[]} containing all paths
	 */
	@Nullable
	String[] getDirPaths(@NotNull String localPath);
	
	/**
	 * <p>Function used to get all paths of files/directories in a directory and all its child directories defined by {@code localPath}.</p>
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @return {@code String[]} containing all paths
	 */
	@Nullable
	String[] getDirPathsDeep(@NotNull String localPath);
	
	/**
	 * <p>Function used to get all paths of files/directories in a directory defined by {@code localPath} and returned in a stream.</p>
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @return {@link Stream} containing all paths
	 */
	@Nullable
	Stream<String> getDirPathsS(@NotNull String localPath);
	
	/**
	 * <p>Function used to get all paths of files/directories in a directory and all its child directories defined by {@code localPath} and returned in a stream.</p>
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @return {@link Stream} containing all paths
	 */
	@Nullable
	Stream<String> getDirPathsDeepS(@NotNull String localPath);
	
	/**
	 * <p>Function used to check if any {@link Domain} is editable and can create new files/folders. (usually a domain that handles a compressed file will not be editable)</p>
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @return Flag that represents if it is possible to edit/create resources
	 */
	boolean canEditCreate(@NotNull String localPath);
	
	/**
	 * <p>Function used to create/modify a resource. If a resource is missing, it will be automatically created. When resource is created its contents will be set to {@code data} </p>
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @param data      content that will be written to resource
	 */
	void makeFile(@NotNull String localPath, byte[] data);
	
	/**
	 * <p>Function used to create/modify a resource. If a resource is missing, it will be automatically created.</p>
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @return {@link BufferedOutputStream} that is a wrapper of specific {@link Domain} implementation.<br>
	 */
	@NotNull
	BufferedOutputStream makeFile(@NotNull String localPath);
	
	/**
	 * <p>Function used to create a {@link DataSignature}.</p>
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @return new instance of {@link DataSignature}
	 */
	@NotNull
	DataSignature createSignature(@NotNull String localPath);
	
	/**
	 * <p>Function used to get the time in ms when a resource was last changed. Useful when working with editable domains. If it is not possible to get time, -1 will be returned.</p>
	 *
	 * @param localPath see {@link IDataManager} -&gt; Concepts -&gt; local path
	 * @return date of last modification or creation if it was never modified in ms or -1 in a case of failure
	 */
	long getLastChange(@NotNull String localPath);
	
}
