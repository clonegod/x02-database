package org.dataguru.mr.kpi;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
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

public class KPITransferAmount {

	public static class TransferAmountMapper extends MapReduceBase implements
			Mapper<Object, Text, Text, LongWritable> {
		private LongWritable size = new LongWritable();
		private Text word = new Text();

		public void map(Object key, Text value,
				OutputCollector<Text, LongWritable> output, Reporter reporter)
				throws IOException {
			KPI kpi = KPI.parseLine(value.toString());
			word.set(kpi.getRequest());
			size.set(NumberUtils.toInt(kpi.getBody_bytes_sent(), 0));
			output.collect(word, size);
		}
	}

	public static class TransferAmountReducer extends MapReduceBase implements
			Reducer<Text, LongWritable, Text, LongWritable> {
		private LongWritable result = new LongWritable();

		public void reduce(Text key, Iterator<LongWritable> values,
				OutputCollector<Text, LongWritable> output, Reporter reporter)
				throws IOException {
			long sum = 0;
			while (values.hasNext()) {
				sum += values.next().get();
			}
			result.set(sum);
			output.collect(new Text("totoal bytes:"), result);
		}
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("hadoop.home.dir", "E:/dataguru/soft/hadoop-common-2.2.0-bin-master"); 
		
		String input = "hdfs://192.168.1.101:9000/in/access.20120104.log";
        String output = "hdfs://192.168.1.101:9000/out/transfer-amount";

		JobConf conf = new JobConf(KPITransferAmount.class);
		conf.setJobName("TransferAmount");

		conf.setMapOutputKeyClass(Text.class);
		conf.setMapOutputValueClass(LongWritable.class);

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(LongWritable.class);

		conf.setMapperClass(TransferAmountMapper.class);
		conf.setCombinerClass(TransferAmountReducer.class);
		conf.setReducerClass(TransferAmountReducer.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, new Path(input));
		FileOutputFormat.setOutputPath(conf, new Path(output));

		JobClient.runJob(conf);
		System.exit(0);
	}

}