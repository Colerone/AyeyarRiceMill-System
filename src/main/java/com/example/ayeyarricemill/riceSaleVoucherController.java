package com.example.ayeyarricemill;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.PrinterJob;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class riceSaleVoucherController {
    @FXML
    private Label lblCustomerName;
    @FXML private Label lblPhone;
    @FXML private Label lblVoucherNo;
    @FXML private Label lblDate;
    @FXML private Label lblTotalAmount;
    @FXML private Label lblCustomerName1;
    @FXML private Label lblSeller;
    @FXML private AnchorPane printableArea;
    @FXML private Button btnBack,btnPrint;
    @FXML private VBox vboxItems; // Dynamic rows တွေ ထည့်မယ့်နေရာ
    @FXML private AnchorPane footerPane;

    public void setData(String customerName, String phone, List<riceSaleRegisterController.SaleItem> items, String sellerName) {
        // အခြေခံ အချက်အလက်များ ဖြည့်ခြင်း
        lblCustomerName.setText(customerName);
        lblCustomerName1.setText(customerName);
        lblPhone.setText(phone);
        if (sellerName != null && !sellerName.isEmpty()) {
            lblSeller.setText(sellerName);
        } else {
            lblSeller.setText("Not Logged In");
        }
        lblDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("d/M/yyyy")));

        // Voucher No ကို ယာယီအားဖြင့် ယနေ့ရက်စွဲနဲ့ Time Stamp သုံးပြီး ထုတ်ပေးခြင်း
        String vNo = "A-" + LocalDate.now().getYear() + "-" + (System.currentTimeMillis() % 10000);
        lblVoucherNo.setText(vNo);

        // အရင်ရှိနေတဲ့ dynamic rows တွေကို ရှင်းထုတ်ပါ
        vboxItems.getChildren().clear();

        double totalAmount = 0;

        // Item List ထဲက အရေအတွက်အတိုင်း Row များ တည်ဆောက်ခြင်း
        for (int i = 0; i < items.size(); i++) {
            riceSaleRegisterController.SaleItem item = items.get(i);

            // Row တစ်ခုအတွက် Container (AnchorPane)
            AnchorPane row = new AnchorPane();
            row.setPrefHeight(35.0);
            row.setPrefWidth(758.0);

            // စဥ် (No)
            Label lblNo = new Label(String.valueOf(i + 1));
            lblNo.setLayoutX(63.0);
            lblNo.setLayoutY(7.0);
            lblNo.setStyle("-fx-font-size: 15;");

            // ပစ္စည်းအမည် (Item Name)
            Label lblName = new Label(item.getItemName());
            lblName.setLayoutX(106.0);
            lblName.setLayoutY(7.0);
            lblName.setPrefWidth(270.0);
            lblName.setStyle("-fx-font-size: 15;");

            // အရေအတွက် (Qty)
            Label lblQty = new Label(String.format("%.0f", item.getQty()));
            lblQty.setLayoutX(394.0);
            lblQty.setLayoutY(7.0);
            lblQty.setStyle("-fx-font-size: 15;");

            // ဈေးနှုန်း (Price)
            Label lblPrice = new Label(String.format("%,.0f", item.getPrice()));
            lblPrice.setLayoutX(468.0);
            lblPrice.setLayoutY(7.0);
            lblPrice.setStyle("-fx-font-size: 15;");

            // စုစုပေါင်း (SubTotal)
            Label lblSub = new Label(String.format("%,.0f", item.getSubTotal()));
            lblSub.setLayoutX(574.0);
            lblSub.setLayoutY(7.0);
            lblSub.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");

            // အောက်ခြေမျဉ်း (Bottom Line for each row)
            Line line = new Line();
            line.setStartX(-140.0);
            line.setEndX(616.0);
            line.setLayoutX(141.0);
            line.setLayoutY(35.0);
            line.setStyle("-fx-stroke: #f0f0f0;"); // အလွန်ပါးသော မျဉ်းရောင်

            row.getChildren().addAll(lblNo, lblName, lblQty, lblPrice, lblSub, line);

            // VBox ထဲသို့ row ထည့်ခြင်း
            vboxItems.getChildren().add(row);

            totalAmount += item.getSubTotal();
        }

        // Total Amount ကို format လုပ်ပြီး ပြခြင်း
        lblTotalAmount.setText(String.format("%,.0f", totalAmount));

        // Footer (Total Amount Section) ကို VBox ရဲ့ အောက်မှာ ကပ်နေအောင် နေရာညှိခြင်း
        // VBox ရဲ့ layout Y + အမြင့် ကို အခြေခံပြီး footer ကို ရွှေ့ပေးပါမယ်
        vboxItems.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            footerPane.setLayoutY(vboxItems.getLayoutY() + newValue.getHeight());
        });
    }

    @FXML
    private void handlePrint() {
        // Hide buttons before printing
        btnPrint.setVisible(false);
        btnBack.setVisible(false);

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(lblCustomerName.getScene().getWindow())) {
            boolean success = job.printPage(printableArea); // Print specific anchor pane
            if (success) {
                job.endJob();
            }
        }

        // Show buttons again
        btnPrint.setVisible(true);
        btnBack.setVisible(true);
    }

    @FXML
    private void handleBack() {
        try {
            // Load previous scene (PadPurchaseS1.fxml)
            // Note: Adjust the FXML filename if different
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SalePage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
//            SceneController.switchCenter("/com/example/ayeyarricemill/HomeContent.fxml");
            // ဒီနေရာမှာ highlight ပြောင်းဖို့ manual ခေါ်ပေးရမယ်
//            if (sideBar1Controller.instance != null) {
//                sideBar1Controller.instance.updateActivePage("Home");
//            }
//            highlightActiveMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
