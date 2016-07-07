package controllers.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompareStrList {
    public List<Object> compare(List<Object> tList,String[] strs){
    	//作为返回的List
        List<Object> list = new ArrayList<Object>();
        //作为保存字符串和数组相同的元素
        List<Object> temp = new ArrayList<Object>();
        
        List<String> strList = new ArrayList(Arrays.asList(strs));
        for (Object object : tList) {            
            for (Object obj : strList) {
                //判断相等
                if(object.equals(obj)){
                    temp.add(object);                    
                }
            }
        }
       //移除相同的元素
        for (Object object : temp) {
            tList.remove(object);
            strList.remove(object);
        }
        //要删除的元素
        list.add(0,tList);
        //要增加的元素
        list.add(1,strList);
    
        
        return list;
    }

}
