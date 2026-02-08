package com.example.ayeyarricemill;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Map;

public class riceSaleDetailController {
    @FXML
    private Label lblCustomerName;
    @FXML
    private Label lblPhone;
    @FXML
    private Label lblVoucherNo;
    @FXML
    private Label lblSellerName;
    @FXML
    private Label lblTotalAmount;

    @FXML private TableView<SaleItemDetail> tableItems;
    @FXML private TableColumn<SaleItemDetail, Integer> colNo;
    @FXML private TableColumn<SaleItemDetail, String> colItemName;
    @FXML private TableColumn<SaleItemDetail, String> colWarehouse;
    @FXML private TableColumn<SaleItemDetail, Double> colQty;
    @FXML private TableColumn<SaleItemDetail, Double> colPrice;
    @FXML private TableColumn<SaleItemDetail, Double> colSubTotal;

    public void initData(riceSaleListController.SaleRecordModel record) {
        // Basic Info များကို Label ထဲထည့်ခြင်း
        lblCustomerName.setText(record.getCustomerName());
        lblPhone.setText(record.getPhone() != null ? record.getPhone() : "-");
        lblVoucherNo.setText(record.getVoucherNo());
        lblSellerName.setText(record.getEmployeeName() != null ? record.getEmployeeName() : "-");
        lblTotalAmount.setText(String.format("%,.0f", record.getTotalAmount()));

        // Table Column များ Setup လုပ်ခြင်း
        setupTableColumns();

        // Items List ကို Table ထဲသို့ ထည့်ခြင်း
//        if (record.getItems() != null) {
//            // Backend မှ လာသော List<Object> (Maps) ကို ObservableList ပြောင်းခြင်း
//            ObservableList<Map<String, Object>> itemsData = FXCollections.observableArrayList();
//            for (Object obj : record.getItems()) {
//                if (obj instanceof Map) {
//                    itemsData.add((Map<String, Object>) obj);
//                }
//            }
//            tableItems.setItems(itemsData);
//        }

        if (record.getItems() != null) {
            Gson gson = new Gson();
            String jsonItems = gson.toJson(record.getItems());
            List<SaleItemDetail> itemList = gson.fromJson(jsonItems, new TypeToken<List<SaleItemDetail>>(){}.getType());

            ObservableList<SaleItemDetail> observableList = FXCollections.observableArrayList(itemList);
            tableItems.setItems(observableList);
        }
    }

    private void setupTableColumns() {
        colNo.setCellValueFactory(column ->
                new ReadOnlyObjectWrapper<>(tableItems.getItems().indexOf(column.getValue()) + 1));

        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colWarehouse.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colSubTotal.setCellValueFactory(new PropertyValueFactory<>("subTotal"));
    }

    public static class SaleItemDetail {
        private String itemName;
        private String warehouseName;
        private Double quantity;
        private Double price;
        private Double subTotal;

        // Getters (TableView က data ပြဖို့အတွက် getter တွေ မဖြစ်မနေလိုအပ်ပါတယ်)
        public String getItemName() { return itemName; }
        public String getWarehouseName() { return warehouseName; }
        public Double getQuantity() { return quantity; }
        public Double getPrice() { return price; }
        public Double getSubTotal() { return subTotal; }
    }
}

