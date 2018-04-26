package com.aysnclife.dataguru.hadoop;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;

//import org.apache.hadoop.fs.FileSystem;
//import org.apache.hadoop.fs.Path;
//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.io.IOUtils;
//import org.apache.hadoop.util.Progressable;

public class FileSaveWithProgress {
	
	/*public static void main(String[] args) throws Exception {
		// file:///home/huang/lesson02_2.dat
		// hdfs://server01:9000/user/huang/in/lesson02_2.txt
		
		String localSrc = args[0];
		String dst = args[1];
		
		URI uri = new URI(localSrc);
		File f = new File(uri);
		if(f.exists()) {
			f.delete();
		}
		
		createTestData(f);
		
		int start = 101;
		int end = 120;
		byte[] targetBytes = readBytes(uri, start, end);
		
		save(dst, targetBytes);
	}
	
	*//**
	 * read bytes from file
	 * @param uri file path
	 * @param start 101
	 * @param end 120
	 * @throws Exception
	 *//*
	private static byte[] readBytes(URI uri, int start, int end) throws Exception {
		ByteBuffer byteBuf = ByteBuffer.allocate(130);
		
		InputStream in = new FileInputStream(new File(uri));
		int n = 0;
		while((n = in.read()) != -1) {
			byteBuf.put((byte)n);
		}
		
		byte[] dst = new byte[20];
		byteBuf.position(start-1);
		byteBuf.get(dst, 0, end-start+1);
		for(byte b : dst) {
			System.out.print((char)b);
		}
		
		in.close();
		
		return dst;
	}

	*//**
	 * write each char by five times, we will get 130 chars totally.
	 *//*
	final static char[] Alpha = "abcdefghijklnmopqrstuvwxyz".toCharArray();
	private static void createTestData(File file) throws Exception {
		StringBuilder buf = new StringBuilder();
		for(int i=0;i<Alpha.length;i++) {
			for(int j=0; j<5; j++) {
				buf.append(Alpha[i]);
			}
		}
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
			fw.write(buf.toString());
			fw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			fw.close();
		}
		
	}
	
	public static void save(String dst, byte[] buf) throws Exception {
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
	*/
	
}
