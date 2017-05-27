package controllers.util;

import com.jfinal.kit.StrKit;

public class NumberConverterUtil {
	/**
     * 数字金额大写转换，思想先写个完整的然后将如零拾替换成零
     * 要用到正则表达式
     */
    public static String convertNumToChinese(double n){
        String fraction[] = {"角", "分"};
        String digit[] = { "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖" };
        String unit[][] = {{"元", "万", "亿"},
                     {"", "拾", "佰", "仟"}};
 
        String head = n < 0? "负": "";
        n = Math.abs(n);
         
        String s = "";
        for (int i = 0; i < fraction.length; i++) {
            s += (digit[(int)(Math.floor(n * 10 * Math.pow(10, i)) % 10)] + fraction[i]).replaceAll("(零.)+", "");
        }
        if(s.length()<1){
            s = "整";    
        }
        int integerPart = (int)Math.floor(n);
 
        for (int i = 0; i < unit[0].length && integerPart > 0; i++) {
            String p ="";
            for (int j = 0; j < unit[1].length && n > 0; j++) {
                p = digit[integerPart%10]+unit[1][j] + p;
                integerPart = integerPart/10;
            }
            s = p.replaceAll("(零.)*零$", "").replaceAll("^$", "零") + unit[0][i] + s;
        }
        return head + s.replaceAll("(零.)*零元", "元").replaceFirst("(零.)+", "").replaceAll("(零.)+", "零").replaceAll("^整$", "零元整");
    }
    
    /*
     * 数字转英文
     */
    private static String numberToWord(int num, String val) {
        String ones[] = {" ", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE", "TEN", "ELEVEN", 
                "TWELVE", "THIRTEEN", "FOURTEEN", "FIFTEEN", "SIXTEEN", "SEVENTEEN", "EIGHTEEN", "NINETEEN"};
        String tens[] = {" ", " ", "TWENTY", "THIRTY", "FOURTY", "FIFTY", "SIXTY", "SEVENTY", "EIGHTY", "NINETY"};
        String str = "";
        if (num > 19) {
            str = tens[num / 10] + " " + ones[num % 10];
        } else {
            str = ones[num];
        }
        if (num >0 && !StrKit.isBlank(val.trim())) {
            str += val;
        }
        return str;
    }
    
    public static String numberToWord(int number) {
        return (numberToWord(((number / 100) % 10), " HUNDRED ") +
                numberToWord((number % 100), " ")).trim();
    }
    
    public static void main(String agrs[]) {
        //数字转英文
        System.out.println(numberToWord(99));
        System.out.println(numberToWord(134));
        System.out.println(numberToWord(313));
        //整数
//        System.out.println(convertNumToChinese(0));              // 零元整
//        System.out.println(convertNumToChinese(123));            // 壹佰贰拾叁元整
//        System.out.println(convertNumToChinese(1000000));        // 壹佰万元整
//        System.out.println(convertNumToChinese(100000001));      // 壹亿零壹元整
//        System.out.println(convertNumToChinese(1000000000));     // 壹拾亿元整
//        System.out.println(convertNumToChinese(1234567890));     // 壹拾贰亿叁仟肆佰伍拾陆万柒仟捌佰玖拾元整
//        System.out.println(convertNumToChinese(1001100101));     // 壹拾亿零壹佰壹拾万零壹佰零壹元整
//        System.out.println(convertNumToChinese(110101010));      // 壹亿壹仟零壹拾万壹仟零壹拾元整
//     
//        //小数
//        System.out.println(convertNumToChinese(0.12));          // 壹角贰分
//        System.out.println(convertNumToChinese(123.34));        // 壹佰贰拾叁元叁角肆分
//        System.out.println(convertNumToChinese(1000000.56));    // 壹佰万元伍角陆分
//        System.out.println(convertNumToChinese(100000001.78));  // 壹亿零壹元柒角捌分
//        System.out.println(convertNumToChinese(1000000000.90)); // 壹拾亿元玖角
//        System.out.println(convertNumToChinese(1234567890.03)); // 壹拾贰亿叁仟肆佰伍拾陆万柒仟捌佰玖拾元叁分
//        System.out.println(convertNumToChinese(1001100101.00)); // 壹拾亿零壹佰壹拾万零壹佰零壹元整
//        System.out.println(convertNumToChinese(110101010.10));  // 壹亿壹仟零壹拾万壹仟零壹拾元壹角
//         
//        //负数
//        System.out.println(convertNumToChinese(-0.12));          // 负壹角贰分
//        System.out.println(convertNumToChinese(-123.34));        // 负壹佰贰拾叁元叁角肆分
//        System.out.println(convertNumToChinese(-1000000.56));    // 负壹佰万元伍角陆分
//        System.out.println(convertNumToChinese(-100000001.78));  // 负壹亿零壹元柒角捌分
//        System.out.println(convertNumToChinese(-1000000000.90)); // 负壹拾亿元玖角
//        System.out.println(convertNumToChinese(-1234567890.03)); // 负壹拾贰亿叁仟肆佰伍拾陆万柒仟捌佰玖拾元叁分
//        System.out.println(convertNumToChinese(-1001100101.00)); // 负壹拾亿零壹佰壹拾万零壹佰零壹元整
//        System.out.println(convertNumToChinese(-110101010.10));  // 负壹亿壹仟零壹拾万壹仟零壹拾元壹角
        
        
    }
}
