package com.skaliy.linkmanager.server.fxapp;

import com.skaliy.linkmanager.server.connection.DBConnectionFile;
import com.skaliy.linkmanager.server.server.Server;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

public class Controller {

    @FXML
    private TextArea textAreaLogs;

    @FXML
    private Label labelStatus;

    @FXML
    private Button buttonStart;

    public void initialize() {
        final Server[] server = {null};
        final Thread[] thread = {null};

        buttonStart.setOnAction(event -> {

            DBConnectionFile file = new DBConnectionFile("db.txt");
            BufferedReader dataConnection;

            try {
                dataConnection = file.read();
            } catch (FileNotFoundException e) {
                textAreaLogs.appendText("Файл с параметрами подключения к БД не существует!\n" +
                        "Создайте файл \"server.txt\" со значениями host, user, password.");
                return;
            }

            if (server[0] == null) {
                try {
                    server[0] = new Server(
                            7777,
                            dataConnection.readLine(),
                            dataConnection.readLine(),
                            dataConnection.readLine());
                } catch (IOException | SQLException | ClassNotFoundException e) {
                    textAreaLogs.appendText("Упс! Что-то пошло не так.\n"
                            + "Проверьте параметы подключения к БД в файле \"server.txt\"!\n");
                    server[0] = null;
                    return;
                }

                server[0].setTextAreaLogs(textAreaLogs);

                thread[0] = new Thread(server[0]);
                thread[0].start();

                while (true) {
                    if (server[0].getDb().isConnected())
                        break;
                    else try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                labelStatus.setText("Подключение установлено!");
                buttonStart.setText("Отключить");
                textAreaLogs.appendText("[SERVER] - start\n");

            } else {
                server[0].getDb().closeConnection();
                server[0] = null;
                thread[0].stop();
                thread[0] = null;
                buttonStart.setText("Запустить");
                labelStatus.setText("Соединение закрыто!");
                textAreaLogs.appendText("[SERVER] - shutdown\n");
            }

        });

        Main.stage.setOnCloseRequest(event -> {
            if (server[0] != null) {
                server[0].getDb().closeConnection();
                server[0] = null;
            }
            if (thread[0] != null) {
                thread[0].stop();
                thread[0] = null;
            }
        });
    }

}