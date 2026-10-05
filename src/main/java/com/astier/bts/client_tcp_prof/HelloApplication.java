/*
 * Copyright (c) 2026 Michaël Moser.
 *
 * Auteur : Michaël Moser
 * Tous droits réservés.
 */

//--enable-native-access=javafx.graphics
package com.astier.bts.client_tcp_prof;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("hello-view.fxml"));

        stage.setOnCloseRequest(event -> {
            try {
                if (HelloController.enRun) {
                    HelloController.tcp.deconnection();
                }
                System.exit(0);
            } catch (Exception ignoreg) {
            }
        });

        stage.setTitle("TCP-Client  MM");
        stage.getIcons().add(new Image("/icone/index.jpg"));
        stage.setResizable(false);
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }
}