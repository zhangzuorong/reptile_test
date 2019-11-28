package swing;

import org.apache.commons.lang3.StringUtils;
import org.pushingpixels.substance.api.skin.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;


/**
 * 开发公司：山东海豚数据技术有限公司
 * 版权：山东海豚数据技术有限公司
 * <p>
 * SwingHaha
 *
 * @author zzr
 * @created Create Time: 2019/11/22
 */
public class SwingHaha {
    /**
     * 抓取页面数据所需的参数
     */
    //爬虫获取规则
    public static String needRules = "//div[@class='productImg-wrap']/a/tidyText()";
    //目标页面链接
    public static String needUrl = "https://list.tmall.com/search_product.htm?q=";
    //淘宝目标页面链接
    public static String taobaoNeedUrl = "https://s.taobao.com/search?tab=old&q=";
    //天猫链接
    public static String tmallLink = "https://detail.tmall.com/item.htm?id=";
    //淘宝链接
    public static String taobaoLink = "https://item.taobao.com/item.htm?&id=";
    //采集时间间隔
    public static int sleepTime = 3000;

    private JPanel panel1;
    private JButton btnClear;
    private JPanel panel2;
    private JLabel labelSrc;
    private JLabel abelDes;
    private JTextField srcPath;
    private JTextField desPath;
    private JButton btnSelect;// 选择源文件
    private JButton btnOpen;// 打开目标文件目录
    private JScrollPane jPane;
    private JTextArea textArea1;
    private JButton btnExec;
    private JLabel sleepLabel;
    private JTextField sleepNum;

    private String inFilePath;
    private String outFilePath;

    private String curDir, curOs, curArch;
    private ExecutorService service = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "output");
        }
    });

    public SwingHaha() {
        curDir = System.getProperty("user.dir");// 当前路径
        curOs = System.getProperty("os.name");// 当前操作系统
        curArch = System.getProperty("os.arch");// 当前操作系统架构
        System.out.println("当前目录：" + curDir + ", 当前操作系统：" + curOs + ", 操作系统架构：" + curArch);

        textArea1.setLineWrap(true);// 激活自动换行功能
        textArea1.setWrapStyleWord(true);// 激活断行不断字功能
        outputUI();

        // 支持拖拽选取文件或目录
        srcPath.setTransferHandler(new TransferHandler() {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean importData(JComponent comp, Transferable t) {
                try {
                    Object o = t.getTransferData(DataFlavor.javaFileListFlavor);
                    String filepath = o.toString();
                    if (filepath.startsWith("[")) {
                        filepath = filepath.substring(1);
                    }
                    if (filepath.endsWith("]")) {
                        filepath = filepath.substring(0, filepath.length() - 1);
                    }
                    if (StringUtils.isNotEmpty(filepath)) {// 这里也可以做文件类型过滤
                        srcPath.setText(filepath);
                        refreshTarget();
                    }
                    return true;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
            @Override
            public boolean canImport(JComponent comp, DataFlavor[] flavors) {
                for (int i = 0; i < flavors.length; i++) {
                    if (DataFlavor.javaFileListFlavor.equals(flavors[i])) {
                        return true;
                    }
                }
                return false;
            }
        });

        btnSelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser("xls","xlsx");
            }
        });

        btnOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooserTwo();
            }
        });

        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea1.setText("");
            }
        });

        btnExec.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                service.submit(new Runnable() {
                    @Override
                    public void run() {
                        String src = srcPath.getText();
                        String target = desPath.getText();
                        String sleep = sleepNum.getText();
                        if (StringUtils.isEmpty(src)) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    showMessage("请先选择源文件");
                                }}
                            );
                            return ;
                        }
                        if (StringUtils.isEmpty(target)) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    showMessage("请先选择导出地址");
                                }}
                            );
                            return ;
                        }
                        if (StringUtils.isEmpty(sleep)) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    showMessage("请先填写采集间隔");
                                }}
                            );
                            return ;
                        }
                        sleepTime = Integer.parseInt(sleepNum.getText())*1000;
                        System.out.println("===================开始===================");
                        try {
                            Reptile.changeStart(inFilePath,outFilePath);
                        } catch (FileNotFoundException e1) {
                            System.out.println("===================程序出错了===================");
                            e1.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    /**
     * 刷新目标文件地址
     */
    private void refreshTarget() {
        desPath.setText(curDir);
    }

    /**
     * 捕获控制台输出到GUI界面上
     */
    protected void outputUI(){
        OutputStream textAreaStream = new OutputStream() {
            public void write(int b) throws IOException {
                textArea1.append(String.valueOf((char)b));
            }

            public void write(byte b[]) throws IOException {
                textArea1.append(new String(b));
            }

            public void write(byte b[], int off, int len) throws IOException {
                textArea1.append(new String(b, off, len));
            }
        };
        PrintStream myOut = new PrintStream(textAreaStream);
        System.setOut(myOut);
        System.setErr(myOut);
    }

    /**
     * 显示提示信息框
     * @param message
     */
    private void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "提示",  JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * 打开文件选择器
     *
     * @param type 要选取的文件类型
     */
    private void fileChooser(String... type) {
        JFileChooser chooser = new JFileChooser(srcPath.getText());
        if (!isEmpty(type)) {
            StringBuilder suffix = new StringBuilder();
            for (String s : type) {
                suffix.append("." + s);
            }
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    suffix.toString(), type);
            // 设置文件过滤类型
            chooser.setFileFilter(filter);
        } else {
            // 设置选择文件夹
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        // 打开选择器面板
        int returnVal = chooser.showOpenDialog(new JPanel());
        // 保存文件从这里入手，输出的是文件名
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            srcPath.setText(path);
            inFilePath = path;
        }
    }

    private void fileChooserTwo(){
        JFileChooser fc=new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);//只能选择目录
        String path=null;
        File f=null;
        int flag = 0;
        try{
            flag=fc.showOpenDialog(null);
        }catch(HeadlessException head){
            System.out.println("Open File Dialog ERROR!");
        }
        if(flag == JFileChooser.APPROVE_OPTION){
            //获得该文件
            f=fc.getSelectedFile();
            path=f.getPath();
        }
        desPath.setText(path);
        outFilePath = path;
    }


    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        try {
            UIManager.setLookAndFeel(new SubstanceMistAquaLookAndFeel()) ;
        } catch (Exception e) {
            System.out.println("Substance Raven Graphite failed to initialize");
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("SwingHaha");
                frame.setContentPane(new SwingHaha().panel1);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setSize(800, 600);
                frame.setResizable(false);// 设置不可拉伸
                frame.setLocationRelativeTo(frame);// 居中
                frame.setVisible(true);
            }
        });
    }
}
