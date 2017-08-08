package controllers.job;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream.GetField;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import models.UserLogin;

import com.jfinal.aop.Before;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import controllers.profile.ProductController;
import controllers.util.bigExcel.BigXlsxHandleUitl;

public class ImportJob implements Runnable{


	public void run() {
		System.out.println((new Date())+"------------job------------------");

		String filepath = PropKit.get("bom_ftp_folder");
		//String filepath = "C:/Users/Administrator/Desktop/job文件测试";
		System.out.println("filepath:" + filepath);
		File file = new File(filepath);
		System.out.println("filename:" +file.getName());
		if (file.isDirectory()) {
			System.out.println("存在文件fileFolder");
            String[] filelist = file.list();
            System.out.println("child_fileName number:"+filelist.length);
            for (int i = 0; i < filelist.length; i++) {
                File readfile = new File(filepath + "\\" + filelist[i]);
                if (!readfile.isDirectory()) {
                	long start = Calendar.getInstance().getTimeInMillis();
                	String file_name = filelist[i];
                	System.out.println("child_fileName:"+file_name);
                	Date begin_time = new Date();
                	String path = readfile.getPath();
                	Record result = importOrder(path);
                	if(result.get("result")){
                		File this_file = new File(path);
                		this_file.delete();
                		System.out.println(path+"导入成功!!!");
                		
                		long end = Calendar.getInstance().getTimeInMillis();
                        long time = (end- start)/1000;
                		Record order = new Record();
                		order.set("office_id", 1);
                		order.set("order_type", "bom");
                		order.set("doc_name", file_name);
                		order.set("create_time", begin_time);
                		order.set("complete_time", new Date());
                		order.set("import_time", time);
                		Db.save("import_log", order);
                	}
                }
            }
		}
		
		
	}
	
	
	// 导入单据
	@Before(Tx.class)
	private synchronized Record importOrder(String path) {
		Connection conn = null;
		//UploadFile uploadFile = getFile("file", path, 20 * 1024 * 1024, "UTF-8");
		File file = new File(path);
		String fileName = file.getName();
		String strFile = file.getPath();
		
		Record resultMap = new Record();
		try {
			//exel格式区分
			 conn = DbKit.getConfig().getDataSource().getConnection();
			 DbKit.getConfig().setThreadLocalConnection(conn);
			 conn.setAutoCommit(false);// 自动提交变成false
			 if (fileName.endsWith(".xlsx")) {
				//导入前先清除掉表中数据
				if(file.exists()){
					Db.update("delete from wmsproduct");
					BigXlsxHandleUitl.processFile(strFile);
					conn.commit();
					resultMap.set("result", true);
					resultMap.set("cause","导入成功！");
				}else{
					resultMap.set("result", false);
					resultMap.set("cause", "文件不存在");
				}
			} else {
				resultMap.set("result", false);
				resultMap.set("cause", "导入失败，目前只支持（xlsx）格式文件");
			}

		} catch (Exception e) {
			e.printStackTrace();
			resultMap.set("result", false);
			try {
				if (null != conn)
					conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally{
			try {
				if (null != conn) {
					conn.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			} finally {
				DbKit.getConfig().removeThreadLocalConnection();
			}
		}
		return resultMap;
	}


}
