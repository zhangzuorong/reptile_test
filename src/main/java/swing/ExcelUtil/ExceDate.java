package swing.ExcelUtil;

import lombok.Data;

/**
 * 开发公司：山东海豚数据技术有限公司
 * 版权：山东海豚数据技术有限公司
 * <p>
 * ExceDate
 *
 * @author zzr
 * @created Create Time: 2019/11/23
 */
@Data
public class ExceDate {
    /**
     * 商家编码
     */
    private String spbh;

    /**
     * 货品编号
     */
    private String hpbh;

    /**
     * 可发库存
     */
    private String kfkc;

    /**
     * 天猫链接
     */
    private String tmlj;

    /**
     * 天猫链接ID
     */
    private String tmljid;

    /**
     * 淘宝链接
     */
    private String tblj;

    /**
     * 淘宝链接ID
     */
    private String tbljid;
}
