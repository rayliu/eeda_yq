package controllers.eeda.import_excel;


import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;


@RequiresAuthentication
public class CheckOrder extends Controller {
	private Logger logger = Logger.getLogger(CheckOrder.class);
	Subject currentUser = SecurityUtils.getSubject();
	//Long user_id = null;
	
	/**
	 * 校验表头是否和数据库的相符
	 * @param title
	 * @param execlType
	 * @return
	 */
	public boolean checkoutExeclTitle(String[] title, String execlType) {
		boolean flag = true;
		List<Record> dbTitleList = Db.find("select execl_title from execl_title where execl_type = ? ", execlType);
		if (dbTitleList != null) {
			// 判断总数是否相等
			if (dbTitleList.size() != title.length) {
				flag = false;
			}else{
				// 判断是否所有列标题一致
				List<String> titleList = new ArrayList<String>(dbTitleList.size());
				for (Record record : dbTitleList) {
					titleList.add(record.getStr("execl_title").trim());
				}
				
				for (int i = 0; i < title.length; i++) {
					String excelTitle = title[i];
					if (!titleList.contains(excelTitle.trim())) {
						flag = false;
					}
				}
			}
		}
		return flag;
	}
	

	
	/**
	 * 邮箱校验
	 */
	public static boolean checkEmail (String email){
		boolean flag = true;
		if (!email.matches("[\\w\\.\\-]+@([\\w\\-]+\\.)+[\\w\\-]+")) {
			flag = false;
	    }
		return flag;
	}

	/**
	 * 手机号效验
	 */
	public static boolean checkPhone(String phone){
		boolean flag = true;
		if(!phone.matches("^[1](([3][0-9])|([4][5,7,9])|([5][0,1,4,6,8,9])|([6][6])|([7][3,5,6,7,8])|([8][0-9])|([9][8,9]))[0-9]{8}$")){
			flag =false;
		}
		return flag;
	}
	
	/**
	 * 日期格式校验
	 * @param value
	 * @return
	 */
	public static boolean checkDate(String dateValue) {    
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");  
        try{  
            Date date = (Date)formatter.parse(dateValue);   
            return dateValue.equals(formatter.format(date));  
        }catch(Exception e){  
            return false;  
        }  
    }  
	
	

	
	
	/**
	 * 数据校验
	 * @param lines
	 * @return
	 */
	@Before(Tx.class)
	public Record importTJCheck( List<Map<String, String>> lines, String activity_id) {
		Record result = new Record();
		result.set("result",true);
		//if ("true".equals(importResult.get("result"))) {
			int rowNumber = 2;//最左边的编码行
			String error_msg = "";
			try {
				for (Map<String, String> line :lines) {
					String attendee_name = line.get("姓名").trim();
					String attendee_mobile = line.get("手机号").trim();
					String attendee_mail = line.get("邮件").trim();
					String ticket_name = line.get("票种名称").trim();
					String attendee_company_name = line.get("公司名称").trim();
					String attendee_job_title = line.get("职务").trim();
					if(StringUtils.isBlank(attendee_name)){
						error_msg += "第"+rowNumber+"行【姓名】，字段不能为空<br/>";
					}
					
					if(StringUtils.isNotBlank(attendee_mobile)){
						if(!checkPhone(attendee_mobile)){
							error_msg += "第"+rowNumber+"行【手机号】，手机号格式不正确<br/>"; 
						}
					}else{
						error_msg += "第"+rowNumber+"行【手机号】，字段不能为空<br/>";
					}
					
					if(StringUtils.isNotBlank(attendee_mail)){
						if(!checkEmail(attendee_mail)){
							error_msg += "第"+rowNumber+"行【邮件】，邮件格式不正确<br/>";
						}
					}else{
						error_msg += "第"+rowNumber+"行【邮件】，字段不能为空<br/>";
					}
					
					if(StringUtils.isNotBlank(ticket_name)){
						Record check_ticket = Db.findFirst("SELECT * FROM t_activity_ticket WHERE activity_id = ? AND ticket_title = ? ",activity_id,ticket_name);
						if(check_ticket==null){
							error_msg += "第"+rowNumber+"行【票种名称】，系统无此数据，请核对是否有误<br/>";
						}
					}else{
						error_msg += "第"+rowNumber+"行【票种名称】，字段不能为空<br/>";
					}
					
					rowNumber++;
				}
				
				if(StringUtils.isNotBlank(error_msg)){
					throw new Exception();
				}
			} catch (Exception e) {
				result.set("result", false);
				result.set("cause", error_msg);
				return result;
			} 
		return result;
	}

	
    /**
     * mails_list
     * @param lines
     * @param login_user
     * @param order_id
     * @return
     */
	public Record importMLValue( List<Map<String, String>> lines, Record login_user, String form_id) {
		Connection conn = null;
		Record result = new Record();
		result.set("result",true);
		
		String table_name = "form_" + form_id;
		List<Record> list = Db.find("select * from eeda_form_field where "
                + " form_id=? order by if(isnull(seq),1,0), seq", form_id);
		String[] title_name = new String[list.size()];
    	for(Record item : list){
    		String name = item.getStr("FIELD_DISPLAY_NAME");
    		title_name[list.indexOf(item)] = name;
    	}

		int rowNumber = 1;
		try {
			conn = DbKit.getConfig().getDataSource().getConnection();
			DbKit.getConfig().setThreadLocalConnection(conn);
			conn.setAutoCommit(false);// 自动提交变成false
			
			Record order = null;
			for (Map<String, String> line :lines) {
				order = new Record();
				for(Record item : list){
					String nick_name = item.getStr("FIELD_DISPLAY_NAME");
					String file_name = item.getStr("FIELD_NAME");
					String item_id = item.getLong("ID").toString();
					file_name = "f"+ item_id + "_" + file_name;
					String value = line.get(nick_name).trim();
					if(StringUtils.isNotBlank(value)){
						order.set(file_name, value);
					}
				}
				Db.save(table_name, order);
				rowNumber++;
			}
			conn.commit();
			result.set("cause","成功导入( "+(rowNumber-1)+" )条数据！");
		} catch (Exception e) {
			System.out.println("导入操作异常！");
			System.out.println(e.getMessage());
			e.printStackTrace();
			
			try {
				if (null != conn)
					conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}

			result.set("result", false);
			result.set("cause", "导入失败<br/>数据导入至第" + (rowNumber)
						+ "行时出现异常:" + e.getMessage() + "<br/>导入数据已取消！");
		} finally {
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
		
		return result;
	}
	
}
