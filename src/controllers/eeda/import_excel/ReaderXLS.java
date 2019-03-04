package controllers.eeda.import_excel;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFFontFormatting;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFCellUtil;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
@SuppressWarnings({"rawtypes","unused","deprecation"})
public class ReaderXLS{
	private POIFSFileSystem fs;
    private HSSFWorkbook wb;
    private HSSFSheet sheet;
    private HSSFRow row;
    public static String[] xlsTitle;
    public static List<Map<String,String>> xlsContent;

    /**
     * 读取Excel表格表头的内容
     * @param InputStream
     * @return String 表头内容的数组
     */
    private String[] readExcelTitle(InputStream is) throws Exception{
        try {
            fs = new POIFSFileSystem(is);
            wb = new HSSFWorkbook(fs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sheet = wb.getSheetAt(0);
        row = sheet.getRow(0);
        // 标题总列数
        int colNum = row.getPhysicalNumberOfCells();
        System.out.println("colNum:" + colNum);
        //String[] title = new String[colNum];
        xlsTitle = new String[colNum];
        for (int i = 0; i < colNum; i++) {
            //title[i] = getStringCellValue(row.getCell((short) i));
            xlsTitle[i] = getCellFormatValue(row.getCell((short) i));
        }
        return xlsTitle;
    }

    /**
     * 读取Excel数据内容
     * @param InputStream
     * @return Map 包含单元格数据内容的Map对象
     */
    private List<Map<String,String>> readExcelContent(InputStream is) throws Exception{
        //String str = "";
    	xlsContent = new ArrayList<Map<String,String>>();
        try {
            fs = new POIFSFileSystem(is);
            wb = new HSSFWorkbook(fs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sheet = wb.getSheetAt(0);
        // 得到总行数
        int rowNum = sheet.getLastRowNum();
        row = sheet.getRow(0);
        /*
        getPhysicalNumberOfCells 是获取不为空的列个数。 
        getLastCellNum 是获取最后一个不为空的列是第几个。
        */
        int colNum = row.getPhysicalNumberOfCells();
        // 正文内容应该从第二行开始,第一行为表头的标题
        for (int i = 1; i <= rowNum; i++) {
        	Map<String, String> rowData = new HashMap<String, String>();
            row = sheet.getRow(i);
            int j = 0;
            while (j < colNum) {
                // 每个单元格的数据内容用"-"分割开，以后需要时用String类的replace()方法还原数据
                // 也可以将每个单元格的数据设置到一个javabean的属性中，此时需要新建一个javabean
                // str += getStringCellValue(row.getCell((short) j)).trim() +
                // "-";
                //str += getCellFormatValue(row.getCell((short) j)).trim() + "    ";
                //j++;
        		rowData.put(xlsTitle[j], getCellFormatValue(row.getCell((short) j)).trim());
            	j++;
            	
            }
            //content.put(i, str);
            //str = "";
            xlsContent.add(rowData);
        }
        int ss = xlsContent.size()-1;
        for (int j = ss; j >=0; j--) {
        	int num = 0;
    		for (int i = 0; i < xlsTitle.length; i++) {
                if("".equals(xlsContent.get(j).get(xlsTitle[i]))){
                	if(num == xlsTitle.length-1){
                		xlsContent.remove(j);
                	}
                	++num;
                }
            }
        }
        
        return xlsContent;
    }

    /**
     * 获取单元格数据内容为字符串类型的数据
     * 
     * @param cell Excel单元格
     * @return String 单元格数据内容
     */
	private String getStringCellValue(HSSFCell cell) throws Exception{
        String strCell = "";
        switch (cell.getCellType()) {
        case HSSFCell.CELL_TYPE_STRING:
            strCell = cell.getStringCellValue();
            break;
        case HSSFCell.CELL_TYPE_NUMERIC:
            strCell = String.valueOf(cell.getNumericCellValue());
            break;
        case HSSFCell.CELL_TYPE_BOOLEAN:
            strCell = String.valueOf(cell.getBooleanCellValue());
            break;
        case HSSFCell.CELL_TYPE_BLANK:
            strCell = "";
            break;
        default:
            strCell = "";
            break;
        }
        if (strCell.equals("") || strCell == null) {
            return "";
        }
        if (cell == null) {
            return "";
        }
        return strCell;
    }

    /**
     * 获取单元格数据内容为日期类型的数据
     * 
     * @param cell
     *            Excel单元格
     * @return String 单元格数据内容
     */
	private String getDateCellValue(HSSFCell cell) throws Exception{
        String result = "";
        try {
            int cellType = cell.getCellType();
            if (cellType == HSSFCell.CELL_TYPE_NUMERIC) {
                Date date = cell.getDateCellValue();
                result = (date.getYear() + 1900) + "-" + (date.getMonth() + 1)
                        + "-" + date.getDate();
            } else if (cellType == HSSFCell.CELL_TYPE_STRING) {
                String date = getStringCellValue(cell);
                result = date.replaceAll("[年月]", "-").replace("日", "").trim();
            } else if (cellType == HSSFCell.CELL_TYPE_BLANK) {
                result = "";
            }
        } catch (Exception e) {
            System.out.println("日期格式不正确!");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 根据HSSFCell类型设置数据
     * @param cell
     * @return
     */
    private String getCellFormatValue(HSSFCell cell) throws Exception{
        String cellvalue = "";
        if (cell != null) {
            // 判断当前Cell的Type
            switch (cell.getCellType()) {
            // 如果当前Cell的Type为NUMERIC
            case HSSFCell.CELL_TYPE_NUMERIC:
            case HSSFCell.CELL_TYPE_FORMULA: {
                // 判断当前的cell是否为Date
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    // 如果是Date类型则，转化为Data格式
                    //方法1：这样子的data格式是带时分秒的：2011-10-12 0:00:00
                    //cellvalue = cell.getDateCellValue().toLocaleString();
                    //方法2：这样子的data格式是不带带时分秒的：2011-10-12
                    Date date = cell.getDateCellValue();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    cellvalue = sdf.format(date);
                }   
                // 如果是纯数字
                else {
                    // 取得当前Cell的数值,返回：3.000008976E8
                    //cellvalue = String.valueOf(cell.getNumericCellValue());
                	if(!"NaN".equals(String.format("%.0f", cell.getNumericCellValue()))){
                		cellvalue = String.format("%.0f", cell.getNumericCellValue());
                	}
                	else{
                	cellvalue = String.format( cell.getStringCellValue());
                	}
                }
                break;
            }
            // 如果当前Cell的Type为STRING
            case HSSFCell.CELL_TYPE_STRING:
                // 取得当前的Cell字符串
                cellvalue = cell.getRichStringCellValue().getString();
                break;
            // 默认的Cell值
            default:
                cellvalue = " ";
            }
        } else {
            cellvalue = "";
        }
        return cellvalue;

    }
    
    //读取Excel表格标题
    public static String[] getXlsTitle(File xlsFile) throws Exception {
    	
    	InputStream is = new FileInputStream(xlsFile);
        ReaderXLS excelReader = new ReaderXLS();
        excelReader.readExcelTitle(is);
    	return xlsTitle;
    }
    //读取Excel表格内容
    public static List<Map<String,String>> getXlsContent(File xlsFile) throws Exception {
    	InputStream is = new FileInputStream(xlsFile);
    	ReaderXLS excelReader = new ReaderXLS();
    	xlsContent = excelReader.readExcelContent(is);
    	return xlsContent;
    }
    /*public static void main(String[] args) {
        try {
            // 对读取Excel表格标题测试
            InputStream is = new FileInputStream("d:\\广电运通.xls");
            ReaderXLS excelReader = new ReaderXLS();
            String[] title = excelReader.readExcelTitle(is);
            System.out.println("获得Excel表格的标题:");
            for (String s : title) {
                System.out.print(s + " ");
            }

            // 对读取Excel表格内容测试
            InputStream is2 = new FileInputStream("d:\\广电运通.xls");
            //Map<Integer, String> map = excelReader.readExcelContent(is2);
            List<Map<String,String>> content = excelReader.readExcelContent(is2);
            
        	for (Map map2 : content) {
            	System.out.println();
            	for (int i = 0; i <= map2.size()-1; i++) {
                    System.out.print(title[i]+":"+map2.get(title[i])+"  ");
                }
			}
				
        } catch (FileNotFoundException e) {
            System.out.println("未找到指定路径的文件!");
            e.printStackTrace();
        }
    }*/
    
}
