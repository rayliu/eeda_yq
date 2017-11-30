package controllers.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;

public class FileUploadUtil {
	public static void uploadFile(List<UploadFile> fileList, 
	        String orderId,
	        Long userId, 
	        String tableName, boolean isLand) throws Exception {
	    for (int i = 0; i < fileList.size(); i++) {
            File file = fileList.get(i).getFile();
            //file.length()/1024/1024
            if(getFileSize(file)>10){
                throw new Exception("文件不能超过10M.");
            }
            String fileName = file.getName();
            
            Record r = new Record();
            if(isLand){
                r.set("land_id", orderId);
            }else{
                r.set("order_id", orderId);
            }
            r.set("uploader", userId);
            r.set("doc_name", fileName);
            r.set("upload_time", new Date());
            Db.save(tableName, r);
        }
		
	}
	
	public static void uploadTypeFile(List<UploadFile> fileList, 
	        String orderId,
	        Long userId, 
	        String tableName, boolean isLand,String type,String bill_type) throws Exception {
	    for (int i = 0; i < fileList.size(); i++) {
            File file = fileList.get(i).getFile();
            //file.length()/1024/1024
            if(getFileSize(file)>20){
                throw new Exception("文件不能超过20M.");
            }
            String fileName = file.getName();
            
            Record r = new Record();
            if(isLand){
                r.set("land_id", orderId);
            }else{
                r.set("order_id", orderId);
            }
    		r.set("order_type",type);
            r.set("uploader", userId);
            r.set("doc_name", fileName);
            r.set("bill_type", bill_type);            
            r.set("upload_time", new Date());
            Db.save(tableName, r);
        }
		
	}
	
	@SuppressWarnings("resource")
    public static double getFileSize(File file) throws IOException{
	    FileInputStream fis = new FileInputStream(file);
	    double fileSize = (double)((double)fis.available()/1024/1024);
	    return fileSize;
	}
}
