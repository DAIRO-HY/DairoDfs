package cn.dairo.mysql.model.tool.code.tool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class FileUtil {
    /**
     * 保存字符串到文件
     *
     * @param fn
     * @param con
     */
    public static void savaStr(String fn, String con) {
        savaStr(fn, con, false);
    }

    /**
     * 保存字符串到文件
     *
     * @param fn
     * @param con
     * @param isCover:是否覆盖
     */
    public static void savaStr(String fn, String con, boolean isCover) {
        BufferedWriter output = null;
        try {
            File file = new File(fn);
            if (!file.exists()) {

                // 获取路径
                File pDir = new File(file.getParent());
                if (!pDir.exists()) {
                    // 建文件夹
                    pDir.mkdirs();
                }
            } else {
                if (!isCover) {
                    return;
                }
            }

            output = new BufferedWriter(new FileWriter(file, false));
            output.write(con);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                output.close();
            } catch (Exception e) {
            }
        }
    }
}
