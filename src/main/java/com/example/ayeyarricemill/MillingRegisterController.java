package com.example.ayeyarricemill;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

public class MillingRegisterController {

    // --- FXML UI Components ---
    @FXML
    private Pane step1Container;
    @FXML
    private Pane step2Container;
    @FXML
    private Pane step3Container;
    @FXML
    private Pane wareHouseGroup;
    @FXML
    private VBox TotalOutput;
    @FXML
    private Pane finalYield;

    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<PaddyPurchase> comboVoucherNo;
    @FXML
    private ComboBox<Warehouse> comboTargetWarehouse;
    @FXML
    private Button btnOkay, btnCalculate, btnConfirmProduction, btnStartMilling, btnFinishMilling;
    @FXML
    private TextField txtHeadRice, txtBrokenRice, txtBrokenBran, txtBran;
    @FXML
    private Label lblSourceWarehouse, lblPaddyType, lblQtyMilled, lblStatus;
    @FXML
    private Label lblTotalOutputs, lblFinalYield, lblCurrentStock, lblMaxCapacity, lblSpaceStatus;

    // --- Variables ---
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final String BASE_URL = "http://localhost:9090/api";

    @FXML
    public void initialize() {
        setupStepVisibility();
        setupComboBoxes();
        loadInitialData();

        // Confirm Button ကို စစချင်း ဖျောက်ထားမယ်
        btnConfirmProduction.managedProperty().bind(btnConfirmProduction.visibleProperty());
        btnConfirmProduction.setVisible(false);

        // --- Button Events ---
        btnOkay.setOnAction(e -> {
            PaddyPurchase selected = comboVoucherNo.getValue();
            if (selected != null) {
                // Data တွေကို Label ထဲ ထည့်ပေးတဲ့ function ကို ဒီမှာ ခေါ်ရပါမယ်
                populateVoucherInfo(selected);
                step2Container.setVisible(true);
                btnStartMilling.setVisible(true);
                btnFinishMilling.setVisible(false);
            } else {
                showError("Please choose voucher.");
            }
        });

        btnStartMilling.setOnAction(e -> {
            lblStatus.setText("Milling in progress..."); // Status ပြောင်းမယ်
            lblStatus.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold; -fx-font-size: 14px;"); // လိမ္မော်ရောင်လေးပြောင်းမယ်
            btnStartMilling.setVisible(false);           // Start ခလုတ် ဖျောက်မယ်
            btnFinishMilling.setVisible(true);          // Finish ခလုတ် ဖော်မယ်
        });

        // --- 3. Finish Milling Button Logic ---
        btnFinishMilling.setOnAction(e -> {
            lblStatus.setText("Completed");   // Status ပြောင်းမယ်
            lblStatus.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px;"); // အစိမ်းရောင်ပြောင်းမယ်
            step3Container.setVisible(true);  // ညာဘက်အကွက်ကြီးကို ဖော်မယ်

            btnFinishMilling.setVisible(false);
            wareHouseGroup.setVisible(false);
            TotalOutput.setVisible(false);
            finalYield.setVisible(false);
        });

        btnCalculate.setOnAction(e -> {
            boolean success = performCalculation();
            if (success) {
                wareHouseGroup.setVisible(true);
                TotalOutput.setVisible(true);
                finalYield.setVisible(true);
                // Calculate နှိပ်ပြီးမှ Confirm Button ကို ပြမယ်
                btnConfirmProduction.setVisible(true);
            }
        });
        btnConfirmProduction.setOnAction(e -> handleConfirmProduction());
    }

    private void populateVoucherInfo(PaddyPurchase selected) {
        // Voucher ထဲက data တွေကို UI ပေါ်တင်ပေးခြင်း
        lblSourceWarehouse.setText(selected.getWarehouseName());
        lblPaddyType.setText(selected.getPaddyType());
        lblQtyMilled.setText(selected.getNetWeight() + " Tins");
        lblStatus.setText("Pending");
    }


    private void setupStepVisibility() {
        // Managed Property ကို Visible နဲ့ ချိတ်ထားမှ ပျောက်နေရင် နေရာမယူမှာ ဖြစ်ပါတယ်
        step2Container.managedProperty().bind(step2Container.visibleProperty());
        step3Container.managedProperty().bind(step3Container.visibleProperty());

        btnStartMilling.managedProperty().bind(btnStartMilling.visibleProperty());
        btnFinishMilling.managedProperty().bind(btnFinishMilling.visibleProperty());

        // စစချင်းမှာ Step 1 ပဲ ပြထားမယ်
        step1Container.setVisible(true);
        step2Container.setVisible(false);
        step3Container.setVisible(false);
    }

    private void handleOkAction() {
        if (comboVoucherNo.getValue() != null) {
            populateVoucherInfo();
            // OK နှိပ်ရင် အဆင့် ၂ (အလယ်ကွက်) ကို ဖော်မယ်
            step2Container.setVisible(true);
        } else {
            showError("Choose voucher");
        }
    }

