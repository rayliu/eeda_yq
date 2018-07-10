package controllers.job;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream.GetField;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
                File readfile = new File(filepath + "/" + filelist[i]);
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
		
		System.out.println((new Date())+"------------un_zip------------------");
		unZip(PropKit.get("img_ftp_folder"), PropKit.get("old_zip_ftp_folder"));
		//unZip("F:\\kevin_zip\\","F:\\kevin_zip\\old_zip\\");
	}
	
	
	// 导入单据
	@Before(Tx.class)
	private synchronized Record importOrder(String path) {
		Connection conn = null;
		//UploadFile uploadFile = getFile("file", path, 20 * 1024 * 1024, "UTF-8");
		File file = new File(path);
		String fileName = file.getName();
		String strFile = file.getPath();
		System.out.println("进入import strFile:"+strFile);
		Record resultMap = new Record();
		resultMap.set("result", false);
		try {
			//exel格式区分
			 conn = DbKit.getConfig().getDataSource().getConnection();
			 DbKit.getConfig().setThreadLocalConnection(conn);
			 conn.setAutoCommit(false);// 自动提交变成false
			 if (fileName.endsWith(".xlsx")) {
				//导入前先清除掉表中数据
				System.out.println("进入import xlsx");
				if(file.exists()){
					System.out.println("进入import xlsx 存在");
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
	
	public static void unZip(String path,String new_path){
		boolean unzip_result = false;
		try {
	    	File file = new File(path);
	    	File[] filelist = file.listFiles();
	    	for (int i = 0; i < filelist.length; i++) {
	    		File f = filelist[i];
	    		if(f.getName().endsWith(".zip")){
	    			System.out.println("存在zip,开始解压");
	    			String pathName = f.getAbsolutePath();
	    			unzip_result = unZipFile(pathName , path);
	    			
	    			if(unzip_result){
	    				if(moveTotherFolders(pathName , new_path)){
	    					System.out.println("移动成功");
	    				}else{
	    					System.out.println("移动失败");
	    				}
	    			}else{
	    				System.out.println("解压失败");
	    			}
	    		}
	    	}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static boolean unZipFile(String pathName, String toPath) {
	    File desFile = null;
	    InputStream in = null;
	    OutputStream out = null;
	    ZipFile zf = null;
	    try {
	        //Log.v("unZipFile", "unzip start");
	        System.out.println("unzip start");
	        zf = new ZipFile(pathName, Charset.forName("GBK"));
	        for (Enumeration <? > entries = zf.entries(); entries.hasMoreElements();) {
	            ZipEntry entry = ((ZipEntry) entries.nextElement());
	            if (entry.isDirectory()) {
	                continue;
	            }
	            in = zf.getInputStream(entry);
	            String strEntry = entry.getName();
	            String strEntryFilename = strEntry.substring(strEntry.lastIndexOf("/") + 1);
	            String filePath = toPath + File.separator + strEntryFilename;
	            desFile = new File(filePath);
	            out = new FileOutputStream(desFile);
	            byte buffer2[] = new byte[1024 * 1024];
	            int len = -1;
	            while ((len = in .read(buffer2)) != -1) {
	                out.write(buffer2, 0, len);
	                //Log.v("unZipFile", "unZipFile len =" + len);
	                //System.out.println("unZipFile len =" + len);
	            }
	            //FileUtils.renameFile(desFile.getAbsolutePath(), filePath);
	        }
	        //Log.v("unZipFile", "unzip finish");
	        System.out.println("unzip finish");
	        return true;
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	        return false;
	    } finally {;
	    	try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        try {
				zf.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}
	
	public static boolean moveTotherFolders(String startPath , String endPath){
		File startFile = null;
		String newFilePath = null;
		boolean result = false;
	    try {
	        startFile = new File(startPath);
	        File tmpFile = new File(endPath);//获取文件夹路径
	        if(!tmpFile.exists()){//判断文件夹是否创建，没有创建则创建新文件夹
	            tmpFile.mkdirs();
	        }

	        newFilePath = endPath + startFile.getName();
	        File file = new File(endPath);
	    	File[] filelist = file.listFiles();
	    	for (int i = 0; i < filelist.length; i++) {
	    		File f = filelist[i];
	    		if(f.getName().endsWith(startFile.getName())){
	    			//文件名重复处理
	    			Date date = new Date();
	    			long code = date.getTime();
	    			newFilePath = newFilePath.substring(0, newFilePath.indexOf(".zip"))+"-"+code+".zip";
	    			System.out.println(newFilePath);
	    		}
	    	}
	    	result = startFile.renameTo(new File(newFilePath));
	    	if(result){
	    		System.out.println("success");
	    	}else{
	    		System.out.println("fail");
	    	}
	    } catch (Exception e) {
	        //log.info("文件移动异常！文件名：《{}》 起始路径：{}",fileName,startPath);
	    	System.out.println("文件移动异常！文件名：《"+startFile.getAbsolutePath()+"》 起始路径："+startPath);
	    }
	    return result;
	}


}
