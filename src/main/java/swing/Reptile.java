package swing;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.DateUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import swing.ExcelUtil.ExceDate;
import swing.ExcelUtil.ImportExcelUtil;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;
import us.codecraft.webmagic.selector.Selector;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static org.apache.http.client.utils.DateUtils.formatDate;
import static swing.SwingHaha.*;

/**
 * 开发公司：山东海豚数据技术有限公司
 * 版权：山东海豚数据技术有限公司
 * <p>
 * Reptile
 *
 * @author zzr
 * @created Create Time: 2019/11/23
 */
public class Reptile implements PageProcessor {

    public static List<ExceDate> needList;

    public static Map<String,Object> kfkcMap;

    //设置一个全局的参数存放登陆cookies
    private static Set<Cookie> cookies;

    // 部分一：抓取网站的相关配置，包括编码、重试次数等、抓取间隔
    private Site site = Site.me().setRetryTimes(3).setSleepTime(sleepTime);

    @Override
    public Site getSite() {
//        for (Cookie cookie : cookies) {
//            site.addCookie(cookie.getName().toString(), cookie.getValue().toString());
//        }
        site.addCookie("JSESSIONID", "3E89CDC1E29D96EF225C36B2396D2C4E");
        return site;
    }

    @Override
    // process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
    public void process(Page page) {

        String msg = page.getHtml().xpath(needRules).toString();
        String endMsg = "";
        if (StringUtils.isNotBlank(msg)) {
            String strOne = StringUtils.substringBefore(msg,"&");
            String strEnd = StringUtils.substringAfter(strOne,"id=");
            endMsg = strEnd;
        }

        // 部分二：定义如何抽取页面信息，并保存下来
        String hpbmStr = StringUtils.substringAfter(page.getUrl().toString(),"q=");

        ExceDate exceDate = new ExceDate();
        exceDate.setHpbh(hpbmStr);
        exceDate.setTmlj(tmallLink + endMsg);
        exceDate.setTmljid(endMsg);
        exceDate.setKfkc(kfkcMap.get(hpbmStr)+"");

        if (StringUtils.isEmpty(endMsg)) {//天猫没有获取到
            Map<String,Object> map = new HashMap<>();
            map.put("JSESSIONID","3E89CDC1E29D96EF225C36B2396D2C4E");
            page.addTargetRequests(page.getHtml().links().regex("https://s.taobao.com/search?tab=old&q=9787040330755").all());
            String msgTaobao = page.getHtml().xpath("//div[@class='pic']/a/tidyText()").toString();
            System.out.println("msgTaobao="+msgTaobao);
        }

        needList.add(exceDate);
        System.out.println("货品编码为" + hpbmStr + "的产品，天猫ID为："+ endMsg);


    }

//    @Test
//    public void test(){
//        try {
//            Login();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


//    public static void Login() throws IOException {
//        System.setProperty("webdriver.chrome.driver",
//                "C:\\Users\\86136\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe");
//
//        WebDriver driver = new ChromeDriver();
//        driver.manage().window().maximize();
//        driver.get("https://login.taobao.com/member/login.jhtml");
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        driver.findElement(By.xpath("//div[@class='field username-field']/span/input")).sendKeys("13687681257");
//        driver.findElement(By.xpath("//div[@class='field pwd-field']/span/input")).sendKeys("Zuorong.118");
//        //获取点击按钮
//        WebElement element = driver.findElement(By.xpath("//button[@class='submit']"));
//
//        //模拟点击
//        element.click();
//        //很重要的一步获取登陆后的cookies
//        cookies = driver.manage().getCookies();
//        driver.close();
//    }

