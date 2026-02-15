package com.example.ayeyarricemill;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.ResourceBundle;

public class ManagerListController implements Initializable {

    @FXML private TableView<User> managerTable;
    @FXML private TableColumn<User, String> col_no;
    @FXML private TableColumn<User, String> col_username;
    @FXML private TableColumn<User, String> col_email;
    @FXML private TableColumn<User, String> col_password;
    @FXML private TableColumn<User, String> col_code;
    @FXML private TableColumn<User, Void> col_action;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private ObservableList<User> managerList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadManagerData();
    }

    private void setupTable() {
        col_username.setCellValueFactory(new PropertyValueFactory<>("username"));
        col_email.setCellValueFactory(new PropertyValueFactory<>("email"));

        col_password.setCellValueFactory(new PropertyValueFactory<>("password"));
        col_code.setCellValueFactory(new PropertyValueFactory<>("securityCode"));

        // Password Column ကို Eye Icon နဲ့ ပြောင်းလဲခြင်း
        setupSecureColumn(col_password);

        // Security Code Column ကို Eye Icon နဲ့ ပြောင်းလဲခြင်း
        setupSecureColumn(col_code);

        // အစဉ်လိုက် နံပါတ်ပြရန် (No Column)
        col_no.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        // Action Column တွင် Ban Button ထည့်ခြင်း
        addActionButtons();
    }



    private void addActionButtons() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Ban");
                    {
                        btn.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");
                        btn.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            handleBanManager(user);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
            }
        };
        col_action.setCellFactory(cellFactory);
    }



    private void loadManagerData() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9090/api/users/all"))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        List<User> users = gson.fromJson(response.body(), new TypeToken<List<User>>(){}.getType());
                        managerList.setAll(users);
                        managerTable.setItems(managerList);
                    }
                });
    }

    @FXML
    private void handleAddNewManager() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ManagerLogin.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("+Add New Manager");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/example/ayeyarricemill/images/Logo3.png")));
            stage.initModality(Modality.APPLICATION_MODAL); // Form ပေါ်နေတုန်း နောက်က Table ကို နှိပ်လို့မရအောင်
            stage.showAndWait(); // Form ပိတ်သွားတဲ့အထိ စောင့်မယ်

            // Form ပိတ်သွားရင် Table ကို refresh ပြန်လုပ်တဲ့ function ခေါ်မယ်
             loadManagerData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleBanManager(User user) {
        // ဒီနေရာမှာ Backend က Delete API ကို လှမ်းခေါ်တဲ့ logic ရေးလို့ရပါတယ်
        System.out.println("Banning Manager: " + user.getUsername());
        // အခုလောလောဆယ် list ထဲကပဲ ဖြုတ်ပြထားမယ်
//        managerList.remove(user);
    }


    private void setupSecureColumn(TableColumn<User, String> column) {
        column.setCellFactory(col -> new TableCell<>() {
            private final FontAwesomeIconView eyeIcon = new FontAwesomeIconView(FontAwesomeIcon.EYE_SLASH);
            private final Label textLabel = new Label("********");
            private final HBox container = new HBox(10, eyeIcon, textLabel);
            private boolean isRevealed = false;

            {
                container.setAlignment(Pos.CENTER_LEFT);
                eyeIcon.setStyle("-fx-cursor: hand;");
                eyeIcon.setSize("15");

                eyeIcon.setOnMouseClicked(event -> {
                    isRevealed = !isRevealed;
                    if (isRevealed) {
                        eyeIcon.setIcon(FontAwesomeIcon.EYE);
                        textLabel.setText(getItem()); // Data အမှန်ကိုပြမယ်
                    } else {
                        eyeIcon.setIcon(FontAwesomeIcon.EYE_SLASH);
                        textLabel.setText("********"); // ပြန်ဖုံးမယ်
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    // Cell ကို refresh လုပ်တိုင်း မူလအတိုင်း ပြန်ဖုံးထားမယ်
                    if (!isRevealed) textLabel.setText("********");
                    setGraphic(container);
                }
            }
        });
    }



    public static class User {
        private String username;
        private String email;
        private String password;
        private String securityCode;

        // Getters
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public String getSecurityCode() { return securityCode; }

        // Setters (လိုအပ်လျှင်)
        public void setUsername(String username) { this.username = username; }
        public void setEmail(String email) { this.email = email; }
        public void setPassword(String password) { this.password = password; }
        public void setSecurityCode(String securityCode) { this.securityCode = securityCode; }
    }
}
