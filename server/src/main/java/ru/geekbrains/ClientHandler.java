package ru.geekbrains;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;
    private boolean isAuth = false;

    public String getNick() {
        return nick;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            final long t = System.currentTimeMillis();
            new Thread(() -> {
                try {
                    while (true) {
                        if (System.currentTimeMillis() <= (t + 5000L)) {
                            String msg = in.readUTF();
                            if (msg.startsWith("/auth ")) {
                                // /auth login1 pass1
                                String[] tokens = msg.split(" ");
                                String nick = SQLHandler.getNickByLoginPass(tokens[1], tokens[2]);
                                if (nick != null) {
                                    if (server.isNickBusy(nick)) {
                                        out.writeUTF("Учетная запись уже используется");
                                        continue;
                                    }
                                    out.writeUTF("/authok " + nick);
                                    this.nick = nick;
                                    server.subscrible(this);
                                    isAuth = true;
                                    break;
                                } else {
                                    out.writeUTF("wrong login/password");
                                }
                            }
                        } else {
                            out.writeUTF("время авторизации истекло, перезапусти клиент");
                            socket.close();
                            out.close();
                            in.close();
                        }
                    }
                    while (isAuth) {
                        String msg = in.readUTF();
                        server.broadcastMsg(this, msg);
                        System.out.println(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscrible(this);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}