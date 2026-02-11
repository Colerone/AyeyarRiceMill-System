package com.example.ayeyarricemill;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;

public class FinanceController {
    @FXML private Label lblOpeningBalance;
    @FXML private Label lblTotalIncome;
    @FXML private Label lblTotalExpense;
    @FXML private Label lblClosingBalance;

    // လက်ရှိ Login ဝင်ထားသူ (Static field ဖြစ်လို့ အခြား Controller ကနေ လှမ်းထည့်ပေးနိုင်တယ်)
    public static String loggedInUsername;

    private final String BASE_URL = "http://localhost:9090/api/finance/check-setup/";
    private final ObjectMapper mapper = new ObjectMapper();
    private final DecimalFormat df = new DecimalFormat("#,###");

    @FXML
    public void initialize() {
        // Debug လုပ်ရန်
        System.out.println("Finance Dashboard loading for: " + loggedInUsername);

        if (loggedInUsername != null && !loggedInUsername.isEmpty()) {
            fetchFinanceData();
        } else {
            System.err.println("Finance Error: User context is missing!");
            updateUIWithDefaults("Login လိုအပ်သည်");
        }
    }

    private void fetchFinanceData() {
        HttpClient client = HttpClient.newHttpClient();
        String fullUrl = BASE_URL + loggedInUsername;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("Accept", "application/json")
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            JsonNode rootNode = mapper.readTree(response.body());

                            // Backend field name 'initialBalance' နှင့် ကိုက်ညီရမည်
                            double initialBalance = rootNode.has("initialBalance") ?
                                    rootNode.get("initialBalance").asDouble() : 0.0;

                            // TODO: နောက်ပိုင်းတွင် Total Income နှင့် Expense ကို သီးခြား API ဖြင့် ခေါ်ယူရန်
                            double totalIncome = 0.0;
                            double totalExpense = 0.0;
                            double closingBalance = initialBalance + totalIncome - totalExpense;

                            Platform.runLater(() -> {
                                updateUI(initialBalance, totalIncome, totalExpense, closingBalance);
                            });

                        } catch (Exception e) {
                            System.err.println("Data Parsing Error: " + e.getMessage());
                        }
                    } else {
                        System.out.println("No setup data found. Status: " + response.statusCode());
                        Platform.runLater(() -> updateUI(0.0, 0.0, 0.0, 0.0));
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        System.err.println("Network Error: " + ex.getMessage());
                        updateUIWithDefaults("Error");
                    });
                    return null;
                });
    }

    private void updateUI(double ob, double inc, double exp, double cb) {
        lblOpeningBalance.setText(df.format(ob));
        lblTotalIncome.setText(df.format(inc));
        lblTotalExpense.setText(df.format(exp));
        lblClosingBalance.setText(df.format(cb));
    }

    private void updateUIWithDefaults(String message) {
        lblOpeningBalance.setText(message);
        lblTotalIncome.setText(message);
        lblTotalExpense.setText(message);
        lblClosingBalance.setText(message);
    }

    @FXML
    private void handleAddNewTransaction() {
        // TODO: စာရင်းအသစ်သွင်းသည့် Popup သို့မဟုတ် Window အသစ်ဖွင့်ရန်
        System.out.println("Opening Transaction Entry Form for " + loggedInUsername);

        // ဥပမာ- Alert ပြခြင်း
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("လုပ်ဆောင်ချက်");
        alert.setHeaderText(null);
        alert.setContentText("ဝင်ငွေ/ထွက်ငွေ စာရင်းသွင်းသည့် Form ကို ဤနေရာတွင် ချိတ်ဆက်ပါမည်။");
        alert.showAndWait();
    }
}