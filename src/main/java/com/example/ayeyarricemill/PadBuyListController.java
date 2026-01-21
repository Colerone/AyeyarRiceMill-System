package com.example.ayeyarricemill;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class PadBuyListController {
    @FXML
    private TableView<PaddyPurchase> purchaseTable;
    @FXML
    private TableColumn<PaddyPurchase, Number> colNo;
    @FXML
    private TableColumn<PaddyPurchase,String> colVoucherNo;
    @FXML
    private TableColumn<PaddyPurchase, String> colDate;
    @FXML
    private TableColumn<PaddyPurchase, String> colSupplier;
    @FXML
    private TableColumn<PaddyPurchase, String> colVariety;
    @FXML
    private TableColumn<PaddyPurchase, String> colNetWeight;
    @FXML
    private TableColumn<PaddyPurchase, String> colTotalAmount;
    @FXML
    private TableColumn<PaddyPurchase,String> instock;
    @FXML
    private TableColumn<PaddyPurchase, Void> colAction;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final String API_URL = "http://localhost:9090/api/paddy_purchases";

    @FXML
    public void initialize() {
        setupTable();
        loadData();
    }

    private void setupTable() {
        // အမှတ်စဉ် (No)
        colNo.setCellValueFactory(data -> new SimpleIntegerProperty(purchaseTable.getItems().indexOf(data.getValue()) + 1));

        colVoucherNo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBatchNo()));

        // ရက်စွဲ (Date) - Backend field 'purchaseDate' ကို သုံးထားသည်
        colDate.setCellValueFactory(data -> {
            String date = data.getValue().getPurchaseDate();
            return new SimpleStringProperty(date != null ? date.split("T")[0] : "-");
        });

        // အခြား Column များ
        colSupplier.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSupplierName()));

        // Variety (Paddy Type) - Backend field 'paddyType' ကို သုံးထားသည်
        colVariety.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPaddyType() != null ? data.getValue().getPaddyType() : "-"));

        colNetWeight.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.2f Tins", data.getValue().getNetWeight() != null ? data.getValue().getNetWeight() : 0.0)));
        colTotalAmount.setCellValueFactory(data -> new SimpleStringProperty(String.format("%,.0f MMK", data.getValue().getTotalAmount() != null ? data.getValue().getTotalAmount() : 0.0)));

        instock.setCellValueFactory(data -> {
            String status = data.getValue().getStatus();
            // Default ကို Stock လို့ သတ်မှတ်ထားခြင်း
            return new SimpleStringProperty(status != null ? status : "Stock");
        });

        // Status အရောင်ခွဲပြခြင်း
        instock.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Stock".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #d47742; -fx-font-weight: bold;"); // Stock ဆိုရင် အစိမ်း
                    } else if ("Milled".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #2a8d67; -fx-font-weight: bold;"); // Milled ဆိုရင် လိမ္မော်ရောင်
                    }
                }
            }
        });

        // Action Button
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Detail");

            {
                btn.setStyle("-fx-background-color: #4478e5; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
                btn.setOnAction(event -> {
                    PaddyPurchase item = getTableView().getItems().get(getIndex());
                    openDetailView(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(btn);
            }
        });
    }

    private void loadData() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        List<PaddyPurchase> items = gson.fromJson(response.body(), new TypeToken<List<PaddyPurchase>>() {
                        }.getType());
                        Platform.runLater(() -> purchaseTable.setItems(FXCollections.observableArrayList(items)));
                    } else {
                        System.err.println("Failed to load data. Status: " + response.statusCode());
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    private void openDetailView(PaddyPurchase item) {
        try {
            // Data Transfer: ရွေးလိုက်တဲ့ Data ကို Detail Controller ဆီ ပို့မယ်
//            PaddyRegDetailController.selectedPurchase = item;
//
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("PaddyRegDetail.fxml"));
//            Parent root = loader.load();
//
//            // Window အသစ်နဲ့ ဖွင့်ချင်ရင်
//            Stage stage = new Stage();
//            stage.setScene(new Scene(root));
//            stage.setTitle("Purchase Detail - " + item.getBatchNo());
//            stage.show();

            // လက်ရှိ Window မှာပဲ ပြောင်းချင်ရင် အောက်က code ကိုသုံးပါ
            // Stage currentStage = (Stage) purchaseTable.getScene().getWindow();
            // currentStage.setScene(new Scene(root));

            PaddyRegDetailController.selectedPurchase = item;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("PaddyRegDetail.fxml"));
            Parent root = loader.load();

            // Create a Modal Stage (Popup Box Style)
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL); // Block interaction with parent window
//            popupStage.initStyle(StageStyle.UTILITY); // Minimal window decorations (closer to a box)
//            popupStage.initStyle(StageStyle.UNDECORATED);      // ❌ Close button မပါ
//            popupStage.initModality(Modality.APPLICATION_MODAL); // နောက်ခံ lock


            // Set Owner to center the popup over the main window
            Stage mainStage = (Stage) purchaseTable.getScene().getWindow();
            popupStage.initOwner(mainStage);

            Scene scene = new Scene(root);
            popupStage.setScene(scene);
            popupStage.setTitle("Purchase Detail: " + item.getBatchNo());
            popupStage.setResizable(false);

            popupStage.showAndWait(); // Wait until closed


        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Cannot open item's detail page: " + e.getMessage());
            alert.show();
        }
    }

    public static class PaddyPurchase {
        private String id;
        private String batchNo;
        private String purchaseDate; // LocalDateTime is sent as String in JSON
        private String supplierName;
        private String supplierPhone;
        private String purchaserName;
        private String paddyType;
        private String moisture;
        private String impurity;
        private String yellowDamage;
        private String warehouseId;
        private String warehouseName;
        private Double totalWeight;
        private Double moistureDeduction;
        private Double impurityDeduction;
        private Double netWeight;
        private Double basePrice;
        private Double qualityDeduction;
        private Double netPrice;
        private Double totalAmount;
        private String status; // stock or milled

        // Getters
        public String getBatchNo() {
            return batchNo;
        }

        public String getPurchaseDate() {
            return purchaseDate;
        }

        public String getSupplierName() {
            return supplierName;
        }

        public String getSupplierPhone() {
            return supplierPhone;
        }

        public String getPurchaserName() {
            return purchaserName;
        }

        public String getPaddyType() {
            return paddyType;
        }
        public String getStatus() {return status;}
        public String getWarehouseId() { return warehouseId; }

        public Double getNetWeight() {
            return netWeight;
        }

        public Double getTotalAmount() {
            return totalAmount;
        }

        public Double getMoistureDeduction() {
            return moistureDeduction;
        }

        public Double getImpurityDeduction() {
            return impurityDeduction;
        }

        public Double getTotalWeight() {
            return totalWeight;
        }

        public String getInputMoi() { return moisture; }
        public String getInputWaste() { return impurity; }
        public String getInputYell() { return yellowDamage; }

        public Double getBasePrice() {
            return basePrice;
        }

        public Double getQualityDeduction() {
            return qualityDeduction;
        }

        public Double getNetPrice() {
            return netPrice;
        }

        public String getWarehouseName() {
            return warehouseName;
        }
    }
}
