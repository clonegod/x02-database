package org.dataguru.mr.emp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class a {

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
			System.out.println(newDateStr+"...");
		}
		try {
			return new SimpleDateFormat("yyMMdd").parse(newDateStr);
		} catch (ParseException e) {
		}
		return null;
	}
	
	
	public static void main(String[] args) {
		System.out.println(parseDate("09-6锟斤拷-81"));
	}
}
