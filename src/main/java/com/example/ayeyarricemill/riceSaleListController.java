package com.example.ayeyarricemill;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class riceSaleListController {
    @FXML
    private void add(javafx.scene.input.MouseEvent event){
        try{
            Node source =(Node) event.getSource();
            Scene scene = source.getScene();
            Stage stage = (Stage) scene.getWindow();

            Parent root = FXMLLoader.load(getClass().getResource("/com/example/ayeyarricemill/SalePage2.fxml"));
            scene.setRoot(root);
            stage.setMaximized(true);
            stage.show();
        }catch(Exception e){
            System.err.println("Error loading to vital scene: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
