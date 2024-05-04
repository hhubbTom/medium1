import ij.IJ;
import ij.ImagePlus;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Main extends Application {

    String defaultImage = "";
    Image oImage;//通过设计两个image，一个用来保存现有的，一个保存当前的，方便展示原图。
    Image cImage;//当前图像current

    @Override
    public void start(Stage primaryStage) {

        TextField textSrc = new TextField();
        textSrc.setText(defaultImage);
        Button btnFile = new Button("浏览文件");
        Button btnReset = new Button("显示链接原图");
        Button btnHD = new Button("灰度图");
        Button btn2 = new Button("二值图");
        Button btnGama = new Button("伽马变换"); //伽马变换按钮
        Button btnSave = new Button("保存图片");

        ImageView imageView = new ImageView();
        StackPane imagePane = new StackPane(); // 使用 StackPane 居中显示图像
        imagePane.setPadding(new Insets(10));

        // 通过文件浏览打开图像
        btnFile.setOnAction(e -> {//用户点击文件浏览会调用下列代码
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                textSrc.setText(file.getAbsolutePath()); // 设置文本框显示文件路径以便用户查看路径是否有错误
                try {
                    // 检查文件扩展名，如果是TIF文件则转换为BMP，否则加载普通图片。tif文件无法直接处理所以要这样
                    if (file.getName().toLowerCase().endsWith(".tif")) {
                        File bmpFile = TiffToBmp(file);
                        oImage = new Image(bmpFile.toURI().toString());
                    } else {
                        oImage = loadImageFromFile(file);
                    }
                    cImage = oImage;
                    displayImage(cImage, imagePane);//展示图片
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // 显示原图按钮
        btnReset.setOnAction(e -> {
            if (oImage != null) {
                displayImage(oImage, imagePane);
            }
        });

        // 转换为灰度图
        btnHD.setOnAction(e -> {
            if (oImage != null) {
                cImage = ToGrayImage(oImage);
                displayImage(cImage, imagePane);
            }
        });

        // 转换为二值图
        btn2.setOnAction(e -> {
            if (cImage != null) {
                cImage = ToBinaryImage(oImage);
                displayImage(cImage, imagePane);
            }
        });

        // 伽马变换按钮
        btnGama.setOnAction(e -> {
            if (oImage != null) {
                cImage = GamaTrans(oImage, 0.08, 1.45); // 设置伽马变换参数，看起来比较明亮的参数
                displayImage(cImage, imagePane);
            }
        });

        // 保存图片
        btnSave.setOnAction(e -> {
            if (cImage != null) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save Image");
                File file = fileChooser.showSaveDialog(primaryStage);
                if (file != null) {
                    try {
                        ImageIO.write(SwingFXUtils.fromFXImage(cImage, null), "png", file);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        HBox butBox = new HBox(20);
        butBox.getChildren().addAll(btnFile, btnReset, btnHD, btn2, btnGama, btnSave); // 按钮盒子

        VBox vBox = new VBox(10);
        vBox.setPadding(new Insets(10, 10, 10, 10));
        vBox.getChildren().addAll(textSrc, butBox);

        vBox.getChildren().add(imagePane); // 将 StackPane 添加到 VBox 中

        Scene scene = new Scene(vBox, 600, 500);

        primaryStage.setTitle("刘烨磊‘s 实验一");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private Image loadImageFromFile(File file) throws IOException {
        return new Image(file.toURI().toString());
    }

    private void displayImage(Image image, StackPane imagePane) {
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(400);
        imageView.setFitWidth(500);
        imageView.setPreserveRatio(true);
        imagePane.getChildren().clear();
        imagePane.getChildren().add(imageView);
    }

    private Image ToGrayImage(Image oImage) {//灰度图转换函数
        int width = (int) oImage.getWidth();
        int height = (int) oImage.getHeight();
        WritableImage grayImage = new WritableImage(width, height);
        PixelReader pixelReader = oImage.getPixelReader();
        PixelWriter pixelWriter = grayImage.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                double gray = color.getRed() * 0.299 + color.getGreen() * 0.587 + color.getBlue() * 0.114;//使用加权平均法计算灰度值
                pixelWriter.setColor(x, y, Color.color(gray, gray, gray));
            }
        }
        return grayImage;
    }

    private Image ToBinaryImage(Image oImage) {//二值图转换函数
        int width = (int) oImage.getWidth();
        int height = (int) oImage.getHeight();
        WritableImage binaryImage = new WritableImage(width, height);
        PixelReader pixelReader = oImage.getPixelReader();
        PixelWriter pixelWriter = binaryImage.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                double brightness = color.getBrightness();
                if (
                        brightness > 0.5) {//大于0.5就是白的，小于就是黑的
                    pixelWriter.setColor(x, y, Color.WHITE);
                } else {
                    pixelWriter.setColor(x, y, Color.BLACK);
                }
            }
        }
        return binaryImage;
    }

    private File TiffToBmp(File tiffFile) throws IOException {
        // 使用 ImageJ 库处理 TIFF 文件
        ImagePlus imagePlus = IJ.openImage(tiffFile.getAbsolutePath());
        if (imagePlus != null) {
            // 将 TIFF 图像保存为 BMP 格式
            String bmpFileName = tiffFile.getAbsolutePath().replace(".tif", ".bmp");
                    IJ.saveAs(imagePlus, "BMP", bmpFileName);
            return new File(bmpFileName);
        } else {
            throw new IOException("Failed to open TIFF image.");//设置的抛出异常
        }
    }

    // 伽马变换函数
    private Image GamaTrans(Image oImage, double c, double gamma) {
        PixelReader pixelReader = oImage.getPixelReader();
        int width = (int) oImage.getWidth();
        int height = (int) oImage.getHeight();
        WritableImage transformedImage = new WritableImage(width, height);
        PixelWriter pixelWriter = transformedImage.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                double red = color.getRed() * 255;
                double green = color.getGreen() * 255;
                double blue = color.getBlue() * 255;
                // 应用伽马变换
                double newRed = c * Math.pow(red, gamma);//采用gama变换，c*i^r
                double newGreen = c * Math.pow(green, gamma);
                double newBlue = c * Math.pow(blue, gamma);
                // 将变换后的颜色值限制在 [0, 255] 范围内
                newRed = Math.min(255, Math.max(0, newRed));
                newGreen = Math.min(255, Math.max(0, newGreen));
                newBlue = Math.min(255, Math.max(0, newBlue));
                // 将颜色值映射回 [0, 1] 范围内
                newRed /= 255;
                newGreen /= 255;
                newBlue /= 255;
                pixelWriter.setColor(x, y, Color.color(newRed, newGreen, newBlue));
            }
        }
        return transformedImage;
    }
}
