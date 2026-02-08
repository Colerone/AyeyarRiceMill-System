package com.example.ayeyarricemill;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

public class riceSaleListController {
    @FXML
    private TableView<SaleRecordModel> tableSales;
    @FXML
    private TableColumn<SaleRecordModel, Integer> colNo;
    @FXML
    private TableColumn<SaleRecordModel, String> colDate;
    @FXML
    private TableColumn<SaleRecordModel, String> colCustomerName;
    @FXML
    private TableColumn<SaleRecordModel, String> colVoucherNo;
    @FXML
    private TableColumn<SaleRecordModel, Void> colAction;

    @FXML
    private DatePicker filterDatePicker;
    @FXML
    private Button btnResetFilter;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final ObservableList<SaleRecordModel> masterData = FXCollections.observableArrayList();

    // Backend API URL
    private final String SALE_API = "http://localhost:9090/api/sales";
    private FilteredList<SaleRecordModel> filteredData;

    @FXML
    public void initialize() {
        filteredData = new FilteredList<>(masterData, p -> true);
        tableSales.setItems(filteredData);

        setupTableColumns();
        loadSaleRecords();
        setupFilterLogic();
    }

    private void setupTableColumns() {
        // အစဉ်လိုက် နံပါတ်ပြရန် (No column)
        colNo.setCellValueFactory(column ->
                new ReadOnlyObjectWrapper<>(tableSales.getItems().indexOf(column.getValue()) + 1));

        colDate.setCellValueFactory(new PropertyValueFactory<>("saleDate"));
        colDate.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText("");
                } else {
                    // 2026-02-07T14:18:48.169 → 2026-02-07
                    setText(item.split("T")[0]);
                }
            }
        });
        colCustomerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colVoucherNo.setCellValueFactory(new PropertyValueFactory<>("voucherNo"));

        setupActionColumn();
    }

    private void setupActionColumn() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnDetail = new Button("Detail");

            {
                btnDetail.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");
                btnDetail.setOnAction(event -> {
                    SaleRecordModel record = getTableView().getItems().get(getIndex());
                    goToDetailPage(record);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnDetail);
                }
            }
        });
    }

    private void setupFilterLogic() {
        // Master Data ကို FilteredList ထဲ ထည့်ထားသည်
//        filteredData = new FilteredList<>(masterData, p -> true);

        // DatePicker တန်ဖိုး ပြောင်းလဲမှုအား နားထောင်ခြင်း
        filterDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyFilter(newValue);
        });

        // Reset Button နှိပ်လျှင် Filter ဖျက်ရန်
        if (btnResetFilter != null) {
            btnResetFilter.setOnAction(e -> {
                filterDatePicker.setValue(null);
                applyFilter(null);
            });
        }
    }

    private void applyFilter(LocalDate selectedDate) {
        filteredData.setPredicate(record -> {
            // ရက်စွဲ မရွေးထားလျှင် အကုန်ပြမည်
            if (selectedDate == null) {
                return true;
            }

            try {
                String recordDateStr = record.getSaleDate();
//                LocalDate recordDate = LocalDate.parse(recordDateStr);
//                return recordDate.equals(selectedDate);
                LocalDate recordDate = LocalDate.parse(recordDateStr.split("T")[0]);
                return recordDate.equals(selectedDate);
            } catch (Exception e) {
                // Parse လုပ်လို့မရရင် console မှာ ပြပေးထားမယ် (Debug လုပ်ဖို့)
                System.err.println("Date format error for: " + record.getSaleDate());
                return false;
            }
        });

        // Table ကို Update လုပ်သည်
//        tableMilling.setItems(filteredData);
        tableSales.refresh();
    }

    /**
     * Backend API မှ အရောင်းမှတ်တမ်းများကို ဆွဲယူခြင်း
     */
    private void loadSaleRecords() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SALE_API))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    // JSON string ကို Java List အဖြစ်ပြောင်းလဲခြင်း
                    List<SaleRecordModel> records = gson.fromJson(json, new TypeToken<List<SaleRecordModel>>() {
                    }.getType());

                    Platform.runLater(() -> {
                        masterData.setAll(records);

                        applyFilter(filterDatePicker.getValue());
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        System.out.println("API ချိတ်ဆက်မှု အဆင်မပြေပါ");
                    });
                    return null;
                });
    }


    private void goToDetailPage(SaleRecordModel record) {
        try {
            // detail fxml ဖိုင်လမ်းကြောင်းကို သေချာစစ်ဆေးပါ
            FXMLLoader loader = new FXMLLoader(getClass().getResource("riceSaleDetail.fxml"));
            Parent root = loader.load();

            // Detail Controller သို့ data လှမ်းပို့ခြင်း (Controller အမည်ကို ကိုက်ညီအောင် ပြင်ပါ)
             riceSaleDetailController detailController = loader.getController();
            detailController.initData(record);


            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
//            stage.setResizable(false);
            stage.setScene(new Scene(root));
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/example/ayeyarricemill/images/AppImage (1).jpg")));
            stage.setTitle("Sale Detail - " + record.getVoucherNo());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Detail Page ကို ဖွင့်၍မရပါ");
        }
    }

    /**
     * JSON Data ကို လက်ခံမည့် Model Class (Internal)
     */
    public static class SaleRecordModel {
        private String id;
        private String voucherNo;
        private String customerName;
        private String phone;
        private String saleDate; // Backend မှ LocalDateTime ကို String အဖြစ် ရရှိမည်
        private Double totalAmount;
        private String employeeName;
        private List<Object> items;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public String getVoucherNo() {
            return voucherNo;
        }

        public String getCustomerName() {
            return customerName;
        }

        public String getSaleDate() {
            return saleDate;
        }

        public Double getTotalAmount() {
            return totalAmount;
        }

        public String getPhone() {
            return phone;
        }

        public String getEmployeeName() {
            return employeeName;
        }

        public List<Object> getItems() {
            return items;
        }
    }
}
