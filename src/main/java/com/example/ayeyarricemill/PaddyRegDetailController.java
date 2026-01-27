package com.example.ayeyarricemill;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class PaddyRegDetailController {

    // PadBuyListController ထဲက PaddyPurchase class အမျိုးအစားအတိုင်း
    // ဒါမှသာ "selectedPurchase = item" ဆိုပြီး ပို့လိုက်တဲ့အခါ error မတက်မှာ
    public static PadBuyListController.PaddyPurchase selectedPurchase;

    // FXML Labels (ညာဘက် Information Box)
    @FXML private Label lblDate;
    @FXML private Label lblSupplierName;
    @FXML private Label lblPurchaser;
    @FXML private Label lblPhone;
    @FXML private Label lblVariety;
    @FXML private Label lblNetWeightBox;
    @FXML private Label lblNetPriceBox;
    @FXML private Label lblGrandTotal;
    @FXML private Label lblWarehouse;

    // FXML Labels (ဘယ်ဘက် Weight Calculation)
    @FXML private Label lblTotalWeight;
    @FXML private Label lblMoistureCut;
    @FXML private Label lblImpurityCut;
    @FXML private Label lblNetWeight;

    // FXML Labels (ဘယ်ဘက် Price Calculation)
    @FXML private Label lblBasePrice;
    @FXML private Label lblQualityCut;
    @FXML private Label lblNetPrice;
    @FXML private Label lblMoisture;
    @FXML private Label lblImpurity;
    @FXML private Label lblYellow;
    @FXML private Button btnBack;

    @FXML
    public void initialize() {
        if (selectedPurchase != null) {
            loadData();
        } else {
            System.err.println("Error: selectedPurchase is null. Check openDetailView logic.");
        }

        // Back Button Action
        btnBack.setOnAction(event -> closeWindow());
    }

    private void loadData() {
        // --- Information Box (ညာဘက်ခြမ်း) ---
        String dateStr = selectedPurchase.getPurchaseDate();
        lblDate.setText(dateStr != null ? dateStr.split("T")[0] : "-");

        lblSupplierName.setText(selectedPurchase.getSupplierName());
        lblPurchaser.setText(selectedPurchase.getPurchaserName() != null ? selectedPurchase.getPurchaserName() : "-");
        lblPhone.setText(selectedPurchase.getSupplierPhone());
        lblVariety.setText(selectedPurchase.getPaddyType());

        if (lblWarehouse != null) {
            lblWarehouse.setText(selectedPurchase.getWarehouseName());
        }

        // Backend ကနေ ပို့လိုက်တဲ့ % value များကို ပြသခြင်း
        lblMoisture.setText(selectedPurchase.getInputMoi() != null ? selectedPurchase.getInputMoi() + " %" : "0.0 %");
        lblImpurity.setText(selectedPurchase.getInputWaste() != null ? selectedPurchase.getInputWaste() + " %" : "0.0 %");
        lblYellow.setText(selectedPurchase.getInputYell() != null ? selectedPurchase.getInputYell() + " %" : "0.0 %");


        // --- Weight Calculation Section (ဘယ်ဘက်ခြမ်း) ---
        lblTotalWeight.setText(formatWeight(selectedPurchase.getTotalWeight()));
        lblMoistureCut.setText("-" + formatWeight(selectedPurchase.getMoistureDeduction()));
        lblImpurityCut.setText("-" + formatWeight(selectedPurchase.getImpurityDeduction()));
        lblNetWeight.setText(formatWeight(selectedPurchase.getNetWeight()));

        // Box ထဲက Net Weight
        lblNetWeightBox.setText(formatWeight(selectedPurchase.getNetWeight()));

        // --- Price Calculation Section (ဘယ်ဘက်ခြမ်း) ---
        lblBasePrice.setText(formatCurrency(selectedPurchase.getBasePrice()));
        lblQualityCut.setText("-" + formatCurrency(selectedPurchase.getQualityDeduction()));
        lblNetPrice.setText(formatCurrency(selectedPurchase.getNetPrice()));



        // Box ထဲက Net Price
        lblNetPriceBox.setText(formatCurrency(selectedPurchase.getNetPrice()));

        // Grand Total Box
        lblGrandTotal.setText(formatCurrency(selectedPurchase.getTotalAmount()));
    }

    // Helper methods for formatting
    private String formatWeight(Double value) {
        if (value == null) return "0.00 Tins";
        return String.format("%.2f Tins", value);
    }

    private String formatCurrency(Double value) {
        if (value == null) return "0 MMK";
        return String.format("%,.0f MMK", value);
    }

    private void closeWindow() {
        Stage stage = (Stage) btnBack.getScene().getWindow();
        stage.close();
    }
}