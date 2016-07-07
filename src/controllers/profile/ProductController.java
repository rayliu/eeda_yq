package controllers.profile;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.Category;
import models.ParentOfficeModel;
import models.Party;
import models.Product;
import models.yh.profile.Contact;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.util.ParentOffice;
import controllers.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ProductController extends Controller {

    private Logger logger = Logger.getLogger(ProductController.class);
    Subject currentUser = SecurityUtils.getSubject();

    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);

    @RequiresPermissions(value = { PermissionConstant.PERMSSION_PT_LIST })
    public void index() {
        render("/eeda/profile/product/productList.html");
    }

    @RequiresPermissions(value = { PermissionConstant.PERMSSION_PT_LIST })
    public void list() {
        String searchStr = getPara("sSearch");// 查询文本框
        String sLimit = "";
        Map productListMap = null;
        String categoryId = getPara("categoryId");
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null
                && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
                    + getPara("iDisplayLength");
        }
        String category = getPara("category");
        String sql = "";
        String sqlTotal = "";
        String searchConditions = "";
        if (StringUtils.isNotEmpty(searchStr)) {
            searchConditions = " and (item_name like '%" + searchStr + "%'"
                    + " or item_no like '%" + searchStr + "%'"
                    + " or serial_no like '%" + searchStr + "%'"
                    + " or item_desc like '%" + searchStr + "%')";
        }
        if (categoryId == null || "".equals(categoryId)) {
            sqlTotal = "select count(1) total from product";
            sql = "select *,(select name from category  where id = "
                    + categoryId
                    + ") category_name from product order by id desc" + sLimit;
        } else {
            sqlTotal = "select count(1) total from product where category_id = "
                    + categoryId;
            sql = "select *,(select name from category where id = "
                    + categoryId + ") category_name from product "
                    + "where category_id = " + categoryId + searchConditions
                    + " order by id desc " + sLimit;
        }
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> products = Db.find(sql);
        productListMap = new HashMap();
        productListMap.put("sEcho", pageIndex);
        productListMap.put("iTotalRecords", rec.getLong("total"));
        productListMap.put("iTotalDisplayRecords", rec.getLong("total"));
        productListMap.put("aaData", products);
        renderJson(productListMap);
    }

    public void add() {
        setAttr("saveOK", false);
        render("/eeda/profile/product/productEdit.html");
    }

    public void edit() {
        long id = getParaToLong();

        Product product = Product.dao.findById(id);
        setAttr("product", product);
        render("/eeda/profile/product/productEdit.html");
    }

    public void delete() {
        Product product = Product.dao.findById(getPara("productId"));
        /*
         * product.set("category_id", null); product.delete();
         */
        Object obj = product.get("is_stop");
        if (obj == null || "".equals(obj) || obj.equals(false) || obj.equals(0)) {
            product.set("is_stop", true);
        } else {
            product.set("is_stop", false);
        }
        product.update();
        renderJson("{\"success\":true}");
    }

    public void update() {
        Product product = Product.dao
                .findFirst(
                        "select p.*,c.name cname from product p left join category c on c.id = p.category_id where p.id = ?",
                        getPara("productId"));
        renderJson(product);
    }

    public void getCategory() {
        Category category = Category.dao.findFirst(
                "select c.name cname from category c  where c.id = ?",
                getPara("categoryId"));
        renderJson(category);
    }

    @Before(Tx.class)
    public void save() {
        Product product = null;
        String id = getPara("id");

        if (id != null && !id.equals("")) {
            product = Product.dao.findById(id);
            String size = getPara("size");
            String width = getPara("width");
            String volume = getPara("volume");
            String weight = getPara("weight");
            String height = getPara("height");
            product.set("item_name", getPara("item_name"))
                    .set("serial_no", getPara("serial_no"))
                    .set("item_no", getPara("item_no"))
                    .set("item_desc", getPara("item_desc"))
                    .set("unit", getPara("unit"))
                    .set("category_id", getPara("categoryId"));
            if (size != null && !"".equals(size)) {
                product.set("size", size);
            }
            if (width != null && !"".equals(weight)) {
                product.set("width", width);
            }
            if (volume != null && !"".equals(volume)) {
                product.set("volume", volume);
            }
            if (weight != null && !"".equals(weight)) {
                product.set("weight", weight);
            }
            if (height != null && !"".equals(height)) {
                product.set("height", height);
            }
            product.update();
        } else {
            product = new Product();
            String itemName = getPara("item_name");
            String itemNo = getPara("item_no");
            String itemDesc = getPara("item_desc");
            String size = getPara("size");
            String width = getPara("width");
            String unit = getPara("unit");
            String volume = getPara("volume");
            String weight = getPara("weight");
            String height = getPara("height");

            product.set("item_name", itemName)
                    .set("serial_no", getPara("serial_no"))
                    .set("item_no", itemNo).set("item_desc", itemDesc)
                    .set("unit", getPara("unit"))
                    .set("category_id", getPara("categoryId"));
            if (size != null && !"".equals(size)) {
                product.set("size", size);
            }
            if (width != null && !"".equals(weight)) {
                product.set("width", width);
            }
            if (volume != null && !"".equals(volume)) {
                product.set("volume", volume);
            }
            if (weight != null && !"".equals(weight)) {
                product.set("weight", weight);
            }
            if (height != null && !"".equals(height)) {
                product.set("height", height);
            }
            product.save();
        }
        renderJson(product);
    }

    // 查出客户的根类别
    public void searchCustomerCategory() {
        String customerId = getPara("customerId");

        Category category = Category.dao
                .findFirst(
                        "select * from category where customer_id =? and parent_id is null",
                        customerId);

        if (category == null) {
            category = new Category();
            category.set("name", "root");
            category.set("customer_id", customerId);
            category.save();
        }

        renderJson(category);
    }

    // 查出客户的根类别
    public void searchCustomerCategory2() {
        // String customerId = getPara("customerId");

        List<Category> categories = new ArrayList<Category>();
        List<Product> list = Product.dao.find("select * from product");
        for (Product product : list) {
            Category category = Category.dao
                    .findFirst(
                            "select * from category where customer_id =? and parent_id is null",
                            product.get("customer_id"));
            if (category == null) {
                category = new Category();
                category.set("name", "root");
                category.set("customer_id", product.get("customer_id"));
                category.save();
            }
            categories.add(category);
        }
        renderJson(categories);
    }

    // 查出当前节点的子节点, 如果当前节点没有categoryId, 它就是公司的根节点（新建一个）
    public void searchNodeCategory() {
        Long categoryId = getParaToLong("categoryId");
        Long customerId = getParaToLong("customerId");

        logger.debug("categoryId=" + categoryId + ", customerId=" + customerId);
        List<Category> categories = Category.dao.find(
                "select * from category where parent_id=? and customer_id =?",
                categoryId, customerId);

        renderJson(categories);

    }

    // 查找产品对象
    public void getProduct() {
        Product product = Product.dao
                .findFirst(
                        "select p.*,c.name cname from product p left join category c on c.id = p.category_id where p.id = ?",
                        getPara("productId"));
        renderJson(product);
    }

    // 保存类别
    @Before(Tx.class)
    public void saveCategory() {
        String categoryId = getPara("categoryId");
        Category category = Category.dao.findById(categoryId);
        /*
         * String categoryName = ""; List<Category> categories =
         * Category.dao.find
         * ("select * from category where parent_id = ? and id != ?",
         * category.get("parent_id"), categoryId); for(Category c : categories){
         * categoryName += c.get("name"); } if(!categoryName.contains(name)){
         * category.set("name", getPara("name")); category.set("customer_id",
         * getPara("customerId")); category.update(); }
         */
        category.set("name", getPara("categoryName"));
        category.set("customer_id", getPara("customerId"));
        category.update();
        renderJson(category);
    }

    // 新增类别
    @RequiresPermissions(value = { PermissionConstant.PERMSSION_PT_CREATE })
    @Before(Tx.class)
    public void addCategory() {
        String parentId = getPara("categoryId");
        Category category = null;
        if (parentId != null) {
            category = new Category();
            Category c = Category.dao
                    .findFirst(
                            "select * from category where name like '%新类别%' and parent_id = ? order by name desc limit 0,1",
                            parentId);
            if (c != null) {
                String categoryName = c.get("name");
                Pattern pattern = Pattern.compile("\\d+");
                Matcher matcher = pattern.matcher(categoryName);
                Integer integer = 1;
                if (matcher.find()) {
                    integer = Integer.parseInt(matcher.group(0)) + 1;
                }
                category.set("name", "新类别" + integer);
            } else {
                category.set("name", "新类别1");
            }
            category.set("customer_id", getPara("customerId"));
            category.set("parent_id", parentId);
            category.save();
        }
        renderJson(category);
    }

    // 删除类别
    @RequiresPermissions(value = { PermissionConstant.PERMSSION_PT_DELETE })
    @Before(Tx.class)
    public void deleteCategory() {
        boolean flag = true;
        String cid = getPara("categoryId");
        // removeChildern(cid);
        List<Product> products = Product.dao.find(
                "select * from product where category_id = ?", cid);
        try {
            for (Product product : products) {
                product.delete();
            }
            Category.dao.deleteById(cid);
        } catch (RuntimeException e) {
            flag = false;
        }
        if (flag) {
            renderJson("{\"success\":true}");
        } else {
            renderJson("{\"success\":false}");
        }
    }

    // 删除子类别
    @RequiresPermissions(value = { PermissionConstant.PERMSSION_PT_DELETE })
    @Before(Tx.class)
    private void removeChildern(String cid) {
        List<Category> categories = Category.dao.find(
                "select * from category where parent_id = ?", cid);
        if (categories.size() > 0) {
            for (Category c : categories) {
                removeChildern(c.get("id").toString());
                List<Product> products = Product.dao.find(
                        "select * from product where category_id = ?",
                        c.get("id"));
                for (Product product : products) {
                    product.delete();
                }
                c.delete();
            }
        } else {
            List<Product> products = Product.dao.find(
                    "select * from product where category_id = ?", cid);
            for (Product product : products) {
                product.delete();
            }
            Category.dao.deleteById(cid);
        }
    }

    // 查找类别
    public void searchCategory() {
        Category category = Category.dao.findById(getPara("categoryId"));
        renderJson(category);
    }

    // 查找类别
    public void findCategory() {
        Category category = Category.dao.findById(getPara("categoryId"));
        renderJson(category);
    }

    // 查找客户
    public void searchAllCustomer() {
        Long parentID = pom.getParentOfficeId();

        List<Party> parties = Party.dao
                .find("select p.id pid, p.*, cat.id cat_id from party p "
                        + " left join category cat on p.id = cat.customer_id "
                        + " left join office o on p.office_id = o.id  "
                        + " where p.type = ? and cat.parent_id is null and (o.id = ? or o.belong_office = ? )",
                        Party.PARTY_TYPE_CUSTOMER, parentID, parentID);
        createRootForParty(parties);

        List<Party> rootParties = Party.dao
                .find("select p.id pid, p.*, cat.id cat_id from party p "
                        + "left join category cat on p.id = cat.customer_id "
                        + "left join office o on p.office_id = o.id "
                        + "where p.type = ? and cat.parent_id is null and (o.id = ? or o.belong_office = ? ) ",
                        Party.PARTY_TYPE_CUSTOMER, parentID, parentID);
        renderJson(rootParties);
    }

    private void createRootForParty(List<Party> parties) {
        for (Party party : parties) {
            Long customerId = party.getLong("id");
            logger.debug("customerId="+customerId);
            List<Category> categories = Category.dao.find(
                    "select * from category where customer_id = ?", customerId);
            if (categories.size() == 0) {
                Category category = new Category();
                Party customerParty = Party.dao
                        .findFirst("select p.* from party p where p.id="
                                + customerId);
                category.set("name", customerParty.get("company_name"));
                category.set("customer_id", customerId);
                category.save();
            }
        }
    }

    // 添加一行新数据
    public void productSave() {
        String category_id = getPara("category_UpdateId");
        String itemId = getPara("itemId");
        String item_no = getPara("item_no_update");
        String serial_no = getPara("serial_no_update");
        String item_name = getPara("item_name_update");
        String size = getPara("size_update") == "" ? null
                : getPara("size_update");
        String width = getPara("width_update") == "" ? null
                : getPara("width_update");
        ;
        String height = getPara("height_update") == "" ? null
                : getPara("height_update");
        ;
        String insurance_amount = getPara("insurance_amount_update") == "" ? null
                : getPara("insurance_amount_update");
        ;
        String unit = getPara("unit_update");
        String volume = getPara("volume_update") == "" ? null
                : getPara("volume_update");
        ;
        String weight = getPara("weight_update") == "" ? null
                : getPara("weight_update");
        ;
        String item_desc = getPara("item_desc_update");
        String item_no_hidden = getPara("item_no_hidden");// 隐藏属性用于判断编辑是否改了产品型号
        String status = "ok";
        Product p = null;
        if (!"".equals(itemId)) {
            if (!item_no_hidden.equals(item_no)) {
                Record rec = Db.findFirst(
                        "select c.customer_id from category c where c.id =?",
                        category_id);
                String findSame = "select c.customer_id, p.* from product p, category c where p.category_id=c.id and item_no =? and c.customer_id=?";
                Record sameRec = Db.findFirst(findSame, item_no.trim(),
                        rec.get("customer_id"));
                if (sameRec != null) {
                    status = "item_no";
                }
            }
            if (!"item_no".equals(status)) {
                p = Product.dao.findById(itemId);
                p.set("category_id", category_id).set("size", size)
                        .set("width", width).set("height", height)
                        .set("volume", volume).set("weight", weight)
                        .set("serial_no", serial_no)
                        .set("item_name", item_name).set("item_no", item_no)
                        .set("insurance_amount", insurance_amount)
                        .set("unit", unit).set("item_desc", item_desc).update();
            }
        } else {
            Record rec = Db.findFirst(
                    "select c.customer_id from category c where c.id =?",
                    category_id);
            String findSame = "select c.customer_id, p.* from product p, category c where p.category_id=c.id and item_no =? and c.customer_id=?";
            Record sameRec = Db.findFirst(findSame, item_no.trim(),
                    rec.get("customer_id"));
            if (sameRec != null) {
                status = "item_no";
            } else {
                p = new Product();
                p.set("category_id", category_id).set("size", size)
                        .set("width", width).set("height", height)
                        .set("volume", volume).set("weight", weight)
                        .set("serial_no", serial_no)
                        .set("item_name", item_name).set("item_no", item_no)
                        .set("unit", unit).set("item_desc", item_desc)
                        .set("insurance_amount", insurance_amount).save();
            }
        }
        renderText(status);
    }

    // 保存产品
    @RequiresPermissions(value = { PermissionConstant.PERMSSION_PT_UPDATE })
    public void updateProductById() {
        String id = getPara("id");
        String filedName = getPara("fieldName");
        String value = getPara("value");
        Product product = null;

        if (id != null && !id.equals("")) {
            product = Product.dao.findById(id);

            if ("item_no".equals(filedName)) {
                Record rec = Db.findFirst(
                        "select c.customer_id, p.* from product p, category c "
                                + "where p.category_id=c.id and p.id=?", id);

                String findSame = "select c.customer_id, p.* from product p, category c "
                        + "where p.category_id=c.id and item_no =? and c.customer_id=?";
                Record sameRec = Db.findFirst(findSame, value.trim(),
                        rec.get("customer_id"));
                if (sameRec != null) {
                    renderJson(new Product());
                    return;
                }
            }
            product.set(filedName, value).update();
            renderJson(product);
        }

    }

    public void saveProductByField() {
        String returnValue = "";
        String id = getPara("id");
        Product item = Product.dao.findById(id);
        String item_no = getPara("item_no");
        String serial_no = getPara("serial_no");
        String item_name = getPara("item_name");
        String unit = getPara("unit");
        String itemDesc = getPara("item_desc");
        String size = getPara("size");
        String width = getPara("width");
        String height = getPara("height");
        String weight = getPara("weight");
        String insuranceAmount = getPara("insurance_amount");
        if (!"".equals(item_no) && item_no != null) {
            Category category = Category.dao.findById(item.get("category_id"));
            Product product = Product.dao
                    .findFirst("select * from product p left join category c on c.id = p.category_id where p.item_no = '"
                            + item_no
                            + "' and c.customer_id = "
                            + category.get("customer_id"));
            if (product == null) {
                item.set("item_no", item_no).update();
                returnValue = item_no;
            } else {
                // 产品型号已存在
                returnValue = "repetition";
            }
        } else if (!"".equals(item_name) && item_name != null) {
            item.set("item_name", item_name).update();
            returnValue = item_name;
        } else if (!"".equals(itemDesc) && itemDesc != null) {
            item.set("item_desc", itemDesc).update();
            returnValue = itemDesc;
        } else if (!"".equals(size) && size != null) {
            item.set("size", size).update();
            returnValue = size;
        } else if (!"".equals(width) && width != null) {
            item.set("width", width).update();
            returnValue = width;
        } else if (!"".equals(height) && height != null) {
            item.set("height", height).update();
            returnValue = height;
        } else if (!"".equals(weight) && weight != null) {
            item.set("weight", weight).update();
            returnValue = weight;
        } else if (!"".equals(unit) && unit != null) {
            item.set("unit", unit).update();
            returnValue = unit;
        } else if (!"".equals(serial_no) && serial_no != null) {
            item.set("serial_no", serial_no).update();
            returnValue = serial_no;
        } else if (!"".equals(insuranceAmount) && insuranceAmount != null) {
            item.set("insurance_amount", insuranceAmount).update();
            returnValue = insuranceAmount;
        }
        if (item.get("size") != null && item.get("width") != null
                && item.get("height") != null) {
            Double volume = Double.parseDouble(item.get("size") + "") / 1000
                    * Double.parseDouble(item.get("width") + "") / 1000
                    * Double.parseDouble(item.get("height") + "") / 1000;
            volume = Double.parseDouble(String.format("%.2f", volume));
            item.set("volume", volume).update();
        }
        renderText(returnValue);// 必须返回传进来的值，否则js会报错
    }

    // 校验类别是否已存在
    public void checkCategory() {
        String id = getPara("id");
        String name = getPara("name");
        Category category = Category.dao.findById(id);
        List<Category> categories = Category.dao.find(
                "select * from category where parent_id = ? and id != ?",
                category.get("parent_id"), id);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", name);
        map.put("categories", categories);
        renderJson(map);
    }

    public void searchAllUnit() {
        List<Record> offices = Db.find("select * from unit");
        renderJson(offices);
    }
}
