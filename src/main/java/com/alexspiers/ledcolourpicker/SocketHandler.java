package com.alexspiers.ledcolourpicker;

import java.io.*;
import java.net.Socket;

class SocketHandler implements Runnable {
    private final ColourPicker colourPicker;
    private final DataOutputStream dos;
    private SerialManager serialManager;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private DataInputStream dis;
    private boolean isRunning = true;


    public SocketHandler(ColourPicker colourPicker, SerialManager serialManager, Socket socket) throws IOException {
        this.colourPicker = colourPicker;
        this.serialManager = serialManager;
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
        this.dis = new DataInputStream(inputStream);
        this.dos = new DataOutputStream(outputStream);
    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                String message = dis.readUTF();

                if("smooth".equals(message)){
                    colourPicker.smooth();
                } else {

                    int colour = Integer.parseInt(message);
                    int red = (colour >> 16) & 0xff;
                    int green = (colour >> 8) & 0xff;
                    int blue = (colour) & 0xff;

                    int colourSend = (red << 16) | (blue << 8) | green;

                    colourPicker.setColor(colour);
                    serialManager.writeString(colourSend + "\n");
                }
            }
        } catch (Exception e) {
            isRunning = false;
            e.printStackTrace();
        }
    }
}
