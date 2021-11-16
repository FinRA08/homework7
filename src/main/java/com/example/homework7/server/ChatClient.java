package com.example.homework7.server;

import com.example.homework7.server.Controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClient {//

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;


    public final Controller controller;

    public ChatClient(Controller controller){
        this.controller = controller;
        openConnection();
    }


    public void openConnection(){
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    while (true){
                        final String msgAuth = in.readUTF();
                        if(msgAuth.startsWith("/authok")){
                            final String[] split = msgAuth.split(" ");
                            final String[] nick = split;
                            controller.addMessage("Успешная авторизация под ником " + nick);
                            controller.setAuth(true);
                            break;
                        }
                    }
                    while (true){
                        final String message = in.readUTF();
                        if ("/end".equals(message)){
                            controller.setAuth(false);
                            break;
                        }
                        controller.addMessage(message);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }finally {
                    closeConnection();
                }
            }).start();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private  void closeConnection(){
        if (socket != null){
            try {
                socket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        if (in != null){
            try {
                out.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    public void sendMessage(String message){
        try {
            out.writeUTF(message);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
