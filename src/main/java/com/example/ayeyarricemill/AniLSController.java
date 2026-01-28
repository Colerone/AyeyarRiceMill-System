package com.example.ayeyarricemill;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static javafx.scene.layout.AnchorPane.setLeftAnchor;
import static javafx.scene.layout.AnchorPane.setTopAnchor;

public class AniLSController implements Initializable {
    @FXML private VBox VBox_Blue; // Log in ကိုသွားချင်တဲ့ VBox
    @FXML private VBox SignUpContainer; // Sign up form box
    @FXML private Label LabelSignUp; // SignUpLabel အတွက်
    @FXML private Label smallLabel;
    @FXML private Label userLabel;
    @FXML private TextField userTextField;
    @FXML private Label roleLabel;
    @FXML private TextField roleField;
    @FXML private FontAwesomeIconView roleIcon;
    @FXML private AnchorPane AniLog; // Login ပထမ anchorPane၏ variable
    //    login page ကို responsive ညီအောင်လုပ်နိုင်ရန်
    @FXML private AnchorPane AniModal; // Login ဒုတိယ anchorPane၏ variable
    //box အပြာ၏ text များ
    @FXML private Label WelLabel1; //
    @FXML private Label WelLabel2;
    @FXML private Button WelLabel3;
    @FXML private Line line1;
    @FXML private Label ForgotPassLabel;
    @FXML private FontAwesomeIconView emailIcon;
    @FXML private Label userEmailLabel;
    @FXML private TextField userEmailField;
    @FXML private PasswordField passField;
    @FXML private Button SignUPButton;


    // API သုံးခြင်းအတွက်လိုအပ်သောအရာများ
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final String BASE_URL = "http://localhost:9090/api/users/register";
    private final String BASE_URL1 = "http://localhost:9090/api/users/login";
//private final String BASE_URL = "http://127.0.0.1:9090/api/users";
    // သွားပေါ်ချင်တဲ့ နေရာရဲ့ Width
    private static final double Panel_Width1 = 525;
    private static final double Panel_Width2 = 336;

    // စစချင်း ညာဘက်မှာပေါ်မယ်
    private boolean isOverLayOnRight = true;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        WelLabel1.setText("Welcome, friend");
        WelLabel2.setText("you have already account?");
        WelLabel3.setText("SIGN IN");
        LabelSignUp.setText("Sign Up to Ayeyar Rice Mill");

