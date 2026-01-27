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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

public class MillingRegListController {
    @FXML
    private TableView<MillingRecord> tableMilling;
    @FXML private TableColumn<MillingRecord, Number> colNo;
    @FXML private TableColumn<MillingRecord, String> colDate;
    @FXML private TableColumn<MillingRecord, String> colPaddyType;
    @FXML private TableColumn<MillingRecord, Double> colTotalMilled;
    @FXML private TableColumn<MillingRecord, Void> colAction;

    // Filter အတွက် အသစ်ထည့်ထားသော Component
    @FXML private DatePicker filterDatePicker;
    @FXML private Button btnResetFilter;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final String API_URL = "http://localhost:9090/api/milling_records";

    // Data တွေကို သိမ်းထားရန် // အသစ်
    private ObservableList<MillingRecord> masterData = FXCollections.observableArrayList();
    private FilteredList<MillingRecord> filteredData;

    @FXML
    public void initialize() {
        // ၁။ FilteredList ကို initialize လုပ်ပြီး Table ကို ချိတ်မယ် // အသစ်
        filteredData = new FilteredList<>(masterData, p -> true);
        tableMilling.setItems(filteredData);

        setupColumns();
        setupFilterLogic();
        loadData();
    }

    private void setupColumns() {
//        // ၁။ No Column (Auto Increment)
//        colNo.setCellValueFactory(column ->
//                new ReadOnlyObjectWrapper<>(tableMilling.getItems().indexOf(column.getValue()) + 1)
//        );

        // No Column (Index ပြန်တွက်ရန် filteredData ကို သုံးထားသည်)
        colNo.setCellValueFactory(column -> {
            int index = tableMilling.getItems().indexOf(column.getValue());
            return new ReadOnlyObjectWrapper<>(index + 1);
        });

        // ၂။ Data Columns
        colDate.setCellValueFactory(new PropertyValueFactory<>("millingDate"));
        colPaddyType.setCellValueFactory(new PropertyValueFactory<>("paddyType"));
        colTotalMilled.setCellValueFactory(new PropertyValueFactory<>("inputQtyTins"));

        // ၃။ Action Column (Buttons)
        setupActionButtons();
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

            // Record ထဲက ရက်စွဲ (String) ကို LocalDate ပြောင်းပြီး နှိုင်းယှဉ်ခြင်း
            // မှတ်ချက် - MillingRecord ထဲက format နှင့် ကိုက်ညီရပါမည် (ဥပမာ - yyyy-MM-dd)
            try {
                String recordDateStr = record.getMillingDate();
//                LocalDate recordDate = LocalDate.parse(recordDateStr);
//                return recordDate.equals(selectedDate);
                LocalDate recordDate = LocalDate.parse(recordDateStr);
                return recordDate.equals(selectedDate);
            } catch (Exception e) {
                // Parse လုပ်လို့မရရင် console မှာ ပြပေးထားမယ် (Debug လုပ်ဖို့)
                System.err.println("Date format error for: " + record.getMillingDate());
                return false;
            }
        });

        // Table ကို Update လုပ်သည်
//        tableMilling.setItems(filteredData);
        tableMilling.refresh();
    }

    private void setupActionButtons() {
        Callback<TableColumn<MillingRecord, Void>, TableCell<MillingRecord, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btnView = new Button("Detail");
            {
                btnView.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
                btnView.setOnAction(event -> {
                    MillingRecord data = getTableView().getItems().get(getIndex());
                    System.out.println("Viewing: " + data.getBatchNo());
                    showDetailWindow(data);
                    // ဤနေရာတွင် Detail Window ဖွင့်ရန် code ရေးနိုင်သည်
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(btnView);
            }
        };
        colAction.setCellFactory(cellFactory);
    }

    private void showDetailWindow(MillingRecord record) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MillingListDetail.fxml"));
            Parent root = loader.load();

            MillingListDetailController controller = loader.getController();
            controller.setMillingData(record); // ဒီမှာ record တစ်ခုလုံးကို detail ဆီ ပို့လိုက်တာပါ

            Stage stage = new Stage();
            stage.setTitle("Milling Detail - " + record.getBatchNo());
            // ၁။ ဒီ Window မပိတ်မချင်း နောက်က Window ကို နှိပ်လို့မရအောင် လုပ်ခြင်း
            stage.initModality(Modality.APPLICATION_MODAL);

            // ၂။ ဘယ် Window ရဲ့ အပေါ်မှာ ပေါ်လာမှာလဲဆိုတာ သတ်မှတ်ခြင်း (Optional)
//            stage.initOwner(ownerStage);

            // ၃။ Window ပုံစံကို Resize လုပ်လို့မရအောင် သတ်မှတ်ခြင်း (Optional)
            stage.setResizable(false);

            stage.setScene(new Scene(root));
            stage.showAndWait(); // show() အစား showAndWait() ကို သုံးလျှင် ပိုသေချာပါသည်
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .build();

        httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> {
                    if (res.statusCode() == 200) {
                        List<MillingRecord> list = gson.fromJson(res.body(), new TypeToken<List<MillingRecord>>() {}.getType());
                        Platform.runLater(() -> {
//                            tableMilling.setItems(FXCollections.observableArrayList(list));
                            // အဓိက အချက်- table ဆီ တိုက်ရိုက်မထည့်ဘဲ masterData ထဲကို ထည့်ရပါမယ်
                            masterData.setAll(list);
                            // Filter ကိုလည်း ပြန်စစ်ပေးဖို့ လိုပါတယ် (အရင် ရွေးထားတာ ရှိနေရင်)
                            applyFilter(filterDatePicker.getValue());
                        });
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> System.err.println("API Error: " + ex.getMessage()));
                    return null;
                });
    }

    public static class MillingRecord {
        private String id;
        private String millingDate;
        private String batchNo;
        private String paddyType;
        private String sourceWarehouse;
        private String targetWarehouse;
        private Double inputQtyTins;
        private Double headRiceBags;
        private Double brokenRiceBags;
        private Double brokenBranBags;
        private Double branBags;
        private Double totalOutputBags;
        private Double yieldPercentage;
        private String recordedBy;

        public String getId() { return id; }
        public String getMillingDate() { return millingDate; }
        public String getBatchNo() { return batchNo; }
        public String getPaddyType() { return paddyType; }
        public String getSourceWarehouse() { return sourceWarehouse; }
        public String getTargetWarehouse() { return targetWarehouse; }
        public Double getInputQtyTins() { return inputQtyTins; }
        public Double getHeadRiceBags() { return headRiceBags; }
        public Double getBrokenRiceBags() { return brokenRiceBags; }
        public Double getBrokenBranBags() { return brokenBranBags; }
        public Double getBranBags() { return branBags; }
        public Double getTotalOutputBags() { return totalOutputBags; }
        public Double getYieldPercentage() { return yieldPercentage; }
        public String getRecordedBy() { return recordedBy; }
    }

}
