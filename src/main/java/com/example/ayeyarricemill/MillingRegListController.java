package com.example.ayeyarricemill;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class MillingRegListController {
    @FXML
    private TableView<MillingRecord> tableMilling;
    @FXML private TableColumn<MillingRecord, Number> colNo;
    @FXML private TableColumn<MillingRecord, String> colDate;
    @FXML private TableColumn<MillingRecord, String> colPaddyType;
    @FXML private TableColumn<MillingRecord, Double> colTotalMilled;
    @FXML private TableColumn<MillingRecord, Void> colAction;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final String API_URL = "http://localhost:9090/api/milling_records";

    @FXML
    public void initialize() {
        setupColumns();
        loadData();
    }

    private void setupColumns() {
        // ၁။ No Column (Auto Increment)
        colNo.setCellValueFactory(column ->
                new ReadOnlyObjectWrapper<>(tableMilling.getItems().indexOf(column.getValue()) + 1)
        );

        // ၂။ Data Columns
        colDate.setCellValueFactory(new PropertyValueFactory<>("millingDate"));
        colPaddyType.setCellValueFactory(new PropertyValueFactory<>("paddyType"));
        colTotalMilled.setCellValueFactory(new PropertyValueFactory<>("inputQtyTins"));

        // ၃။ Action Column (Buttons)
        setupActionButtons();
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
            stage.setScene(new Scene(root));
            stage.show();
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
                            tableMilling.setItems(FXCollections.observableArrayList(list));
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
    }

}
