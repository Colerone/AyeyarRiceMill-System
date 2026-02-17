package com.example.ayeyarricemill;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.application.Platform;
import javafx.scene.layout.HBox;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

public class HomeContentController {

    @FXML
    private Label todayMillingLabel;
    @FXML private Label todaySalesLabel;
    @FXML private Label todayPurchaseCountLabel;
    @FXML private PieChart inventoryPieChart;

    @FXML private Label name1, name2, name3, name4, name5;
    @FXML private HBox bar1, bar2, bar3, bar4, bar5;

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public void initialize() {
        fetchTodayMillingTotal();
        fetchTodaySalesTotal();
        fetchTodayPurchaseCount();
        fetchInventorySummary();
        fetchTopSellingRice();
    }

    private void fetchTodayMillingTotal() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9090/api/milling_records/today-total-tins"))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(total -> {
                    Platform.runLater(() -> {
                        try {
                            double value = Double.parseDouble(total);
                            // ကိန်းဂဏန်းကို format လုပ်ပြီး Label မှာ ပြမယ်
                            todayMillingLabel.setText(String.format("%,.0f", value));
                        } catch (Exception e) {
                            todayMillingLabel.setText("0");
                        }
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    private void fetchTodaySalesTotal() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9090/api/sales/today-total-sales"))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(total -> Platform.runLater(() -> {
                    try {
                        double value = Double.parseDouble(total);
                        // ငွေပမာဏဖြစ်လို့ 1,000,000 စသဖြင့် comma လေးတွေနဲ့ လှအောင်ပြမယ်
                        todaySalesLabel.setText(String.format("%,.0f", value));
                    } catch (Exception e) {
                        todaySalesLabel.setText("0");
                    }
                }))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    private void fetchTodayPurchaseCount() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9090/api/paddy_purchases/today-count"))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(count -> Platform.runLater(() -> {
                    try {
                        // အရေအတွက်ဖြစ်လို့ Integer အနေနဲ့ ပြမယ်
                        todayPurchaseCountLabel.setText(count);
                    } catch (Exception e) {
                        todayPurchaseCountLabel.setText("0");
                    }
                }))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    private void fetchInventorySummary() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9090/api/warehouses/inventory-summary"))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    Map<String, Double> summary = gson.fromJson(json, new TypeToken<Map<String, Double>>(){}.getType());

                    Platform.runLater(() -> {
                        updatePieChart(summary);
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    private void updatePieChart(Map<String, Double> summary) {
        double raw = summary.getOrDefault("Raw", 0.0);
        double good = summary.getOrDefault("Good", 0.0);
        double total = raw + good;

        if (total == 0) {
            inventoryPieChart.setTitle("No Inventory Data");
            return;
        }

        // Percentage တွက်ချက်ခြင်း
        double rawPercent = (raw / total) * 100;
        double goodPercent = (good / total) * 100;

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data(String.format("Raw (%.1f%%)", rawPercent), raw),
                new PieChart.Data(String.format("Good (%.1f%%)", goodPercent), good)
        );

        inventoryPieChart.setData(pieChartData);
        inventoryPieChart.setTitle("Inventory Ratio");
        inventoryPieChart.getStylesheets().add(
                getClass().getResource("/design.css").toExternalForm()
        );
    }

    private void fetchTopSellingRice() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9090/api/sales/top-selling"))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    // LinkedHashMap သုံးမှ sorting အစီအစဉ်အတိုင်း ရမှာပါ
                    Map<String, Double> topItems = gson.fromJson(json, new TypeToken<LinkedHashMap<String, Double>>(){}.getType());
                    Platform.runLater(() -> updateTopSellingUI(topItems));
                });
    }

    private void updateTopSellingUI(Map<String, Double> topItems) {
        Label[] names = {name1, name2, name3, name4, name5};
        HBox[] bars = {bar1, bar2, bar3, bar4, bar5};

        // အရင်ရှင်းထားမယ်
        for(int i=0; i<5; i++) {
            names[i].setText("");
            bars[i].setPrefWidth(0);
        }

        if (topItems == null || topItems.isEmpty()) return;

        // အများဆုံးရောင်းရတဲ့ ပမာဏကို ရှာမယ် (Bar အရှည်တွက်ဖို့)
        double maxQty = topItems.values().stream().max(Double::compare).orElse(1.0);
        double maxWidth = 380.0; // HBox ရဲ့ အရှည်ဆုံး limit

        int index = 0;
        for (Map.Entry<String, Double> entry : topItems.entrySet()) {
            if (index >= 5) break;

            // Label မှာ နာမည်နဲ့ အရေအတွက်ပြမယ်
            names[index].setText(String.format("%s (%,.0f bags)", entry.getKey(), entry.getValue()));

            // Bar အရှည်ကို တွက်မယ် (အရေအတွက်များလေ Bar ရှည်လေ)
            double calculatedWidth = (entry.getValue() / maxQty) * maxWidth;
            bars[index].setPrefWidth(calculatedWidth);

            index++;
        }
    }
}