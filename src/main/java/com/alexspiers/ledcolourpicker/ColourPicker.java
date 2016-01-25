package com.alexspiers.ledcolourpicker;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.*;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class ColourPicker extends JPanel implements ChangeListener, SerialPortEventListener {

    private SerialManager serialManager;
    private SocketManager socketManager;

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        String text;
        try {
            while((text = serialManager.getInputStream().readLine()) != null) {
                System.out.println("Arduino:" + text);
            }
        } catch( IOException e ) {
            e.printStackTrace();
        }
    }

    protected JColorChooser tcc;

    public ColourPicker() {
        super(new BorderLayout());

        //Setup Serial manager
        serialManager = new SerialManager(this);

        //Setup Socket manager
        socketManager = new SocketManager(this, serialManager);
        Thread socketThread = new Thread(socketManager);
        socketThread.start();

        //Set up color chooser for setting text color
        tcc = new JColorChooser();
        tcc.setColor(new Color(50, 200, 100));
        tcc.getSelectionModel().addChangeListener(this);
        tcc.setBorder(BorderFactory.createTitledBorder("Choose Text Color"));

        add(tcc, BorderLayout.PAGE_END);
    }

    public void stateChanged(ChangeEvent e) {
        Color newColor = tcc.getColor();
        int red  = newColor.getRed();
        int green  = newColor.getGreen();
        int blue  = newColor.getBlue();
        int sendColor = (red << 16) | (blue << 8) | green;

        serialManager.writeString(sendColor + "\n");
    }

    public void setColor(int color){
        tcc.setColor(color);
    }

    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("ColorChooserDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new ColourPicker();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    public void smooth() {
        playSound();

        Color color = tcc.getColor();

        double stepAmount = 100;

        double red = color.getRed();
        double green = color.getGreen();
        double blue = color.getBlue();

        int redTarget = 30;
        int blueTarget = 5;
        int greenTarget = 0;

        double redDiff = red - redTarget;
        double greenDiff = green - greenTarget;
        double blueDiff = blue - blueTarget;

        double redScale = redDiff / (stepAmount * 2);
        double greenScale = greenDiff / (stepAmount);
        double blueScale = blueDiff / (stepAmount * 1.5);

        boolean colorSet = false;

        while(!colorSet) {
            if (!intervalContains(redTarget - 1, redTarget + 1, red)){
                red -= redScale;
            }
            if (!intervalContains(greenTarget - 1, greenTarget + 1, green)){
                green -= greenScale;
            }

            if (!intervalContains(blueTarget - 1, blueTarget + 1, blue)) {
                blue -= blueScale;
            }

            if(intervalContains(redTarget - 1, redTarget + 1, red)
                    && intervalContains(greenTarget - 1, greenTarget + 1, green)
                    && intervalContains(blueTarget - 1, blueTarget + 1, blue)) {
                colorSet = true;
            }

            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int sendColor = ((int) red << 16) | ((int) blue << 8) | (int) green;
            serialManager.writeString(sendColor + "\n");
            tcc.setColor(redTarget, greenTarget, blueTarget);
        }
    }

    static boolean intervalContains(double low, double high, double num) {
        return num >= low && num <= high;
    }

    public static synchronized void playSound() {
        new Thread(new Runnable() {
            // The wrapper thread is unnecessary, unless it blocks on the
            // Clip finishing; see comments.
            public void run() {
                try {
                    File audioFile = new File("/home/alex/HDD/Projects/LEDColourPicker/src/main/dist/output.wav");
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                    AudioFormat format = audioStream.getFormat();
                    DataLine.Info info = new DataLine.Info(Clip.class, format);
                    Clip audioClip = (Clip) AudioSystem.getLine(info);
                    audioClip.open(audioStream);
                    audioClip.start();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }).start();
    }
}
