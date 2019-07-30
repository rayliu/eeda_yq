package controllers.form;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.map.HashedMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Record;

public class TemplateService {

    public static TemplateService getInstance() {
        return new TemplateService();
    }

    // 举例：#{柱状图}, 必须是一个表的名字，不能是字段（含.）
    // 结果：转换成<div form_name='form_69' class='eeda_chart_container'></div>
    // 即 eeda_chart_container是容器，在JS中替换输出成真正的图表
    public String processCharts(String content, Long office_id) {
        Pattern pattern = Pattern.compile("(?<=#\\{)[^\\}]+");// 匹配花括号
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String formName = matcher.group(0);

            if (formName.indexOf(".") == -1) {
                Record formRec = FormUtil.getFormOrField(formName, office_id);
                if (formRec != null) {
                    Long formId = formRec.getLong("id");
                    String html = "<div form_name='form_" + formId
                            + "' class='eeda_chart_container' style='width: 350px;height:250px;'></div>";
                    String replaceFormName = "#{" + formName + "}";
                    content = content.replace(replaceFormName, html);
                }
            }
        }
        return content;
    }

    public Map processTab(String content) {
        Map docMap = new HashedMap<String, String>();
        String newContent = content;

        Document doc = Jsoup.parse(content);
        // table with tab
        Elements tables = doc.select("table:contains(#{选项卡-)");
        if (tables.size() > 0) {
            String tabString = "<div class='HuiTab'>" + "<div class='tabBar clearfix' style='margin-bottom: 5px;'>";
            Element parent = null;
            for (Element table : tables) {
                parent = table.parent();
                Element row1 = table.select("tr:eq(0)").first();
                Elements row1_tds = row1.select("td");
                for (Element td : row1_tds) {
                    if (StrKit.isBlank(td.text()))
                        continue;
                    int tdindex = td.text().indexOf("#{选项卡-");
                    if(td.text().indexOf("#{选项卡-")!=-1) {
                    	String tabName = td.text().split("-")[1].replace("}", "");
                    	tabString += "<span>" + tabName + "</span>";
                    }
                }
                tabString += "</div>";

                Element row2 = table.select("tr:eq(1)").first();
                if(row2 != null) {
	                Elements row2_tds = row2.select("td");
	                for (Element td : row2_tds) {
                		String tabContent = td.text();
                    	tabString += "<div class='tabCon'>" + tabContent + "</div>";
	                }
                }
                table.remove();
            }
            tabString += "</div>";

            parent.append(tabString);
        }
        docMap.put("body", doc.body().html());
        Element jsEl = doc.head().selectFirst("script");
        if (jsEl != null) {
            docMap.put("head", jsEl.data());
        }
        return docMap;
    }
}
