package com.example.ayeyarricemill;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class TermsConditionController {

    public void backAction(javafx.event.ActionEvent event) throws IOException {
        Node source = (Node) event.getSource();
        Scene scene = source.getScene();
        Stage stage = (Stage) scene.getWindow();

        // SettingPage.fxml ကို load လုပ်မယ်
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/ayeyarricemill/SettingPage.fxml"));

        scene.setRoot(root);
        stage.setMaximized(true);
        stage.show();
    }
}
