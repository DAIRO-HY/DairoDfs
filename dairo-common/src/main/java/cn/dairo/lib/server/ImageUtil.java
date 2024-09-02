package cn.dairo.lib.server;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * 图片处理工具类：<br>
 * 功能：缩放图像、切割图像、图像类型转换、彩色转黑白、文字水印、图片水印等
 *
 * @author Administrator
 */
public class ImageUtil {

    /**
     * 几种常见的图片格式
     */
    public static String IMAGE_TYPE_GIF = "gif";// 图形交换格式
    public static String IMAGE_TYPE_JPG = "jpg";// 联合照片专家组
    public static String IMAGE_TYPE_JPEG = "jpeg";// 联合照片专家组
    public static String IMAGE_TYPE_BMP = "bmp";// 英文Bitmap（位图）的简写，它是Windows操作系统中的标准图像文件格式
    public static String IMAGE_TYPE_PNG = "png";// 可移植网络图形
    public static String IMAGE_TYPE_PSD = "psd";// Photoshop的专用格式Photoshop

    /**
     * @param inFile:输入的文件
     * @param outFile:输出文件
     * @param x:距离左边的距离
     * @param y:距离顶部的距离
     * @param w:裁剪宽
     * @param h:裁剪高
     * @param scale:缩放比例
     * @return
     */
    public static boolean jcropImage(String inFile, String outFile, int x, int y, int w, int h, double scale) {
        int width = new BigDecimal(w * scale).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        int height = new BigDecimal(h * scale).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        return jcropImage(inFile, outFile, x, y, w, h, width, height);
    }

