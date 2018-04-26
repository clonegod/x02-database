package org.dataguru.mr.emp;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * 2) 求各个部门的人数和平均工资
 *
 */
public class Test02 {
	
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
				
				String salary = "";
				String comm = "";
				
				if(items.length==6) {// MGR IS NULL && COMM IS NULL
					salary = items[4];
					comm = "0";
				} else if(items.length==7) { // COMM IS NULL
					salary = items[5];
					comm = "0";
				} else {
					salary = items[5];
					comm = items[6];
				}
				
				double total = Double.parseDouble(salary) 
						+ (comm==null?0:Double.parseDouble(comm));
				
				context.write(new Text(deptno), new Text("SALARY:"+String.format("%.2f", total)));
			}
			
		}
	}
	
	public static class Reduce extends Reducer<Text, Text, Text, Text> {

		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			
			String dName = "";
			int empCount = 0;
			double sumSalary = 0;
			for (Text text : values) {
				String record = text.toString();
				String items[] = record.split(":");
				if("DNAME".equals(items[0])) {
					dName = items[1];
				} else {
					empCount++;
					sumSalary += Double.parseDouble(items[1]);
				}
			}
			
			double avgSalary = 0;
			// OPERATIONS HAS NO EMP
			if(empCount > 0) {
				avgSalary = sumSalary/empCount;
			}
			context.write(new Text(dName), 
					new Text("empCount="+empCount + ", avgSalary=" + String.format("%.2f", avgSalary)));
		}
	}
	
	public static void main(String[] args) throws Exception {
		System.setProperty("hadoop.home.dir", "E:/dataguru/soft/hadoop-common-2.2.0-bin-master"); 
    	
        String input = "hdfs://192.168.1.102:9000/in/lession-06";
        String output = "hdfs://192.168.1.102:9000/out/tmp118";

        Job job = new Job();
		job.setJarByClass(Test02.class);
		job.setJobName("Test02");
		FileInputFormat.addInputPath(job, new Path(input));
		FileOutputFormat.setOutputPath(job, new Path(output));
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
	
}
