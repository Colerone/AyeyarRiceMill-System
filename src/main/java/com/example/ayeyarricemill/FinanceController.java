package com.example.ayeyarricemill;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class FinanceController {
    @FXML
    private Label lblTotalIncome;
    @FXML private Label lblTotalExpense;
    @FXML private Label lblClosingBalance;

    // Input Fields
    @FXML private ComboBox<String> comboType;
    @FXML private ComboBox<String> comboCategory; // Category ကို ComboBox အနေနဲ့ပြောင်းလဲထားသည်
    @FXML private TextField txtAmount;
    @FXML private TextField txtNote;
    @FXML private DatePicker filterDatePicker;
    @FXML private Button btnResetFilter;

    // Table & Columns (FXML IDs မှ ပြန်လည်ညှိနှိုင်းထားသည်)
    @FXML private TableView<TransactionModel> tableView;
    @FXML private TableColumn<TransactionModel, Integer> colNo;
    @FXML private TableColumn<TransactionModel, String> colTime;
    @FXML private TableColumn<TransactionModel, String> colType;
    @FXML private TableColumn<TransactionModel, String> colAmount; // Category အတွက် သုံးထားသည်ဟု FXML တွင်တွေ့ရသည်
    @FXML private TableColumn<TransactionModel, String> colNote;   // Amount အတွက် သုံးထားသည်ဟု FXML တွင်တွေ့ရသည်
    @FXML private TableColumn<TransactionModel, String> colNote1;  // Record By Name
    @FXML private TableColumn<TransactionModel, String> colNote11; // Record By Role
    @FXML private TableColumn<TransactionModel, String> colNote111;// Note အတွက် ဖြစ်နိုင်သည်

    private final ObservableList<TransactionModel> masterData = FXCollections.observableArrayList();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final String BASE_URL = "http://localhost:9090/api/transactions";

    public static String loggedInUsername;
    public static String currentUserRole;
    private FilteredList<TransactionModel> filteredData;

    @FXML
    public void initialize() {
        filteredData = new FilteredList<>(masterData, p -> true);
        tableView.setItems(filteredData);

        // Table Columns Mapping
        colNo.setCellValueFactory(column ->
                new ReadOnlyObjectWrapper<>(tableView.getItems().indexOf(column.getValue()) + 1));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("category")); // FXML အရ colAmount သည် category ပြရန်
        colNote.setCellValueFactory(new PropertyValueFactory<>("formattedAmount")); // FXML အရ colNote သည် amount ပြရန်
        colNote1.setCellValueFactory(new PropertyValueFactory<>("recordedBy"));
        colNote11.setCellValueFactory(new PropertyValueFactory<>("role"));
        colNote111.setCellValueFactory(new PropertyValueFactory<>("note"));

        // ComboBox Data
        comboType.setItems(FXCollections.observableArrayList("Income", "Expense"));
        comboCategory.setItems(FXCollections.observableArrayList("Business", "Personal"));

        setDefaultValues();
        // Load Data
        fetchTransactions();
        setupFilterLogic();
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
                LocalDateTime ldt = LocalDateTime.parse(record.getRawTime());
                return ldt.toLocalDate().equals(selectedDate);
            } catch (Exception e) {
                // Parse လုပ်လို့မရရင် console မှာ ပြပေးထားမယ် (Debug လုပ်ဖို့)
                System.err.println("Date format error for: " + record.getRawTime());
                return false;
            }
        });

        // Table ကို Update လုပ်သည်
//        tableMilling.setItems(filteredData);
        tableView.refresh();
    }

    private void setDefaultValues() {
        comboType.setValue("Income");      // Default: Income
        comboCategory.setValue("Business"); // Default: Business
    }

    private void fetchTransactions() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/all"))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseBody -> {
                    try {
                        List<Map<String, Object>> list = objectMapper.readValue(responseBody, new TypeReference<>() {});
                        Platform.runLater(() -> {
                            masterData.clear();
                            double totalIncome = 0;
                            double totalExpense = 0;

                            for (Map<String, Object> item : list) {
                                String type = (String) item.get("type");
                                String category = (String) item.get("category");
                                double amount = ((Number) item.get("amount")).doubleValue();
                                String note = (String) item.get("note");
                                String time = (String) item.get("timestamp");
                                String recordedBy = (String) item.get("recordedBy");
                                String role = (String) item.get("role");

                                masterData.add(new TransactionModel(time, type, category, amount, note, recordedBy, role));

                                if ("Income".equalsIgnoreCase(type)) totalIncome += amount;
                                else totalExpense += amount;


                            }


//                            tableView.setItems(masterData);
                            updateSummary(totalIncome, totalExpense);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    /**
     * စာရင်းအသစ်ထည့်သွင်းခြင်း
     */
    @FXML
    private void handleAddTransaction() {
        try {
            String type = comboType.getValue();
            String category = comboCategory.getValue();
            String amountStr = txtAmount.getText();
            String note = txtNote.getText();

            if (type == null || category == null || amountStr.isEmpty()) {
                showSimpleAlert("Warning", "Please fill in all required information completely");
                return;
            }

            double amount = Double.parseDouble(amountStr);

            // Backend သို့ပို့မည့် Data
            Map<String, Object> data = Map.of(
                    "type", type,
                    "category", category,
                    "amount", amount,
                    "note", note,
                    "recordedBy", loggedInUsername, // လက်ရှိ Login User Name ထည့်ရန်
                    "role", currentUserRole // လက်ရှိ Role ထည့်ရန်
            );

            String jsonBody = objectMapper.writeValueAsString(data);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/add"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(res -> {
                        Platform.runLater(() -> {
                            fetchTransactions(); // Table ပြန် Update လုပ်
                            clearInputs();
                        });
                    });

        } catch (NumberFormatException e) {
            showSimpleAlert("Wrong", "Please enter the amount in numbers only.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateSummary(double income, double expense) {
        DecimalFormat df = new DecimalFormat("#,###");
        lblTotalIncome.setText(df.format(income));
        lblTotalExpense.setText(df.format(expense));
        lblClosingBalance.setText(df.format(income - expense));
    }

    private void clearInputs() {
        txtAmount.clear();
        txtNote.clear();
        setDefaultValues();
    }

    private void showSimpleAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Table တွင်ပြသရန် Model Class
     */
    public static class TransactionModel {
        private String time;
        private String rawTime;
        private String type;
        private String category;
        private double amount;
        private String note;
        private String recordedBy;
        private String role;
        private String formattedAmount;

        public TransactionModel(String time, String type, String category, double amount, String note, String recordedBy, String role) {
            this.rawTime = time; // <-- မူရင်း သိမ်းထား
            try {
                if (time != null && time.contains("T")) {
                    LocalDateTime ldt = LocalDateTime.parse(time);
                    this.time = ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a"));
                } else {
                    this.time = time;
                }
            } catch (Exception e) {
                this.time = time;
            }
            this.type = type;
            this.category = category;
            this.amount = amount;
            this.note = note;
            this.recordedBy = recordedBy;
            this.role = role;
            this.formattedAmount = new DecimalFormat("#,###").format(amount);
        }

        public String getTime() { return time; }
        public String getRawTime() {
            return rawTime;
        }
        public String getType() { return type; }
        public String getCategory() { return category; }
        public double getAmount() { return amount; }
        public String getNote() { return note; }
        public String getRecordedBy() { return recordedBy; }
        public String getRole() { return role; }
        public String getFormattedAmount() { return formattedAmount; }
    }
}