    /**
     * @param inFile:输入的文件
     * @param outFile:输出文件
     * @param x:距离左边的距离
     * @param y:距离顶部的距离
     * @param w:裁剪宽
     * @param h:裁剪高
     * @param width:目标宽
     * @param height:目标高
     * @return
     */
    public static boolean jcropImage(String inFile, String outFile, int x, int y, int w, int h, int width, int height) {
        InputStream in = null;
        ImageReader reader = null;
        try {

            // 读取流
            in = new FileInputStream(inFile);
            ImageInputStream iis = ImageIO.createImageInputStream(in);

            // 获取文件后缀
            String ext = inFile.substring(inFile.lastIndexOf(".") + 1);

            Iterator<ImageReader> iterator = ImageIO.getImageReadersByFormatName(ext);
            reader = (ImageReader) iterator.next();
            reader.setInput(iis, true);
            ImageReadParam param = reader.getDefaultReadParam();
            Rectangle rect = new Rectangle(x, y, w, h);
            param.setSourceRegion(rect);
            BufferedImage bi = reader.read(0, param);
            Image image = bi.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage newBi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = newBi.getGraphics();
            g.drawImage(image, 0, 0, null); // 绘制缩小后的图
            g.dispose();
            ImageIO.write(newBi, "JPEG", new File(outFile));// 输出到文件流
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                in.close();
            } catch (Exception e) {
            }
        }
    }

    /*
     * 根据尺寸图片居中裁剪
     */
    public static void cutCenterImage(String src, String dest, int w, int h) throws IOException {
        Iterator iterator = ImageIO.getImageReadersByFormatName("jpg");
        ImageReader reader = (ImageReader) iterator.next();
        InputStream in = new FileInputStream(src);
        ImageInputStream iis = ImageIO.createImageInputStream(in);
        reader.setInput(iis, true);
        ImageReadParam param = reader.getDefaultReadParam();
        int imageIndex = 0;
        Rectangle rect = new Rectangle((reader.getWidth(imageIndex) - w) / 2, (reader.getHeight(imageIndex) - h) / 2, w,
                h);
        param.setSourceRegion(rect);
        BufferedImage bi = reader.read(0, param);
        ImageIO.write(bi, "jpg", new File(dest));

    }

    /*
     * 图片裁剪二分之一
     */
    public static void cutHalfImage(String src, String dest) throws IOException {
        Iterator iterator = ImageIO.getImageReadersByFormatName("jpg");
        ImageReader reader = (ImageReader) iterator.next();
        InputStream in = new FileInputStream(src);
        ImageInputStream iis = ImageIO.createImageInputStream(in);
        reader.setInput(iis, true);
        ImageReadParam param = reader.getDefaultReadParam();
        int imageIndex = 0;
        int width = reader.getWidth(imageIndex) / 2;
        int height = reader.getHeight(imageIndex) / 2;
        Rectangle rect = new Rectangle(width / 2, height / 2, width, height);
        param.setSourceRegion(rect);
        BufferedImage bi = reader.read(0, param);
        ImageIO.write(bi, "jpg", new File(dest));
    }

    /**
     * 图片压缩
     *
     * @param inFile:输入文件
     * @param outFile:输出文件
     * @param maxWidth:最大宽度
     * @param maxHeight:最大高度
     * @param isZoom:小尺寸是否强制放大
     * @return
     */
    public static boolean compress(String inFile, String outFile, int maxWidth, int maxHeight, boolean isZoom) {

        // 没有设置最大值
        if (maxWidth == 0 || maxHeight == 0) {

            // 直接剪切 不做任何处理
            if (new File(inFile).renameTo(new File(outFile))) {
                return true;
            } else {
                return false;
            }
        }
        try {
            BufferedImage bufImg = ImageIO.read(new File(inFile));
            int outWidth = 0;// 输出宽度
            int outHeight = 0;// 输出高度

            int trueWidth = bufImg.getWidth();// 实际宽度
            int trueHeight = bufImg.getHeight();// 实际宽度

            double scale = ((double) trueWidth / trueHeight) / ((double) maxWidth / maxHeight);

            if (trueWidth < maxWidth && trueHeight < maxHeight) {

                // 图片比理想的小
                if (!isZoom) {

                    // 直接剪切 不做任何处理
                    if (new File(inFile).renameTo(new File(outFile))) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }

            // 缩放处理
            if (scale > 1) {

                // 放大后的尺寸比理想的要宽
                outWidth = maxWidth;
                outHeight = trueHeight * outWidth / trueWidth;
            } else {

                // 放大后的尺寸比理想的要高
                outHeight = maxHeight;
                outWidth = trueWidth * outHeight / trueHeight;
            }

            Image image = bufImg.getScaledInstance(outWidth, outHeight, Image.SCALE_SMOOTH);
            BufferedImage newBi = new BufferedImage(outWidth, outHeight, BufferedImage.TYPE_INT_RGB);
            Graphics g = newBi.getGraphics();
            g.drawImage(image, 0, 0, null); // 绘制缩小后的图
            g.dispose();

            // 输出到文件
            ImageIO.write(newBi, "JPEG", new File(outFile));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 设置图片尺寸 ,比例不一致将做变形处理
     *
     * @param inFile:输入文件
     * @param outFile:输出文件
     * @param width:最大宽度
     * @return
     */
    public static boolean reSize(String inFile, String outFile, int width, int height) {

        // 没有设置最大值
        if (width == 0 || height == 0) {
            return false;
        }
        try {
            BufferedImage bufImg = ImageIO.read(new File(inFile));
            Image image = bufImg.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage newBi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = newBi.getGraphics();
            g.drawImage(image, 0, 0, null); // 绘制缩小后的图
            g.dispose();

            // 输出到文件
            ImageIO.write(newBi, "JPEG", new File(outFile));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /*
     * 图片缩放
     */
    public static void zoomImage(String src, String dest, int w, int h) throws Exception {
        double wr = 0, hr = 0;
        File srcFile = new File(src);
        File destFile = new File(dest);
        BufferedImage bufImg = ImageIO.read(srcFile);
        Image Itemp = bufImg.getScaledInstance(w, h, bufImg.SCALE_SMOOTH);
        wr = w * 1.0 / bufImg.getWidth();
        hr = h * 1.0 / bufImg.getHeight();
        AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(wr, hr), null);
        Itemp = ato.filter(bufImg, null);
        try {
            ImageIO.write((BufferedImage) Itemp, dest.substring(dest.lastIndexOf(".") + 1), destFile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}