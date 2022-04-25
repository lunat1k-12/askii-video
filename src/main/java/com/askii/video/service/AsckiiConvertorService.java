package com.askii.video.service;

import com.askii.video.service.state.AppState;
import com.askii.video.service.text.TextToGraphics;
import com.askii.video.service.video.VideoRecorderService;
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

import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.bytedeco.opencv.global.opencv_core.cvGet2D;
import static org.bytedeco.opencv.global.opencv_core.cvGetMat;
import static org.opencv.core.CvType.CV_32FC1;

public class AsckiiConvertorService {

    public static final String DENSITY = "Ñ@#W$9876543210?!abc;:+=-,._                      ";

    private final StyledDocument doc;
    private final SimpleAttributeSet attributeSet;
    private final JFrame frame;
    private final TextToGraphics textToGraphics = new TextToGraphics();
    private final VideoRecorderService videoService = new VideoRecorderService();

    public AsckiiConvertorService(StyledDocument doc,
                                  SimpleAttributeSet attributeSet,
                                  JFrame frame) {
        this.doc = doc;
        this.attributeSet = attributeSet;
        this.frame = frame;
    }

    public void startVideoProcessing() throws IOException, InterruptedException {
        // 0-default camera, 1 - next...so on
        final OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.setImageHeight(480);
        grabber.setImageWidth(640);
        grabber.setFrameNumber(5);
        videoService.initRecorder("test_video.mp4", "mp4", 20);

        int index = 0;
        try {
            grabber.start();
            OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
            while (AppState.isIsRunning()) {
                IplImage img = converter.convert(grabber.grab());
                if (img != null) {
                    IplImage resizedImage = IplImage.create(170, 60,
                            img.depth(), img.nChannels());
                    opencv_imgproc.cvResize(img, resizedImage);

                    convertAndPrintImage(resizedImage, index);
                }
                index++;
                Thread.sleep(10);
            }
            videoService.endRecord();
            System.out.println("Exit application");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void convertAndPrintImage(IplImage image, int globalIndex) {
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
            BufferedImage buffImg = textToGraphics.renderImage(sb.toString());
            videoService.addImage(buffImg, globalIndex);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public double map(double num, int in_min, int in_max, int out_min, int out_max) {
        return Math.floor((num - in_min) * (out_max - out_min) / (in_max - in_min) + out_min);
    }
}
