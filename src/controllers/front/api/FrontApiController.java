package controllers.front.api;

import bsh.EvalError;
import bsh.Interpreter;
import cn.hutool.core.io.file.FileWriter;

import com.jfinal.core.Controller;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
//import org.apache.ibatis.jdbc.ScriptRunner;
//import org.apache.ibatis.io.Resources;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;

public class FrontApiController extends Controller {

    /**
     * url 对应执行本方法
     */
    public void index() {
        LogKit.debug("enter FrontApiController index()");
        
        execute();

        //调试完成后, 将debug方法内容更新到数据库并注释掉本方法!
        //debug();
    }

    private void execute() {
        //动态执行java代码
        String originUrl = getAttr("originUrl");
        Record rec = Db.findFirst("select * from t_eeda_api where url=? and is_delete='N'", originUrl);
        //待计算的表达式，将计算结果赋值给outValue
        String evalString = rec.getStr("codes");
        String codeType = rec.getStr("code_type");
        switch (codeType){
            case "java":
                runJava(evalString);
                break;
            case "sql":
                runSql(evalString);
                break;
        }
    }

    private void runJava(String evalString) {
        //调用beanshell进行计算
        Interpreter interpreter = new Interpreter();
        try {
            
            interpreter.setStrictJava(true);//严格使用java代码格式
            //设置输入参数：
            interpreter.set("Db", Db.use());
            interpreter.set("cl", this);
            interpreter.set("ctrl", this);
            interpreter.eval(evalString);
        } catch (EvalError evalError) {
            evalError.printStackTrace();
        }
    }

    private void runSql(String evalString) {
        /*
        try {
            // 建立连接
            Connection conn = Db.use().getConfig().getConnection();
            ScriptRunner  runner = new ScriptRunner(conn);
            runner.setSendFullScript(true);
//            runner.setErrorLogWriter(null);
//            runner.setLogWriter(null);
            //生成SQL脚本
            String templateFolder = PropKit.get("ui_folder");
            String filePath = templateFolder+"/tmp.sql";
            File file = new File(filePath);

            FileWriter writer = new FileWriter(file);
            writer.write(evalString);
            // 执行SQL脚本
            Reader fr = new FileReader(file);
            runner.runScript(fr);
            renderText("sql excuted done. ");
           // updatedCount = Db.update(evalString);
        } catch (Exception e) {
            e.printStackTrace();
            renderText("sql execute error!");
        }
        */
    }

    /*
        url: http://localhost:8080/webadmin/msg1/list
        将需要调试的java代码放到本方法
     */
    private void debug() {
        Controller ctrl = this;
        LogKit.fatal("debug only, 当出现这句话表明没有注释本方法!!!!!-----------------------");

        //正式代码
        String wwwRoot = PropKit.get("user_template_root");
        ctrl.setAttr("getListUrl", "/webadmin/msg1/edit");
        LogKit.warn(String.valueOf(ctrl.getAttr("getListUrl")));
        ctrl.render(wwwRoot+"/template/layui/backend/msg/edit.html");
    }


}
