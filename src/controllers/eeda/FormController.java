package controllers.eeda;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.resource.StringTemplateResourceLoader;

import com.google.gson.Gson;
//import cache.EedaServiceCache;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import cn.hutool.core.io.FileUtil;
import controllers.eeda.import_excel.CheckOrder;
import controllers.eeda.import_excel.ReaderXLS;
import controllers.eeda.import_excel.ReaderXlSX;
import controllers.form.FormClickService;
import controllers.form.FormEventConfigService;
import controllers.form.FormService;
import controllers.form.FormTableConfigService;
import controllers.form.FormUtil;
import controllers.form.TemplateService;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.PingYinUtil;
import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;
import models.UserLogin;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class FormController extends Controller {
    private Log logger = Log.getLog(FormController.class);
    private static GroupTemplate gt=null;
    
    private GroupTemplate getGroupTemplate() throws IOException{
        if(gt==null){
          StringTemplateResourceLoader resourceLoader = new StringTemplateResourceLoader(); 
          Configuration cfg = Configuration.defaultConfiguration();
          gt = new GroupTemplate(resourceLoader, cfg);
        }
        return gt;
    }
    @Before({EedaMenuInterceptor.class, Tx.class})
    @SuppressWarnings("unchecked")
    public void index() throws IOException {
        logger.debug("thread["+Thread.currentThread().getName()+
                "] -------------Eeda form---------------");
        UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
        long office_id = user.getLong("office_id");
        
        String module_id = getPara(0);
        String action = getPara(1);
        Long order_id = getParaToLong(2);
        //以下为表单的标准 action
        
        //list 跳转到form 列表查询 页面;
        //doQuery ajax 列表查询 动作; 
        
        //add 跳转到新增页面; 
        //doAdd 新增动作
        
        //edit 跳转到编辑页面; 
        //doUpdate 编辑的保存动作
        
        
        //click 表单按钮的动作
        //valueChange 表单按钮的动作, 参数 {field_name, value, old_value}
        //tableConfig 从表表单的配置, 参数 {field_id_list}
        
        setAttr("action", action);
        setAttr("module_id", module_id);
        
        if("doQuery".equals(action)){
            
            Map<String,Object> listMap = queryForm(Long.valueOf(module_id));
            renderJson(listMap);
            return;
        }
        
        //获取对应的title菜单栏
        Record title = Db.findFirst("select m1.module_name level2, m1.id level2_id,m2.module_name level1 from eeda_modules m1 "
        		+ " LEFT JOIN eeda_modules m2 on m1.parent_id = m2.id"
        		+ " where m1.id = ?",module_id);
        
        if(title != null){
            setAttr("level1", title.get("level1"));
            setAttr("level2", title.get("level2"));
            setAttr("level2_id", title.get("level2_id"));
        }
       
        Record formRec = Db.findFirst("select * from eeda_form_define where "
                + " module_id=? and office_id=?", module_id, office_id);
        if(formRec ==null){
            logger.debug("-------------form 没有定义!---------------");
            redirect("/");
            return;
        }
        Long form_id = formRec.getLong("id");
        logger.debug("-------------Eeda module:"+module_id+", form_id:"+form_id+", action: "+action+"---------------");
        
        if("eventConfig".equals(action)){
            FormEventConfigService ecs = new FormEventConfigService(this);
            List<Record> itemList = ecs.getEventConfig(user, form_id);
            renderJson(itemList);
        }else if("tableConfig".equals(action)){
            String jsonStr = getPara("field_id_list");
            Gson gson = new Gson();
            ArrayList<String> fieldIdList = gson.fromJson(jsonStr, ArrayList.class);
            FormTableConfigService ets = new FormTableConfigService(this);
            List<Record> list = ets.getTableConfig(fieldIdList, office_id);
            renderJson(list);
        }else if("valueChange".equals(action)){
            List<Record> recList = Db.find("select * from eeda_form_event where "
                    + " menu_type='value_change' and form_id=?", form_id);
            for (Record event : recList) {
                if("set_css".equals(event.getStr("type"))){
                    Record cssRec = Db.findFirst("select * from eeda_form_event_css where event_id=?", event.getLong("id"));
                    List<Record> cssItemList = Db.find("select * from eeda_form_event_css_item where "
                            + " event_id=?", event.getLong("id"));
                    cssRec.set("set_field_list", cssItemList);
                    event.set("set_css", cssRec);
                }
            }
            renderJson(recList);
        }else if("click".equals(action)){
            Long btn_id = order_id;
            FormClickService ecs = new FormClickService(this);
            
            List<Record> recList;
            try {
                recList = ecs.handleClickAction(title, form_id, btn_id, office_id);
                renderJson(recList);
            }  catch (Exception e) {
                e.printStackTrace();
                Record errRec = new Record();
                errRec.set("error_code", 500);
                errRec.set("error_msg", e.getMessage());
                renderJson(errRec);
            }
        }else if(!action.startsWith("do")){
            if("edit".equals(action)){
                edit(form_id, order_id, formRec);
            }else if("view".equals(action)){
                
            }else if("add".equals(action)){
                edit(form_id, null, formRec);
            }else if("list".equals(action)){
                if("Y".equals(formRec.getStr("is_single_record"))){//单页显示，不需要list
                    Record orderRec = Db.findFirst("select * from form_"+form_id);
                    if(orderRec!=null){//有一条数据，跳转去edit页面
                        redirect("/form/"+module_id+"-edit-"+orderRec.getLong("id"));
                    }else{//无数据，跳转去add页面，有可能是一个展示页面，不需要记录数据
                        redirect("/form/"+module_id+"-add");
                    }
                }else{
                    list(form_id);
                    render("/eeda/form/listTemplate.html");
                }
                return;
            }
            
            render("/eeda/form/template.html");
        }else if("doDelete".equals(action)){
            FormService formService = new FormService(this);
            String ip = MainController.getIpAddress(getRequest());
            formService.saveSysLog(action,getPara("data"),form_id,order_id,user_id,office_id,ip);
            String form_name = "form_"+form_id;
            Record form  = Db.findById(form_name, order_id);
            form.set("eeda_delete","Y");
                boolean result = Db.update(form_name, form);
                renderJson("{\"result\":"+result+"}");
        }else{
            Record rec = new Record();
            if("doGet".equals(action)){
                rec = FormUtil.getFormData(form_id, order_id, office_id);
                renderJson(rec);
            }else if ("doAdd".equals(action) || "doUpdate".equals(action)){
                rec = saveForm();//如果是set_value, 对外部表操作，则返回空rec
                if(rec.getColumnNames().length>0){
                    String ip = MainController.getIpAddress(getRequest());
    //            	saveSysLog(action,getPara("data"),form_id,order_id,user_id,office_id,ip);
                    String jsonStr = getPara("data");
                    Gson gson = new Gson();
                    Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);
                    String dto_order_id = (String)dto.get("order_id");
                    Record sysLog = new Record();
                    if(StrKit.isBlank(dto_order_id)){
                        action = "doAdd";
                    }
                	
                    sysLog.set("log_type", "operate");
                    sysLog.set("operation_obj", getPara("data"));
                    sysLog.set("action_type", action);
                    sysLog.set("create_stamp", new Date());
                    sysLog.set("user_id", user_id);
                    sysLog.set("ip",ip);
                    sysLog.set("office_id", office_id);
                    sysLog.set("form_id", form_id);
                    sysLog.set("order_id", rec.get("id").toString());
                    Db.save("sys_log", sysLog);
                }
                renderJson(rec);
            }
            
        }
    }
    
    
    public void chart() {
        String form_id = getPara("form_id");
        Record chartRec = Db.findFirst("select * from eeda_form_charts where form_id=?", form_id);
        renderJson(chartRec);
    }
    
	public void import_excel(){
    	String module_id = getPara("module_id");
    	String form_id = getPara("form_id");
    	UploadFile uploadFile = getFile();
		File file = uploadFile.getFile();
		
		UserLogin user = LoginUserController.getLoginUser(this);
		String fileName = file.getName(); //文件名称
		String msg = "";
		boolean result = false;

		Record resultMap = new Record();
		try {
			String[] title = null;
			List<Map<String, String>> content = new ArrayList<Map<String, String>>();
			//exel格式区分
			long start = new Date().getTime();
			if (fileName.endsWith(".xls")) {
				title = ReaderXLS.getXlsTitle(file);
				content = ReaderXLS.getXlsContent(file);
			} else if (fileName.endsWith(".xlsx")) {
				title = ReaderXlSX.getXlsTitle(file);
				content = ReaderXlSX.getXlsContent(file);
			} else {
				resultMap.set("result", false);
				resultMap.set("cause", "导入失败，请选择正确的excel文件（xls/xlsx）");
			}

			if (title != null && content.size() > 0) {
				CheckOrder checkOrder = new CheckOrder();
//			
//				// 内容校验
//				resultMap = checkOrder.importMLCheck(content,order_id);
				// 内容开始导入
//				if(resultMap.getBoolean("result")){
					resultMap = checkOrder.importMLValue(content, user.toRecord(), form_id);
//				}
				System.out.println("总行数：" + content.size());
				System.out.println(new Date().getTime() - start);
			}else{
				resultMap.set("result", false);
				resultMap.set("cause", "导入失败，excel内容不能为空");
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.set("result", false);
			resultMap.set("cause", "导入失败，请检测excel内容是否填写规范（尽量避免函数求值列内容）<br/>（建议使用Microsoft Office Excel软件操作数据）");
		}

		renderJson(resultMap);
    }
	
	
    private boolean saveSysLog(String action,String jsonStr,long form_id,long order_id,long user_id,long office_id,String ip){
        Record sysLog = new Record();
        sysLog.set("log_type", "operate");
        sysLog.set("operation_obj", jsonStr);
        sysLog.set("action_type", action);
        sysLog.set("create_stamp", new Date());
        sysLog.set("user_id", user_id);
        sysLog.set("ip",ip);
        sysLog.set("office_id", office_id);
        sysLog.set("form_id", form_id);
        sysLog.set("order_id", order_id);
        return Db.save("sys_log", sysLog);
    }
    
    private Record saveForm(){
        Record rec = new Record();
        String jsonStr=getPara("data");
        
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);
        
        String module_id = (String) dto.get("module_id");
        Record formRec = Db.findFirst("select * from eeda_form_define where "
                + " module_id=?", module_id);
        if(formRec ==null){
            logger.debug("-------------form 没有定义!---------------");
            redirect("/");
            return rec;
        }
        Long form_id = formRec.getLong("id");
        Long office_id = formRec.getLong("office_id");
        if("set_value".equals(dto.get("type"))){
        	List<Record> esvi_list = Db.find("select * from eeda_form_event_set_value_item where event_id = ?",dto.get("event_id"));
        	if(esvi_list.size()>0){
        		for(Record esvi :esvi_list){
            		Record field = Db.findFirst("select * from eeda_form_field where form_id = ? and field_display_name = ?",form_id,esvi.get("name"));
                	if(field!=null){
                		String form_name = "form_"+form_id;
                		String column_name = "f"+field.get("id")+"_"+field.get("field_name");
                		Record form = Db.findById(form_name, dto.get("order_id"));
                		form.set(column_name, esvi.get("value"));
                		Db.update(form_name, form);
                		form.set("form_name", form_name);
                		rec = form;
                	}
            	}
        	}
        }else{
        	
            for (Entry<String, ?> entry : dto.entrySet()) { 
                String key = entry.getKey();
                if(key.startsWith("form_"+form_id)){
                    String colName = key.split("-")[1];
                    String value = String.valueOf(entry.getValue()).trim();
                    //处理-自动编号
                    String fieldId = colName.split("_")[0].substring(1);
                    String fieldName = colName.split("_")[1];
                    Record fieldRec = 
                            Db.findFirst("select * from eeda_form_field where form_id=? and field_name=?",
                                    form_id, fieldName);
                    if(fieldRec != null){
                        String field_type = fieldRec.getStr("field_type");
                        switch(field_type) {
                            case "自动编号":
                                if(StrKit.isBlank(value)){
                                    String orderNo = handleAutoNo(form_id, colName, fieldId);
                                    value = orderNo;
                                }
                                break;
                            case "图片":
                                rec.set(colName, value);
                                break;
                            case "日期":
                            case "日期时间":
                                if(StrKit.isBlank(value)){
                                    continue;
                                }
                                break;
                        }
                    }
                    if("null".equals(value))
                        continue;
                    rec.set(colName, value);
                }
            }
            
            
            
            String order_id = (String) dto.get("order_id");
            if (StrKit.isBlank(order_id)) {
                String event_id = "";
                Object event_id_obj = dto.get("event_id");
                if (event_id_obj instanceof java.lang.Double) {
                    event_id = String.valueOf(((Double) event_id_obj).longValue());
                } else {
                    event_id = (String) event_id_obj;
                }
                
                String event_form_id = "";
                Object event_form_id_obj = dto.get("form_id");
                if (event_form_id_obj instanceof java.lang.Double) {
                    event_form_id = String.valueOf(((Double) event_form_id_obj).longValue());
                } else {
                    event_form_id = (String) event_form_id_obj;
                }
                
                if(StringUtils.isNotBlank(event_id)&&StringUtils.isNotBlank(event_form_id)){
                
                	List<Record> esvi_list = Db.find("select * from eeda_form_event_save_set_value_item where event_id = ?", event_id);
                	if(esvi_list.size()>0){
                		for(Record esvi :esvi_list){
                    		Record field = Db.findFirst("select * from eeda_form_field where form_id = ? and field_display_name = ?",form_id,esvi.get("name"));
                        	if(field!=null){
                        		UserLogin user = LoginUserController.getLoginUser(this);
                        		String column_name = "f"+field.get("id")+"_"+field.get("field_name");
                        		
                        		if("系统变量.当前用户名".equals(esvi.get("value"))){
                        			rec.set(column_name, user.get("c_name"));
                        		}else if("系统变量.当前时间".equals(esvi.get("value"))){
                        			rec.set(column_name, new Date());
                        		}else{
                        			rec.set(column_name, esvi.get("value"));
                        		}
                        	}
                    	}
                	}
                }
                Db.save("form_"+form_id, rec);
                rec.set("form_name","form_"+form_id);
            }else{
                rec.set("id", Long.valueOf(order_id));
                Db.update("form_"+form_id, rec);
            }
            
            //img_list
            List<Map<String, ?>> img_list = (ArrayList<Map<String, ?>>)dto.get("img_list");
            saveFiles(rec.getLong("id"), img_list, "img");
            //file_list
            List<Map<String, ?>> file_list = (ArrayList<Map<String, ?>>)dto.get("file_list");
            saveFiles(rec.getLong("id"), file_list, "file");
            //处理从表保存
            List<Map<String, ?>> detailList = (ArrayList<Map<String, ?>>)dto.get("detail_tables");
            for (Map<String, ?> detail : detailList) {
                 String table_id = (String) detail.get("table_id");
                 String field_id = table_id.split("_")[2];
                 //1.通过field_id 找到对应的明细表 form_id
                 Record detailRec = Db.findFirst("select distinct form.* from eeda_form_field f, eeda_form_field_type_detail_ref ref, eeda_form_define form "
                    +" where f.id = ref.field_id "
                    +" and ref.target_form_name = form.name"
                    +" and f.id = ?", field_id);
                 //2.通过field_id 找到对应的明细表 的 关联字段, 现在先做单个//TODO
                 Record detailConditionRec = Db.findFirst("select ref.* from eeda_form_field f, eeda_form_field_type_detail_ref_join_condition ref "
                         +" where f.id = ref.field_id "
                         +" and f.id = ?", field_id);
                 String field_from = detailConditionRec.getStr("field_from");
                 String field_to = detailConditionRec.getStr("field_to");
                 //主表关联值
                 Record field_rec = FormService.getFieldName(field_from.split("\\.")[0], field_from.split("\\.")[1], office_id);//获取数据库对应的名称: f59_xh
                 String field_from_name = "f"+field_rec.getLong("id")+"_"+field_rec.getStr("field_name");
                 Object from_field_value = rec.get(field_from_name);
                 //从表关联值
                 Record field_to_rec = FormService.getFieldName(field_to.split("\\.")[0], field_to.split("\\.")[1], office_id);//获取数据库对应的名称: f59_xh
                 String field_to_name = "f"+field_to_rec.getLong("id")+"_"+field_to_rec.getStr("field_name");
                 
                 Long detail_form_id = detailRec.getLong("id");
                 List<Map<String, ?>> detailDataList = (List<Map<String, ?>>)detail.get("data_list");
                 for (Map<String, ?> rowMap : detailDataList) {
                     Record rowRec = new Record();
                     for (Entry<String, ?> entry : rowMap.entrySet()) { 
                         String colName = entry.getKey();
                         String value = String.valueOf(entry.getValue()).trim();
                         if("id".equals(colName)){
                             if(!StrKit.isBlank(value)){
                                 rowRec.set(colName, Long.valueOf(value));
                             }
                         }else{
                             rowRec.set(colName, value);
                         }
                     }
                     rowRec.set(field_to_name, from_field_value);//关联字段 赋值
                     if(rowRec.get("id")==null){
                         Db.save("form_"+detail_form_id, rowRec);
                     }else{
                    	 if("Y".equals(rowRec.get("is_delete"))){
                    		 Record re = Db.findById("form_"+detail_form_id, rowRec.get("id"));
                    		 if(re!=null){
                    			 Db.delete("form_"+detail_form_id, re);
                    		 }
                    	 }else{
                    		 Db.update("form_"+detail_form_id, rowRec);
                    	 }
                     }
                }
            }
        }
        return rec;
    }
    private void saveFiles(Long order_id, List<Map<String, ?>> img_list, String type) {
        if(img_list.size()>0){
        	for(int i =0;i<img_list.size();i++){
        		String id = (String) img_list.get(i).get("id");
        		String is_delete = (String) img_list.get(i).get("is_delete");
        		Record img = new Record();
        		if(StringUtils.isBlank(id)){
        			img.set("img_name", img_list.get(i).get("name"));
        			img.set("field_id", img_list.get(i).get("field_id"));
        			img.set("img_url", img_list.get(i).get("url"));
        			img.set("order_id", order_id);
        			img.set("type", type);
        			Db.save("eeda_form_field_type_img", img);
        		}else{
        			img = Db.findById("eeda_form_field_type_img", id);
        			
        			if(img!=null){
        				if("Y".equals(is_delete)){
        				    img.set("is_delete", "Y");
        					Db.update("eeda_form_field_type_img", img);
        				}
        			}
        		}
        	}
        }
    }
    private String handleAutoNo(Long form_id, String colName, String fieldId) {
        List<Record> fieldRecs = 
                Db.find("select * from eeda_form_field_type_auto_no_item where field_id=? order by id",
                        fieldId);
        String strNo="";
        for (Record record : fieldRecs) {
            String type = record.getStr("type");
            if("固定文字".equals(type)){
                strNo += record.getStr("value");
            }else if("日期变量".equals(type)){
                Date d=new Date();
                SimpleDateFormat sf=new SimpleDateFormat(record.getStr("value"));
                strNo += sf.format(d);
            }else if("流水号位数".equals(type)){
                String seqLength = record.getStr("value");
                int iSeqLength = Integer.parseInt(seqLength);
                Record order_rec = Db.findFirst("select * from form_"+form_id+" where "+colName+" like '%"+strNo+"%' order by id desc ");
                String latestNo = "0";
                if(order_rec!=null){
                    latestNo = order_rec.getStr(colName).replaceAll(strNo, "");
                }
                
                int iSeqNo = Integer.parseInt(latestNo)+1;
                
                String serial = String.valueOf(iSeqNo);   //构造后的序号
                int length = serial.length();
                if( length < iSeqLength){
                    int c = iSeqLength - length;
                    String zero = "";
                    for (int j = 0; j < c; j++) {
                        zero += "0";
                    }
                    serial = zero+serial;
                }
                strNo += serial;
                
            }
        }
        return strNo;
    }
    
    private List<Record> list(Long form_id){
        setAttr("btnList", getFormBtns(form_id, "list"));
        List<Record> fieldList = new ArrayList<Record>();
        Record re = Db.findFirst("select * from eeda_form_define where id = ?",form_id);
        long office_id = re.getLong("office_id");
        if("search_form".equals(re.get("type"))){
            fieldList = getDisplayCols(form_id, office_id);
        }else if("form".equals(re.get("type"))){
            fieldList = Db.find("select * from eeda_form_field where "
                    + " form_id=? order by if(isnull(seq),1,0), seq", form_id);
        }
        setAttr("form_id", form_id);
        setAttr("field_list", fieldList);
        setAttr("field_list_json", JsonKit.toJson(fieldList));
        return fieldList;
    }
    
    public List<Record> list(Long form_id,Controller controller){
    	controller.setAttr("btnList", getFormBtns(form_id, "list"));
        List<Record> fieldList = new ArrayList<Record>();
        Record re = Db.findFirst("select * from eeda_form_define where id = ?",form_id);
        long office_id = re.getLong("office_id");
        if("search_form".equals(re.get("type"))){
            fieldList = getDisplayCols(form_id, office_id);
        }else if("form".equals(re.get("type"))){
        	fieldList = Db.find("select * from eeda_form_field where "
                    + " form_id=? order by if(isnull(seq),1,0), seq", form_id);
        }
        controller.setAttr("form_id", form_id);
        controller.setAttr("field_list", fieldList);
        controller.setAttr("field_list_json", JsonKit.toJson(fieldList));
        return fieldList;
    }
    
    public static List<Record> getDisplayCols (Long form_id, Long office_id){
        List<Record> fieldList = new ArrayList<Record>();
    		//查数据列表
    		List<Record> custom_search_cols_list = Db.find("select * from eeda_form_custom_search_cols where form_id = ?",form_id);
    		for(int j = 0;j<custom_search_cols_list.size();j++){
    			String expression = custom_search_cols_list.get(j).get("expression");
    			int index = expression.indexOf(".");
    			String lie_name = expression.substring(index+1);
    			String biao_name = expression.substring(0,index);
    			Record define_source = Db.findFirst("select * from eeda_form_define where name=? and office_id=?",biao_name, office_id);
    			if("ID".equals(lie_name)){
    			    Record field = new Record();
    			    field.set("field_name","id");
                    field.set("listed","N");
                    field.set("custom_search","N");
                    fieldList.add(field);
    			}else{
    			    String source_form_id=define_source.get("id").toString();
        			Record field = Db.findFirst("select * from eeda_form_field where field_display_name=? and form_id=? ", lie_name, source_form_id);
        			field.set("listed","Y");
        			field.set("custom_search","Y");
        			fieldList.add(field);
    			}
    		}
    		return fieldList;
    }
    
    private List<Record> getFormBtns(Long formId, String type) {
        List<Record> recList = Db.find(
                "select * from eeda_form_btn where form_id=? and type=?", formId, type);
        return recList;
    }
    
    private Record getFormCharts(Long formId) {
        Record rec = Db.findFirst(
                "select * from eeda_form_charts where form_id=?", formId);
        return rec;
    }
    
    
    
    
    private Map<String,Object> queryForm(Long form_id){
    	List<Record> orderList = new ArrayList<Record>();
    	//两种参数写法, 第一种为空就换第二种再试
    	String condition = DbUtils.buildConditions(getParaMap());
    	if(StrKit.isBlank(condition) && StrKit.notBlank(getPara("target_field")) && StrKit.notBlank(getPara("like_str"))){
    	    String fields = getPara("target_field");
    	    String like_str = getPara("like_str");
    	    String[] fieldsArr = fields.split(",");
    	    List<String> list = new ArrayList<String>();
    	    for (int i = 0; i < fieldsArr.length; i++) {
                String fieldName=fieldsArr[i];
                String likeStr = fieldName+" like '%"+like_str+"%'";
                list.add(likeStr);
            }
    	    condition=" and ("+StringUtils.join(list.toArray(), " or ") +")";
    	}
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
        String sql = "";
        Record define = Db.findFirst("select * from eeda_form_define where id = ?",form_id);
        long office_id=define.getLong("office_id");
        if("search_form".equals(define.get("type"))){
            String lieNameStr = getLieNameStr(form_id, office_id);
            String biaoNameStr = "" ;//getBiaoNameStr(form_id, office_id);//join
            String joinStr = getJoinStr(form_id, office_id);//left join
            String filterStr = getFilterStr(form_id);
            sql = "select "+lieNameStr+" from "+biaoNameStr+" "+joinStr+" where 1=1 ";//TODO: and eeda_delete='N' 
            int index = biaoNameStr.indexOf(" ");
            //+ " order by "+biaoNameStr.substring(index)+".id desc " 
            orderList = Db.find(sql+ condition+filterStr +sLimit);
        }else if("form".equals(define.get("type"))){
            sql = "select * from form_"+form_id+" where 1=1 and eeda_delete='N' "; 
            orderList = Db.find(sql+ condition + " order by id desc " +sLimit);
        }

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        setAttr("queryTotal", sqlTotal);
        
        
        Map<String,Object> orderListMap = new HashMap<String,Object>();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", orderList);
        return orderListMap;
    }
    
    public static String getLieNameStr(Long form_id, long office_id){
    	String lieNameStr = "";
    	List<Record> colsList = Db.find("select * from eeda_form_custom_search_cols where form_id = ?",form_id);
    	for(int i = 0;i<colsList.size();i++){
    		String expression = colsList.get(i).get("expression");
    		int index = expression.indexOf(".");
    		String lie_name = expression.substring(index+1);
			String biao_name = expression.substring(0,index);
			String biao_name_py = PingYinUtil.getFirstSpell(expression.substring(0,index));
			Record define = Db.findFirst("select * from eeda_form_define where name = ? and office_id=?",biao_name, office_id);
			Record field = Db.findFirst("select * from eeda_form_field where field_display_name = ? and form_id = ?",lie_name,define.get("id"));
			String str = "";
			if(lieNameStr.length()>0){
				str = ",";
			}
			if("ID".equals(lie_name)){
			    lieNameStr += str+biao_name_py+".id";
			}else{
			    lieNameStr += str+biao_name_py+".f"+field.get("id")+"_"+field.get("field_name");
			}
    	}
    	return lieNameStr;
    }
    
    
    
    private String getBiaoNameStr(Long form_id, long office_id){
    	String biaoNameStr = "";
    	List<Record> sourceList = Db.find("select * from eeda_form_custom_search_source_condition where form_id = ?",form_id);
		String form_name = sourceList.get(0).get("form_left");
		Record re = Db.findFirst("select * from eeda_form_define where name = ? and office_id=?",form_name, office_id);
		biaoNameStr = "form_"+re.get("id")+" "+PingYinUtil.getFirstSpell(form_name);
    	return biaoNameStr;
    }
    
    public static String getJoinStr(Long form_id , long office_id){
        //TODO: 加office ID
        //判断是join 还是 left join
        //将两个表名存入Record中
        Map<String, String> leftJoinMap = new HashMap<String, String>();
        Map<String, String> joinMap = new HashMap<String, String>();
    	String joinStr = "";
    	List<Record> sourceList = Db.find("select * from eeda_form_custom_search_source where form_id = ?",form_id);
    	for(int i = 0;i<sourceList.size();i++){
    	    String form_name1 = sourceList.get(i).get("form_name");
            Record form_define1 = Db.findFirst("select * from eeda_form_define where name=? and office_id=?", form_name1, office_id);
            String form_py1 = PingYinUtil.getFirstSpell(form_name1);
            String connectType1 = sourceList.get(i).get("connect_type"); 
            
            if(i==(sourceList.size()-1)){
                continue;
            }
            String form_name2 = sourceList.get(i+1).get("form_name");
            Record form_define2 = Db.findFirst("select * from eeda_form_define where name=? ",form_name2);
            String form_py2 = PingYinUtil.getFirstSpell(form_name2);
            String connectType2 = sourceList.get(i+1).get("connect_type"); 
            if(StrKit.isBlank(connectType2))
                continue;
            
            if("left_join".equals(connectType2)){
                String key = form_name1+"-"+form_name2;
                String joinTableStr = "form_"+form_define1.getLong("id")+" "+form_py1
                        +" left join "+"form_"+form_define2.getLong("id")+" "+form_py2;
                leftJoinMap.put(key, joinTableStr);
            }else{
                String key = form_name1+"-"+form_name2;
                String joinTableStr = "form_"+form_define1.getLong("id")+" "+form_py1
                        +", "+"form_"+form_define2.getLong("id")+" "+form_py2;
                joinMap.put(key, joinTableStr);
            }
    	}
    	
    	//构造两个连接表的条件
    	Map<String, String> fieldJoinMap = new HashMap<String, String>();
    	List<Record> sourceConditionList = Db.find("select * from eeda_form_custom_search_source_condition where form_id = ?",form_id);
    	for(int i = 0;i<sourceConditionList.size();i++){
    		String form_left = sourceConditionList.get(i).get("form_left");
    		Record form_left_define = Db.findFirst("select * from eeda_form_define where name=? and office_id=?",form_left, office_id);
    		String form_left_py = PingYinUtil.getFirstSpell(form_left);
    		String form_left_field = sourceConditionList.get(i).get("form_left_field");
    		int index_left = form_left_field.indexOf(".");
    		Record field_left = Db.findFirst("select * from eeda_form_field where form_id = ? and field_display_name=?",
    		        form_left_define.get("id"),form_left_field.substring(index_left+1));
    		String lie_name_left = "f"+field_left.get("id")+"_"+field_left.get("field_name");
    		
    		String form_right = sourceConditionList.get(i).get("form_right");
    		Record form_right_define = Db.findFirst("select * from eeda_form_define where name=? and office_id=?", form_right, office_id);
    		String form_right_py = PingYinUtil.getFirstSpell(form_right);
    		String form_right_field = sourceConditionList.get(i).get("form_right_field");
    		int index_right = form_right_field.indexOf(".");
    		Record field_right = Db.findFirst("select * from eeda_form_field where form_id = ? and field_display_name=?",form_right_define.get("id"),form_right_field.substring(index_right+1));
    		String lie_name_right = "f"+field_right.get("id")+"_"+field_right.get("field_name");
    		
    		String operator = sourceConditionList.get(i).get("operator");
    		
    		String key = form_left+"-"+form_right;
    		String value = form_left_py+"."+lie_name_left+"="+form_right_py+"."+lie_name_right;
    		fieldJoinMap.put(key, value);
    	}

        String onStr = "";
        //构造SQL
        for (String key : leftJoinMap.keySet()) {
            String talbeValue = leftJoinMap.get(key);
            String condition = fieldJoinMap.get(key);
            onStr+=talbeValue+" on "+condition;
        }
    	return onStr;
    }

    public static String getFilterStr(Long form_id){
    	String filterStr = "";
    	List<Record> filterList = Db.find("select * from eeda_form_custom_search_filter where form_id = ?",form_id);
    	for(int i = 0;i<filterList.size();i++){
    		String param_name = filterList.get(i).get("param_name");
    		int index = param_name.indexOf(".");
    		String param_name_py = PingYinUtil.getFirstSpell(param_name);
    		String lie_name_py = param_name_py.substring(index);
    		String biao_name_py = param_name_py.substring(0,index);
    		
    		
    		String data_type = filterList.get(i).get("data_type");
    		String must_flag = filterList.get(i).get("must_flag");
    		String default_value = filterList.get(i).get("default_value");
    		
    		Record field = Db.findFirst("select eff.* from eeda_form_field eff"
    				+ " left join eeda_form_define efd on efd.id = eff.form_id"
    				+ " where efd.name = ? and eff.field_name = ? ",param_name.substring(0,index),lie_name_py);
    		
    		filterStr+=" and "+biao_name_py+".f"+field.get("id")+"_"+lie_name_py+" = '"+default_value+"'";
    	}
    	return filterStr;
    }
    
    private void edit(Long form_id, Long order_id, Record formRec) throws IOException{
        Long officeId = formRec.getLong("office_id");
        List<Record> fieldList = Db.find("select * from eeda_form_field where "
                + " form_id=?", form_id);
        
        
        List<Record> eventsList = Db.find("select * from eeda_form_event where form_id=?", form_id);
        formRec.set("events", eventsList);
        
        String form_name = formRec.getStr("name");
        String template_content = formRec.getStr("template_content");
        TemplateService ts = TemplateService.getInstance();
        template_content = ts.processTab(template_content);
        template_content = ts.processCharts(template_content, officeId);

        for (Record fieldRec : fieldList) {
            String fieldDisplayName=fieldRec.getStr("field_display_name");
            String fieldName=fieldRec.getStr("field_name");
            String replaceNameOrigin = "#{"+form_name+"."+fieldDisplayName+"}";
            String fieldType = fieldRec.getStr("field_type");
            String read_only = fieldRec.getStr("read_only");
            String replaceNameDest ="";
            String inputId = "form_"+form_id+"-f"+fieldRec.get("id")+"_"+fieldName.toLowerCase();
            
            String requiredStr = "";
            if("Y".equals(fieldRec.getStr("REQUIRED"))){
                requiredStr = "<span style='float:left;color:red;line-height: 31px;font-size: 16px;margin-left: -10px;'>*</span>";
            }
            String disabled = "";
            if("Y".equals(read_only)){
                disabled = "disabled";
            }
            if("自动编号".equals(fieldType)){
                replaceNameDest = "<label class='search-label'>"+fieldDisplayName+"</label>"
                        + "<div class='formControls col-xs-8 col-sm-8'>"
                        + "  <input type='text' name='"+inputId+"' class='input-text' autocomplete='off'  placeholder='系统自动生成' disabled>"
                        + "</div>"+requiredStr;
            }else if("文本".equals(fieldType)||"网址".equals(fieldType)){
                
                replaceNameDest = "<label class='search-label'>"+fieldDisplayName+"</label>"
                        + "<div class='formControls col-xs-8 col-sm-8'>"
                        + "  <input type='text' name='"+inputId+"' class='input-text "+disabled+"' autocomplete='off' "+disabled+" >"
                        + "</div>"+requiredStr;
            }else if("全国城市".equals(fieldType)){
                replaceNameDest = "<label class='search-label'>"+fieldDisplayName+"</label>"
                        + "<div class='col-xs-8 col-sm-8'>"+
                        "    <input id='"+inputId+"_province' type='text' class='province' field_type='list' value='' style='display:none;'/>"+
                        "    <input id='"+inputId+"' type='text' field_type='list' value='' style='display:none;'/>"+
                        "    <input type='text' class='input-text city_input "+disabled+"'"+
                        "    name='"+inputId+"' autocomplete='new-password'"+
                        "    placeholder='请选择城市' "+disabled+" >"+
                        "    <div id='"+inputId+"_list' class='area-list pull-right dropDown-menu default dropdown-scroll' tabindex='-1'  "+
                        "    style='top: 35%; left: 2%; display: none;'>"+
                        "        <div class='area-list-title'>"+
                        "            <input data-id='0' data-level='0' type='button' value='省份' class='this'>"+
                        "            <input data-id='0' data-level='1' type='button' value='城市'>"+
                        "            <input data-id='0' data-level='2' type='button' value='县区'>"+
                        "            <span class='tips'>如不需选县区，请点击外面空白区域</span>"+
                        "        </div>"+
                        "        <div class='area-list-content' style='clear:both;'>"+
                        "            "+
                        "        </div>"+
                        "    </div>"+
                        "        "+
                        "    <ul id='"+inputId+"_list——1' class='pull-right dropDown-menu default dropdown-scroll' tabindex='-1' style='top: 35%; left: 2%;'>"+
                        "    </ul>"+
                        "</div>"+requiredStr;
            }else if("日期".equals(fieldType)){
                replaceNameDest = "<div id='"+inputId+"_div'>"
                        + "<label class='search-label'>"+fieldDisplayName+"</label>"
                        + " <div class='formControls col-xs-8 col-sm-8'>"
                        + "    <input type='text' onfocus='WdatePicker({dateFmt:\"yyyy-MM-dd\"})' name='"+inputId+"' class='input-text Wdate'>"
                        + " </div> "
                        + "</div> "+requiredStr;
            }else if("日期时间".equals(fieldType)){
                replaceNameDest = "<div id='"+inputId+"_div'>"
                        + "<label class='search-label'>"+fieldDisplayName+"</label>"
                        + " <div class='formControls col-xs-8 col-sm-8'>"
                        + "    <input type='text' onfocus='WdatePicker({dateFmt:\"yyyy-MM-dd HH:mm:ss\"})' name='"+inputId+"' class='input-text Wdate "+disabled+"' "+disabled+">"
                        + " </div> "
                        + "</div> "+requiredStr;
            }else if("多行文本".equals(fieldType)||"网页HTML".equals(fieldType)){
                replaceNameDest = "<div id='"+inputId+"_div'>"
                        + "<label class='search-label'>"+fieldDisplayName+"</label>"
                        + " <div class='formControls col-xs-8 col-sm-8'>"
                        + "    <textarea class='textarea valid "+disabled+"' placeholder='' name='"+inputId+"' "+disabled+"></textarea>"
                        + " </div> "
                        + "</div> "+requiredStr;
            }else if("复选框".equals(fieldType)){
                FormService fs = new FormService(this);
                replaceNameDest = fs.processFieldType_checkbox(form_name, fieldRec, fieldRec.getLong("id"));
                replaceNameDest="<div id='"+form_name+"-"+fieldDisplayName+"_div'>"+replaceNameDest+"</div> "+requiredStr;
            }else if("从表引用".equals(fieldType)){
                FormService fs = new FormService(this);
                replaceNameDest = fs.processFieldType_detail(form_name, fieldRec, fieldRec.getLong("id"));
                replaceNameDest="<div id='"+form_name+"-"+fieldDisplayName+"_div'>"+replaceNameDest+"</div> "+requiredStr;
            }else if("字段引用".equals(fieldType)){
                FormService fs = new FormService(this);
                replaceNameDest = fs.processFieldType_ref(form_name, fieldRec, fieldRec.getLong("id"), officeId);
                replaceNameDest="<div id='"+form_name+"-"+fieldDisplayName+"_div' style='height:0px;'>"+replaceNameDest+"</div> "+requiredStr;
            }else if("按钮".equals(fieldType)){
                FormService fs = new FormService(this);
                replaceNameDest = fs.processFieldType_btn(form_name, fieldRec, fieldRec.getLong("id"));
                replaceNameDest="<div id='"+form_name+"-"+fieldDisplayName+"_div'>"+replaceNameDest+"</div> ";
            }else if("下拉列表".equals(fieldType)){
                FormService fs = new FormService(this);
                replaceNameDest = fs.processFieldType_dropdown(form_name, fieldRec, fieldRec.getLong("id"));
                replaceNameDest="<div id='"+form_name+"-"+fieldDisplayName+"_div'>"+replaceNameDest+"</div> "+requiredStr;
            }else if("附件".equals(fieldType)){
                FormService fs = new FormService(this);
                replaceNameDest = fs.processFieldType_fileUpload(form_name, fieldRec, fieldRec.getLong("id"));
                replaceNameDest="<div id='"+form_name+"-"+fieldDisplayName+"_div'>"+replaceNameDest+"</div> "+requiredStr;
            }else if("图片".equals(fieldType)){
                FormService fs = new FormService(this);
                replaceNameDest = fs.processFieldType_imgUpload(form_name, fieldRec, fieldRec.getLong("id"));
                replaceNameDest="<div id='"+form_name+"-"+fieldDisplayName+"_div'>"+replaceNameDest+"</div> ";
            }else{
                replaceNameDest = "<label class='search-label'>"+fieldDisplayName+"</label>"
                        + "<input type='text' name='"+inputId+"' class='form-control'>";
            }
            template_content = template_content.replace(replaceNameOrigin, replaceNameDest);
        }
        
        //不需要在这里替换值, 应该是把UI组件+id 显示出来, 再通过JSON set value进去
//        GroupTemplate gt = getGroupTemplate();
//        Template t = gt.getTemplate(template_content);
//        t.binding("t_"+form_id, orderRec); 
//        String form_content = t.render(); 
        
        formRec.set("fields", fieldList);
        formRec.set("template_content", "");
        setAttr("form_define", JsonKit.toJson(formRec));
        setAttr("order_id", order_id);
        setAttr("form_content", template_content);
        setAttr("btnList", getFormBtns(form_id, "edit"));
        setAttr("charts", getFormCharts(form_id));
    }
    
    public void uploadImg(){
        String order_id = getPara("order_id");
        String img_url = getPara("img_url");
        
        UploadFile file = getFile();
        String fileName = file.getFileName();
        
        UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
        long office_id = user.getLong("office_id");
        
        Date d=new Date();
        SimpleDateFormat sf=new SimpleDateFormat("yyyyMMdd");
        String dateStr = sf.format(d);
        //不加d_就不能创建目录，见鬼了。。。。
        String webroot = FileUtil.getWebRoot().getPath();
        String targetDir = webroot+"/upload/"+office_id+"/pic_"+dateStr;
        FileUtil.mkdir(targetDir);
        FileUtil.copy(webroot+"/upload/"+fileName, targetDir, true);
        FileUtil.del(webroot+"/upload/"+fileName);
        
        Record re = new Record();
        re.set("file_name", fileName);
        re.set("file_url", "/upload/"+office_id+"/pic_"+dateStr+"/"+fileName);
        renderJson(re);
    }
    
    public void uploadFile(){
        UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
        long office_id = user.getLong("office_id");
        
        Date d=new Date();
        SimpleDateFormat sf=new SimpleDateFormat("yyyyMMdd");
        String dateStr = sf.format(d);
        
        File file = getFile().getFile();
        String webroot = FileUtil.getWebRoot().getPath();
        
        String fileName = file.getName();
        //不加doc_ 就不能创建目录，见鬼了。。。。
        String targetDir = webroot+"/upload/"+office_id+"/doc_"+dateStr;
        FileUtil.mkdir(targetDir);
        FileUtil.copy(webroot+"/upload/"+fileName, targetDir, true);
        FileUtil.del(file);
        
        Record re = new Record();
        re.set("file_name", fileName);
        re.set("file_url", "/upload/"+office_id+"/doc_"+dateStr+"/"+fileName);
        re.set("result", true);
        renderJson(re);
    }
}
