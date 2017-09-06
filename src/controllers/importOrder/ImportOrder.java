package controllers.importOrder;

import interceptor.SetAttrLoginUserInterceptor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.UserLogin;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;

import controllers.profile.LoginUserController;
import controllers.profile.TradeItemController;
import controllers.tr.joborder.TrJobOrderController;
import controllers.util.ReaderXLS;
import controllers.util.ReaderXlSX;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ImportOrder extends Controller {

	private Logger logger = Logger.getLogger(ImportOrder.class);
	Subject currentUser = SecurityUtils.getSubject();


	public void index() {
		String order_type = getPara("order_type");
		String order_id = getPara("order_id");
		
		UploadFile uploadFile = getFile();
		File file = uploadFile.getFile();
		String fileName = file.getName();

		UserLogin user = LoginUserController.getLoginUser(this);
        Long userId = user.getLong("id");
        Long officeId = user.getLong("office_id");
		Record resultMap = new Record();
		try {
			String[] title = null;
			List<Map<String, String>> content = new ArrayList<Map<String, String>>();
			//exel格式区分
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
			
			//导入模板表头（标题）校验
			if (title != null && content.size() > 0) {
				CheckOrder checkOrder = new CheckOrder();
				//if (checkOrder.checkoutExeclTitle(title, order_type)) {
				if (true) {
					if("transJobOrder".equals(order_type)){
						// 内容校验
						//resultMap = checkOrder.importTJCheck(content);
						// 内容开始导入
						if(true){
							resultMap = checkOrder.importTJValue(content, userId, officeId);
						}
					}else if("tradeJobOrder".equals(order_type)){
						// 内容校验
						//resultMap = checkOrder.importTJCheck(content);
						// 内容开始导入
						if(true){
							TrJobOrderController tradeJobOrder = new TrJobOrderController();
							resultMap = tradeJobOrder.importTJValue(content, order_id, officeId);
						}
					}else if("trade_item".equals(order_type)){
						// 内容校验
						//resultMap = checkOrder.importTJCheck(content);
						// 内容开始导入
						if(true){
							TradeItemController tradeItem = new TradeItemController();
							resultMap = tradeItem.importValue(content, order_id, officeId);
						}
					}
				} else {
					resultMap.set("result", false);
					resultMap.set("cause", "导入失败，excel标题列与模板excel标题列不一致");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.set("result", false);
			resultMap.set("cause","导入失败，请检测excel内容是否填写规范（尽量避免函数求值列内容）<br/>（建议使用Microsoft Office Excel软件操作数据）");
		}
		logger.debug("result:" + resultMap.get("result") + ",cause:"
				+ resultMap.get("cause"));

		renderJson(resultMap);
	}

}
