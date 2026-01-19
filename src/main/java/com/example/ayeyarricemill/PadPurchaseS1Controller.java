package com.example.ayeyarricemill;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PadPurchaseS1Controller {

    public static String loggedInUsername;

    //fxml inputs
    @FXML
    private TextField supplierNameField;
    @FXML
    private TextField phoneField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<String> paddyVarietyCombo;
    @FXML
    private TextField totalWeightField;
    @FXML
    private TextField moistureField;
    @FXML
    private TextField impurityField;
    @FXML
    private TextField yellowDamageField;

    // FXML Containers to Hide/Show
    @FXML
    private AnchorPane weightBox;      // Left Box
    @FXML
    private AnchorPane priceBox;       // Middle Box
    @FXML
    private AnchorPane warehouseBox;   // Right Box
    @FXML
    private AnchorPane voucherBox;     // Voucher Panel
    @FXML
    private Button calcTotalButton;
    @FXML
    private Label voucherLabel;


    // Result Labels
    @FXML
    private Label lblTotalWeight;
    @FXML
    private Label lblMoistureCut;
    @FXML
    private Label lblImpurityCut;
    @FXML
    private Label lblNetWeight;
    @FXML
    private Label lblBasePrice;
    @FXML
    private Label lblQualityCut;
    @FXML
    private Label lblNetPrice;

    // Warehouse Box
    @FXML
    private ComboBox<InventoryAddController.Warehouse> warehouseCombo;
    @FXML
    private Label lblCurrentStock;
    @FXML
    private Label lblMaxCapacity;
    @FXML
    private Label lblSpaceStatus;

    // Voucher Labels
    @FXML
    private Label vBatchNo;
    @FXML
    private Label vVariety;
    @FXML
    private Label vNetWeight;
    @FXML
    private Label vUnitPrice;
    @FXML
    private Label vTotalAmount;
    @FXML
    private Button ConPurchase;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    // Backend API
    private final String PURCHASE_API_URL = "http://localhost:9090/api/paddy_purchases";
    private final String PADDY_PRICE_URL = "http://localhost:9090/api/paddy_price";
    private final String WAREHOUSE_URL = "http://localhost:9090/api/warehouses";
    // Stock Update လုပ်ပေးမယ့် API
    private final String INVENTORY_ADD_URL = "http://localhost:9090/api/inventory-add/";

    //Data holders
    private List<PaddyPrice> allPaddyPrices = new ArrayList<>();
    private double currentSelectedPrice = 0.0;
    private double finalNetWeight = 0.0;
    private double finalNetPrice = 0.0;
    double qualityCutPrice;


    @FXML
    public void initialize() {
        // ၁။ စစချင်းမှာ Box များကို ဖျောက်ထားမည်
        hideResultBoxes();

        // ၂။ DB မှ စပါးဈေးနှုန်းများနှင့် ဂိုဒေါင်စာရင်းများကို ဆွဲယူမည်
        loadPaddyPricesFromDB();
        loadRawWarehouses();

        // ၃။ Listeners များ သတ်မှတ်ခြင်း
        paddyVarietyCombo.setOnAction(e -> handleVarietySelection());
        warehouseCombo.setOnAction(e -> updateWarehouseInfo());

//        confirm button action
                ConPurchase.setOnAction(e->handleConfirmPurchase());
    }

    private void hideResultBoxes() {
        setVisible(weightBox, false);
        setVisible(priceBox, false);
        setVisible(warehouseBox, false);
        setVisible(voucherBox, false);
        setVisible(calcTotalButton, false);
        setVisible(voucherLabel, false);
        setVisible(ConPurchase, false);
    }

    private void setVisible(javafx.scene.Node node, boolean visible) {
        if (node != null) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
    }

    //  DB မှ စပါးအမျိုးအစားနှင့် ဈေးနှုန်းများ ဆွဲယူခြင်း
    private void loadPaddyPricesFromDB() {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(PADDY_PRICE_URL)).GET().build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        allPaddyPrices = gson.fromJson(response.body(), new TypeToken<List<PaddyPrice>>() {
                        }.getType());

                        // ComboBox ထဲသို့ အမျိုးအစား နာမည်များသာ ထည့်ခြင်း
                        List<String> varietyNames = allPaddyPrices.stream()
                                .map(PaddyPrice::getPaddyType)
                                .collect(Collectors.toList());

                        Platform.runLater(() -> {
                            paddyVarietyCombo.setItems(FXCollections.observableArrayList(varietyNames));
                        });
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("Paddy Price API Error: " + ex.getMessage());
                    return null;
                });
    }

    // Variety ရွေးရင် Price ကို Data Holder ထဲ သိမ်းထားပြီး Label မှာ ပြမယ် နှိပ်မှပြမှာ
    private void handleVarietySelection() {
        String selectedType = paddyVarietyCombo.getValue();
        allPaddyPrices.stream()
                .filter(p -> p.getPaddyType().equals(selectedType))
                .findFirst()
                .ifPresent(p -> {
                    currentSelectedPrice = p.getPrice();

                    // Base Price Label တွင် ဈေးနှုန်းကို ချက်ချင်းပြသပေးခြင်း
                    Platform.runLater(() -> {
                        lblBasePrice.setText(currentSelectedPrice + " MMK");
                    });

                    System.out.println("Selected Variety Price: " + currentSelectedPrice);
                });
    }

    @FXML
    private void handleCalculateDeduction() {
        try {
            if (paddyVarietyCombo.getValue() == null) {
                showAlert("Warning", "Choose paddy type");
                return;
            }

            double totalWeight = Double.parseDouble(totalWeightField.getText());
            double moisture = Double.parseDouble(moistureField.getText());
            double impurity = Double.parseDouble(impurityField.getText());
            double yellow = Double.parseDouble(yellowDamageField.getText());
//
//            // --- Deduction Logic ---
//            double moistureCut = (moisture > 14.0) ? (moisture - 14.0) * (totalWeight / 100) : 0;
//            double impurityCut = (impurity > 1.0) ? (impurity - 1.0) * (totalWeight / 100) : 0;
//            finalNetWeight = totalWeight - moistureCut - impurityCut;
//
//            // --- Price Logic ---
//            double qualityCutPrice = (yellow > 0.5) ? 500 : 0;
//            finalNetPrice = currentSelectedPrice - qualityCutPrice;

            // standard rate သတ်မှတ်ချက် (အပြင်ကအတိုင်း)
            double stdMoisture = 14.0;
            double stdImpurity = 1.0;
            double stdYellow = 0.5;

            double moistureRate = 0.02; // 1 point လျှင် 0.02 တင်း
            double impurityRate = 0.01; // 1 point လျှင် 0.01 တင်း
            double yellowPriceDiscountRate = 0.01; // 1 point လျှင် 1% ဈေးလျှော့

            double moistureCut = 0;
            if(moisture > stdMoisture){
                moistureCut = (moisture - stdMoisture) * moistureRate * totalWeight;
            }

            double impurityCut = 0;
            if(impurity > stdImpurity){
                impurityCut = (impurity - stdImpurity) * impurityRate * totalWeight;
            }

             finalNetWeight = totalWeight - moistureCut - impurityCut;

            // ဈေးနှုန်းအလျှော့တွက်ချက်ခြင်း
            double priceDiscount = 0;
            if(yellow > stdYellow){
                double discountPercentage = (yellow - stdYellow) * yellowPriceDiscountRate;
                priceDiscount = currentSelectedPrice * discountPercentage;
            }

             qualityCutPrice = currentSelectedPrice - priceDiscount;
            finalNetPrice = finalNetWeight * qualityCutPrice;

            // UI Update
            lblTotalWeight.setText(totalWeight + " Tins");
            lblMoistureCut.setText("-" + String.format("%.2f", moistureCut) + " Tins");
            lblImpurityCut.setText("-" + String.format("%.2f", impurityCut) + " Tins");
            lblNetWeight.setText(String.format("%.2f", finalNetWeight) + " Tins");

            lblBasePrice.setText(currentSelectedPrice + " MMK");
            lblQualityCut.setText("- " + priceDiscount + " MMK");
            lblNetPrice.setText(qualityCutPrice + " MMK");

            // Result Box များ ဖော်ပြခြင်း
            setVisible(weightBox, true);
            setVisible(priceBox, true);
            setVisible(warehouseBox, true);
            setVisible(calcTotalButton, true);

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Just write number correctly");
        }
    }

    @FXML
    private void handleCalculateTotal() {
        if (warehouseCombo.getValue() == null) {
            showAlert("Warning", "Choose first inventory");
            return;
        }

        vBatchNo.setText("A-" + (int) (Math.random() * 10000));
        vVariety.setText(paddyVarietyCombo.getValue());
        vNetWeight.setText(String.format("%.2f", finalNetWeight) + " Tins");
        vUnitPrice.setText(qualityCutPrice + " MMK");

        double totalAmount = finalNetWeight * qualityCutPrice;
        vTotalAmount.setText(String.format("%.0f", totalAmount) + " MMK");

        setVisible(voucherBox, true);
        setVisible(voucherLabel, true);
        setVisible(ConPurchase, true);
    }

    // Purchase confirm လုပ်ပြီး Inventory ထဲထည့်ခြင်း
