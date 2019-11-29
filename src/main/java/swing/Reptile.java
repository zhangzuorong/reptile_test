package swing;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.DateUtils;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import swing.ExcelUtil.ExceDate;
import swing.ExcelUtil.ExportExcelUtil;
import swing.ExcelUtil.ImportExcelUtil;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.io.*;
import java.util.*;
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

    public static List<ExceDate> needList;//能获取到所需id的集合
    public static List<ExceDate> noGetList;//不能获取到所需id的集合

    public static Map<String,Object> kfkcMap;//可发库存集合
    public static Map<String,Object> sjbmMap;//商家编码集合

    //设置一个全局的参数存放登陆cookies
    private static Set<Cookie> cookies;

    // 部分一：抓取网站的相关配置，包括编码、重试次数等、抓取间隔
    private Site site = Site.me().setRetryTimes(3).setSleepTime(sleepTime);

    @Override
    public Site getSite() {
//        if (cookies.size() > 0) {
//            for (Cookie cookie : cookies) {
//                site.addCookie(cookie.getName().toString(), cookie.getValue().toString());
//            }
//        }
        return site;
    }

    @Override
    // process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
    public void process(Page page) {

        String msg = page.getHtml().xpath(needRules).toString();
        String msgTao = page.getHtml().xpath(taoBaoRules).toString();
        String tianMaoMsg = "";
        if (StringUtils.isNotBlank(msg)) {
            String strOne = StringUtils.substringBefore(msg,"&");
            String strEnd = StringUtils.substringAfter(strOne,"id=");
            tianMaoMsg = strEnd;
        }
        //System.out.println("msgTaoBao"+msgTao);

        // 部分二：定义如何抽取页面信息，并保存下来
        String hpbmStr = StringUtils.substringAfter(page.getUrl().toString(),"q=");

        ExceDate exceDate = new ExceDate();
        exceDate.setHpbh(hpbmStr);
        exceDate.setTmlj(tmallLink + tianMaoMsg);
        exceDate.setTmljid(tianMaoMsg);
        exceDate.setKfkc(kfkcMap.get(hpbmStr)+"");
        exceDate.setSpbh(sjbmMap.get(hpbmStr)+"");

//        if (StringUtils.isEmpty(tianMaoMsg)) {//天猫没有获取到
//            System.out.println("#########################"+hpbmStr+"在天猫未获取到###########################");
//            List<String> list = new ArrayList<>();
//            list.add("https://s.taobao.com/search?tab=old&q=9787040330755");
//            page.addTargetRequests(list);
//            System.out.println("#########################追加链接完成###########################");
//        }

        if (StringUtils.isEmpty(exceDate.getTbljid()) && StringUtils.isEmpty(exceDate.getTmljid())) {
            noGetList.add(exceDate);
        }else {
            needList.add(exceDate);
        }

        System.out.println("货品编码为" + hpbmStr + "的产品，天猫ID为："+ tianMaoMsg);
    }
//    @Test
//    public void test(){
//        try {
//            Login();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


    public static void Login() throws IOException {
        System.setProperty("webdriver.chrome.driver",
                "D:\\develop\\Chrome\\driver78\\chromedriver.exe");


        WebDriver driver = new ChromeDriver();
        driver.get("https://login.taobao.com/member/login.jhtml");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 获取 网页的 title
        WebElement checkbox = driver.findElement(By.xpath("//i[@class='iconfont static']"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", checkbox);

        WebElement userName = driver.findElement(By.xpath("//input[@id='TPL_username_1']"));
        js.executeScript("arguments[0].value='13687681257'", userName);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement passWord = driver.findElement(By.xpath("//input[@id='TPL_password_1']"));
        js.executeScript("arguments[0].value='Zuorong.118'", passWord);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement loginButton = driver.findElement(By.xpath("//button[@id='J_SubmitStatic']"));
        js.executeScript("arguments[0].click();", loginButton);

        if (driver.findElement(By.xpath("//span[@class='nc-lang-cnt']")).getText().indexOf("拖动") > -1) {
            WebElement passWordAgain = driver.findElement(By.xpath("//input[@id='TPL_password_1']"));
            js.executeScript("arguments[0].value='Zuorong.118'", passWordAgain);

            WebElement slider = driver.findElement(By.id("nc_1_n1z"));

            Actions action = new Actions(driver);
            action.dragAndDropBy(slider,500,0).perform();

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //很重要的一步获取登陆后的cookies
        cookies = driver.manage().getCookies();
        //driver.close();
    }

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
        map.put("淘宝链接","tblj");
        map.put("天猫链接ID","tmljid");

        List<Map<String,Object>> resultList = null;
        try {
            resultList = ImportExcelUtil.getListTwo(fis,fileName, map);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("======================导入失败======================");
        }
        ArrayList<ExceDate> clientList = JSON.parseObject(JSON.toJSONString(resultList), new TypeReference<ArrayList<ExceDate>>() {});
        String[] str = new String[clientList.size()];
        sjbmMap = new HashMap<>();
        kfkcMap = new HashMap<>();
        needList = new ArrayList<>();
        noGetList = new ArrayList<>();
        for (int i = 0; i < clientList.size(); i++){
            kfkcMap.put(clientList.get(i).getHpbh(),clientList.get(i).getKfkc());
            sjbmMap.put(clientList.get(i).getHpbh(),clientList.get(i).getSpbh());
            str[i] = needUrl+clientList.get(i).getHpbh();
        }
//        try {
//            Login();
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("########################模拟登陆失败#####################");
//            System.out.println(e);
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
        String dateStr = DateUtils.formatDate(ca.getTime(),"HHmmss");
        ExportExcelUtil.getExcel(outFilePath+"\\"+onlyName+ dateStr +".xls",needList);

        if (noGetList.size() > 0) {
            System.out.println("===================导出未获取到的数据===================");
            ExportExcelUtil.getExcel(outFilePath+"\\"+"未抓到ID"+onlyName+ dateStr +".xls",noGetList);
        }
        System.out.println("=======================导出完毕======================");
    }

}
