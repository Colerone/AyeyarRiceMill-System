package com.example.ayeyarricemill;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class InventoryDetailsController {
    // InventoryAddController မှ ပေးမည့် warehouse data
    public static InventoryAddController.Warehouse selectedWarehouse;
    public static String loggedInUserRole = "";
    @FXML private Label totalQtyLabel;
    @FXML private Label varietiesLabel;
    @FXML private Label usedSpaceLabel;
    @FXML private Label availableSpaceLabel;
    @FXML private Label warehouse;

    @FXML private TableView<InventoryItem> detailTable;
    @FXML private TableColumn<InventoryItem, Number> colNo;
    @FXML private TableColumn<InventoryItem, String> colType;
    @FXML private TableColumn<InventoryItem, Number> colQty;
    @FXML private TableColumn<InventoryItem, String > colDate;
    @FXML private TableColumn<InventoryItem, String> colStatus;

    private HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson  = new Gson();

    //backendAPI Path
    private final String API_BASE_URL = "http://localhost:9090/api/inventory_items/warehouses/";
    private final ObservableList<InventoryItem> itemList = FXCollections.observableArrayList();

    @FXML
    public void initialize(){
        if(selectedWarehouse != null){
            calculateDashboardStatus(); // Box 1 3 4 အတွက်
            setupTable(); // table setup
            loadInventoryItems();
            if (warehouse != null) {
                warehouse.setText(selectedWarehouse.getName());
            }

        }else {
            System.out.println("Error: No warehouse selected for detail view.");
        }
    }



    private void calculateDashboardStatus(){
        // Box 1: Total Quantity & Units
        boolean isRaw = "Raw".equalsIgnoreCase(selectedWarehouse.getType());
        String unit = isRaw ? "Tins" : "Bags";
        int currentStock = selectedWarehouse.getCurrentStock() != null ? selectedWarehouse.getCurrentStock() : 0;

        if (totalQtyLabel != null) {
            totalQtyLabel.setText(currentStock + unit);
        }

        // box 3 4 space percentages
        int capacity = selectedWarehouse.getCapacity() != null ? selectedWarehouse.getCapacity() : 1;
        double usedPercentage = ((double) currentStock / capacity) * 100;
        double availablePercentage = 100 - usedPercentage;

        if(usedSpaceLabel != null){
            usedSpaceLabel.setText(String.format("%.0f%%", usedPercentage));
        }

        if(availableSpaceLabel != null){
            availableSpaceLabel.setText(String.format("%.0f%%", availablePercentage));
        }
    }

    private void setupTable() {
        colNo.setCellValueFactory(column ->
                new SimpleIntegerProperty(detailTable.getItems().indexOf(column.getValue()) + 1));

        colType.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getItemName()));
        colQty.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getQuantity()));
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus()));

//        colDate.setCellValueFactory(cell -> {
//            if(cell.getValue().getArrivalDate() != null){
//                try{
//                    String formattedDate = cell.getValue().getArrivalDate().split("T")[0];
//                    return new SimpleStringProperty(formattedDate);
//                }catch(Exception e){
//                    return new SimpleStringProperty("-");
//                }
//            }
//            return new SimpleStringProperty("-");
//        });

        colDate.setCellValueFactory(cellData -> {
            String dateStr = cellData.getValue().getArrivalDate();
            if (dateStr != null && dateStr.contains("T")) {
                // T ပါနေရင် (ISO Format) အလှပြင်မယ်
                return new javafx.beans.property.SimpleStringProperty(dateStr.replace("T", " ").substring(0, 16));
            }
            return new javafx.beans.property.SimpleStringProperty(dateStr);
        });
        detailTable.setItems(itemList);
    }

    private void loadInventoryItems(){
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + selectedWarehouse.getId()))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response->{
                    if(response.statusCode() == 200){
                        List<InventoryItem> data = gson.fromJson(response.body(),
                                new TypeToken<List<InventoryItem>>(){}.getType());

                        Platform.runLater(() -> {
                            itemList.setAll(data);
                            detailTable.refresh();
                            calculateVarieties(data);
                        });
                    }
                })
                .exceptionally(ex->{
                    System.err.println("API Error: " + ex.getMessage());
                    return  null;
                });
    }

    // Box 2 varieties funtion
    private void calculateVarieties(List<InventoryItem> items){
        if(varietiesLabel == null) return;
        if(items == null || items.isEmpty()) {
            varietiesLabel.setText("0 Types");
            return;
        }

        Set<String> uniqueName = new HashSet<>();
        for(InventoryItem item : items){
            if(item.getItemName() != null){
                uniqueName.add(item.getItemName().trim().toLowerCase());
            }
        }

        varietiesLabel.setText(uniqueName.size() + " Types");
    }

    @FXML
    private void handleBack(){
        System.out.println("Returning to warehouse list...");
    }

    public static class InventoryItem {
        private String id;
        private String warehouseId;
        private String itemName;
        private Integer quantity;
        private String unit;
        private String arrivalDate;
        private String status;

        public String getId() { return id; }
        public String getWarehouseId() { return warehouseId; }
        public String getItemName() { return itemName; }
        public Integer getQuantity() { return quantity; }
        public String getUnit() { return unit; }
        public String getArrivalDate() { return arrivalDate; }
        public String getStatus() { return status; }
    }



}
