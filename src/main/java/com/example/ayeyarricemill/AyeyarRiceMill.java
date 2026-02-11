package com.example.ayeyarricemill;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class AyeyarRiceMill extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AyeyarRiceMill.class.getResource("AniLogSign.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/example/ayeyarricemill/images/Logo3.png")));
        stage.setTitle("AyeyarRiceMill");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }
}
