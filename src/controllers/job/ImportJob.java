package controllers.job;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream.GetField;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
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

import controllers.util.bigExcel.BigXlsxHandleUitl;

public class ImportJob implements Runnable{

	public void run() {
		System.out.println("------------job------------------");
		
		String filepath = PropKit.get("bom_ftp_folder");
		File file = new File(filepath);
		if (file.isDirectory()) {
            String[] filelist = file.list();
            for (int i = 0; i < filelist.length; i++) {
                File readfile = new File(filepath + "\\" + filelist[i]);
                if (!readfile.isDirectory()) {
                	String path = readfile.getPath();
                	Record re = importOrder(path);
                	if(re.get("result")){
                		File this_file = new File(path);
                		this_file.delete();
                		System.out.println(path+"导入成功!!!");
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
		}
		return resultMap;
	}


}
