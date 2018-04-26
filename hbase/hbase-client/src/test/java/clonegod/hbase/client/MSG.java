package clonegod.hbase.client;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

public class MSG {
	String rowkey;
	String sender;
	String receiver;
	String content;
	
	public MSG() {
		createMsg();
	}
	
	/**
	 * 生成测试数据
	 */
	private  void createMsg() {
		String[] users = new String[] {"alice", "bob", "cindy"};
		
		Random r = new Random();
		String sender = users[r.nextInt(users.length)];
		String receiver = users[r.nextInt(users.length)];
		while(sender == receiver) {
			receiver = users[r.nextInt(users.length)];
		}
		
		Date date = new Date();
		String datetime = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(date);
		
		int max = 3;
		for(String user : users) {
			if(user.length() > max) {
				max = user.length();
			}
		}
		
		String rowkey = String.format("%s|%s|%s", 
							StringUtils.leftPad(sender, max, '0'), StringUtils.leftPad(receiver, max, '0'), datetime);
		
		this.rowkey = rowkey; 
		this.sender = sender; 
		this.receiver = receiver;
		this.content = String.format("%s send msg to %s at: %s", sender, receiver, datetime);
	}

}