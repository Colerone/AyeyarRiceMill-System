package com.example.ayeyarricemill;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MillingListDetailController {
    // FXML ဖိုင်ထဲရှိ fx:id များ (Paddy Info)
    @FXML
    private Label lblDate;
    @FXML private Label lblRecordedBy;
    @FXML private Label lblVoucherNo;
    @FXML private Label lblSourceWarehouse;
    @FXML private Label lblPaddyType;
    @FXML private Label lblTotalMilled;

    // Output Materials (Bag Counts)
    @FXML private Label lblHeadRice;
    @FXML private Label lblBrokenRice;
    @FXML private Label lblBrokenBran;
    @FXML private Label lblBran;

    // Summary Info
    @FXML private Label lblFinalYield;
    @FXML private Label lblTotalOutputs;
    @FXML private Label lblTargetWarehouse;

    @FXML private Button btnBack;

    /**
     * MillingRegListController မှ Detail button နှိပ်လိုက်သည့်အခါ
     * ရွေးချယ်ထားသော data object အားလုံးကို လက်ခံယူပြီး UI တွင် ပြသရန်
     */
    public void setMillingData(MillingRegListController.MillingRecord record) {
        // ထိပ်ပိုင်း Header အချက်အလက်
        lblDate.setText(record.getMillingDate());
        lblRecordedBy.setText(record.getRecordedBy()); // Default ပြထားခြင်း (သို့မဟုတ် login user)

        // ဘယ်ဘက်ခြမ်း - စပါးအချက်အလက် (Paddy Info)
        lblVoucherNo.setText(record.getBatchNo());
        lblSourceWarehouse.setText(record.getSourceWarehouse());
        lblPaddyType.setText(record.getPaddyType());
        lblTotalMilled.setText(String.format("%.0f Tins", record.getInputQtyTins()));

        // ညာဘက်ခြမ်း - ထွက်ကုန်ပစ္စည်းများ (Output Materials)
        lblHeadRice.setText(String.format("%.0f Bags", record.getHeadRiceBags()));
        lblBrokenRice.setText(String.format("%.0f Bags", record.getBrokenRiceBags()));
        lblBrokenBran.setText(String.format("%.0f Bags", record.getBrokenBranBags()));
        lblBran.setText(String.format("%.0f Bags", record.getBranBags()));

        // အောက်ခြေပိုင်း - အနှစ်ချုပ် (Summary)
        lblFinalYield.setText(String.format("%.1f %%", record.getYieldPercentage()));
        lblTotalOutputs.setText(String.format("%.0f Bags", record.getTotalOutputBags()));
        lblTargetWarehouse.setText(record.getTargetWarehouse());
    }

    @FXML
    public void initialize() {
        // Back Button နှိပ်လျှင် လက်ရှိ window ကို ပိတ်ရန်
        if (btnBack != null) {
            btnBack.setOnAction(e -> {
                Stage stage = (Stage) btnBack.getScene().getWindow();
                stage.close();
            });
        }
    }
}
