package com.example.ayeyarricemill;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.PrinterJob;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

import static javafx.scene.layout.AnchorPane.setLeftAnchor;
import static javafx.scene.layout.AnchorPane.setTopAnchor;

public class PaddyVoucherController {

    public static class PurchaseData{
        public static String supplierName;
        public static String variety;
        public static String phone;
        public static String date;

        public static String totalWeight;
        public static String moistureCut;
        public static String impurityCut;
        public static String netWeight;

        public static String inputMoi;
        public static String inputWaste;
        public static String inputYell;

        public static String basePrice;
        public static String yellowCut;
        public static String netPrice;
        public static String totalAmount;

        public static String purchaserName;
        public static String batchNo;
    }

    @FXML private Label lblSupplierName;
    @FXML private Label lblVariety;
    @FXML private Label lblPhone;
    @FXML private Label lblDate;

    @FXML private Label lblTotalWeight;
    @FXML private Label lblMoistureCut;
    @FXML private Label lblImpurityCut;
    @FXML private Label lblBasePrice;

    @FXML private Label lblYellowCut;
    @FXML private Label lblNetPrice;
    @FXML private Label lblNetWeight;
    @FXML private Label lblGrandTotal;

    @FXML private Label lblSignSupplier; // Supplier လက်မှတ်အောက်ကနာမည်
    @FXML private Label lblSignPurchaser;

    @FXML private Label moiDe;
    @FXML private Label impuriDe;
    @FXML private Label yeDe;

    @FXML private Button btnPrint;
    @FXML private Button btnBack;

    @FXML private AnchorPane VoucherRoot;
    @FXML private AnchorPane printableArea; // The main print area

    @FXML
    public void initialize(){

        if (VoucherRoot != null && printableArea != null) {
            VoucherRoot.widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number oldNumber, Number newNumber) {
                    double newleftAnchor = (newNumber.doubleValue() - printableArea.getPrefWidth()) / 2.0;
                    setLeftAnchor(printableArea, newleftAnchor);
                }
            });

            VoucherRoot.heightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number oldNumber, Number newNumber) {
                    double newTopAnchor = (newNumber.doubleValue() - printableArea.getPrefHeight()) / 2.0;
                    setTopAnchor(printableArea, newTopAnchor);
                }
            });

            setLeftAnchor(printableArea, (VoucherRoot.getWidth() - printableArea.getPrefWidth()) / 2.0);
            setTopAnchor(printableArea, (VoucherRoot.getHeight() - printableArea.getPrefHeight()) / 2.0);

        }

        lblSupplierName.setText(PurchaseData.supplierName);
        lblVariety.setText(PurchaseData.variety);
        lblPhone.setText(PurchaseData.phone);
        lblDate.setText(PurchaseData.date);

        lblTotalWeight.setText(PurchaseData.totalWeight + " Tins");
        lblMoistureCut.setText(PurchaseData.moistureCut + " Tins");
        lblImpurityCut.setText(PurchaseData.impurityCut + " Tins");
        lblNetWeight.setText(PurchaseData.netWeight + " Tins");

        lblBasePrice.setText(PurchaseData.basePrice + " MMK");
        lblYellowCut.setText(PurchaseData.yellowCut + " MMK");
        lblNetPrice.setText(PurchaseData.netPrice + " MMK");

        moiDe.setText("("+PurchaseData.inputMoi + "%)");
        impuriDe.setText("("+PurchaseData.inputWaste + "%)");
        yeDe.setText("("+PurchaseData.inputYell + "%)");

        lblGrandTotal.setText(PurchaseData.totalAmount);

        lblSignSupplier.setText(PurchaseData.supplierName); // Supplier က အပေါ်ကနာမည်နဲ့တူတူပဲ
        lblSignPurchaser.setText(PurchaseData.purchaserName); // Login ဝင်ထားသူ

        btnPrint.setOnAction(e -> handlePrint());
        btnBack.setOnAction(e -> handleBack());
    }

    private void handlePrint() {
        // Hide buttons before printing
        btnPrint.setVisible(false);
        btnBack.setVisible(false);

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(lblSupplierName.getScene().getWindow())) {
            boolean success = job.printPage(printableArea); // Print specific anchor pane
            if (success) {
                job.endJob();
            }
        }

        // Show buttons again
        btnPrint.setVisible(true);
        btnBack.setVisible(true);
    }

    private void handleBack() {
        try {
            // Load previous scene (PadPurchaseS1.fxml)
            // Note: Adjust the FXML filename if different
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PurPage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/example/ayeyarricemill/images/AppImage (1).jpg")));
            stage.setTitle("AyeyarRiceMill");
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
