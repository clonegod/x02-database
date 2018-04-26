package com.aysnclife.dataguru.hadoop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;

//import org.apache.hadoop.fs.FsUrlStreamHandlerFactory;
//import org.apache.hadoop.fs.FileSystem;
//import org.apache.hadoop.fs.Path;
//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.io.IOUtils;
//import org.apache.hadoop.util.Progressable;

public class FileSaveWithRead {/*
	
	private static final int BYTES_TOTAL = 130;
	public static void main(String[] args) throws Exception {
		// file:///home/huang/lesson02_3.dat
		// hdfs://server01:9000/user/huang/in/lesson02_3.txt
		// file:///home/huang/lesson02_3_output.dat
		
		String localSrc = args[0];
		String hdfs_dst = args[1];
		String output = args[2];
		
		File f = null;
		URI uri = new URI(localSrc);
		f = new File(uri);
		f.delete();
		
		// step01
		byte[] allBytes = createTestData(f);
		System.out.println("allBytes length:" + allBytes.length);
		
		// step02
		saveToHDFS(hdfs_dst, allBytes);
		
		// step03
		byte[] loadedBytes = readFromHDFS(hdfs_dst);
		
		// step04
		writeToLocalFileSystem(loadedBytes, 101, 120, new File(new URI(output)));
		
		System.out.println("Done!");
	}
	

	*//**
	 * step01: 
	 * 	write each char by five times, we will get 130 chars totally.
	 *//*
	final static char[] Alpha = "abcdefghijklnmopqrstuvwxyz".toCharArray();
	private static byte[] createTestData(File file) throws Exception {
		ByteBuffer byteBuf = ByteBuffer.allocate(130);
		for(int i=0;i<Alpha.length;i++) {
			for(int j=0; j<5; j++) {
				byteBuf.put((byte)Alpha[i]);
			}
		}
		return byteBuf.array();
		
	}
	
	*//**
	 * step02: 
	 * 	write all 130 bytes to hdfs 
	 *//*
	public static void saveToHDFS(String dst, byte[] buf) throws Exception {
		InputStream in = new ByteArrayInputStream(buf);
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(URI.create(dst), conf);
		OutputStream out = fs.create(new Path(dst), new Progressable() {
			public void progress() {
				System.out.print(".");
			}
		});
		IOUtils.copyBytes(in, out, 4096, true);
	}
	
	
	*//**
	 * step03: 
	 * 	read all bytes from hdfs
	 *//*
	private static byte[] readFromHDFS(String path) throws Exception {
		// must not set this to static initial!!!
		URL.setURLStreamHandlerFactory(new FsUrlStreamHandlerFactory());
		
	    InputStream in = null;
	    ByteArrayOutputStream out = null;
	    try{
	            in = new URL(path).openStream();
	            out = new ByteArrayOutputStream();
	            IOUtils.copyBytes(in, out, 4096, false);
	    } finally {
	            IOUtils.closeStream(in);
	            IOUtils.closeStream(out);
	    }
	    
	    return out.toByteArray();
	}
	
	*//**
	 * step04:
	 * 	get bytes from 101 to 120, and write to local file system 
	 * @param loadedBytes
	 * @param start
	 * @param end
	 * @param output
	 *//*
	private static void writeToLocalFileSystem(byte[] loadedBytes, int start, int end, File output) 
			throws Exception  {
		ByteBuffer byteBuffer = ByteBuffer.wrap(loadedBytes);
		byteBuffer.position(start-1);
		
		byte[] dst = new byte[20];
		byteBuffer.get(dst, 0, end - start + 1);
		
		FileOutputStream fos = new FileOutputStream(output);
		fos.write(dst);
		fos.flush();
	}
*/}
