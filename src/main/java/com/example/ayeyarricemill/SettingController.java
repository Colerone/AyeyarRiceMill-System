package com.example.ayeyarricemill;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SettingController {

    @FXML
    private void projectInfo(javafx.scene.input.MouseEvent event){
        try{
            Node source =(Node) event.getSource();
            Scene scene = source.getScene();
            Stage stage = (Stage) scene.getWindow();

            Parent root = FXMLLoader.load(getClass().getResource("/com/example/ayeyarricemill/projectInfoPage.fxml"));
            scene.setRoot(root);
            stage.setMaximized(true);
            stage.show();
        }catch(Exception e){
            System.err.println("Error loading to vital scene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void termsConditions(javafx.scene.input.MouseEvent event){
        try{
            Node source =(Node) event.getSource();
            Scene scene = source.getScene();
            Stage stage = (Stage) scene.getWindow();

            Parent root = FXMLLoader.load(getClass().getResource("/com/example/ayeyarricemill/TeamRulePage.fxml"));
            scene.setRoot(root);
            stage.setMaximized(true);
            stage.show();
        }catch(Exception e){
            System.err.println("Error loading to vital scene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void aboutUs(javafx.scene.input.MouseEvent event){
        try{
            Node source =(Node) event.getSource();
            Scene scene = source.getScene();
            Stage stage = (Stage) scene.getWindow();

            Parent root = FXMLLoader.load(getClass().getResource("/com/example/ayeyarricemill/AboutPage.fxml"));
            scene.setRoot(root);
            stage.setMaximized(true);
            stage.show();
        }catch(Exception e){
            System.err.println("Error loading to vital scene: " + e.getMessage());
            e.printStackTrace();
        }
    }


}