package controllers.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class PoiUtils {
	static SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd");
	static Map<String,CellStyle> styleMap = new HashMap<String,CellStyle>(); //存储单元格样式的Map
	
	
	public static void main(String[] args) {
		/**
		 * 读文件
		 */
		//readExcel("c:/a.xlsx");
		
		/**
		 * 写文件
		 */
		//testWrite("D:/a.xls","D:/b.xls");
	}
	
	/**
	 * 读excel 
	 * @param filePath excel路径
	 */
	public static Map<String, List<Object>> readExcel(String filePath){
		Workbook book = null;
		Map<String, List<Object>> map = new HashMap<String, List<Object>>();
		List<Object> headList = new ArrayList<Object>();
		List<Object> content = null;
		try {
			book = getExcelWorkbook(filePath);
			Sheet sheet = getSheetByNum(book,1);
			
			int lastRowNum = sheet.getLastRowNum();
			
			Row row = null;
			for(int i=0;i<=lastRowNum;i++){
				row = sheet.getRow(i);
				if(row != null){
					String str = "";
					int lastCellNum = row.getLastCellNum();
					Cell cell = null;
					if(i == 0){
						for(int j=0;j<lastCellNum;j++){
							cell = row.getCell(j);
								headList.add(cell);
							}
						}
					else{
						content = new ArrayList<Object>();
						for(int j=0;j<lastCellNum;j++){
							cell = row.getCell(j);
							if(j == 0){
								str += cell;
							}
							content.add(cell);
						}
						if(map.containsKey(str)){
							List<Object> exist = map.get(str);
							exist.addAll(content);
						}else{
							map.put(str, content);							
						}
					}
				}				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		for(Object head : headList){
			System.out.println("head : " + head);
		}
		for(Map.Entry<String, List<Object>> entry : map.entrySet()){
			System.out.println("key :" + entry.getKey());
			for(Object val : entry.getValue()){
				System.out.println("val : " + val);
			}
		}
		return map;
	}

	
	/**
	 * 根据单元格的格式 返回单元格的格式中文
	 * @param type_style
	 * @return
	 */
	private static String getCellStyleByChinese(String type_style) {
		String cell_style_cn = "";
		if(type_style.contains("GENERAL")){
			cell_style_cn = "常规";
		}else if(type_style.equals("_ * #,##0.00_ ;_ * \\-#,##0.00_ ;_ * \"-\"??_ ;_ @_ ")){
			cell_style_cn = "会计专用";
		}else if(type_style.equals("0")){
			cell_style_cn = "整数";
		}else if(type_style.contains("YYYY/MM") || type_style.contains("YYYY\\-MM")){
			cell_style_cn = "日期";
		}else if(type_style.equals("0.00%")){
			cell_style_cn = "百分比";
		}else {
			cell_style_cn = "不符合规定格式类型:"+type_style;
//			cell_style_cn = type_style;
		}
		return cell_style_cn;
	}
	
	
	/**
	 * 写内容到excel中
	 * @throws IOException 
	 */
	public static void testWrite(String srcFilePath,String tarFilePath){
		FileOutputStream out = null;
		try {
			Workbook book = getExcelWorkbook(srcFilePath);
			Sheet sheet = getSheetByNum(book,1);
			
			Map<String,String> map = new HashMap<String, String>();
			List<Map<String,String>> list = new LinkedList<Map<String,String>>();
			/*map.put("A", "4,INT");
			map.put("B", "小红,GENERAL");
			map.put("C", "18,INT");
			map.put("D", "1990-03-10,DATE");
			map.put("E", "0.056,PERCENT");
			map.put("F", "4800,DOUBLE");*/
			list.add(map);
			
			int startRow = 6;
			boolean result = writeToExcel(list, sheet,startRow);
			if(result){
				out = new FileOutputStream(tarFilePath);
				book.write(out);
				System.out.println("文件写入完成！");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 将传入的内容写入到excel中sheet里
	 * @param list
	 */
	public static boolean writeToExcel(List<Map<String,String>> list,Sheet sheet,int startRow){
		boolean result = false;
		try {
			Map<String,String> map = null;
			Row row = null;
			for(int i=0;i<list.size();i++){
				map = list.get(i);
				row = sheet.getRow(startRow-1);
				if(row == null){
					row = sheet.createRow(startRow-1);
				}
				startRow ++;
				Cell cell = null;
				
				BigDecimal db = null;
				for(Map.Entry<String,String> entry : map.entrySet()){
					String key = entry.getKey();
					int colNum = NumberUtils.toNum_new(key)-1;
					
					String value_type = entry.getValue();
					String value = value_type.split(",")[0];
					String style = value_type.split(",")[1];
					
					cell = row.getCell(colNum);
					if(cell == null){
						cell = row.createCell(colNum);
					}
					if(style.equals("GENERAL")){
						cell.setCellValue(value);
					}else{
						if(style.equals("DOUBLE") || style.equals("INT")){
							db = new BigDecimal(value,java.math.MathContext.UNLIMITED);
							cell.setCellValue(db.doubleValue());
						}else if(style.equals("PERCENT")){
							db = new BigDecimal(value,java.math.MathContext.UNLIMITED);
							cell.setCellValue(db.doubleValue());
						}else if(style.equals("DATE")){
							java.util.Date date = sFormat.parse(value);
							cell.setCellValue(date);
						}
						cell.setCellStyle(styleMap.get(style));
					}
				}
			}
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		return result;
	}
	/**
	 * 获取excel的Workbook
	 * @throws IOException 
	 */
	public static Workbook getExcelWorkbook(String filePath) throws IOException{
		Workbook book = null;
		File file  = null;
		FileInputStream fis = null;	
		
		try {
			file = new File(filePath);
			if(!file.exists()){
				throw new RuntimeException("文件不存在");
			}else{
				fis = new FileInputStream(file);
				book = WorkbookFactory.create(fis);
				initStyleMap(book);
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		} finally {
			if(fis != null){
				fis.close();
			}
		}
		return book;
	}
	
	/**
	 * 根据索引 返回Sheet
	 * @param number
	 */
	public static Sheet getSheetByNum(Workbook book,int number){
		Sheet sheet = null;
		try {
			sheet = book.getSheetAt(number-1);
//			if(sheet == null){
//				sheet = book.createSheet("Sheet"+number);
//			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		return sheet;
	}
	
	/**
	 * 初始化格式Map
	 */
	
	
	public static void initStyleMap(Workbook book){
		DataFormat hssfDF = book.createDataFormat();
		
		CellStyle doubleStyle = book.createCellStyle(); //会计专用
		doubleStyle.setDataFormat(hssfDF.getFormat("_ * #,##0.00_ ;_ * \\-#,##0.00_ ;_ * \"-\"??_ ;_ @_ ")); //poi写入后为会计专用
		styleMap.put("DOUBLE", doubleStyle);
		
		CellStyle intStyle = book.createCellStyle(); //会计专用
		intStyle.setDataFormat(hssfDF.getFormat("0")); //poi写入后为会计专用
		styleMap.put("INT", intStyle);
        
        CellStyle yyyyMMddStyle = book.createCellStyle();//日期yyyyMMdd
        yyyyMMddStyle.setDataFormat(hssfDF.getFormat("yyyy-MM-dd"));
        styleMap.put("DATE", yyyyMMddStyle);
        
        CellStyle percentStyle = book.createCellStyle();//百分比
        percentStyle.setDataFormat(hssfDF.getFormat("0.00%"));
        styleMap.put("PERCENT", percentStyle);
	}
	
	public class ExcelResult{
		Map content=null;
		List header=null;
		
		public ExcelResult(Map content, List header) {
			this.content = content;
			this.header = header;
		}
		
		public Map getContent() {
			return content;
		}
		public void setContent(Map content) {
			this.content = content;
		}
		public List getHeader() {
			return header;
		}
		public void setHeader(List header) {
			this.header = header;
		}
		
	}
	
	/**
     * 写内容到excel中
     * @throws IOException 
     */
    @SuppressWarnings("deprecation")
    public static String generateExcel(String[] headers, String[] fields, String sql){
        String fileName="";
        try {
            System.out.println("generateExcel begin...");
            String filePath = "WebRoot/download/list";//"/home/default/ROOT/download/list";
            File file = new File(filePath);
            if(!file.exists()){
             file.mkdir();
            }
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String outFileName = "Result-" + format.format(date) + ".xls";
            //String filename = srcFilePath;//"C:/NewExcelFile.xls" ;
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("FirstSheet");  

            HSSFRow rowhead = sheet.createRow((short)0);
            for (int i = 0; i < headers.length; i++) {
                rowhead.createCell((short)i).setCellValue(headers[i]);
            }

            List<Record> recs = Db.find(sql);
            if(recs!=null){
                for (int j = 1; j <= recs.size(); j++) {
                    HSSFRow row = sheet.createRow((short)j);
                    Record rec = recs.get(j-1);
                    for (int k = 0; k < fields.length;k++){
                        Object obj = rec.get(fields[k]);
                        String strValue = "";
                        if(obj != null){
                            strValue =obj.toString();
                        }
                        row.createCell((short)k).setCellValue(strValue);
                    }
                }
            }

            fileName = filePath+"/"+outFileName;
            System.out.println("fileName: "+fileName);
            FileOutputStream fileOut = new FileOutputStream(fileName);
            workbook.write(fileOut);
            fileOut.close();
            System.out.println("Your excel file has been generated!");
            fileName = "/download/list/"+outFileName;
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
        return fileName;
    }



		/**
		 * 写内容到excel中
		 * @throws IOException 
		 */
		@SuppressWarnings("deprecation")
		public static String generateExcel(String[] headers, String[] fields, String sql,String company_name){
		    String fileName="";
		    try {
		        System.out.println("generateExcel begin...");
		        String filePath = "WebRoot/download/list";//"/home/default/ROOT/download/list";
		        File file = new File(filePath);
		        if(!file.exists()){
		         file.mkdir();
		        }
		        Date date = new Date();
		        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		        String outFileName = company_name+"-" + format.format(date) + ".xls";
		        //String filename = srcFilePath;//"C:/NewExcelFile.xls" ;
		        HSSFWorkbook workbook = new HSSFWorkbook();
		        HSSFSheet sheet = workbook.createSheet("FirstSheet");  
		
		        HSSFRow rowhead = sheet.createRow((short)0);
		        for (int i = 0; i < headers.length; i++) {
		            rowhead.createCell((short)i).setCellValue(headers[i]);
		        }
		
		        List<Record> recs = Db.find(sql);
		        if(recs!=null){
		            for (int j = 1; j <= recs.size(); j++) {
		                HSSFRow row = sheet.createRow((short)j);
		                Record rec = recs.get(j-1);
		                for (int k = 0; k < fields.length;k++){
		                    Object obj = rec.get(fields[k]);
		                    String strValue = "";
		                    if(obj != null){
		                        strValue =obj.toString();
		                    }
		                    row.createCell((short)k).setCellValue(strValue);
		                }
		            }
		        }
		
		        fileName = filePath+"/"+outFileName;
		        System.out.println("fileName: "+fileName);
		        FileOutputStream fileOut = new FileOutputStream(fileName);
		        workbook.write(fileOut);
		        fileOut.close();
		        System.out.println("Your excel file has been generated!");
		        fileName = "/download/"+outFileName;
		    } catch ( Exception ex ) {
		        ex.printStackTrace();
		    }
		    return fileName;
		}
}


