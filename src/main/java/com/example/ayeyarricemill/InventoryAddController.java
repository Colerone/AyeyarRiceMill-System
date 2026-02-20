package com.example.ayeyarricemill;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class InventoryAddController {

    public static String loggedInUserRole = "";
    @FXML
    private VBox addWarehouse;
    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<String> typeCombo;
    @FXML
    private TextField capacityField;

    @FXML
    private TableView<Warehouse> inventoryTable;
    @FXML
    private TableColumn<Warehouse, Number> colNo;
    @FXML
    private TableColumn<Warehouse, String> colName;
    @FXML
    private TableColumn<Warehouse, String> colType;
    @FXML
    private TableColumn<Warehouse, Number> colCapacity;
    @FXML
    private TableColumn<Warehouse, Void> colAction;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final String API_URL = "http://localhost:9090/api/warehouses";
    private final ObservableList<Warehouse> warehouseList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
//        setupPermissions();
        Platform.runLater(this::setupPermissions);

        // ComboBox Setup
        typeCombo.setItems(FXCollections.observableArrayList("Good", "Paddy"));
        typeCombo.setValue("Good");

        // Table Columns Setup
        setupTable();

        // Load existing data
        loadWarehouses();
    }

    private void setupPermissions() {
        System.out.println("Access Granted for Role: " + loggedInUserRole);

        // MANAGER ဖြစ်နေလျှင် Add New Warehouse Box ကို ဖျောက်ထားမည်
        // (စာလုံးအကြီး/အသေး မှားနိုင်သဖြင့် equalsIgnoreCase သုံးခြင်းက ပိုစိတ်ချရသည်)
        if ("MANAGER".equalsIgnoreCase(loggedInUserRole)) {
            if (addWarehouse != null) {
                addWarehouse.setManaged(false);
                addWarehouse.setVisible(false);
                inventoryTable.setLayoutY(180);
                if (addWarehouse.getParent() instanceof VBox) {
                    ((VBox) addWarehouse.getParent()).setSpacing(0);
                }

            }
        }
    }


    private void setupTable() {
        colNo.setCellValueFactory(column -> new SimpleIntegerProperty(inventoryTable.getItems().indexOf(column.getValue()) + 1));
        colName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        colType.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getType()));
        colCapacity.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getCapacity()));

        // Action Buttons (Detail/Delete)
        setupActionButtons();

        inventoryTable.setItems(warehouseList);
    }

    private void setupActionButtons() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button detailBtn = new Button("Detail");
            private final Button deleteBtn = new Button("Delete");
            private final HBox container = new HBox(10, detailBtn, deleteBtn);

            {
                detailBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");

                if ("MANAGER".equalsIgnoreCase(loggedInUserRole)) {
                    deleteBtn.setVisible(false);
                    deleteBtn.setManaged(false);
                }

                detailBtn.setOnAction(event -> {
                    Warehouse selected = getTableView().getItems().get(getIndex());
                    handleDetail(selected);
                });

                deleteBtn.setOnAction(event -> {
                    Warehouse selected = getTableView().getItems().get(getIndex());
                    handleDelete(selected);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    @FXML
    private void handleSubmit() {
        String name = nameField.getText();
        String type = typeCombo.getValue();
        String capacityStr = capacityField.getText();

        if (name.isEmpty() || capacityStr.isEmpty()) {
            showAlert("Error", "Please fill all inputs");
            return;
        }

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setName(name);
        newWarehouse.setType(type);
        newWarehouse.setCapacity(Integer.parseInt(capacityStr));

        String json = gson.toJson(newWarehouse);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        Platform.runLater(() -> {
                            clearFields();
                            loadWarehouses();
                        });
                    }
                });
    }

    private void loadWarehouses() {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL)).GET().build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        List<Warehouse> data = gson.fromJson(response.body(), new TypeToken<List<Warehouse>>() {
                        }.getType());
                        Platform.runLater(() -> {
                            warehouseList.setAll(data);
                        });
                    }
                });
    }

    private void handleDelete(Warehouse warehouse) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/" + warehouse.getId()))
                .DELETE()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> {
                    if (res.statusCode() == 200) {
                        Platform.runLater(() -> warehouseList.remove(warehouse));
                    }
                });
    }

    private void handleDetail(Warehouse warehouse) {
        try{
            InventoryDetailsController.selectedWarehouse = warehouse;
            InventoryDetailsController.loggedInUserRole = loggedInUserRole;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/ayeyarricemill/InventoryDetailPage.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (javafx.stage.Stage) inventoryTable.getScene().getWindow();

            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

            System.out.println("Switching to Detail View for: " + warehouse.getName());

        } catch (java.io.IOException e) {
        e.printStackTrace();
        showAlert("Error", "Detail Page ကို ဖွင့်ရယူရာတွင် အမှားအယွင်းရှိနေပါသည်။ ဖိုင်လမ်းကြောင်းကို စစ်ဆေးပါ။");
    }
        System.out.println("Opening detail for: " + warehouse.getName());
    }

    private void clearFields() {
        nameField.clear();
        capacityField.clear();
        typeCombo.setValue("Good");
    }

    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setContentText(content);
            alert.show();
        });
    }

    public static class Warehouse {
        private String id;
        private String no;
        private String name;
        private String type;
        private Integer capacity;
        private Integer currentStock = 0;
        private String unit;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNo() {
            return no;
        }

        public void setNo(String no) {
            this.no = no;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getCapacity() {
            return capacity;
        }

        public void setCapacity(Integer capacity) {
            this.capacity = capacity;
        }

        public Integer getCurrentStock() {
            return currentStock;
        }

        public void setCurrentStock(Integer currentStock) {
            this.currentStock = currentStock;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }

}