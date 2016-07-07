package controllers.util;

import java.text.ParseException;
import java.util.regex.Pattern;

public class NumberUtils {
	/**
	 * 数字转字母
	 * @return
	 */
	public static String toLetterString(int number) {
		if (number < 1) {// 出错了
			return null;
		}
		if (number < 27) {
			return String.valueOf((char) ('A' + number - 1));
		}
		if (number % 26 == 0) {
			return toLetterString(number / 26 - 1) + "Z";
		}
		return toLetterString(number / 26)+ String.valueOf((char) ('A' + number % 26 - 1));
	}
	
	

	/**
	 * 判断是否是数字
	 */
	public static boolean isNumeric(String str){ 
		if(str.contains("-") || str.contains("(") || str.contains(")") ){
			str = str.replace("-","").replace("(","").replace(")","");
		}
		String value = "";
		boolean b = true;
		if(str.contains("..")){
			return false;
		}else if(str.substring(str.length() - 1).equals(".")){
			return false;
		}
		for (int i = 0; i < str.length(); i++) {
			value = str.substring(i,i+1);
			if(!value.equals(".")){
				java.text.DecimalFormat nf = new java.text.DecimalFormat("00.00"); 
				try {
					nf.parse(value);
				} catch (ParseException e1) {
					b = false;
					return b;
				}
			}
		}
		return b;
	} 

	
	/* 
	  *   将字符串转为整数 
	  */ 
	public static   int   toNum_new(String   str) 
	{ 
	        char[]   ch=str.toCharArray(); 
	        int   ret=0; 
	        for(int   i=0;i <ch.length;i++) 
	        { 
	            ret*=26; 
	            ret+=ch2int(ch[i]); 
	        } 
	        return   ret; 
	}
	/* 
	  *   A~Z/a~z   转为1~26 
	  */ 
	public static   int   ch2int(char   ch) 
	{ 
	        if(ch >= 'a'&&ch <='z') 
	                  return   ch -'a'+1; 
	        if(ch >= 'A'&&ch <= 'Z') 
	                  return   ch -'A'+1; 
	        throw   new   java.lang.IllegalArgumentException(); 
	} 
	
	public static void main(String[] args) {
// java.text.NumberFormat nf2 = new java.text.DecimalFormat("00.00");
//		try {
//			System.out.println(nf2.parse("1"));
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
	}

}
