import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FFmpegExample {
    public static void main(String[] args) {
        try {
            // 设置FFmpeg命令
            String inputImagePath = "/Users/zhoulq/dev/java/idea/dairo-dfs/dairo-dfs-server/1.jpg";
            String outputImagePath = "/Users/zhoulq/dev/java/idea/dairo-dfs/dairo-dfs-server/1.crop.jpg";

            String[] command = {
                    "ffmpeg", "-i", inputImagePath,
                    "-vf", "crop=100:100:0:0", // 裁剪参数
                    "-f", "image2pipe", "-vcodec", "mjpeg", "-"
            };

            // 创建ProcessBuilder
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true); // 合并标准错误和标准输出

            // 启动进程
            Process process = pb.start();

            // 处理输出流
            try (InputStream is = process.getInputStream();
                 BufferedInputStream bis = new BufferedInputStream(is);
                 FileOutputStream fos = new FileOutputStream(outputImagePath)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            // 等待进程完成
            int exitCode = process.waitFor();
            System.out.println("Process exited with code: " + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