    public static void changeStart(String inFilePath,String outFilePath) throws FileNotFoundException {
        String fileName = StringUtils.substringAfterLast(inFilePath,"\\");
        String onlyName = StringUtils.substringBeforeLast(fileName,".");
        String endName = StringUtils.substringAfterLast(fileName,".");
        FileInputStream fis = new FileInputStream(inFilePath);
        Map<String,Object> map = new HashMap<>();
        map.put("商家编码","spbh");
        map.put("货品编号","hpbh");
        map.put("可发库存","kfkc");
        map.put("天猫链接","tmlj");

        List<Map<String,Object>> resultList = null;
        try {
            resultList = ImportExcelUtil.getListTwo(fis,fileName, map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<ExceDate> clientList = JSON.parseObject(JSON.toJSONString(resultList), new TypeReference<ArrayList<ExceDate>>() {});
        String[] str = new String[clientList.size()];
        kfkcMap = new HashMap<>();
        needList = new ArrayList<>();
        for (int i = 0; i < clientList.size(); i++){
            kfkcMap.put(clientList.get(i).getHpbh(),clientList.get(i).getKfkc());
            str[i] = needUrl+clientList.get(i).getHpbh();
        }
//        try {
//            System.out.println("==================模拟登陆开始=============");
//            Login();
//        } catch (IOException e) {
//            System.out.println("===================登陆失败===================");
//            e.printStackTrace();
//        }
        Spider.create(new Reptile())
                .addUrl(str)
                //.addPipeline(new ConsolePipeline())
                .thread(5)
                //启动爬虫
                .run();
        System.out.println("===================获取完毕，开始导出===================");
        Calendar ca = Calendar.getInstance();
        ca.setTime(new Date());
        ca.add(Calendar.HOUR_OF_DAY,8);
        getExcel(outFilePath+"\\"+onlyName+ DateUtils.formatDate(ca.getTime(),"HHmmss")+"."+endName);
        System.out.println("===================导出完毕===================");
    }

    //====================================================================================================================

    public static void getExcel(String outFilePath){
        String jsonStr = "{listHead: [" +
                "{title: '货品编号',column: 'hpbh'}," +
                "{title: '可发库存',column: 'kfkc'}," +
                "{title: '天猫链接',column: 'tmlj'}," +
                "{title: '天猫链接ID',column: 'tmljid'}" +
                "]}";

        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        List<Map<String, Object>> listHead = (List<Map<String, Object>>) jsonObject.get("listHead");

        exportExcel(listHead,needList,outFilePath);

    }

    public static void exportExcel(List<Map<String, Object>> listHead, List<ExceDate> listDataSource, String outFilePath) {
        HSSFWorkbook workbook = getHSSFWorkbook(listHead, listDataSource);
        File file1 = new File(outFilePath);
        try {
            FileOutputStream out = new FileOutputStream(file1);
            workbook.write(out);
            out.close();
        } catch (IOException var7) {
            var7.printStackTrace();
        }
    }

    public static HSSFWorkbook getHSSFWorkbook(List<Map<String, Object>> listHead, List<ExceDate> listDataSource) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("sheet1");
        HSSFRow headerRow = sheet.createRow(0);
        for (int i = 0; i < listHead.size(); i ++){
            if (listHead.get(i).get("column") != null) {
                HSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(listHead.get(i).get("title").toString());
            }
        }
        int index = 0;
        Iterator var6 = listDataSource.iterator();

        while(var6.hasNext()) {
            Object obj = var6.next();
            ++index;
            HSSFRow headerRowL = sheet.createRow(index);
            Map<String, Object> map = getFiledsInfo(obj);
            map.forEach((k, v) -> {
                for (int i = 0; i < listHead.size(); i ++){
                    if (listHead.get(i).get("column") != null && org.apache.commons.lang.StringUtils.equals(k, listHead.get(i).get("column").toString())) {
                        HSSFCell cell = headerRowL.createCell(i);
                        if (org.apache.commons.lang.StringUtils.equals(listHead.get(i).get("column").toString(), "kfkc")) {
                            try{
                                cell.setCellValue(Integer.parseInt(v.toString()));
                            }catch (Exception e){
                                cell.setCellValue("");
                            }
                        } else {
                            cell.setCellValue(v.toString());
                        }
                    }
                }
            });
        }

        return workbook;
    }

    public static Map<String, Object> getFiledsInfo(Object o) {
        List<Field> fieldList = new ArrayList();
        Class tempClass = o.getClass();
        if (org.apache.commons.lang.StringUtils.equals(tempClass.toString(), "class java.util.HashMap")) {
            return (Map)o;
        } else {
            while(tempClass != null) {
                fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
                tempClass = tempClass.getSuperclass();
            }

            Map infoMap = new HashMap();

            for(int i = 0; i < fieldList.size(); ++i) {
                Object value = getFieldValueByName(((Field)fieldList.get(i)).getName(), o);
                if (org.apache.commons.lang.StringUtils.equals(((Field)fieldList.get(i)).getType().toString(), "class java.util.Date") && value != null && !org.apache.commons.lang.StringUtils.equals(value.toString(), "")) {
                    value = formatDate((Date)value, (String)null);
                }

                infoMap.put(((Field)fieldList.get(i)).getName(), value != null ? value : "暂无此项数据");
            }

            return infoMap;
        }
    }

    public static Object getFieldValueByName(String fieldName, Object o) {
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method method = o.getClass().getMethod(getter);
            Object value = method.invoke(o);
            return value;
        } catch (Exception var6) {
            System.out.println(var6.getMessage());
            return null;
        }
    }
}
