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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public static String loggedInUsername;

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
            handleFinishConfirmation();
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

    private void handleFinishConfirmation(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Milling process confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure that milling process has been completed?");

        // Button စာသားများကို ပြောင်းလဲခြင်း (Yes/No)
        ButtonType buttonYes = new ButtonType("Yes");
        ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonYes, buttonNo);


        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonYes) {
            lblStatus.setText("Completed");   // Status ပြောင်းမယ်
            lblStatus.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px;"); // အစိမ်းရောင်ပြောင်းမယ်
            step3Container.setVisible(true);  // ညာဘက်အကွက်ကြီးကို ဖော်မယ်

            btnFinishMilling.setVisible(false);
            wareHouseGroup.setVisible(false);
            TotalOutput.setVisible(false);
            finalYield.setVisible(false);
        }
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

//    private void handleOkAction() {
//        if (comboVoucherNo.getValue() != null) {
//            populateVoucherInfo();
//            // OK နှိပ်ရင် အဆင့် ၂ (အလယ်ကွက်) ကို ဖော်မယ်
//            step2Container.setVisible(true);
//        } else {
//            showError("Choose voucher");
//        }
//    }

//    private void handleFinishMillingAction() {
//        // Finish Milling နှိပ်ရင် ညာဘက်က Stock အကွက်ကို ဖော်မယ်
//        step3Container.setVisible(true);
//
//        // Data သိမ်းမယ့် Function ကို ဒီမှာ ခေါ်နိုင်ပါတယ်
//        // saveMillingRecord();
//    }

    private void setupComboBoxes() {
        comboVoucherNo.setConverter(new StringConverter<>() {
            @Override public String toString(PaddyPurchase p)
            { return p == null ? "" : p.getBatchNo(); }

            @Override
            public PaddyPurchase fromString(String s) {
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
        Warehouse targetWarehouse = comboTargetWarehouse.getValue();

        if (voucher == null || targetWarehouse == null) {
            showError("You need to choose warehouse");
            return;
        }

//        ကြိတ်ခွဲမှုမှတ်တမ်း သိမ်းဆည်းခြင်း (Milling Record)
        saveMillingRecord(voucher, targetWarehouse);

        reduceRawStock(voucher.getWarehouseName(), voucher.getNetWeight().intValue());
        // ၁။ Voucher Status ကို "Milled" သို့ ပြောင်းခြင်း
        updateVoucherStatus(voucher.id, "Milled");

        // ၂။ ထွက်လာသည့် ဆန်အမျိုးအစားများကို Inventory ထဲ သိမ်းခြင်း
        saveToInventory(targetWarehouse.id, voucher.getPaddyType() + "( Head Rice )", parse(txtHeadRice.getText()));
        saveToInventory(targetWarehouse.id, voucher.getPaddyType() + " (Broken Rice )", parse(txtBrokenRice.getText()));
        saveToInventory(targetWarehouse.id, "Broken Rice + Bran", parse(txtBrokenBran.getText()));
        saveToInventory(targetWarehouse.id, "Bran", parse(txtBran.getText()));

        // ၄။ Good Warehouse ၏ လက်ရှိ Stock ကိုလည်း ပေါင်းထည့်ပေးရန် (Warehouse Table Update)
        double totalNewBags = parse(lblTotalOutputs.getText());
        updateWarehouseStock(targetWarehouse.getName(), (int) totalNewBags, "/add-stock");

        showSuccess("Successfully milling process and has also been added to the warehouse.");
        resetForm();
    }

    private void saveMillingRecord(PaddyPurchase voucher, Warehouse targetWarehouse) {
        Map<String, Object> record = new HashMap<>();
        record.put("voucherId", voucher.id);
        record.put("batchNo", voucher.getBatchNo());
        record.put("paddyType", voucher.getPaddyType());
        record.put("sourceWarehouse", voucher.getWarehouseName());
        record.put("targetWarehouse", targetWarehouse.getName());
        // ၁။ Date ကို စစ်ဆေးပြီး ထည့်သွင်းခြင်း
        String mDate = (datePicker.getValue() != null) ? datePicker.getValue().toString() : java.time.LocalDate.now().toString();
        record.put("millingDate", mDate);
        record.put("recordedBy", loggedInUsername);
        record.put("inputQtyTins", parse(lblQtyMilled.getText()));

        // Outputs
        record.put("headRiceBags", parse(txtHeadRice.getText()));
        record.put("brokenRiceBags", parse(txtBrokenRice.getText()));
        record.put("brokenBranBags", parse(txtBrokenBran.getText()));
        record.put("branBags", parse(txtBran.getText()));
        record.put("totalOutputBags", parse(lblTotalOutputs.getText()));
        record.put("yieldPercentage", parse(lblFinalYield.getText()));

        String json = gson.toJson(record);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/milling_records"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> {
                    if (res.statusCode() != 200 && res.statusCode() != 201) {
                        System.err.println("Milling Record Save Failed: " + res.body());
                    }
                });
    }

    private void reduceRawStock(String warehouseName, int quantity) {
        updateWarehouseStock(warehouseName, quantity, "/reduce-stock");
    }

    private void updateWarehouseStock(String warehouseName, int quantity, String endpoint) {
        Map<String, Object> data = new HashMap<>();
        data.put("warehouseName", warehouseName);
        data.put("quantity", quantity);

        String json = gson.toJson(data);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/warehouses" + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> {
                    if (res.statusCode() != 200) {
                        System.err.println("Stock update failed for " + warehouseName + ": " + res.body());
                    }
                });
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

//        InventoryItem item = new InventoryItem();
//        item.itemName = itemName;
//        item.quantity = (int) quantity;
//        item.unit = "Bag";
//        item.status = "Available";

        Map<String, Object> item = new HashMap<>();
        item.put("warehouseId", warehouseId);
        item.put("itemName", itemName);
        item.put("quantity", (int) quantity);
        item.put("unit", "Bag");
        item.put("status", "Available");

        String json = gson.toJson(item);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/inventory-logic/add_or_update/" + warehouseId))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> {
                    if(res.statusCode() != 200) {
                        System.err.println("Inventory Save Failed: " + res.body());
                    }
                });
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

            if (hr == 0 && br == 0 && bb == 0 && b == 0) {
                showError("Please write input");
                return false;
            }

            double qtyMilled = parse(lblQtyMilled.getText()); // ဥပမာ - 100 Tins
            double totalOutput = hr + br + bb + b;
            // --- Logic: Tin 100 လျှင် အိတ် 35 ထက်မပိုရ ---
            // Formula: Max Allowed Bag = (Qty Milled / 100) * 35
            double maxAllowedBags = (qtyMilled / 100.0) * 45.0;

            if (totalOutput > maxAllowedBags) {
                showError(String.format("The input is invalid. The total number of bags " +
                        "for %.2f baskets must not exceed %.2f.", qtyMilled, maxAllowedBags));

                // သတ်မှတ်ချက်ထက် ကျော်နေရင် အောက်က box တွေကို ပြန်ဖျောက်ထားမယ်
                wareHouseGroup.setVisible(false);
                TotalOutput.setVisible(false);
                finalYield.setVisible(false);
                btnConfirmProduction.setVisible(false);
                return false;
            }

            // တွက်ချက်မှု မှန်ကန်လျှင် UI တွင် ပြသမည်
            lblTotalOutputs.setText(String.format("%.2f Bags", totalOutput));
            if (qtyMilled > 0) {
                double yield = (hr / qtyMilled) * 100;
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
        if (s == null || s.isEmpty()) return 0;
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