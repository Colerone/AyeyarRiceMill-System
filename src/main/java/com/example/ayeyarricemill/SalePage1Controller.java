package com.example.ayeyarricemill;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SalePage1Controller implements Initializable {
    @FXML
    private BorderPane borderPane; // HomePage မှ BorderPane fx:id

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try{
            Parent sideBar = FXMLLoader.load(getClass().getResource("/com/example/ayeyarricemill/sideBar1.fxml"));
            Parent naviBar = FXMLLoader.load(getClass().getResource("/com/example/ayeyarricemill/naviBar.fxml"));
            Parent contentArea = FXMLLoader.load(getClass().getResource("/com/example/ayeyarricemill/riceSaleList.fxml"));

            BorderPane innerPane = new BorderPane();
//            innerPane.setTop(naviBar);
//            innerPane.setCenter(contentArea);
            borderPane.setTop(naviBar);
            borderPane.setLeft(sideBar);
//            borderPane.setCenter(innerPane);
            borderPane.setCenter(contentArea);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
