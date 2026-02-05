package com.example.ayeyarricemill;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class riceSaleRegisterController {
    @FXML
    private TableView<SaleItem> tableSale;
    @FXML
    private TableColumn<SaleItem, Integer> colNo;
    @FXML
    private TableColumn<SaleItem, String> colItemName;
    @FXML
    private TableColumn<SaleItem, String> colWarehouse;
    @FXML
    private TableColumn<SaleItem, Double> colQty;
    @FXML
    private TableColumn<SaleItem, Double> colPrice;
    @FXML
    private TableColumn<SaleItem, Double> colSubTotal;
    @FXML
    private TableColumn<SaleItem, Void> colAction;

    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField txtCustomerName, txtPhone, txtQty;
    @FXML
    private ComboBox<MarketItem> comboItem;
    @FXML
    private ComboBox<Warehouse> comboWarehouse;
    @FXML
    private Label lblPricePerBag, lblAvailableStock, lblTotalAmount, lblLabelSale;
    @FXML
    private AnchorPane totalAmountPane;
    @FXML
    private Button btnAddSale, btnSubmit, btnVoucher, btnCalculate;

    public static String loggedInUsername;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final ObservableList<SaleItem> saleDataList = FXCollections.observableArrayList();

    private final String RICE_PRICE_API = "http://localhost:9090/api/rice_price";
    private final String BROKEN_RICE_API = "http://localhost:9090/api/brokenRice_price";
    private final String OTHER_PRICE_API = "http://localhost:9090/api/other_price";
    private final String WAREHOUSE_API = "http://localhost:9090/api/warehouses"; // Warehouse API
    private final String INVENTORY_ITEMS_API = "http://localhost:9090/api/inventory_items/warehouses/";
    private final String DEDUCT_STOCK_API = "http://localhost:9090/api/inventory-logic/deduct-stock";

    @FXML
    public void initialize() {

        datePicker.setValue(LocalDate.now());

        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                // ဒီနေ့နဲ့ မတူတဲ့ ရက်မှန်သမျှကို Disable လုပ်ခြင်း
                if (date != null && !date.equals(LocalDate.now())) {
                    setDisable(true);
                    // ပိတ်ထားတဲ့ ရက်တွေကို အရောင်မှိန်ပြချင်ရင် (Optional)
                    setStyle("-fx-background-color: #f4f4f4; -fx-text-fill: #b0b0b0;");
                }
            }
        });

        // ၁။ စစချင်းမှာ ညာဘက်အခြမ်းကို ဖျောက်ထားမယ်
        lblLabelSale.setVisible(false);
        tableSale.setVisible(false);
        totalAmountPane.setVisible(false);
        btnCalculate.setVisible(false);
        btnVoucher.setVisible(false);

        setupTableColumns();
        loadMarketPrices();
        loadGoodWarehouses();

        // Item ရွေးလိုက်ရင် ဈေးနှုန်းပြောင်းဖို့ listener
        comboItem.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                lblPricePerBag.setText(String.format("%,.0f", newVal.getPrice()) + " MMK");
                updateStockLabel();
            }
        });

        comboWarehouse.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateStockLabel();
        });
    }

    private void updateStockLabel() {
        MarketItem selectedItem = comboItem.getValue();
        Warehouse selectedWH = comboWarehouse.getValue();

        // နှစ်ခုလုံး ရွေးထားမှသာ API လှမ်းခေါ်မယ်
        if (selectedItem == null || selectedWH == null) {
            lblAvailableStock.setText("0 Bag");
            return;
        }

        String url = INVENTORY_ITEMS_API + selectedWH.getId();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    // InventoryItem list အဖြစ်ပြောင်းလဲခြင်း
                    List<InventoryItemDTO> items = gson.fromJson(res.body(), new TypeToken<List<InventoryItemDTO>>() {
                    }.getType());

                    // ရွေးထားတဲ့ Item Name နဲ့ တူတဲ့ ပစ္စည်းကိုပဲ ရှာမည်
                    return items.stream()
                            .filter(i -> i.getItemName() != null &&
                                    i.getItemName().trim().equalsIgnoreCase(selectedItem.getName().trim()))
                            .findFirst()
                            .map(InventoryItemDTO::getQuantity)
                            .orElse(0); // မတွေ့ရင် 0
                })
                .thenAccept(stock -> Platform.runLater(() -> {
                    lblAvailableStock.setText(stock + " Bags");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> lblAvailableStock.setText("Error"));
                    return null;
                });
    }


    private void loadGoodWarehouses() {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(WAREHOUSE_API)).GET().build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    List<Warehouse> all = gson.fromJson(res.body(), new TypeToken<List<Warehouse>>() {
                    }.getType());
                    // Type က "Good" ဖြစ်တာတွေကိုပဲ filter လုပ်မယ်
                    return all.stream()
                            .filter(w -> "Good".equalsIgnoreCase(w.getType()))
                            .collect(Collectors.toList());
                })
                .thenAccept(list -> Platform.runLater(() -> {
                    if (!list.isEmpty()) {
                        comboWarehouse.setItems(FXCollections.observableArrayList(list));
                        // --- ပထမဆုံး Warehouse ကို Auto ရွေးမည် ---
                        comboWarehouse.getSelectionModel().selectFirst();

                        // Item လည်း ရွေးထားပြီးသားဆိုရင် Stock Label တန်းပေါ်လာအောင် ခေါ်ပေးမယ်
                        updateStockLabel();
                    }


                    comboWarehouse.setConverter(new StringConverter<>() {
                        @Override
                        public String toString(Warehouse w) {
                            return w == null ? "" : w.getName();
                        }

                        @Override
                        public Warehouse fromString(String s) {
                            return null;
                        }
                    });
                }));
    }

    private void setupTableColumns() {
        colNo.setCellValueFactory(new PropertyValueFactory<>("no"));
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colWarehouse.setCellValueFactory(new PropertyValueFactory<>("warehouse"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colSubTotal.setCellValueFactory(new PropertyValueFactory<>("subTotal"));

        setupActionColumn();
        tableSale.setItems(saleDataList);
    }

    private void loadMarketPrices() {
        // API ၃ ခုလုံးကနေ data ကို တစ်ပြိုင်တည်းလှမ်းခေါ်မယ်
        CompletableFuture<List<MarketItem>> riceFetch = fetchData(RICE_PRICE_API, "rice");
        CompletableFuture<List<MarketItem>> brokenFetch = fetchData(BROKEN_RICE_API, "broken");
        CompletableFuture<List<MarketItem>> otherFetch = fetchData(OTHER_PRICE_API, "other");

        CompletableFuture.allOf(riceFetch, brokenFetch, otherFetch).thenRun(() -> {
            List<MarketItem> allItems = new ArrayList<>();
            allItems.addAll(riceFetch.join());
            allItems.addAll(brokenFetch.join());
            allItems.addAll(otherFetch.join());

            Platform.runLater(() -> {
                if (!allItems.isEmpty()) {
                    comboItem.setItems(FXCollections.observableArrayList(allItems));
                    // --- ပထမဆုံး Item ကို Auto ရွေးမည် ---
                    comboItem.getSelectionModel().selectFirst();

                    // ဈေးနှုန်း label ကို update လုပ်ပေးခြင်း
                    MarketItem first = allItems.get(0);
                    lblPricePerBag.setText(String.format("%,.0f", first.getPrice()) + " MMK");
                }


                comboItem.setConverter(new javafx.util.StringConverter<MarketItem>() {
                    @Override
                    public String toString(MarketItem item) {
                        return item == null ? "" : item.getName();
                    }

                    @Override
                    public MarketItem fromString(String s) {
                        return null;
                    }
                });
            });
        });
    }

//    private CompletableFuture<List<MarketItem>> fetchData(String url, String type) {
//        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
//        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
//                .thenApply(res -> {
//                    List<MarketItem> items = new ArrayList<>();
//                    if (res.statusCode() == 200) {
//                        // Backend modal တွေက variable name ကွဲနေတာကို ညှိပြီး MarketItem object ထဲထည့်မယ်
//                        if (type.equals("other")) {
//                            List<OtherPriceRaw> raw = gson.fromJson(res.body(), new TypeToken<List<OtherPriceRaw>>(){}.getType());
////                            raw.forEach(r -> items.add(new MarketItem(r.Type, r.price)));
//                            raw.forEach(r -> {
//                                if (r.Type != null) {
//                                    items.add(new MarketItem(r.Type, r.price != null ? r.price : 0.0));
//                                }
//                            });
//                        }

    /// /                        else {
    /// /                            List<PriceRaw> raw = gson.fromJson(res.body(), new TypeToken<List<PriceRaw>>(){}.getType());
    /// /                            raw.forEach(r -> items.add(new MarketItem(r.riceType, r.price)));
    /// /                        }
//                        else if (type.equals("broken")) {
//                            // BrokenRicePrice ကလည်း riceType ကိုပဲ သုံးတာမို့ PriceRaw နဲ့ ဖတ်လို့ရပါတယ်
//                            List<PriceRaw> raw = gson.fromJson(res.body(), new TypeToken<List<PriceRaw>>(){}.getType());
//                            raw.forEach(r -> {
//                                if (r.riceType != null) {
//                                    items.add(new MarketItem(r.riceType, r.price != null ? r.price : 0.0));
//                                }
//                            });
//                        }
//                        else {
//                            // Rice Price အတွက်
//                            List<PriceRaw> raw = gson.fromJson(res.body(), new TypeToken<List<PriceRaw>>(){}.getType());
//                            raw.forEach(r -> {
//                                if (r.riceType != null) {
//                                    items.add(new MarketItem(r.riceType, r.price != null ? r.price : 0.0));
//                                }
//                            });
//                        }
//                    }
//                    return items;
//                });
//    }
    private CompletableFuture<List<MarketItem>> fetchData(String url, String type) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    List<MarketItem> items = new ArrayList<>();
                    if (res.statusCode() == 200) {
                        try {
                            if (type.equals("other")) {
                                // OtherPrice အတွက် (Field Name: Type)
                                List<OtherPriceRaw> raw = gson.fromJson(res.body(), new TypeToken<List<OtherPriceRaw>>() {
                                }.getType());
                                for (OtherPriceRaw r : raw) {
                                    if (r.Type != null) {
                                        items.add(new MarketItem(r.Type, r.price != null ? r.price : 0.0));
                                    }
                                }
                            } else {
                                // Rice နဲ့ Broken Rice အတွက် (Field Name: riceType)
                                List<PriceRaw> raw = gson.fromJson(res.body(), new TypeToken<List<PriceRaw>>() {
                                }.getType());
                                for (PriceRaw r : raw) {
                                    if (r.riceType != null) {
                                        items.add(new MarketItem(r.riceType, r.price != null ? r.price : 0.0));
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return items;
                });
    }


    @FXML
    private void handleAddToSale() {
        MarketItem selectedItem = comboItem.getSelectionModel().getSelectedItem();
        Warehouse warehouse = comboWarehouse.getValue();
        String qtyStr = txtQty.getText();

        if (selectedItem == null || warehouse == null || qtyStr.isEmpty()) {
            showError("Enter all inputs");
            return;
        }

        double qty = Double.parseDouble(qtyStr);
        try {
            qty = Double.parseDouble(qtyStr);
        } catch (NumberFormatException e) {
            showError("Please write just number");
            return;
        }

        if (qty <= 0) {
            showError("Quantity must greater than 0");
            return;
        }

        // --- Stock Validation (အသစ်ထည့်ထားသောအပိုင်း) ---
        // lblAvailableStock ထဲက "162 Bags" ဆိုတဲ့ စာသားထဲကနေ 162 ကိုပဲ ယူမယ်
        String stockText = lblAvailableStock.getText().replace(" Bags", "").replace(" Bag", "").trim();
        double availableStock = 0;

        try {
            if (!stockText.equalsIgnoreCase("Error") && !stockText.isEmpty()) {
                availableStock = Double.parseDouble(stockText);
            }
        } catch (NumberFormatException e) {
            availableStock = 0;
        }

        if (qty > availableStock) {
            showError(" Insufficient stock. The current available stock is only " + availableStock + " bags.");
            return; // ရှိတဲ့ stock ထက် ပိုနေရင် table ထဲ ထည့်မပေးဘဲ ဒီမှာတင် ရပ်လိုက်မယ်
        }


        double subTotal = qty * selectedItem.getPrice();

        // Table ထဲ ထည့်မယ်
        SaleItem newItem = new SaleItem(
                saleDataList.size() + 1,
                selectedItem.getName(),
                warehouse.getName(),
                warehouse.getId(),

                qty,
                selectedItem.getPrice(),
                subTotal
        );
        saleDataList.add(newItem);

        // ညာဘက်အခြမ်းကို ဖော်မယ်
        lblLabelSale.setVisible(true);
        tableSale.setVisible(true);
//        totalAmountPane.setVisible(true);
//        btnVoucher.setVisible(true);
        btnCalculate.setVisible(true);

//        calculateTotal();
        clearInputs();
    }

    @FXML
    private void handleVoucher() {
        if (saleDataList.isEmpty()) {
            showError("No items to sale");
            return;
        }

        String custName = txtCustomerName.getText().isEmpty() ? "Unknown Customer" : txtCustomerName.getText();
        String phone = txtPhone.getText().isEmpty() ? "-" : txtPhone.getText();
        // Voucher ထုတ်တဲ့အခါ Inventory ထဲက Stock နှုတ်မယ့် List
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (SaleItem item : saleDataList) {
            // Backend Map structure: { "warehouseName": "", "itemName": "", "quantity": 0 }
            String warehouseNameEncoded = item.getWarehouse().replace(" ", "%20");
            String finalUrl = "http://localhost:9090/api/inventory-logic/deduct/" + warehouseNameEncoded;// Space ပါရင် error မတက်အောင် encode လုပ်ခြင်း
            Map<String, Object> payload = new HashMap<>();
            payload.put("warehouseName", item.getWarehouse()); // Name သုံးရမည် (Backend logic အရ)
            payload.put("itemName", item.getItemName());
            payload.put("quantity", (int) item.getQty());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(finalUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                    .build();

            futures.add(httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(res -> {
                        if (res.statusCode() != 200) {
                            // Error message ကို ပိုသိသာအောင် ထုတ်ပြပါ
                            String errorMsg = res.body();
                            Platform.runLater(() -> showError("Failed to deduct stock for " + item.getItemName() + ": " + res.body()));
                            throw new RuntimeException("API Error");

                        }
                    }));
        }

        // အားလုံးပြီးစီးသွားမှ လုပ်ဆောင်မည့် အပိုင်း
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> Platform.runLater(() -> {
                    switchToVoucherScene(custName, phone, new ArrayList<>(saleDataList));
                    showInfo("Sale registered and stock updated!");
                    saleDataList.clear();
                    calculateTotal();
                    tableSale.setVisible(false);
                    totalAmountPane.setVisible(false);
                    btnVoucher.setVisible(false);
                    btnCalculate.setVisible(false);
                    txtCustomerName.clear();
                    txtPhone.clear();
                }))
                .exceptionally(ex -> {
                    // တစ်ခုခုမှားယွင်းခဲ့ရင် console မှာ ကြည့်နိုင်အောင်
                    ex.printStackTrace();
                    return null;
                });
    }

    private void switchToVoucherScene(String customerName, String phone, List<SaleItem> items) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("riceSaleVocher.fxml"));
            Parent root = loader.load();

            // Voucher Controller ကို ရယူပြီး data ပို့ပေးခြင်း
            riceSaleVoucherController controller = loader.getController();
            controller.setData(customerName, phone, items, loggedInUsername);

            // လက်ရှိ Window (Stage) ကို ယူပြီး Scene အသစ် ပြောင်းခြင်း
            Stage stage = (Stage) btnVoucher.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Rice Sale Voucher");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not load Voucher Page: " + e.getMessage());
        }
    }


    @FXML
    private void handleAmount() {
        calculateTotal();
        totalAmountPane.setVisible(true);
        btnVoucher.setVisible(true);
    }

    private void calculateTotal() {
        double total = saleDataList.stream().mapToDouble(SaleItem::getSubTotal).sum();
        lblTotalAmount.setText(String.format("%,.0f", total));
    }

    private void setupActionColumn() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnDelete = new Button("\uD83D\uDDD1");

            {
                btnDelete.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-cursor: hand;");
                btnDelete.setOnAction(event -> {
                    SaleItem item = getTableView().getItems().get(getIndex());
                    saleDataList.remove(item);
                    calculateTotal();
                    updateRowNumbers();
                    if(saleDataList.isEmpty()) {
                        tableSale.setVisible(false);
                        lblLabelSale.setVisible(false);
                        btnCalculate.setVisible(false);
                        totalAmountPane.setVisible(false);
                        btnVoucher.setVisible(false);
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDelete);
            }
        });
    }

    private void updateRowNumbers() {
        for (int i = 0; i < saleDataList.size(); i++) {
            saleDataList.get(i).setNo(i + 1);
        }
    }

    private void clearInputs() {
        txtQty.clear();
        comboItem.getSelectionModel().clearSelection();
        comboWarehouse.getSelectionModel().clearSelection();
        lblAvailableStock.setText("0 Bags");
        lblPricePerBag.setText("0");

        datePicker.setValue(LocalDate.now());
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }

    private void showInfo(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
            alert.setHeaderText(null);
            alert.show();
        });
    }

    public static class Warehouse {
        private String id, name, type;
        private int currentStock;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public int getCurrentStock() {
            return currentStock;
        }
    }

    public static class InventoryItemDTO {
        private String itemName;
        private Integer quantity;

        public String getItemName() {
            return itemName;
        }

        public Integer getQuantity() {
            return quantity;
        }
    }

    public static class MarketItem {
        private String name;
        private double price;

        public MarketItem(String name, double price) {
            this.name = name;
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }
    }

    public static class SaleItem {
        private int no;
        private String itemName, warehouse,warehouseId;
        private double qty, price, subTotal;

        public SaleItem(int no, String itemName, String warehouse,String warehouseId, double qty, double price, double subTotal) {
            this.no = no;
            this.itemName = itemName;
            this.warehouse = warehouse;
            this.warehouseId = warehouseId;
            this.qty = qty;
            this.price = price;
            this.subTotal = subTotal;
        }

        public int getNo() {
            return no;
        }
        public String getWarehouseId() { return warehouseId; }

        public void setNo(int no) {
            this.no = no;
        }

        public String getItemName() {
            return itemName;
        }

        public String getWarehouse() {
            return warehouse;
        }

        public double getQty() {
            return qty;
        }

        public double getPrice() {
            return price;
        }

        public double getSubTotal() {
            return subTotal;
        }
    }

    public static class InventoryItem {
        private String id;
        private String warehouseId;
        private String itemName;
        private Integer quantity;
        private String unit;
        private String arrivalDate;
        private String status;

        public void setItemName(String itemName) { this.itemName = itemName; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public String getId() { return id; }
        public String getWarehouseId() { return warehouseId; }
        public String getItemName() { return itemName; }
        public Integer getQuantity() { return quantity; }
        public String getUnit() { return unit; }
        public String getArrivalDate() { return arrivalDate; }
        public String getStatus() { return status; }
    }

    private static class PriceRaw {
        @SerializedName("riceType")
        String riceType;
        @SerializedName("price")
        Double price;
    }

    private static class OtherPriceRaw {
        @SerializedName("Type")
        String Type;

        @SerializedName("price")
        Double price;
    }
}


