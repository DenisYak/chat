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
    private boolean isAuthorized = false;
    private boolean timeIsOut = false;

    private final long timeOut = 5000L;

    public String getNick() {
        return nick;
    }

    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        final long t = System.currentTimeMillis();
        new Thread(() -> {
            while (true) {
                if (System.currentTimeMillis() >= (t + timeOut)) {
                    try {
                        out.writeUTF("время авторизации истекло, перезапустите клиент");
                        System.out.println("время авторизации истекло, перезапустите клиент");
                        timeIsOut = true;
                        socket.close();
                        out.close();
                        in.close();
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }

        }).start();
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    while (!timeIsOut) {
                        System.out.println("ништяк");
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
                                ClientHandler.this.nick = nick;
                                server.subscribe(ClientHandler.this);
                                isAuthorized = true;
                                break;
                            } else {
                                out.writeUTF("wrong login/password");
                            }
                        }
                    }
                    while (isAuthorized) {
                        String msg = in.readUTF();
                        server.broadcastMsg(ClientHandler.this, msg);
                        System.out.println(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(ClientHandler.this);
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