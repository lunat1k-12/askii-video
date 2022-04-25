package com.askii.video;

import com.askii.video.service.AsckiiConvertorService;
import com.askii.video.service.text.TextToGraphics;
import org.bytedeco.javacv.FrameGrabber;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;

public class AskiiMainApplication {

    public static void main(String[] args) throws FrameGrabber.Exception {
        drawFrame().startVideoProcessing();
//         new TextToGraphics().renderImage("Hello\nHere!");
    }

    public static AsckiiConvertorService drawFrame() {
        JFrame frame = new JFrame("Askii cam");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container cp = frame.getContentPane();
        cp.setBackground(Color.black);
        JTextPane pane = new JTextPane();
        pane.setBackground(Color.black);
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setForeground(attributeSet, Color.white);
        StyleConstants.setBackground(attributeSet, Color.black);
        StyleConstants.setFontFamily(attributeSet, "Monospaced");
        pane.setEditable(false);

        // Set the attributes before adding text
        pane.setCharacterAttributes(attributeSet, true);

        StyledDocument doc = pane.getStyledDocument();

        JScrollPane scrollPane = new JScrollPane(pane);
        cp.add(scrollPane, BorderLayout.CENTER);

        frame.setSize(1400, 1000);
        frame.setVisible(true);
        return new AsckiiConvertorService(doc, attributeSet, frame);
    }
}
