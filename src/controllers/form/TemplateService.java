package controllers.form;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jfinal.kit.StrKit;

public class TemplateService {
    
    public static TemplateService getInstance(){
        return new TemplateService();
    }
    
    public String processTab(String content){
        String newContent = content;
        
        Document doc = Jsoup.parse(content);
        // table with tab
        Elements tables = doc.select("table:contains(#{选项卡-)");
        if(tables.size()>0){
            String tabString = "<div class='HuiTab'>"
                                + "<div class='tabBar clearfix' style='margin-bottom: 5px;'>";
            Element parent = null;
            for (Element table : tables) {
                parent = table.parent();
                Element row1 = table.select("tr:eq(0)").first();
                Elements row1_tds = row1.select("td");
                for(Element td : row1_tds){
                    if(StrKit.isBlank(td.text()))
                        continue;
                    String tabName = td.text().split("-")[1].replace("}", "");
                    tabString +="<span>"+tabName+"</span>";
                }
                tabString +="</div>";
                
                Element row2 = table.select("tr:eq(1)").first();
                Elements row2_tds = row2.select("td");
                for(Element td : row2_tds){
                    String tabContent = td.text();
                    tabString +="<div class='tabCon'>"+tabContent+"</div>";
                }
                
                table.remove();
              }
            tabString += "</div>";
            
            parent.append(tabString);
        }
        newContent = doc.body().toString();
        return newContent;
    }
}
