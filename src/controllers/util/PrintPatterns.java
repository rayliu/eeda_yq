package controllers.util;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

import com.jfinal.plugin.activerecord.DbKit;

public class PrintPatterns {
	/**
	 * fileName:当前打印的模板
	 * outFileName:当前打印的名称
	 */
	private PrintPatterns(){
		
	}
	public static PrintPatterns getInstance(){
		return new PrintPatterns();
	}
	
	public String print(String fileName,String outFileName,HashMap<String, Object> hm){		
        File file = new File("WebRoot/download");
        if(!file.exists()){
       	 file.mkdir();
        }
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        
        outFileName += "-" + format.format(date) + ".pdf";
		try {
			JasperPrint print = JasperFillManager.fillReport(fileName, hm, DbKit.getConfig().getConnection());
			JasperExportManager.exportReportToPdfFile(print, outFileName);
		} catch (JRException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return outFileName;
   }
	//多张新建一个文件保存
	public String prints(String fileName,int n,String Path,String order_no,String outFileName,HashMap<String, Object> hm){		
        File file = new File(Path+"download/"+order_no);
        if(!file.exists()){
       	 file.mkdir();
        }else{
        	if(n==1){
        		deleteDir(file);
            	file.mkdir();
        	}
        }
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        outFileName += "-" + format.format(date) + ".pdf";
		try {
			JasperPrint print = JasperFillManager.fillReport(fileName, hm, DbKit.getConfig().getConnection());
			JasperExportManager.exportReportToPdfFile(print, outFileName);
		} catch (JRException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return outFileName;
   }
	//删除子目录的方法
	private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }
}
