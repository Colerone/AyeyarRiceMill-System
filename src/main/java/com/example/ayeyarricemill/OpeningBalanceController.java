package com.example.ayeyarricemill;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

public class OpeningBalanceController {
    @FXML private TextField txtInitialBalance;
    @FXML private DatePicker datePicker;
    @FXML private Button btnStart;

    private final String API_URL = "http://localhost:9090/api/finance/setup";
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    // Login ဝင်ထားသူရဲ့ Username (AniLSController ကနေ assign လုပ်ပေးထားတာဖြစ်ရမယ်)
    public static String loggedInUsername;

    @FXML
    public void initialize() {
        datePicker.setValue(LocalDate.now());

        // Debug လုပ်ဖို့
        System.out.println("OpeningBalance initialized for user: " + loggedInUsername);
    }

    @FXML
    private void handleStartAccounting() {
        String balanceStr = txtInitialBalance.getText().trim();

        // 1. Validation စစ်ဆေးခြင်း
        if (balanceStr.isEmpty()) {
            showSimpleAlert(Alert.AlertType.WARNING, "လိုအပ်ချက်", "စတင်မည့် လက်ကျန်ငွေ ရိုက်ထည့်ပါ");
            return;
        }

        try {
            Double balance = Double.parseDouble(balanceStr);
            LocalDate startDate = datePicker.getValue();

            if (loggedInUsername == null || loggedInUsername.isEmpty()) {
                showSimpleAlert(Alert.AlertType.ERROR, "အမှား", "Login အချက်အလက် မရှိပါ။ ကျေးဇူးပြု၍ ပြန်လည် Login ဝင်ပါ။");
                return;
            }

            // Button ကို ခေတ္တပိတ်ထားမယ်
            btnStart.setDisable(true);

            // Backend ကို ပို့မည့် Data Object
            FinanceData setupData = new FinanceData(balance, startDate, loggedInUsername);
            String jsonBody = objectMapper.writeValueAsString(setupData);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        Platform.runLater(() -> {
                            btnStart.setDisable(false);
                            if (response.statusCode() == 200 || response.statusCode() == 201) {
                                navigateToFinanceDashboard();
                            } else {
                                System.err.println("Backend Error: " + response.body());
                                showSimpleAlert(Alert.AlertType.ERROR, "Error", "စာရင်းဖွင့်ခြင်း မအောင်မြင်ပါ: " + response.body());
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            btnStart.setDisable(false);
                            showSimpleAlert(Alert.AlertType.ERROR, "Network Error", "Server နှင့် ချိတ်ဆက်၍ မရပါ");
                        });
                        return null;
                    });

        } catch (NumberFormatException e) {
            showSimpleAlert(Alert.AlertType.ERROR, "အမှား", "လက်ကျန်ငွေတွင် ဂဏန်းများသာ ရိုက်ထည့်ပါ");
        } catch (Exception e) {
            e.printStackTrace();
            showSimpleAlert(Alert.AlertType.ERROR, "Error", "မမျှော်လင့်ထားသော အမှားတစ်ခု ဖြစ်ပွားခဲ့သည်");
        }
    }

    private void navigateToFinanceDashboard() {
        try {
            sideBar1Controller.setActivePage("activeFinance");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FinancePage1.fxml"));
            Parent root = loader.load();

            // FinanceController ဆီကိုလည်း username လက်ဆင့်ကမ်းမယ်
            FinanceController.loggedInUsername = loggedInUsername;

            Stage stage = (Stage) btnStart.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Finance Dashboard - Ayeyar Rice Mill");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showSimpleAlert(Alert.AlertType.ERROR, "Navigation Error", "Finance Dashboard ကို သွား၍မရပါ");
        }
    }

    private void showSimpleAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Backend Request အတွက် POJO Class
    public static class FinanceData {
        public Double initialBalance;
        public String startDate;
        public String userId; // Backend က userId လို့ မျှော်လင့်ထားရင် ဒီအတိုင်းထားပါ

        public FinanceData(Double initialBalance, LocalDate startDate, String userId) {
            this.initialBalance = initialBalance;
            this.startDate = startDate.toString();
            this.userId = userId;
        }
    }
}