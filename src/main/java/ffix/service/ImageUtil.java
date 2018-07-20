package ffix.service;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @author <a href="mailto:max23sim@gmail.com"/>Maxim.
 * @since 20.07.2018.
 */
public class ImageUtil {

    public static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    /**
     * Extract point list from dot map(string) file.
     *
     * @param dotMap dot map as string.
     * @return list of bad points.
     */
    public static List<Point> parsePoints(String dotMap) {
        String[] coords = dotMap.split("\n");
        return Stream.of(coords).parallel().map(v -> v.split(",")).map(v -> {
            int x = 0;
            int y = 0;
            try {
                x = Integer.valueOf(v[0].trim());
                y = Integer.valueOf(v[1].trim());
            } catch (NumberFormatException e) {
                System.err.println(v[1] + ";" + v[2] + " Error:" + e.getMessage());
            }
            return new Point(x, y);
        }).collect(toList());
    }

    /**
     * Calculation fit functions for Red, Green and Blue colors. Return List of spline functions.
     *
     * @param x  coordinate of bad dot.
     * @param y  coorinate of bad dot.
     * @param n  row radius with bad dot in center.
     * @param bi image.
     * @return List with spline interpolations functions for RED, GREEN and BLUE colors.
     */
    //TODO refactor for separate colors.
    public static List<PolynomialSplineFunction> calculateFitFunction(int x, int y, int n, BufferedImage bi) {
        List<Double> xList = new ArrayList<>();
        List<Double> rList = new ArrayList<>();
        List<Double> gList = new ArrayList<>();
        List<Double> bList = new ArrayList<>();

        for (int i = -n; i <= n; i++) {
            if (i == 0 || i == 1 || i == -1) {
                //escape dot place + 1px from left and right
            } else {
                Col res = getColor(x + i, y, bi);
                xList.add((double) x + i);
                rList.add((double) res.r);
                gList.add((double) res.g);
                bList.add((double) res.b);
            }
        }

        SplineInterpolator interpolator = new SplineInterpolator();
        Double[] stockArrX = new Double[xList.size()];
        stockArrX = xList.toArray(stockArrX);
        double[] primitiveArrX = Arrays.stream(stockArrX).mapToDouble(Double::doubleValue).toArray();

        Double[] stockArrR = new Double[rList.size()];
        stockArrR = rList.toArray(stockArrR);
        double[] primitiveArrR = Arrays.stream(stockArrR).mapToDouble(Double::doubleValue).toArray();

        Double[] stockArrG = new Double[gList.size()];
        stockArrG = gList.toArray(stockArrG);
        double[] primitiveArrG = Arrays.stream(stockArrG).mapToDouble(Double::doubleValue).toArray();

        Double[] stockArrB = new Double[bList.size()];
        stockArrB = bList.toArray(stockArrB);
        double[] primitiveArrB = Arrays.stream(stockArrB).mapToDouble(Double::doubleValue).toArray();

        PolynomialSplineFunction pXR = interpolator.interpolate(primitiveArrX, primitiveArrR);
        PolynomialSplineFunction pXG = interpolator.interpolate(primitiveArrX, primitiveArrG);
        PolynomialSplineFunction pXB = interpolator.interpolate(primitiveArrX, primitiveArrB);

        return Arrays.asList(pXR, pXG, pXB);
    }


    /**
     * Calculate color mask from Integer value for selected point.
     *
     * @param x  coordinate
     * @param y  coordinate
     * @param bi image
     * @return {@link Col} structure with Red Green Blue representation.
     */
    private static Col getColor(int x, int y, BufferedImage bi) {
        int c = bi.getRGB(x, y);
        int red = (c & 0x00ff0000) >> 16;
        int green = (c & 0x0000ff00) >> 8;
        int blue = c & 0x000000ff;
        return new Col(red, green, blue);
    }

    /**
     * Color representation structure like Point3.
     * Just made for testing.
     * Need to refactor.
     */
    static class Col {
        final int r, g, b;

        Col(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }
}
