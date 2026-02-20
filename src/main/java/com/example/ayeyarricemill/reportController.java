package com.example.ayeyarricemill;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class reportController {
    @FXML private ComboBox<String> comboMonth;
    @FXML private Button btnGenerate;
    @FXML private Label lblIncome, lblExpense, lblProfit, lblProfitPercentage, lblProfitStatus, lblProfitSign;
    @FXML private LineChart<String, Number> lineChart;
    @FXML private BarChart<String, Number> barChartMilling;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final String BASE_URL = "http://localhost:9090/api/transactions";
    private final String MILLING_URL = "http://localhost:9090/api/milling_records";
    private final DecimalFormat df = new DecimalFormat("#,###");

    @FXML
    public void initialize() {
        // လအမည်များထည့်ခြင်း
        List<String> months = Arrays.stream(Month.values())
                .map(m -> m.getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                .collect(Collectors.toList());
        comboMonth.setItems(FXCollections.observableArrayList(months));

        // လက်ရှိလကို Default ရွေးထားခြင်း
        String currentMonth = LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        comboMonth.setValue(currentMonth);

        // ၃။ Button နှိပ်ရင် Data ပြောင်းဖို့ ချိတ်ဆက်ခြင်း
        btnGenerate.setOnAction(event -> handleGenerateReport());

        String chartStyle =
                ".default-color0.chart-series-line { -fx-stroke: #1a7eca; } " +
                        ".default-color0.chart-line-symbol { -fx-background-color: #1a7eca, white; } " +
                        ".default-color1.chart-series-line { -fx-stroke: #FF0000; } " +
                        ".default-color1.chart-line-symbol { -fx-background-color: #FF0000, white; } " +
                        ".chart-legend-item-symbol.series0 { -fx-background-color: #1a7eca, white; } " +
                        ".chart-legend-item-symbol.series1 { -fx-background-color: #FF0000, white; }";

        lineChart.setStyle(chartStyle);

        // Chart ရဲ့ Animation ကို ပိတ်ထားရင် data update ဖြစ်တာ ပိုမြန်တယ်
        lineChart.setAnimated(true);
        barChartMilling.setAnimated(true); // BarChart ကိုပါ Animation ပိတ်ထားပါ

        // အစဦး Data ဆွဲတင်ခြင်း
        handleGenerateReport();
    }

    @FXML
    private void handleGenerateReport() {
        String selectedMonthName = comboMonth.getValue();
        if (selectedMonthName == null) return;

        Month month = Month.valueOf(selectedMonthName.toUpperCase());
        int year = LocalDate.now().getYear();

        fetchDataAndProcess(month, year);

        handleMillingReport();
    }

    private void fetchDataAndProcess(Month month, int year) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/all"))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    try {
                        List<Map<String, Object>> allTransactions = objectMapper.readValue(json, new TypeReference<>() {});
                        Platform.runLater(() -> processTransactions(allTransactions, month, year));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private void processTransactions(List<Map<String, Object>> allData, Month targetMonth, int year) {
        double currentIncome = 0;
        double currentExpense = 0;

        double lastIncome = 0;
        double lastExpense = 0;

        Month lastMonth = targetMonth.minus(1);
        int lastMonthYear = year;

        if (targetMonth == Month.JANUARY) {
            lastMonth = Month.DECEMBER;
            lastMonthYear = year - 1;
        }


        Map<Integer, Double> incomeMap = new TreeMap<>();
        Map<Integer, Double> expenseMap = new TreeMap<>();

        for (Map<String, Object> item : allData) {
            String timeStr = (String) item.get("timestamp");
            LocalDateTime ldt = LocalDateTime.parse(timeStr);
            double amount = ((Number) item.get("amount")).doubleValue();
            String type = (String) item.get("type");
            String category = (String) item.get("category"); // Category ကို ယူမယ်

            // စစ်ထုတ်ခြင်း: မှန်ကန်သော လ၊ ခုနှစ် နှင့် "Business" category ဖြစ်မှ ယူမည်
            if (ldt.getMonth() == targetMonth && ldt.getYear() == year && "Business".equalsIgnoreCase(category)) {
                int day = ldt.getDayOfMonth();
                if ("Income".equalsIgnoreCase(type)) {
                    currentIncome += amount;
                    incomeMap.put(day, incomeMap.getOrDefault(day, 0.0) + amount);
                } else if ("Expense".equalsIgnoreCase(type)) {
                    currentExpense += amount;
                    expenseMap.put(day, expenseMap.getOrDefault(day, 0.0) + amount);
                }
            }

            // Last Month Data စုခြင်း
            if (ldt.getMonth() == lastMonth &&
                    ldt.getYear() == lastMonthYear &&
                    "Business".equalsIgnoreCase(category)) {

                if ("Income".equalsIgnoreCase(type)) {
                    lastIncome += amount;
                } else if ("Expense".equalsIgnoreCase(type)) {
                    lastExpense += amount;
                }
            }

        }

        // UI Labels Update
        // --- UI Labels Update ---
        lblIncome.setText(df.format(currentIncome));
        lblExpense.setText(df.format(currentExpense));

        double profit = currentIncome - currentExpense;
        lblProfit.setText(df.format(Math.abs(profit))); // ပမာဏကိုပဲပြမယ်

        double lastProfit = lastIncome - lastExpense;
        double percentChange = 0;

// ၁။ Percentage Change ကိုတွက်ခြင်း
        if (lastProfit != 0) {
            // ပုံသေနည်း: ((လက်ရှိ - အရင်) / အရင်၏ absolute value) * 100
            percentChange = ((profit - lastProfit) / Math.abs(lastProfit)) * 100;
        } else {
            // အရင်လက 0 ဖြစ်နေရင်
            if (profit == 0) {
                percentChange = 0;
            } else {
                percentChange = 100; // အသစ်တိုးလာတာ သို့မဟုတ် အသစ်လျော့သွားတာ (100% လို့ပဲ သတ်မှတ်မယ်)
            }
        }

        lblProfitPercentage.setText(String.format("%.1f%%", Math.abs(percentChange)));

// ၂။ 🔴 အရေးကြီးဆုံးအပိုင်း- Status (higher/lower) သတ်မှတ်ခြင်း
// တွက်ချက်ထားတဲ့ percentChange ပေါ်မှာမမူတည်ဘဲ လက်ရှိ Profit နဲ့ အရင်လ Profit ကို တိုက်ရိုက်နှိုင်းယှဉ်ရပါမယ်
        if (profit > lastProfit) {
            lblProfitStatus.setText("higher");
        } else if (profit < lastProfit) {
            lblProfitStatus.setText("lower");
        } else {
            lblProfitStatus.setText("same");
        }

// ၃။ Sign (+ သို့မဟုတ် -) သတ်မှတ်ခြင်း
        if (profit == 0) {
            lblProfitSign.setText("");
        } else {
            lblProfitSign.setText(profit > 0 ? "+" : "-");
        }

//        if (currentIncome > 0 || currentExpense > 0) {
//            // Income ရှိမှ ရာခိုင်နှုန်းတွက်လို့ရမှာမို့လို့ပါ (Division by zero error မတက်အောင်)
//            double percent = 0;
//            if (currentIncome > 0) {
//                percent = (profit / currentIncome) * 100;
//            } else {
//                // Income မရှိဘဲ Expense ပဲရှိရင် -100% လို့ သတ်မှတ်နိုင်ပါတယ်
//                percent = -100.0;
//            }
//
//            lblProfitPercentage.setText(String.format("%.1f", Math.abs(percent)));
//
//            if (profit > 0) {
//                lblProfitStatus.setText("higher");
//            } else if (profit < 0) {
//                lblProfitStatus.setText("lower");
//            } else {
//                lblProfitStatus.setText("balanced");
//            }
//        } else {
//            // Data လုံးဝမရှိလျှင် အားလုံးကို reset ချမည်
//            lblProfitPercentage.setText("0");
//            lblProfitStatus.setText(""); // ဘာစာသားမှမပြတော့ပါ
//            lblProfitSign.setText("");
//        }

        // Update Chart
        updateChart(incomeMap, expenseMap, targetMonth.length(LocalDate.now().isLeapYear()));
    }

    private void updateChart(Map<Integer, Double> incomeData, Map<Integer, Double> expenseData, int daysInMonth) {
        lineChart.getData().clear();

        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");

        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expense");

        for (int i = 1; i <= daysInMonth; i++) {
            String dayLabel = String.valueOf(i);
            incomeSeries.getData().add(new XYChart.Data<>(dayLabel, incomeData.getOrDefault(i, 0.0)));
            expenseSeries.getData().add(new XYChart.Data<>(dayLabel, expenseData.getOrDefault(i, 0.0)));
        }

        lineChart.getData().addAll(incomeSeries, expenseSeries);

        // Chart အရောင်များကို အပြာ နှင့် အနီ သတ်မှတ်ခြင်း (CSS Override)
        // Series 0 (Income) -> Blue, Series 1 (Expense) -> Red
        Platform.runLater(() -> {
            if (incomeSeries.getNode() != null) {
                incomeSeries.getNode().setStyle("-fx-stroke: #1a7eca;"); // Line color
            }
            for (XYChart.Data<String, Number> data : incomeSeries.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-background-color: #1a7eca, blue;");
                }
            }



            if (expenseSeries.getNode() != null) {
                expenseSeries.getNode().setStyle("-fx-stroke: #FF0000; -fx-stroke-width: 2px;"); // Line color
            }
            for (XYChart.Data<String, Number> data : expenseSeries.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-background-color: #FF0000, red;");
                }
            }

            // Symbol (အဝိုင်းလေးများ) အရောင်ပါ ပြောင်းချင်လျှင်
//            for (XYChart.Data<String, Number> data : incomeSeries.getData()) {
//                if (data.getNode() != null) data.getNode().setStyle("-fx-background-color: #1a7eca, blue;");
//            }
//            for (XYChart.Data<String, Number> data : expenseSeries.getData()) {
//                if (data.getNode() != null) data.getNode().setStyle("-fx-background-color: #FF0000, red;");
//            }

            // *** အရေးကြီးဆုံးအပိုင်း- Chart အောက်က Legend (Income/Expense စာသားဘေးကအကွက်) ကို အရောင်ပြောင်းခြင်း ***
            // default အားဖြင့် JavaFX က series order အတိုင်း default color တွေပေးတတ်လို့ manual ပြန်သတ်မှတ်ပေးရပါတယ်
//            for (Node node : lineChart.lookupAll(".chart-legend-item-symbol")) {
//                for (String styleClass : node.getStyleClass()) {
//                    if (styleClass.contains("series0")) { // Income series
//                        node.setStyle("-fx-background-color: #1a7eca, blue;");
//                    } else if (styleClass.contains("series1")) { // Expense series
//                        node.setStyle("-fx-background-color: #FF0000, red;");
//                    }
//                }
//            }

//            for (Node node : lineChart.lookupAll(".chart-legend-item-symbol")) {
//                // စာသားကို ကြည့်ပြီး အရောင်ပေးခြင်း (ဒါမှ လွဲမှာမဟုတ်တော့ပါ)
//                Parent item = node.getParent();
//                if (item != null) {
//                    for (Node child : item.getChildrenUnmodifiable()) {
//                        if (child instanceof Label) {
//                            Label label = (Label) child;
//                            if (label.getText().equals("Income")) {
//                                node.setStyle("-fx-background-color: #1a7eca, blue;");
//                            } else if (label.getText().equals("Expense")) {
//                                node.setStyle("-fx-background-color: #FF0000, red;");
//                            }
//                        }
//                    }
//                }
//            }

            for (Node node : lineChart.lookupAll(".chart-legend-item")) {
                if (node instanceof Label) {
                    Label legendLabel = (Label) node;
                    if (legendLabel.getText().equals("Income")) {
                        // စာသားကို အပြာရောင်ပြောင်းမယ်
                        legendLabel.setStyle("-fx-text-fill: #1a7eca; -fx-font-weight: bold;");
                    } else if (legendLabel.getText().equals("Expense")) {
                        // စာသားကို အနီရောင်ပြောင်းမယ်
                        legendLabel.setStyle("-fx-text-fill: #FF0000; -fx-font-weight: bold;");
                    }
                }
            }

            // --- ၃။ Legend ဘေးက သင်္ကေတ (Symbol) အရောင်ပြောင်းခြင်း ---
            for (Node symbol : lineChart.lookupAll(".chart-legend-item-symbol")) {
                Parent parent = symbol.getParent();
                if (parent instanceof Label) {
                    Label label = (Label) parent;
                    if (label.getText().equals("Income")) {
                        symbol.setStyle("-fx-background-color: #1a7eca, blue;");
                    } else if (label.getText().equals("Expense")) {
                        symbol.setStyle("-fx-background-color: #FF0000, red;");
                    }
                }
            }
        });
    }

    @FXML
    private void showFinancialAnalysis() {
        lineChart.setVisible(true);
        barChartMilling.setVisible(false);
        handleGenerateReport(); // Financial data ကို ပြန်ခေါ်မယ်
    }

    @FXML
    private void showMillingProduction() {
        lineChart.setVisible(false);
        barChartMilling.setVisible(true);
        handleMillingReport(); // Milling data ကို ခေါ်မယ့် function
    }

    private void handleMillingReport() {
        String selectedMonthName = comboMonth.getValue();
        Month month = Month.valueOf(selectedMonthName.toUpperCase());
        int year = LocalDate.now().getYear();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MILLING_URL))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    try {
                        // MillingRecord အစား Map အနေနဲ့ ဖတ်ယူမယ်
                        List<Map<String, Object>> allMilling = objectMapper.readValue(json, new TypeReference<>() {});
                        Platform.runLater(() -> processMillingData(allMilling, month, year));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private void processMillingData(List<Map<String, Object>> data, Month targetMonth, int year) {
        Map<Integer, Double> millingMap = new TreeMap<>();

        for (Map<String, Object> item : data) {
            try {
                // Map ထဲကနေ value တွေကို ပြန်ထုတ်ယူခြင်း
                String dateStr = (String) item.get("millingDate");
                Object qtyObj = item.get("inputQtyTins");
                double qty = (qtyObj instanceof Number) ? ((Number) qtyObj).doubleValue() : 0.0;

                if (dateStr != null) {
                    LocalDate date = LocalDate.parse(dateStr);
                    if (date.getMonth() == targetMonth && date.getYear() == year) {
                        int day = date.getDayOfMonth();
                        millingMap.put(day, millingMap.getOrDefault(day, 0.0) + qty);
                    }
                }
            } catch (Exception e) {
                System.err.println("Skip record due to error: " + e.getMessage());
            }
        }
        updateMillingBarChart(millingMap, targetMonth.length(LocalDate.now().isLeapYear()));
    }

    private void updateMillingBarChart(Map<Integer, Double> dataMap, int daysInMonth) {
        barChartMilling.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tins Milled");

        for (int i = 1; i <= daysInMonth; i++) {
            series.getData().add(new XYChart.Data<>(String.valueOf(i), dataMap.getOrDefault(i, 0.0)));
        }

        barChartMilling.getData().add(series);
    }
}