    private void handleFinishMillingAction() {
        // Finish Milling နှိပ်ရင် ညာဘက်က Stock အကွက်ကို ဖော်မယ်
        step3Container.setVisible(true);

        // Data သိမ်းမယ့် Function ကို ဒီမှာ ခေါ်နိုင်ပါတယ်
        // saveMillingRecord();
    }

    private void setupComboBoxes() {
        comboVoucherNo.setConverter(new StringConverter<>() {
            @Override
            public String toString(PaddyPurchase object) {
                return object == null ? "" : object.getBatchNo();
            }

            @Override
            public PaddyPurchase fromString(String string) {
                return null;
            }
        });
        comboTargetWarehouse.setConverter(new StringConverter<>() {
            @Override
            public String toString(Warehouse object) {
                return object == null ? "" : object.getName();
            }

            @Override
            public Warehouse fromString(String string) {
                return null;
            }
        });

        // --- Warehouse ရွေးလိုက်ရင် Label တွေမှာ data ပြပေးမယ့် အပိုင်း ---
        comboTargetWarehouse.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Label တွေမှာ data ထည့်ပေးခြင်း
                lblCurrentStock.setText(newVal.getCurrentStock() + " Bags");
                lblMaxCapacity.setText(newVal.getCapacity() + " Bags");

                // Space Status (Available/Not Available) စစ်ဆေးခြင်း
                updateSpaceStatus(newVal);
            } else {
                // ဘာမှမရွေးထားရင် Label တွေကို ရှင်းပစ်ခြင်း
                lblCurrentStock.setText("0");
                lblMaxCapacity.setText("0");
                lblSpaceStatus.setText("-");
            }
        });
    }

    private void handleConfirmProduction() {
        PaddyPurchase voucher = comboVoucherNo.getValue();
        Warehouse warehouse = comboTargetWarehouse.getValue();

        if (voucher == null || warehouse == null) {
            showError("You need to choose warehouse");
            return;
        }
        // ၁။ Voucher Status ကို "Milled" သို့ ပြောင်းခြင်း
        updateVoucherStatus(voucher.id, "Milled");

        // ၂။ ထွက်လာသည့် ဆန်အမျိုးအစားများကို Inventory ထဲ သိမ်းခြင်း
        saveToInventory(warehouse.id, "Head Rice (" + voucher.getPaddyType() + ")", parse(txtHeadRice.getText()));
        saveToInventory(warehouse.id, "Broken Rice (" + voucher.getPaddyType() + ")", parse(txtBrokenRice.getText()));
        saveToInventory(warehouse.id, "Broken Rice + Bran", parse(txtBrokenBran.getText()));
        saveToInventory(warehouse.id, "Bran", parse(txtBran.getText()));

        showSuccess("Successfully milling process and has also been added to the warehouse.");
        resetForm();
    }

    private void updateVoucherStatus(String id, String status) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/paddy_purchases/" + id + "/status?newStatus=" + status))
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();
        httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString());
    }

    private void saveToInventory(String warehouseId, String itemName, double quantity) {
        if (quantity <= 0) return;

        InventoryItem item = new InventoryItem();
        item.itemName = itemName;
        item.quantity = (int) quantity;
        item.unit = "Bag";
        item.status = "Available";

        String json = gson.toJson(item);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/inventory-add/" + warehouseId))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString());
    }

    private void loadInitialData() {
        // Paddy Purchases ဆွဲယူခြင်း
        fetchFromApi("/paddy_purchases", new TypeToken<List<PaddyPurchase>>() {
        }, list -> {
            List<PaddyPurchase> stockOnly = list.stream()
                    .filter(p -> "Stock".equalsIgnoreCase(p.getStatus()))
                    .collect(Collectors.toList());
            Platform.runLater(() -> comboVoucherNo.setItems(FXCollections.observableArrayList(stockOnly)));
        });

        fetchFromApi("/warehouses", new TypeToken<List<Warehouse>>() {
        }, list -> {
            List<Warehouse> goodOnly = list.stream()
                    .filter(w -> "Good".equalsIgnoreCase(w.getType()))
                    .collect(Collectors.toList());
            Platform.runLater(() -> comboTargetWarehouse.setItems(FXCollections.observableArrayList(goodOnly)));
        });
    }

    // API Call လွယ်အောင် generic function လေးတစ်ခုလုပ်ထားတာပါ
    private <T> void fetchFromApi(String endpoint, TypeToken<T> typeToken, java.util.function.Consumer<T> callback) {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE_URL + endpoint)).GET().build();
        httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> {
                    if (res.statusCode() == 200) {
                        T data = gson.fromJson(res.body(), typeToken.getType());
                        callback.accept(data);
                    }
                }).exceptionally(ex -> {
                    System.err.println("Error fetching " + endpoint + ": " + ex.getMessage());
                    return null;
                });
    }


    private void loadData() {
        // Load Paddy Purchases (Stock ဖြစ်နေတဲ့ဟာတွေပဲ ယူမယ်)
        HttpRequest purchaseReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/paddy_purchases"))
                .GET().build();

        httpClient.sendAsync(purchaseReq, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        List<PaddyPurchase> all = gson.fromJson(response.body(), new TypeToken<List<PaddyPurchase>>() {
                        }.getType());
                        List<PaddyPurchase> stockOnly = all.stream()
                                .filter(p -> "Stock".equalsIgnoreCase(p.getStatus()))
                                .collect(Collectors.toList());
                        Platform.runLater(() -> comboVoucherNo.setItems(FXCollections.observableArrayList(stockOnly)));
                    }
                });

        // Load Warehouses (Good - ဆန်ထွက်ကုန်ထည့်မယ့် ဂိုဒေါင်တွေပဲ ယူမယ်)
        HttpRequest warehouseReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/warehouses"))
                .GET().build();

        httpClient.sendAsync(warehouseReq, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        List<Warehouse> all = gson.fromJson(response.body(), new TypeToken<List<Warehouse>>() {
                        }.getType());
                        List<Warehouse> goodOnly = all.stream()
                                .filter(w -> "Good".equalsIgnoreCase(w.getType()))
                                .collect(Collectors.toList());
                        Platform.runLater(() -> comboTargetWarehouse.setItems(FXCollections.observableArrayList(goodOnly)));
                    }
                });
    }

    private void populateVoucherInfo() {
        PaddyPurchase selected = comboVoucherNo.getValue();
        if (selected != null) {
            lblSourceWarehouse.setText(selected.getWarehouseName());
            lblPaddyType.setText(selected.getPaddyType());
            lblQtyMilled.setText(selected.getNetWeight() + " Tins");
            lblStatus.setText("Pending");
        }
    }

    private boolean performCalculation() {
        try {
            double hr = parse(txtHeadRice.getText());
            double br = parse(txtBrokenRice.getText());
            double bb = parse(txtBrokenBran.getText());
            double b = parse(txtBran.getText());

            if (hr == 0 && br == 0) {
                showError("Please write input");
                return false;
            }

            double totalOutput = hr + br + bb + b;
            lblTotalOutputs.setText(String.format("%.2f Bags", totalOutput));

            double input = parse(lblQtyMilled.getText());
            if (input > 0) {
                double yield = (totalOutput / input) * 100;
                lblFinalYield.setText(String.format("%.2f%%", yield));
            }
            return true;
        } catch (Exception e) {
            showError("wrong calculation");
            return false;
        }
    }


    private void updateSpaceStatus(Warehouse w) {
        int current = w.currentStock != null ? w.currentStock : 0;
        if (w.capacity != null && current >= w.capacity) {
            lblSpaceStatus.setText("not available");
            lblSpaceStatus.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
        } else {
            lblSpaceStatus.setText("available");
            lblSpaceStatus.setStyle("-fx-text-fill: green; -fx-font-size: 14px;");
        }
    }

    private void resetForm() {
        Platform.runLater(() -> {
            step2Container.setVisible(false);
            step3Container.setVisible(false);
            txtHeadRice.clear();
            txtBrokenRice.clear();
            txtBrokenBran.clear();
            txtBran.clear();
            loadInitialData(); // Data အသစ်ပြန်ခေါ်မယ်
        });
    }

    private double parse(String s) {
        try {
            return Double.parseDouble(s.replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private void clearForm() {
        txtHeadRice.clear();
        txtBrokenRice.clear();
        txtBrokenBran.clear();
        txtBran.clear();
        loadData();
    }

    private void showError(String msg) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.ERROR, msg);
            a.show();
        });
    }

    private void showSuccess(String msg) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
            a.show();
        });
    }


    // --- Static Inner Classes (Backend Models) ---

    public static class PaddyPurchase {
        private String id;
        private String batchNo;
        private String paddyType;
        private String warehouseName;
        private Double netWeight;
        private String status;
        private Double netPrice;

        public String getBatchNo() {
            return batchNo;
        }

        public String getPaddyType() {
            return paddyType;
        }

        public String getWarehouseName() {
            return warehouseName;
        }

        public Double getNetWeight() {
            return netWeight;
        }

        public String getStatus() {
            return status;
        }
    }

    public static class Warehouse {
        private String id;
        private String name;
        private String type;
        private Integer currentStock;
        private Integer capacity;

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        // ဒီ Getter ၂ ခုကို ထပ်ဖြည့်ပေးပါ
        public Integer getCurrentStock() {
            return currentStock;
        }

        public Integer getCapacity() {
            return capacity;
        }
    }

    public static class InventoryItem {
        private String id;
        private String itemName;
        private Integer quantity;
        private String unit;
        private String status;
        private String remark;
    }
}