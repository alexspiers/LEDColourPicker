package com.alexspiers.ledcolourpicker;

import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.List;

public class SocketManager implements Runnable{
    private final ColourPicker colourPicker;
    private ServerSocket serverSocket;
    private SerialManager serialManager;
    private Color color;
    private List<SocketHandler> handlers = new ArrayList<>();

    public SocketManager(ColourPicker colourPicker, SerialManager serialManager){
        this.colourPicker = colourPicker;
        this.serialManager = serialManager;

        try {
            serverSocket = new ServerSocket(25522);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while(true){
                Socket socket = this.serverSocket.accept();
                SocketHandler handler = new SocketHandler(colourPicker, serialManager, socket);
                Thread handlerThread = new Thread(handler);
                handlerThread.start();
                handlers.add(handler);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