        Platform.runLater(()->{
            SignUPButton.getScene().setOnKeyPressed(event->{
                switch (event.getCode()){
                    case ENTER:
                        SignUPButton.arm();
                        SignUPButton.fire();
                        handleAuthAction();
                        break;
                    default:
                        break;
                }
            });
        });

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
        //Sign in/Sign up အတွက် Action သတ်မှတ်ခြင်း
        SignUPButton.setOnAction(event -> handleAuthAction());
    }


    // Register သို့မဟုတ် Login ခေါ်ယူခြင်း
    private void handleAuthAction() {
        if(isOverLayOnRight){
            registerUser();
        }else{
            loginUser();
        }
    }

    private void registerUser() {
        Map<String,String > data = new HashMap<>();
        data.put("username", userTextField.getText());
        data.put("email", userEmailField.getText());
        data.put("password", passField.getText());
        data.put("role", roleField.getText().toUpperCase());

        sendRequest(BASE_URL,  data , "Registration");
    }

    private void loginUser() {
        Map<String, String > data = new HashMap<>();
        data.put("username" , userTextField.getText());
        data.put("password", passField.getText());

        sendRequest(BASE_URL1 , data, "Login");
    }

    private void sendRequest(String url, Map<String, String> data, String type) {
        String json = gson.toJson(data);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response ->{
                    Platform.runLater(()->{
                        if(response.statusCode() == 200){
                            System.out.println(type + "Successful" + response.body());

                            if(type.equals("Login")){
                                // login success ဆို json မှ role ကို ယူပြီး dashboard ကိုသွား
                                JsonObject userJson = gson.fromJson(response.body(), JsonObject.class);
                                String role = userJson.get("role").getAsString();

                                //Login ဝင်ထားတဲ့သူကို username နဲ့ ယူထားမယ်
                                String userName = userJson.get("username").getAsString();
                                RawPriceController.loggedInUsername = userName;
                                GoodPriceController.loggedInUsername = userName;
                                naviBarController.loggedInUsername = userName;
                                naviBarController.loggedInRole = role;
                                InventoryAddController.loggedInUserRole = role;
                                PadPurchaseS1Controller.loggedInUsername = userName;
                                sideBar1Controller.currentUserRole = role;
                                MillingRegisterController.loggedInUsername = userName;
                                riceSaleRegisterController.loggedInUsername = userName;
                                navigateToDashboard(role);
                            }else{
                                // Registration ဆိုလျှင် Alert ပြပြီး Login သို့ပြောင်းရန် တိုက်တွန်းမည်
                                showAlert(Alert.AlertType.INFORMATION, "Registration Success", "Your registration successful.Please Log in again");
                                handleSlideAction(null);
                                clearFields();
                            }
                        }else{
                            System.err.println(type + " Failed: " + response.body());
                            showAlert(Alert.AlertType.ERROR, type + " Error", response.body());
                        }
                    });
                })
                .exceptionally(ex->{
                    Platform.runLater(()->showAlert(Alert.AlertType.ERROR, "Network Error", "cannot connect to backend"));
                    return null;
                });
    }

    private void clearFields() {
        userEmailField.clear();
        userTextField.clear();
        passField.clear();
        roleField.clear();
    }

    private void navigateToDashboard(String role) {
        try{
            String fxmlFile = "";
            String title = "";

            if(role.equalsIgnoreCase("OWNER")){
                fxmlFile = "MainDashboard.fxml";
                title = "OwnerDashBoard - Ayeyar Rice Mill";
            }else{
                fxmlFile = "HomePage.fxml";
                title = "ManagerDashBoard - Ayeyar Rice Mill";
            }

//            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
//            Parent root = loader.load();
//            Stage stage = (Stage) SignUPButton.getScene().getWindow();
//            stage.setTitle(title);
//
//            stage.setScene(new Scene(root));
//            stage.setMaximized(true);
//            stage.show();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) SignUPButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

        }catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "couldn't find dashboard file");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void handleSlideAction(ActionEvent event) {
//        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.2), VBox_Blue);
        clearFields();
        Duration duration = new Duration(1000);
        // VBox_Blue Animation
        TranslateTransition transition1 = new TranslateTransition(duration, VBox_Blue);

        // SignUpContainer Animation
        TranslateTransition transition2 = new TranslateTransition(duration, SignUpContainer);

        if (isOverLayOnRight) {
            transition1.setToX(-Panel_Width1);
            transition2.setToX(Panel_Width2);

            transition1.setOnFinished((e -> {
                WelLabel1.setText("Hello, Friend");
                WelLabel2.setText("If you don't have account, create one");
                WelLabel3.setText("SIGN UP");
                isOverLayOnRight = false;
            }));

            transition2.setOnFinished((e -> {
                LabelSignUp.setText("Sign in to your account");
                LabelSignUp.setTextFill(Color.web("#3e6a8d"));
                userTextField.setPromptText("abcdef...");
                SignUPButton.setText("SIGN IN");
                userEmailLabel.setVisible(false);
                userEmailLabel.setManaged(false);
                userEmailField.setManaged(false);
                userEmailField.setVisible(false);
                emailIcon.setVisible(false);
                emailIcon.setManaged(false);
                smallLabel.setVisible(false);
                smallLabel.setManaged(false);
                roleLabel.setVisible(false);
                roleLabel.setManaged(false);
                roleField.setVisible(false);
                roleField.setManaged(false);
                roleIcon.setVisible(false);
                roleIcon.setManaged(false);
                ForgotPassLabel.setVisible(true);
                ForgotPassLabel.setManaged(true);
                line1.setVisible(true);
                line1.setManaged(true);
                isOverLayOnRight = false;
            }));
        } else {
            // မူလနေရာပြန်ပို့မယ်
            transition1.setToX(0);
            transition2.setToX(0);
            transition1.setOnFinished((e -> {
                WelLabel1.setText("Welcome, friend");
                WelLabel2.setText("you have already account?");
                WelLabel3.setText("SIGN IN");
                isOverLayOnRight = true;
            }));

            transition2.setOnFinished((e -> {
                LabelSignUp.setText("Sign Up to Ayeyar Rice Mill");
                LabelSignUp.setTextFill(Color.BLACK);
                LabelSignUp.setStyle("-fx-font-size: 30; -fx-font-weight: bold; -fx-font-family: Arial Black");
                SignUPButton.setText("SIGN UP");
                userEmailLabel.setVisible(true);
                userEmailLabel.setManaged(true);
                userEmailField.setManaged(true);
                userEmailField.setVisible(true);
                emailIcon.setVisible(true);
                emailIcon.setManaged(true);
                userTextField.setPromptText("HninHsuLwinKyaw");
                ForgotPassLabel.setVisible(false);
                ForgotPassLabel.setManaged(false);
                line1.setVisible(false);
                line1.setManaged(false);
                smallLabel.setVisible(true);
                smallLabel.setManaged(true);
                roleLabel.setVisible(true);
                roleLabel.setManaged(true);
                roleField.setVisible(true);
                roleField.setManaged(true);
                roleIcon.setVisible(true);
                roleIcon.setManaged(true);
            }));
        }

//        All transition အချုပ်
        ParallelTransition parallelTransition = new ParallelTransition(transition1, transition2);
        parallelTransition.play();


    }
}