package com.example.ayeyarricemill;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

import static javafx.scene.layout.AnchorPane.setLeftAnchor;
import static javafx.scene.layout.AnchorPane.setTopAnchor;

public class RawPriceController {
    @FXML private AnchorPane MPRoot;
    @FXML private AnchorPane MPModal;
    @FXML private TableView<PaddyPrice> priceTable;
    @FXML private TableColumn<PaddyPrice, String> colNo;
    @FXML private TableColumn<PaddyPrice, String> colType;
    @FXML private TableColumn<PaddyPrice, Double> colPrice;
    @FXML private TableColumn<PaddyPrice, String> colCreated;
    @FXML private TableColumn<PaddyPrice, String> colCreatedBy;
    @FXML private TableColumn<PaddyPrice, String> colUpdated;
    @FXML private TableColumn<PaddyPrice, String> colUpdatedBy;
    @FXML private TableColumn<PaddyPrice, Void> colAction;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();



    // Backend API URL (src/test.http ရှိ path အတိုင်း ပြင်ဆင်ထားသည်)
    private final String BASE_URL = "http://localhost:9090/api/paddy_price";
    private final ObservableList<PaddyPrice> priceList = FXCollections.observableArrayList();
    public static String loggedInUsername = "Unknown User";

    @FXML
    public void initialize() {
        if(MPRoot != null && MPModal != null){
            MPRoot.widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number oldNumber, Number newNumber) {
                    double newleftAnchor = (newNumber.doubleValue() - MPModal.getPrefWidth())/ 2.0;
                    setLeftAnchor(MPModal,newleftAnchor);
                }
            });

            MPRoot.heightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number oldNumber, Number newNumber) {
                    double newTopAnchor = (newNumber.doubleValue() - MPModal.getPrefHeight()) / 2.0;
                    setTopAnchor(MPModal, newTopAnchor);
                }
            });

            setLeftAnchor(MPModal, (MPRoot.getWidth() - MPModal.getPrefWidth() ) / 2.0);
            setTopAnchor(MPModal, (MPRoot.getHeight() - MPModal.getPrefHeight()) /2.0);

        }
        setupTable();
        loadDataFromBackend();

    }


    private void setupTable() {
        // Table နံပါတ်စဉ်အတွက်
        colNo.setCellFactory(column-> new TableCell<>(){
            @Override
            protected void updateItem(String item, boolean empty){
                super.updateItem(item, empty);
                if(empty){
                    setText(null);
                }else{
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        colType.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPaddyType()));
        colPrice.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getPrice()));
        colCreatedBy.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCreatedBy()));
        colCreated.setCellValueFactory(cell -> formatTimestamp(cell.getValue().getCreatedDate()));
        colUpdatedBy.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUpdatedBy()));
        colUpdated.setCellValueFactory(cell -> formatTimestamp(cell.getValue().getUpdatedDate()));

        // Update/Delete button for action function
        setupActionButtons();
        priceTable.setItems(priceList);
    }

    private SimpleStringProperty formatTimestamp(String timestamp){
        // ပြင်ဆင်ထားသော Logic: timestamp သည် null ဖြစ်မှသာ "-" ပြမည်
        if(timestamp == null || timestamp.isEmpty() || timestamp.equals("null")) {
            return new SimpleStringProperty("-");
        }
        try {
            if (timestamp.contains("T")) {
                return new SimpleStringProperty(timestamp.split("T")[0]);
            }
            return new SimpleStringProperty(timestamp);
        } catch (Exception e) {
            return new SimpleStringProperty("-");
        }
    }

    private void setupActionButtons() {
        Callback<TableColumn<PaddyPrice, Void>, TableCell<PaddyPrice, Void>> cellFactory = param -> new TableCell<>() {
            private final Button editBtn = new Button("Update");
            private final Button deleteBtn = new Button("Delete");
            private final HBox container = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");

                editBtn.setOnAction(event -> {
                    PaddyPrice data = getTableView().getItems().get(getIndex());
                    showPaddyPriceDialog(data);
                });

                deleteBtn.setOnAction(event -> {
                    PaddyPrice data = getTableView().getItems().get(getIndex());
                    handleDeletePrice(data);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        };
        colAction.setCellFactory(cellFactory);
    }

    private void loadDataFromBackend() {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL)).GET().build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        List<PaddyPrice> data = gson.fromJson(response.body(), new TypeToken<List<PaddyPrice>>(){}.getType());
                        Platform.runLater(() -> {
                            priceList.clear();
                            priceList.addAll(data);
                        });
                    }
                });
    }

    @FXML
    private void handleNewPrice() {
        showPaddyPriceDialog(null);
    }

    private void handleDeletePrice(PaddyPrice selected) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure want to delete?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/" + selected.getId()))
                        .DELETE()
                        .build();

                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(res -> {
                            if (res.statusCode() == 200) {
                                Platform.runLater(() -> priceList.remove(selected));
                            }
                        });
            }
        });
    }

    private String normalize(String input) {
        if (input == null) return null;
        return input.toLowerCase().replaceAll("\\s+", "");
    }


    private void showPaddyPriceDialog(PaddyPrice existingData) {
        Dialog<PaddyPrice> dialog = new Dialog<>();
        dialog.setTitle(existingData == null ? "Add new Paddy" : "Update ");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField typeField = new TextField();
        TextField priceField = new TextField();

        if (existingData != null) {
            typeField.setText(existingData.getPaddyType());
            priceField.setText(String.valueOf(existingData.getPrice()));
        }

        grid.add(new Label("Paddy Type"), 0, 0);
        grid.add(typeField, 1, 0);
        grid.add(new Label("Price"), 0, 1);
        grid.add(priceField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    PaddyPrice p = (existingData == null) ? new PaddyPrice() : existingData;
//                    p.setPaddyType(typeField.getText());
                    p.setPaddyType(typeField.getText().trim());

                    p.setPrice(Double.parseDouble(priceField.getText()));

                    if (existingData == null) {
                        p.setCreatedBy(loggedInUsername);
                    } else {
                        p.setUpdatedBy(loggedInUsername);
                    }
                    return p;
                } catch (Exception e) { return null; }
            }
            return null;
        });

        Optional<PaddyPrice> result = dialog.showAndWait();
        result.ifPresent(data -> {
            String method = (existingData == null) ? "POST" : "PUT";
            String url = (existingData == null) ? BASE_URL : BASE_URL + "/" + data.getId();
            saveToBackend(data, method, url);
        });
    }

    private void saveToBackend(PaddyPrice data, String method, String url) {
        String json = gson.toJson(data);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json");

        HttpRequest request = method.equals("POST")
                ? builder.POST(HttpRequest.BodyPublishers.ofString(json)).build()
                : builder.PUT(HttpRequest.BodyPublishers.ofString(json)).build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200 || response.statusCode() == 201) {
                        loadDataFromBackend();
                    } else {
                        Platform.runLater(() -> {
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "cannot store!");
                            errorAlert.show();
                        });
                    }
                });
    }

    public static class PaddyPrice {
        private String id;
        private String paddyType;
        private Double price;
        private String createdBy;
        private String createdDate;
        private String updatedBy;
        private String updatedDate;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getPaddyType() { return paddyType; }
        public void setPaddyType(String paddyType) { this.paddyType = paddyType; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        public String getCreatedDate() { return createdDate; }
        public String getUpdatedBy() { return updatedBy; }
        public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
        public String getUpdatedDate() { return updatedDate; }
    }




}