//    private void handleConfirmPurchase(){
//        InventoryAddController.Warehouse selectedWarehouse = warehouseCombo.getValue();
//        if (selectedWarehouse == null) return;
//
//        // Purchase Record (Transaction) Data ပြင်ဆင်ခြင်း
//        PaddyPurchaseRecord purchaseRecord = new PaddyPurchaseRecord();
//        purchaseRecord.setBatchNo(vBatchNo.getText());
//        purchaseRecord.setSupplierName(supplierNameField.getText());
//        purchaseRecord.setPhone(phoneField.getText());
//        purchaseRecord.setDate(datePicker.getValue() != null ? datePicker.getValue().toString() : LocalDate.now().toString());
//        purchaseRecord.setPaddyVariety(paddyVarietyCombo.getValue());
//
//        try{
//            purchaseRecord.setTotalWeight(Double.parseDouble(totalWeightField.getText()));
//            purchaseRecord.setMoisture(Double.parseDouble(moistureField.getText()));
//            purchaseRecord.setImpurity(Double.parseDouble(impurityField.getText()));
//            purchaseRecord.setYellowDamage(Double.parseDouble(yellowDamageField.getText()));
//        } catch (NumberFormatException e) {
//            showAlert("Error", "Wrong numbers and info");
//            return;
//        }
//
//        purchaseRecord.setNetWeight(finalNetWeight);
//        purchaseRecord.setUnitPrice(qualityCutPrice);
//        purchaseRecord.setTotalAmount(finalNetWeight * qualityCutPrice);
//        purchaseRecord.setWarehouseId(selectedWarehouse.getId());
//        purchaseRecord.setPurchaserName(loggedInUsername);
//
//        // API Call: Purchase History သိမ်းခြင်း
//        String purchaseJson = gson.toJson(purchaseRecord);
//
//        // Backend ကိုပို့ဖို့ Inventory Item ဆောက်
//        InventoryItem newItem = new InventoryItem();
//        newItem.setWarehouseId(selectedWarehouse.getId());
//        newItem.setItemName(paddyVarietyCombo.getValue());
//
//        //Backend ကို integer နဲ့ရေးထားလို့
//        newItem.setQuantity((int) Math.round(finalNetWeight));
//        newItem.setPrice(qualityCutPrice);
//        newItem.setUnit("Tin");
//        newItem.setStatus("Raw Material");
//        newItem.setRemark("Purchased form " + supplierNameField.getText() + " | Batch: " + vBatchNo.getText());
//
//        String inventoryJson = gson.toJson(newItem);
//
//        HttpRequest purchaseRequest = HttpRequest.newBuilder()
//                .uri(URI.create(PURCHASE_API_URL))
//                .header("Content-Type", "application/json")
//                .POST(HttpRequest.BodyPublishers.ofString(purchaseJson))
//                .build();
//
//        httpClient.sendAsync(purchaseRequest, HttpResponse.BodyHandlers.ofString())
//                .thenAccept(purchaseResponse -> {
//                    // Purchase သိမ်းဆည်းမှု အောင်မြင်မှသာ ဆက်လုပ်မည်
//                    if (purchaseResponse.statusCode() == 200) {
//                        System.out.println("Purchase Record Saved Successfully.");
//
//                        // အဆင့် ၂ - Inventory Stock ကို Update လုပ်ခြင်း
//                        HttpRequest inventoryRequest = HttpRequest.newBuilder()
//                                .uri(URI.create(INVENTORY_ADD_URL + selectedWarehouse.getId()))
//                                .header("Content-Type", "application/json")
//                                .POST(HttpRequest.BodyPublishers.ofString(inventoryJson))
//                                .build();
//
//                        httpClient.sendAsync(inventoryRequest, HttpResponse.BodyHandlers.ofString())
//                                .thenAccept(inventoryResponse -> {
//                                    if (inventoryResponse.statusCode() == 200) {
//                                        // နှစ်ခုလုံး အောင်မြင်မှ Success ပြပြီး Voucher သွားမည်
//                                        Platform.runLater(() -> {
//                                            showAlert("Successfully", "Saved record list and item was added Inventoy");
//
//                                            // Voucher Data ပို့ခြင်း
//                                            prepareVoucherData();
//                                            switchToVoucherScene();
//
//                                            // UI Reset
//                                            hideResultBoxes();
//                                            clearInputs();
//                                        });
//                                    } else {
//                                        // Inventory သိမ်းမရလျှင် Error ပြမည်
//                                        Platform.runLater(() -> showAlert("Inventory Error", "Purchase saved but Error inventory " + inventoryResponse.body()));
//                                    }
//                                });
//
//                    } else {
//                        // Purchase Record သိမ်းမရလျှင် Error ပြမည်
//                        Platform.runLater(() -> showAlert("Purchase Error", "cannot store  " + purchaseResponse.body()));
//                    }
//                })
//                .exceptionally(ex -> {
//                    Platform.runLater(() -> showAlert("Connection Error", "cannot server" + ex.getMessage()));
//                    return null;
//                });
//    }

    // Purchase confirm လုပ်ပြီး Backend သို့ ပို့ခြင်း
    private void handleConfirmPurchase() {
        InventoryAddController.Warehouse selectedWarehouse = warehouseCombo.getValue();
        if (selectedWarehouse == null) {
            showAlert("သတိပေးချက်", "ဂိုဒေါင် အရင်ရွေးချယ်ပါ။");
            return;
        }

        // Backend PaddyPurchase Model နှင့် အတိအကျတူသော Record တစ်ခု တည်ဆောက်ခြင်း
        PaddyPurchaseRecord purchaseRecord = new PaddyPurchaseRecord();

        // အခြေခံ အချက်အလက်များ
        purchaseRecord.setBatchNo(vBatchNo.getText());
        purchaseRecord.setSupplierName(supplierNameField.getText());
        purchaseRecord.setSupplierPhone(phoneField.getText());
        purchaseRecord.setPurchaserName(loggedInUsername);

        // စပါးအချက်အလက်နှင့် ဂိုဒေါင်
        purchaseRecord.setPaddyType(paddyVarietyCombo.getValue());
        purchaseRecord.setWarehouseId(selectedWarehouse.getId());
        purchaseRecord.setWarehouseName(selectedWarehouse.getName());
        // အလေးချိန်နှင့် ဈေးနှုန်း တွက်ချက်မှုများ (Label များမှ ဒေတာကို Clean လုပ်ပြီး ယူသည်)
        try {
            purchaseRecord.setTotalWeight(Double.parseDouble(totalWeightField.getText()));

            // Deduction values (Label များထဲမှ ဂဏန်းသီးသန့်ကို ယူခြင်း)
            purchaseRecord.setTotalWeight(Double.parseDouble(totalWeightField.getText()));
            purchaseRecord.setMoisture(Double.parseDouble(moistureField.getText())); // Input %
            purchaseRecord.setImpurity(Double.parseDouble(impurityField.getText())); // Input %
            purchaseRecord.setYellowDamage(Double.parseDouble(yellowDamageField.getText())); // Input %

            double moiCut = Double.parseDouble(lblMoistureCut.getText().replaceAll("[^0-9.]", ""));
            double impCut = Double.parseDouble(lblImpurityCut.getText().replaceAll("[^0-9.]", ""));
            double qualityCut = Double.parseDouble(lblQualityCut.getText().replaceAll("[^0-9.]", ""));

            purchaseRecord.setMoistureDeduction(moiCut);
            purchaseRecord.setImpurityDeduction(impCut);
            purchaseRecord.setNetWeight(finalNetWeight);

            purchaseRecord.setBasePrice(currentSelectedPrice);
            purchaseRecord.setQualityDeduction(qualityCut);
            purchaseRecord.setNetPrice(qualityCutPrice);
            purchaseRecord.setTotalAmount(finalNetWeight * qualityCutPrice);

        } catch (Exception e) {
            showAlert("Error", "ကိန်းဂဏန်းများ မှားယွင်းနေပါသည်။");
            return;
        }

        // API Call: Backend သို့ ပို့ခြင်း
        String purchaseJson = gson.toJson(purchaseRecord);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PURCHASE_API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(purchaseJson))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        // Backend ကနေ တစ်ဆင့်တည်းနဲ့ Purchase ရော Inventory ရော သိမ်းပြီးသားဖြစ်သည်
                        Platform.runLater(() -> {
                            showAlert("Successfully", "Save Buy List and store in inventory");

                            prepareVoucherData();
                            switchToVoucherScene();

                            hideResultBoxes();
                            clearInputs();
                        });
                    } else {
                        Platform.runLater(() -> showAlert("Backend Error", "Cannot store: " + response.body()));
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> showAlert("Connection Error", "Cannot connect backend server: " + ex.getMessage()));
                    return null;
                });
    }

    private void prepareVoucherData() {
        PaddyVoucherController.PurchaseData.supplierName = supplierNameField.getText();
        PaddyVoucherController.PurchaseData.variety = paddyVarietyCombo.getValue();
        PaddyVoucherController.PurchaseData.phone = phoneField.getText();
        PaddyVoucherController.PurchaseData.date = datePicker.getValue() != null ? datePicker.getValue().toString() : "N/A";
        PaddyVoucherController.PurchaseData.totalWeight = totalWeightField.getText();

        PaddyVoucherController.PurchaseData.inputMoi = moistureField.getText();
        PaddyVoucherController.PurchaseData.inputWaste = impurityField.getText();
        PaddyVoucherController.PurchaseData.inputYell = yellowDamageField.getText();

        PaddyVoucherController.PurchaseData.moistureCut = lblMoistureCut.getText().replaceAll("[^0-9.]", "");
        PaddyVoucherController.PurchaseData.impurityCut = lblImpurityCut.getText().replaceAll("[^0-9.]", "");
        PaddyVoucherController.PurchaseData.netWeight = String.format("%.2f", finalNetWeight);
        PaddyVoucherController.PurchaseData.basePrice = String.valueOf(currentSelectedPrice);
        PaddyVoucherController.PurchaseData.yellowCut = lblQualityCut.getText().replaceAll("[^0-9.]", "");
        PaddyVoucherController.PurchaseData.netPrice = String.format("%.2f", qualityCutPrice);
        PaddyVoucherController.PurchaseData.totalAmount = vTotalAmount.getText().replaceAll("[^0-9.]", "");
        PaddyVoucherController.PurchaseData.purchaserName = loggedInUsername;
        PaddyVoucherController.PurchaseData.batchNo = vBatchNo.getText();
    }

    private void clearInputs() {
        supplierNameField.clear();
        phoneField.clear();
        totalWeightField.clear();
        moistureField.clear();
        impurityField.clear();
        yellowDamageField.clear();
        warehouseCombo.setValue(null);
        paddyVarietyCombo.setValue(null);
    }

    private void switchToVoucherScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PaddyVoucher.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ConPurchase.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Purchase Voucher - " + vBatchNo.getText());
            stage.setMaximized(true);
            stage.show();
