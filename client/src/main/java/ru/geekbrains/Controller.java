package ru.geekbrains;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable{
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean authorized;
    private String nick;

    @FXML
    TextField messageField;

    @FXML
    TextArea chatArea;

    @FXML
    TextField loginField;

    @FXML
    PasswordField passField;

    @FXML
    HBox msgPanel, authPanel;

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
        if(this.authorized) {
            authPanel.setVisible(false);
            authPanel.setManaged(false);
            msgPanel.setVisible(true);
            msgPanel.setManaged(true);
        }else {
            authPanel.setVisible(true);
            authPanel.setManaged(true);
            msgPanel.setVisible(false);
            msgPanel.setManaged(false);
            nick = "";
        }
    }

    public void initialize(URL location, ResourceBundle resources) {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            setAuthorized(false);
            Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        while (true) {
                            String str = in.readUTF();
                            // /authok nick1
                            if(str.startsWith("/authok ")) {
                                nick = str.split(" ")[1];
                                setAuthorized(true);
                                break;
                            }
                            chatArea.appendText(str);
                            chatArea.appendText("\n");
                        }
                        while (true) {
                            String str = in.readUTF();
                            chatArea.appendText(str);
                            chatArea.appendText("\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            t.setDaemon(true);
            t.start();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMsg () {
        try {
            out.writeUTF(messageField.getText());
            messageField.clear();
            messageField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendAuth(ActionEvent actionEvent) {
        try {
            out.writeUTF("/auth " + loginField.getText() + " " + passField.getText());
            loginField.clear();
            passField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
