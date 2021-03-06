package com.example.homework7.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private final Socket socket;
    private final ChatServer server;
    private final DataInputStream in;
    private final DataOutputStream out;
    private String nick;


    public ClientHandler(Socket socket, ChatServer server) {
        try {
            this.nick = "";
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    authenticate();
                    readMessages();
                }finally {
                    closeConnection();
                }

            }).start();

        }catch (IOException e){
            throw  new RuntimeException(e);
        }

    }

    private void closeConnection() {
        try {
            if (in != null)
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null)
                out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (socket != null){
                server.unsubscribe(this);
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void authenticate() {
        while (true){
            try {
                final String str = in.readUTF();// "/auth login0 pass0"
                if (str.startsWith("/auth")){
                    final String[] split = str.split(" ");
                    final String login = split[1];
                    final String password = split[2];
                    final String nick = server.getAuthService().getNickByLoginAndPassword(login, password);
                    if (nick != null){
                        if (server.isNickBusy(nick)){
                            sendMessage("???????????????????????? ?????? ??????????????????????");
                            continue;
                        }
                        sendMessage("/author" + nick);
                        this.nick = nick;
                        server.broadcast("???????????????????????? " + nick + "?????????? ?? ??????");
                        server.subscribe(this);
                        break;
                    }else {
                        sendMessage("???????????????? ?????????? ?? ????????????");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        try {
            System.out.println("SERVER: Send message to " + nick);
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessages() {
        try {
            while (true){
                final String msg = in.readUTF();
                if ("/end".equals(msg)){
                    break;
                }
                sendMessage(nick + ": " + msg);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }
}
