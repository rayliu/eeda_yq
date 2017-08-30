package controllers.util;

import com.jfinal.core.Controller;

public class ParaBuilder {
    
    private String strLimit = "";
    private String strSortColName = "";
    private String strSortType = "";
    private String strPageIndex = "";
    
    public ParaBuilder(Controller c){
        strSortType = c.getPara("order[0][dir]")==null?"desc":c.getPara("order[0][dir]");
        String sColumn =  c.getPara("order[0][column]");
        strSortColName =  c.getPara("columns["+sColumn+"][data]")==null?"create_stamp":c.getPara("columns["+sColumn+"][data]") ;
        System.out.println("strSortColName:"+strSortColName);
        if("0".equals(strSortColName)){
            strSortColName = "create_stamp";
            strSortType ="desc";
        }
        
        strPageIndex = c.getPara("draw");
        if (c.getPara("start") != null && c.getPara("length") != null) {
            strLimit = " LIMIT " + c.getPara("start") + ", " + c.getPara("length");
        }
    }
    
    public String getSortColName(){
        return this.strSortColName;
    }
    
    public String getSortType(){
        return this.strSortType;
    }
    
    public String getLimit(){
        return this.strLimit;
    }
    
    public String getPageIndex(){
        return this.strPageIndex;
    }
}
