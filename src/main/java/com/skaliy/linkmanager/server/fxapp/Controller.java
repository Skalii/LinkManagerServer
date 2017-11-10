package com.skaliy.linkmanager.server.fxapp;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;

import com.skaliy.linkmanager.server.server.Server;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class Controller {

    @FXML
    private JFXTextArea textAreaLogs;

    @FXML
    private Label labelStatus;

    @FXML
    private JFXButton buttonStart;

    public void initialize() {
        final Server[] server = {null};
        final Thread[] thread = {null};

        buttonStart.setOnAction(event -> {

            if (server[0] == null) {
                server[0] = new Server(
                        7777,
                        "ec2-54-75-248-193.eu-west-1.compute.amazonaws.com:5432/dfo34hv66rtq0v?sslmode=require",
                        "czzkavntolnnaj",
                        "e09ee81b37a589eec74e93ae409b80922decedcd270be6c80ab313c19276ac4f");

                server[0].setTextAreaLogs(textAreaLogs);

                thread[0] = new Thread(server[0]);
                thread[0].start();


                if (server[0].getDb().isConnected()) {
                    labelStatus.setText("Подключение установлено!");
                    buttonStart.setText("Отключить");
                    textAreaLogs.appendText("[SERVER] - start\n");
                } else {
                    textAreaLogs.appendText("Упс! Возникла проблема.\n");
                }

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
            if (server[0] == null && thread[0] == null) {
                textAreaLogs.appendText("[SERVER] - shutdown\n");
            } else textAreaLogs.appendText("[SERVER] - did not shutdown\n");
        });
    }

}
