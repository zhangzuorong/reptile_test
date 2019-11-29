package swing.ExcelUtil;

import com.alibaba.fastjson.JSONObject;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static org.apache.http.client.utils.DateUtils.formatDate;

/**
 * 开发公司：山东海豚数据技术有限公司
 * 版权：山东海豚数据技术有限公司
 * <p>
 * ExportExcelUtil
 *
 * @author zzr
 * @created Create Time: 2019/11/29
 */
public class ExportExcelUtil {

    public static void getExcel(String outFilePath,List<ExceDate> needList){
        String jsonStr = "{listHead: [" +
                "{title: '商家编码',column: 'spbh'}," +
                "{title: '货品编号',column: 'hpbh'}," +
                "{title: '可发库存',column: 'kfkc'}," +
                "{title: '天猫链接ID',column: 'tmljid'}" +
                "{title: '天猫链接',column: 'tmlj'}," +
                "{title: '淘宝链接',column: 'tblj'}," +
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

                infoMap.put(((Field)fieldList.get(i)).getName(), value != null ? value : "");
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
