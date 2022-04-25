package com.askii.video.service;

import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.CvMat;
import org.bytedeco.opencv.opencv_core.CvScalar;
import org.bytedeco.opencv.opencv_core.IplImage;

import javax.swing.JFrame;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

import static org.bytedeco.opencv.global.opencv_core.cvGet2D;
import static org.bytedeco.opencv.global.opencv_core.cvGetMat;
import static org.opencv.core.CvType.CV_32FC1;

public class AsckiiConvertorService {

    public static final String DENSITY = "Ñ@#W$9876543210?!abc;:+=-,._                      ";

    public final StyledDocument doc;
    public final SimpleAttributeSet attributeSet;
    public final JFrame frame;

    public AsckiiConvertorService(StyledDocument doc,
                                  SimpleAttributeSet attributeSet,
                                  JFrame frame) {
        this.doc = doc;
        this.attributeSet = attributeSet;
        this.frame = frame;
    }

    public void startVideoProcessing() throws FrameGrabber.Exception {
        // 0-default camera, 1 - next...so on
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

                    convertAndPrintImage(resizedImage);
                }
                Thread.sleep(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void convertAndPrintImage(IplImage image) {
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

    public double map(double num, int in_min, int in_max, int out_min, int out_max) {
        return Math.floor((num - in_min) * (out_max - out_min) / (in_max - in_min) + out_min);
    }
}
