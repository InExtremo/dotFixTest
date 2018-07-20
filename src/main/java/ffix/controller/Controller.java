package ffix.controller;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.stage.FileChooser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static ffix.service.ImageUtil.*;

public class Controller implements Initializable {

    private ImageView imageView;
    @FXML
    private ScrollPane sPane;
    @FXML
    private Button openBtn;

    @FXML
    private TextField tfX, tfY, tfStep, tfStep2;

    private final DoubleProperty zoomProperty = new SimpleDoubleProperty(200);

    private BufferedImage bufferedImage;

    private List<Point> cord;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.cord = new ArrayList<>(233000);
        imageView = new ImageView();
        openBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();

            //Set extension filter
            FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
            FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
            fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);
            //fileChooser.setInitialDirectory(new File("D:\\PROG\\raws for test\\1"));
            //Show open file dialog
            File file = fileChooser.showOpenDialog(null);
            try {
                bufferedImage = ImageIO.read(file);
                Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                imageView.setImage(image);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        });
        zoomProperty.addListener(arg0 -> {
            imageView.setFitWidth(zoomProperty.get() * 4);
            imageView.setFitHeight(zoomProperty.get() * 3);
        });
        sPane.addEventFilter(ScrollEvent.ANY, event -> {
            if (event.getDeltaY() > 0) {
                zoomProperty.set(zoomProperty.get() * 1.1);
            } else if (event.getDeltaY() < 0) {
                zoomProperty.set(zoomProperty.get() / 1.1);
            }
        });
        try {
            //default image.
            bufferedImage = ImageIO.read(this.getClass().getResourceAsStream("/images/BURST20180710142435119.jpg"));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        imageView.setImage(SwingFXUtils.toFXImage(bufferedImage, null));
        imageView.preserveRatioProperty().set(true);
        sPane.setContent(imageView);
    }

    @FXML
    public void fix() {

        BufferedImage bi = deepCopy(bufferedImage);
        Graphics gr = bi.getGraphics();
        String dots = getRes();
        cord = parsePoints(dots);

        //row length
        int step1 = Integer.valueOf(tfStep.getText());
        int step2 = Integer.valueOf(tfStep2.getText());

        for (Point point : cord) {
            int fixY = point.x + Integer.valueOf(tfX.getText());
            int fixX = point.y + Integer.valueOf(tfY.getText());
            List<PolynomialSplineFunction> res1 = calculateFitFunction(fixX, fixY, step1, bi);
            for (int i = -1; i <= 1; i++) {
                double r = res1.get(0).value(fixX + step2 + i),
                        g = res1.get(1).value(fixX + step2 + i),
                        b = res1.get(2).value(fixX + step2 + i);
                int fixedR = (int) (r > 255 ? 255 : r < 0 ? 0 : r);
                int fixedG = (int) (g > 255 ? 255 : g < 0 ? 0 : g);
                int fixedB = (int) (b > 255 ? 255 : b < 0 ? 0 : b);
                bi.setRGB(fixX + i, fixY, new Color(fixedR, fixedG, fixedB).getRGB());
            }
        }
        gr.dispose();
        imageView.setImage(SwingFXUtils.toFXImage(bi, null));
    }

    private String getRes() {
        String result = "";
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            result = IOUtils.toString(classLoader.getResourceAsStream("ffix/dotmap.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