//            SceneController.switchCenter("/com/example/ayeyarricemill/PaddyVoucher.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("UI Error", "Cannot open voucher page.");
        }
    }

    private void loadRawWarehouses() {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(WAREHOUSE_URL)).GET().build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        List<InventoryAddController.Warehouse> allWh = gson.fromJson(response.body(), new TypeToken<List<InventoryAddController.Warehouse>>() {
                        }.getType());
                        List<InventoryAddController.Warehouse> rawWh = allWh.stream()
                                .filter(w -> "Raw".equalsIgnoreCase(w.getType()))
                                .collect(Collectors.toList());

                        Platform.runLater(() -> {
                            warehouseCombo.setItems(FXCollections.observableArrayList(rawWh));
                            setupWarehouseComboBoxDisplay();
                        });
                    }
                });
    }

    private void setupWarehouseComboBoxDisplay() {
        warehouseCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(InventoryAddController.Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });
        warehouseCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(InventoryAddController.Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });
    }

    private void updateWarehouseInfo() {
        InventoryAddController.Warehouse selected = warehouseCombo.getValue();
        if (selected != null) {
            lblCurrentStock.setText(selected.getCurrentStock() + " Tins");
            lblMaxCapacity.setText(selected.getCapacity() + " Tins");
            int available = selected.getCapacity() - selected.getCurrentStock();
            lblSpaceStatus.setText(available >= finalNetWeight ? "avaliable" : "Not avaliable");
            lblSpaceStatus.setStyle("-fx-text-fill: " + (available >= finalNetWeight ? "green" : "red") + ";");


        }
    }

    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    public static class PaddyPrice {
        private String id;
        private String paddyType;
        private Double price;

        public String getPaddyType() {
            return paddyType;
        }

        public Double getPrice() {
            return price;
        }
    }

    public static class InventoryItem {
        private String warehouseId;
        private String itemName;
        private Integer quantity;
        private Double price;       // New Field
        private String unit;
        private String status;      // New Field
        private String remark;      // New Field

        public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }
        public void setItemName(String itemName) { this.itemName = itemName; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public void setPrice(Double price) { this.price = price; }
        public void setUnit(String unit) { this.unit = unit; }
        public void setStatus(String status) { this.status = status; }
        public void setRemark(String remark) { this.remark = remark; }
    }

    // Local Warehouse Class
    public static class Warehouse {
        private String id;
        private String name;
        private String type;
        private Integer capacity;
        private Integer currentStock = 0;

        public String getId() { return id; }
        public String getName() { return name; }
        public String getType() { return type; }
        public Integer getCapacity() { return capacity; }
        public Integer getCurrentStock() { return currentStock; }
    }

    public static class PaddyPurchaseRecord {
        private String batchNo;
        private String supplierName;
        private String supplierPhone;
        private String purchaserName;
        private String paddyType;
        private String warehouseId;
        private String warehouseName;
        private Double totalWeight;
        private Double moisture;
        private Double moistureDeduction;
        private Double impurity;
        private Double impurityDeduction;
        private Double yellowDamage;
        private Double netWeight;
        private Double basePrice;
        private Double qualityDeduction;
        private Double netPrice;
        private Double totalAmount;

        // Setters
        public void setBatchNo(String b) { this.batchNo = b; }
        public void setSupplierName(String s) { this.supplierName = s; }
        public void setSupplierPhone(String p) { this.supplierPhone = p; }
        public void setPurchaserName(String p) { this.purchaserName = p; }
        public void setPaddyType(String t) { this.paddyType = t; }
        public void setWarehouseId(String w) { this.warehouseId = w; }
        public void setWarehouseName(String n) { this.warehouseName = n; }
        public void setTotalWeight(Double tw) { this.totalWeight = tw; }
        public void setMoisture(Double m) { this.moisture = m; }
        public void setMoistureDeduction(Double md) { this.moistureDeduction = md; }
        public void setImpurity(Double i) { this.impurity = i; }
        public void setImpurityDeduction(Double id) { this.impurityDeduction = id; }
        public void setYellowDamage(Double yd) { this.yellowDamage = yd; }
        public void setNetWeight(Double nw) { this.netWeight = nw; }
        public void setBasePrice(Double bp) { this.basePrice = bp; }
        public void setQualityDeduction(Double qd) { this.qualityDeduction = qd; }
        public void setNetPrice(Double np) { this.netPrice = np; }
        public void setTotalAmount(Double ta) { this.totalAmount = ta; }
    }
}
