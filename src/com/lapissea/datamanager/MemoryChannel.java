package com.lapissea.datamanager;

import com.lapissea.util.NotNull;
import com.lapissea.util.function.UnsafeConsumer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.function.Consumer;

public class MemoryChannel extends FileChannel{
	
	private       ByteBuffer        src;
	private final IDataManager.Mode mode;
	
	public MemoryChannel(@NotNull byte[] src, @NotNull IDataManager.Mode mode){
		this(ByteBuffer.wrap(src), mode);
	}
	
	public MemoryChannel(@NotNull ByteBuffer src, @NotNull IDataManager.Mode mode){
		this.src=src;
		this.mode=mode;
	}
	
	private void checkWrite(){
		if(!mode.canWrite) throw new RuntimeException("Mode "+mode+" can't write!");
	}
	
	private void putIO(ByteBuffer from, UnsafeConsumer<ByteBuffer, IOException> to, int amount) throws IOException{
		int lim=from.limit();
		from.limit(from.position()+amount);
		to.accept(from);
		from.position(from.limit());
		from.limit(lim);
	}
	
	private void put(ByteBuffer from, Consumer<ByteBuffer> to, int amount){
		int lim=from.limit();
		from.limit(from.position()+amount);
		to.accept(from);
		from.position(from.limit());
		from.limit(lim);
	}
	
	@Override
	public int read(ByteBuffer dst){
		
		synchronized(src){
			int srcAmount=src.remaining();
			if(srcAmount<=0) return -1;
			int dstAmount=dst.remaining();
			
			int amount=Math.min(dstAmount, srcAmount);
			if(amount==0) return 0;
			
			put(src, dst::put, amount);
			
			return amount;
		}
		
	}
	
	@Override
	public long read(ByteBuffer[] dsts, int offset, int length){
		long sum=0;
		while(offset<length){
			sum+=read(dsts[offset++]);
		}
		return sum;
	}
	
	@Override
	public int write(ByteBuffer src){
		checkWrite();
		
		synchronized(this.src){
			int srcAmount=this.src.remaining();
			if(srcAmount<=0) return -1;
			int dstAmount=src.remaining();
			
			int amount=Math.min(dstAmount, srcAmount);
			if(amount==0) return 0;
			
			put(src, this.src::put, amount);
			
			return amount;
		}
		
	}
	
	@Override
	public long write(ByteBuffer[] srcs, int offset, int length){
		long sum=0;
		while(offset<length){
			sum+=write(srcs[offset++]);
		}
		return sum;
	}
	
	@Override
	public long position(){
		return src.position();
	}
	
	@Override
	public FileChannel position(long newPosition){
		src.position(Math.toIntExact(newPosition));
		return this;
	}
	
	@Override
	public long size(){
		return src.limit();
	}
	
	@Override
	public FileChannel truncate(long size){
		return this;
	}
	
	@Override
	public void force(boolean metaData){}
	
	@Override
	public long transferTo(long position, long count, WritableByteChannel target) throws IOException{
		
		synchronized(src){
			int srcAmount=src.remaining();
			if(srcAmount<=0) return -1;
			
			int amount=Math.min(Math.toIntExact(count), srcAmount);
			if(amount==0) return 0;
			
			putIO(src, target::write, amount);
			
			return amount;
		}
	}
	
	@Override
	public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException{
		
		synchronized(this.src){
			int srcAmount=this.src.remaining();
			if(srcAmount<=0) return -1;
			
			int amount=Math.min(Math.toIntExact(count), srcAmount);
			if(amount==0) return 0;
			
			putIO(this.src, src::read, amount);
			
			return amount;
		}
	}
	
	@Override
	public int read(ByteBuffer dst, long position){
		synchronized(this.src){
			int pos=this.src.position();
			try{
				this.src.position(Math.toIntExact(position));
				return read(dst);
			}finally{
				this.src.position(pos);
			}
		}
	}
	
	@Override
	public int write(ByteBuffer src, long position){
		synchronized(this.src){
			int pos=this.src.position();
			try{
				this.src.position(Math.toIntExact(position));
				return write(src);
			}finally{
				this.src.position(pos);
			}
		}
	}
	
	@Override
	public MappedByteBuffer map(MapMode mode, long position, long size){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public FileLock lock(long position, long size, boolean shared){
		return null;
	}
	
	@Override
	public FileLock tryLock(long position, long size, boolean shared){
		return null;
	}
	
	@Override
	protected void implCloseChannel(){
		src=null;
	}
}
