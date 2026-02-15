package com.example.ayeyarricemill;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;

import static javafx.scene.layout.AnchorPane.setLeftAnchor;
import static javafx.scene.layout.AnchorPane.setTopAnchor;

public class ManagerLogInController   implements Initializable {
    @FXML
    private AnchorPane AniLog; // Login ပထမ anchorPane၏ variable
    //    login page ကို responsive ညီအောင်လုပ်နိုင်ရန်
    @FXML private AnchorPane AniModal; // Login ဒုတိယ anchorPane၏ variable
    @FXML private TextField userField;
    @FXML private TextField emailField;
    @FXML private PasswordField passField;
    @FXML private TextField codeField;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (AniLog != null && AniModal != null) {
            AniLog.widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number oldNumber, Number newNumber) {
                    double newleftAnchor = (newNumber.doubleValue() - AniModal.getPrefWidth()) / 2.0;
                    setLeftAnchor(AniModal, newleftAnchor);
                }
            });

            AniLog.heightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number oldNumber, Number newNumber) {
                    double newTopAnchor = (newNumber.doubleValue() - AniModal.getPrefHeight()) / 2.0;
                    setTopAnchor(AniModal, newTopAnchor);
                }
            });

            setLeftAnchor(AniModal, (AniLog.getWidth() - AniModal.getPrefWidth()) / 2.0);
            setTopAnchor(AniModal, (AniLog.getHeight() - AniModal.getPrefHeight()) / 2.0);

        }
    }

    @FXML
    private void handleGenerateCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        codeField.setText(sb.toString());
    }

    @FXML
    private void handleConfirm() {
        // Validation: အကွက်လွတ်ရှိမရှိစစ်မယ်
        if (userField.getText().isEmpty() || passField.getText().isEmpty() || codeField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Form Error", "Please fill all required fields!");
            return;
        }

        // ပေးပို့မည့် Data ကို Map ထဲထည့်မယ်
        Map<String, String> managerData = new HashMap<>();
        managerData.put("username", userField.getText());
        managerData.put("email", emailField.getText());
        managerData.put("password", passField.getText());
        managerData.put("securityCode", codeField.getText());
        managerData.put("role", "MANAGER"); // Manager အကောင့်အဖြစ် သတ်မှတ်

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9090/api/users/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(managerData)))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200 && response.body().contains("Success")) {
                            showAlert(Alert.AlertType.INFORMATION, "Success", "Manager account created successfully!");
                            closeWindow();
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Registration Failed", response.body());
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Server Error", "Could not connect to server."));
                    return null;
                });
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) AniLog.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

