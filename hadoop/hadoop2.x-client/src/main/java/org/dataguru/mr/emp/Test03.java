package org.dataguru.mr.emp;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * 	3) 求每个部门最早进入公司的员工姓名
 *
 */
public class Test03 {
	public static class Map extends Mapper<Object, Text, Text, Text> {

		@Override
		protected void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			
			String text = value.toString();
			
			// SKIP HEADER
			if(text.contains("DEPTNO") || text.startsWith("-")) {
				return;
			}
			
			String[] items = text.split("\\s+");
			
			// KEY
			String deptno = null;
			
			if(items.length < 6) {
				// dept table
				deptno = items[0];
				String dName = items[1];
				context.write(new Text(deptno), new Text("DNAME:"+dName));
			} else {
				// emp table
				deptno = items[items.length-1];
				
				String ename = items[1];
				String hiredate = "";
				
				if("KING".equals(ename)) {
					hiredate = items[3];
				} else {
					hiredate = items[4];
				}
				
				context.write(new Text(deptno), new Text("EMP:"+ename+"|"+hiredate));
			}
			
		}
	}
	
	public static class Reduce extends Reducer<Text, Text, Text, Text> {

		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			
			String dName = "";
			TreeMap<Date, String> sortMap = new TreeMap<Date, String>(); 
			for (Text text : values) {
				String record = text.toString();
				String items[] = record.split(":");
				if("DNAME".equals(items[0])) {
					dName = items[1];
				} else {
					String ename = items[1].split("\\|")[0];
					String dateStr = items[1].split("\\|")[1];
					sortMap.put(parseDate(dateStr), ename);
				}
			}
			Text outValueText = null;
			if(sortMap.isEmpty()) {
				outValueText = new Text("HAS NO EMP");
			} else {
				outValueText = new Text(sortMap.firstEntry().getValue());
			}
			context.write(new Text(dName), outValueText);
		}
	}
	
	private static Date parseDate(String dateStr) {
		String regex = "(\\d+)-(\\d+).*?-(\\d+)";
		String newDateStr = "";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(dateStr);
		if (m.find()) {
			String year = m.group(3);
			String month = m.group(2);
			if(month.length()==1) {
				month = "0"+month;
			}
			String day = m.group(1);
			newDateStr = year+month+day;
		}
		try {
			return new SimpleDateFormat("yyMMdd").parse(newDateStr);
		} catch (ParseException e) {
			throw new RuntimeException("Cann't parse the date string", e);
		}
	}
	
	public static void main(String[] args) throws Exception {
		System.setProperty("hadoop.home.dir", "E:/dataguru/soft/hadoop-common-2.2.0-bin-master"); 
    	
        String input = "hdfs://192.168.1.102:9000/in/lession-06";
        String output = "hdfs://192.168.1.102:9000/out/tmp133";

        Job job = new Job();
		job.setJarByClass(Test03.class);
		job.setJobName("Test03");
		FileInputFormat.addInputPath(job, new Path(input));
		FileOutputFormat.setOutputPath(job, new Path(output));
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
