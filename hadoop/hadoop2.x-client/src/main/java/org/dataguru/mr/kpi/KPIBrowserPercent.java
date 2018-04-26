package org.dataguru.mr.kpi;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;

public class KPIBrowserPercent {
	public static int ipnum = 0;

	public static class KPIBrowserMapper extends MapReduceBase implements
			Mapper<Object, Text, Text, Text> {
		private Text browser = new Text();
		private Text ip = new Text();

		public void map(Object key, Text value,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			KPI kpi = KPI.filterBroswer(value.toString());
			if (kpi.isValid()) {
				browser.set(kpi.getHttp_user_agent());
				ip.set(kpi.getRemote_addr());
				output.collect(browser, ip);
			}
		}
	}

	public static class KPIBrowserReducer extends MapReduceBase implements
			Reducer<Text, Text, Text, Text> {

		Set<String> ips = new HashSet<String>(); // ip去重

		public void reduce(Text key, Iterator<Text> values,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			while (values.hasNext()) {
				ips.add(values.next().toString());
			}
			Text outvalue = new Text(String.valueOf(ips.size()));
			output.collect(key, outvalue);
		}
	}

	public static class KPIBrowserMapper2 extends MapReduceBase implements
			Mapper<Object, Text, Text, Text> {
		public void map(Object key, Text value,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			String[] arr = value.toString().split("\\s+");
			String broserName = arr[0];
			int ips = NumberUtils.toInt(arr[1], 0);
			ipnum += ips; // 统计总的ip数
			output.collect(new Text(broserName), new Text(String.valueOf(ips)));
		}
	}
	public static class KPIBrowserReducer2 extends MapReduceBase implements
			Reducer<Text, Text, Text, Text> {
		public void reduce(Text key, Iterator<Text> values,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			int ips = NumberUtils.toInt(values.next().toString(), 0);
			float percent = (float) ips / (float) ipnum * 100; // 求百分比
			String tmp = "	" + String.valueOf(percent) + "%";
			output.collect(key, new Text(tmp));
		}
	}

	public static void main(String[] args) throws Exception {
		
		System.setProperty("hadoop.home.dir", "E:/dataguru/soft/hadoop-common-2.2.0-bin-master"); 
		
		String input = "hdfs://192.168.1.101:9000/in/access.20120104.log";
        String output = "hdfs://192.168.1.101:9000/out/browser-percent999";

		Path tempDir = new Path("hdfs://192.168.1.101:9000/temp/999");

		JobConf conf = new JobConf(KPIBrowserPercent.class);
		conf.setJobName("KPIBrowserPercent");

		conf.setMapOutputKeyClass(Text.class);
		conf.setMapOutputValueClass(Text.class);

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);

		conf.setMapperClass(KPIBrowserMapper.class);
		conf.setReducerClass(KPIBrowserReducer.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, new Path(input));
		FileOutputFormat.setOutputPath(conf, (tempDir));

		JobClient.runJob(conf);

		JobConf conf2 = new JobConf(KPIBrowserPercent.class);
		conf2.setJobName("KPIBrowserPercent2");

		conf2.setMapOutputKeyClass(Text.class);
		conf2.setMapOutputValueClass(Text.class);

		conf2.setOutputKeyClass(Text.class);
		conf2.setOutputValueClass(Text.class);

		conf2.setMapperClass(KPIBrowserMapper2.class);
		conf2.setReducerClass(KPIBrowserReducer2.class);

		conf2.setInputFormat(TextInputFormat.class);
		conf2.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf2, tempDir);
		FileOutputFormat.setOutputPath(conf2, new Path(output));

		JobClient.runJob(conf2);
		System.exit(0);
	}
}
