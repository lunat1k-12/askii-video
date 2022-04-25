package com.askii.video;

import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.CvMat;
import org.bytedeco.opencv.opencv_core.CvScalar;
import org.bytedeco.opencv.opencv_core.IplImage;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.opencv.core.CvType.CV_32FC1;

public class AskiiMainApplication {

    public static JFrame frame;
    public static StyledDocument doc;
    public static SimpleAttributeSet attributeSet;
    public static final String DENSITY = "Ñ@#W$9876543210?!abc;:+=-,._                      ";

    public static void main(String[] args) throws FrameGrabber.Exception {
        captureFrame();
    }

    public static void drawFrame() {
        frame = new JFrame("Askii cam");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container cp = frame.getContentPane();
        cp.setBackground(Color.black);
        JTextPane pane = new JTextPane();
        pane.setBackground(Color.black);
        attributeSet = new SimpleAttributeSet();
        StyleConstants.setBold(attributeSet, true);
        pane.setEditable(false);

        // Set the attributes before adding text
        pane.setCharacterAttributes(attributeSet, true);

        attributeSet = new SimpleAttributeSet();
        StyleConstants.setItalic(attributeSet, true);
        StyleConstants.setForeground(attributeSet, Color.white);
        StyleConstants.setBackground(attributeSet, Color.black);
        StyleConstants.setFontFamily(attributeSet, "Monospaced");

        doc = pane.getStyledDocument();

        JScrollPane scrollPane = new JScrollPane(pane);
        cp.add(scrollPane, BorderLayout.CENTER);

        frame.setSize(1400, 1000);
        frame.setVisible(true);
    }

    private static void captureFrame() throws FrameGrabber.Exception {
        // 0-default camera, 1 - next...so on
        drawFrame();
        final OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.setImageHeight(480);
        grabber.setImageWidth(640);
        grabber.setFrameNumber(5);

        try {
            grabber.start();
            OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
            while (frame.isVisible()) {
                IplImage img = converter.convert(grabber.grab());
                if (img != null) {
                    IplImage resizedImage = IplImage.create(170, 60,
                            img.depth(), img.nChannels());
                    opencv_imgproc.cvResize(img, resizedImage);

                    convertAndSaveImage(resizedImage);
                }
                Thread.sleep(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void convertAndSaveImage(IplImage image) {
        double r, g, b;

        CvMat mtx = CvMat.createHeader(image.height(), image.width(), CV_32FC1);
        cvGetMat(image, mtx);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mtx.rows(); i++)
        {
            for (int j = 0; j < mtx.cols(); j++)
            {
                CvScalar rgb = cvGet2D(mtx, i, j);
                r = rgb.red();
                g = rgb.green();
                b = rgb.blue();

                double gray = (r + g + b) / 3;
                int index = Double.valueOf(map(gray, 0, 255, 0, DENSITY.length() - 1)).intValue();
                sb.append(DENSITY.charAt(index));
            }
            sb.append('\n');
        }
        try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, sb.toString(), attributeSet);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public static double map(double num, int in_min, int in_max, int out_min, int out_max) {
        return Math.floor((num - in_min) * (out_max - out_min) / (in_max - in_min) + out_min);
    }
}
