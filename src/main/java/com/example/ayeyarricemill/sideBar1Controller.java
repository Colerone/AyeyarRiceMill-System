package com.example.ayeyarricemill;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class sideBar1Controller {

    // လက်ရှိ ဘယ် Page ရောက်နေလဲဆိုတာကို Controller တိုင်းက သိအောင် Static ထားမယ်
    private static String activePage = "Home";
    public static String currentUserRole = "OWNER";
    @FXML private HBox hbHome;
    @FXML private HBox hdPaddyReg;
    @FXML private HBox activePaddyList;
    @FXML private HBox activeMillReg;
    @FXML private HBox activeMillList;
    @FXML private HBox activeRiceReg;
    @FXML private HBox activeRiceList;
    @FXML private HBox activeGoodPrice;
    @FXML private HBox activeRawPrice;
    @FXML private HBox activeInventory;
    @FXML private HBox activeSettings, activeManagerList;
    @FXML private VBox millingSubMenu;  // Paddy Purchase Submenu
    @FXML private VBox millingSubMenu1; // Milling Submenu
    @FXML private VBox millingSubMenu2; // Rice Sales Submenu
    @FXML private VBox millingSubMenu3; // MarketPrice Submenu


    @FXML
    public void initialize() {
        closeAllMenus();
        highlightActiveMenu();

        if (activeManagerList != null) {
            boolean isOwner = "OWNER".equalsIgnoreCase(currentUserRole);
            activeManagerList.setVisible(isOwner);
            activeManagerList.setManaged(isOwner);
        }

        if (activePage.equals("hdPaddyReg") || activePage.equals("activePaddyList")) {
            openMenu(millingSubMenu);
        } else if (activePage.equals("activeMillReg") || activePage.equals("activeMillList")) {
            openMenu(millingSubMenu1);
        } else if (activePage.equals("activeRiceReg") || activePage.equals("activeRiceList")) {
            openMenu(millingSubMenu2);
        } else if (activePage.equals("activeGoodPrice") || activePage.equals("activeRawPrice")) {
            openMenu(millingSubMenu3);
        }

    }

    private void highlightActiveMenu() {
        // အရင်ဆုံး အကုန်လုံးက style ကို ဖယ်လိုက်မယ်
        // (မှတ်ချက် - ကျောင်းသားပေးထားတဲ့ အမည်တွေနဲ့ အောက်မှာ fx:id ပေးရပါမယ်)
        hbHome.getStyleClass().remove("active-menu");
        hdPaddyReg.getStyleClass().remove("active-menu");
        activePaddyList.getStyleClass().remove("active-menu");
        activeMillReg.getStyleClass().remove("active-menu");
        activeMillList.getStyleClass().remove("active-menu");
        activeRiceReg.getStyleClass().remove("active-menu");
        activeRiceList.getStyleClass().remove("active-menu");
        activeGoodPrice.getStyleClass().remove("active-menu");
        activeRawPrice.getStyleClass().remove("active-menu");
        activeInventory.getStyleClass().remove("active-menu");
        activeSettings.getStyleClass().remove("active-menu");
        activeManagerList.getStyleClass().remove("active-menu");

        if (activePage.equals("Home")) hbHome.getStyleClass().add("active-menu");
        else if (activePage.equals("hdPaddyReg")) hdPaddyReg.getStyleClass().add("active-menu");
        else if (activePage.equals("activePaddyList")) activePaddyList.getStyleClass().add("active-menu");
        else if (activePage.equals("activeMillReg")) activeMillReg.getStyleClass().add("active-menu");
        else if (activePage.equals("activeMillList")) activeMillList.getStyleClass().add("active-menu");
        else if (activePage.equals("activeRiceReg")) activeRiceReg.getStyleClass().add("active-menu");
        else if (activePage.equals("activeRiceList")) activeRiceList.getStyleClass().add("active-menu");
        else if (activePage.equals("activeGoodPrice")) activeGoodPrice.getStyleClass().add("active-menu");
        else if (activePage.equals("activeRawPrice")) activeRawPrice.getStyleClass().add("active-menu");
        else if (activePage.equals("activeInventory")) activeInventory.getStyleClass().add("active-menu");
        else if (activePage.equals("activeSettings")) activeSettings.getStyleClass().add("active-menu");
        else if (activePage.equals("activeManagerList")) activeManagerList.getStyleClass().add("active-menu");
    }

    // Paddy Purchase
    @FXML
    public void toggleMillingMenu() {
        if (millingSubMenu.isVisible()) {
            closeMenu(millingSubMenu);
        } else {
            closeMenu(millingSubMenu1);
            closeMenu(millingSubMenu2);
            closeMenu(millingSubMenu3);
            openMenu(millingSubMenu);
        }
    }

    // Milling
    @FXML
    public void toggleMillingMenu1() {
        if (millingSubMenu1.isVisible()) {
            closeMenu(millingSubMenu1);
        } else {
            closeMenu(millingSubMenu);
            closeMenu(millingSubMenu2);
            closeMenu(millingSubMenu3);
            openMenu(millingSubMenu1);
        }
    }

    // Rice Sales
    @FXML
    public void toggleMillingMenu2() {
        if (millingSubMenu2.isVisible()) {
            closeMenu(millingSubMenu2);
        } else {
            closeMenu(millingSubMenu);
            closeMenu(millingSubMenu1);
            closeMenu(millingSubMenu3);
            openMenu(millingSubMenu2);
        }
    }

    @FXML
    public void toggleMillingMenu3(){
        if(millingSubMenu3.isVisible()){
            closeMenu(millingSubMenu3);
        }else {
            closeMenu(millingSubMenu);
            closeMenu(millingSubMenu1);
            closeMenu(millingSubMenu2);
            openMenu(millingSubMenu3);
        }
    }

    //  Helper Methods

    private void closeMenu(VBox menu) {
        menu.setVisible(false);
        menu.setManaged(false);
    }

    private void openMenu(VBox menu) {
        menu.setVisible(true);
        menu.setManaged(true);
    }

    private void closeAllMenus() {
        closeMenu(millingSubMenu);
        closeMenu(millingSubMenu1);
        closeMenu(millingSubMenu2);
        closeMenu(millingSubMenu3);
    }

    @FXML
    private void handleHomeClick(javafx.scene.input.MouseEvent event){
        activePage = "Home";
        try{
            Node source =(Node) event.getSource();
            Scene scene = source.getScene();
            Stage stage = (Stage) scene.getWindow();

            Parent root = FXMLLoader.load(getClass().getResource("/com/example/ayeyarricemill/HomePage.fxml"));
            scene.setRoot(root);
            stage.setMaximized(true);
            stage.show();
        }catch(Exception e){
            System.err.println("Error loading to vital scene: " + e.getMessage());
            e.printStackTrace();
        }
////        SceneController.switchCenter("/com/example/ayeyarricemill/HomeContent.fxml");
////        // ဒီနေရာမှာ highlight ပြောင်းဖို့ manual ခေါ်ပေးရမယ်
//        highlightActiveMenu();
    }

    @FXML
    private void paddyregister(javafx.scene.input.MouseEvent event){
        activePage= "hdPaddyReg";
        try{
            Node source =(Node) event.getSource();
            Scene scene = source.getScene();
            Stage stage = (Stage) scene.getWindow();

            Parent root = FXMLLoader.load(getClass().getResource("/com/example/ayeyarricemill/PurPage.fxml"));
            scene.setRoot(root);
            stage.setMaximized(true);
            stage.show();

//            SceneController.switchCenter("/com/example/ayeyarricemill/PadPurchaseS1.fxml");
//            // ဒီနေရာမှာ highlight ပြောင်းဖို့ manual ခေါ်ပေးရမယ်
//            highlightActiveMenu();
        }catch(Exception e){
            System.err.println("Error loading to vital scene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void paddyList(javafx.scene.input.MouseEvent event){
        activePage= "activePaddyList";
        try{
            Node source =(Node) event.getSource();
            Scene scene = source.getScene();
            Stage stage = (Stage) scene.getWindow();

            Parent root = FXMLLoader.load(getClass().getResource("/com/example/ayeyarricemill/RegisterListPage.fxml"));
            scene.setRoot(root);
            stage.setMaximized(true);
            stage.show();

//            SceneController.switchCenter("/com/example/ayeyarricemill/PadBuyList.fxml");
//            // ဒီနေရာမှာ highlight ပြောင်းဖို့ manual ခေါ်ပေးရမယ်
//            highlightActiveMenu();
        }catch(Exception e){
            System.err.println("Error loading to vital scene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void millingregister(javafx.scene.input.MouseEvent event){
        activePage = "activeMillReg";
        try{
            Node source =(Node) event.getSource();
            Scene scene = source.getScene();
            Stage stage = (Stage) scene.getWindow();

            Parent root = FXMLLoader.load(getClass().getResource("/com/example/ayeyarricemill/MillingPage.fxml"));
            scene.setRoot(root);
            stage.setMaximized(true);
            stage.show();
        }catch(Exception e){
            System.err.println("Error loading to vital scene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void millingRegList(javafx.scene.input.MouseEvent event){
        activePage = "activeMillList";
        try{
            Node source =(Node) event.getSource();
            Scene scene = source.getScene();
            Stage stage = (Stage) scene.getWindow();

            Parent root = FXMLLoader.load(getClass().getResource("/com/example/ayeyarricemill/MillingListPage.fxml"));
            scene.setRoot(root);
            stage.setMaximized(true);
            stage.show();
        }catch(Exception e){
            System.err.println("Error loading to vital scene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void ricesaleregister(javafx.scene.input.MouseEvent event){
        activePage = "activeRiceReg";
        try{
            Node source =(Node) event.getSource();
            Scene scene = source.getScene();
            Stage stage = (Stage) scene.getWindow();

            Parent root = FXMLLoader.load(getClass().getResource("/com/example/ayeyarricemill/SalePage.fxml"));
            scene.setRoot(root);
            stage.setMaximized(true);
            stage.show();
//            SceneController.switchCenter("/com/example/ayeyarricemill/saleRegister.fxml");
//            // ဒီနေရာမှာ highlight ပြောင်းဖို့ manual ခေါ်ပေးရမယ်
//            highlightActiveMenu();
        }catch(Exception e){
            System.err.println("Error loading to vital scene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void salesRecord(javafx.scene.input.MouseEvent event){
        try{
            Node source =(Node) event.getSource();
            Scene scene = source.getScene();
            Stage stage = (Stage) scene.getWindow();

            Parent root = FXMLLoader.load(getClass().getResource("/com/example/ayeyarricemill/SalePage1.fxml"));
            scene.setRoot(root);
            stage.setMaximized(true);
            stage.show();
//            SceneController.switchCenter("/com/example/ayeyarricemill/saleRecord.fxml");
//            // ဒီနေရာမှာ highlight ပြောင်းဖို့ manual ခေါ်ပေးရမယ်
//            highlightActiveMenu();
        }catch(Exception e){
            System.err.println("Error loading to vital scene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void clickInventory(javafx.scene.input.MouseEvent event){
        activePage = "activeInventory";
        try{
            Node source =(Node) event.getSource();
            Scene scene = source.getScene();
            Stage stage = (Stage) scene.getWindow();

            Parent root = FXMLLoader.load(getClass().getResource("/com/example/ayeyarricemill/InventoryPage.fxml"));
            scene.setRoot(root);
            stage.setMaximized(true);
            stage.show();
//            SceneController.switchCenter("/com/example/ayeyarricemill/InventoryAdd.fxml");
//            // ဒီနေရာမှာ highlight ပြောင်းဖို့ manual ခေါ်ပေးရမယ်
//            highlightActiveMenu();
        }catch(Exception e){
            System.err.println("Error loading to vital scene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void clickSettings(javafx.scene.input.MouseEvent event){
        activePage = "activeSettings";
        try{
            Node source =(Node) event.getSource();
            Scene scene = source.getScene();
            Stage stage = (Stage) scene.getWindow();

            Parent root = FXMLLoader.load(getClass().getResource("/com/example/ayeyarricemill/SettingPage.fxml"));
            scene.setRoot(root);
            stage.setMaximized(true);
            stage.show();
//            SceneController.switchCenter("/com/example/ayeyarricemill/Setting.fxml");
//            highlightActiveMenu();
        }catch(Exception e){
            System.err.println("Error loading to vital scene: " + e.getMessage());
            e.printStackTrace();
        }

    }

    @FXML
    private void clickManagerList(javafx.scene.input.MouseEvent event) {
        activePage = "activeManagerList";
        try {
            switchPage(event, "/com/example/ayeyarricemill/ManagerListPage.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void switchPage(javafx.scene.input.MouseEvent event, String fxmlPath) throws Exception {
        Node source = (Node) event.getSource();
        Scene scene = source.getScene();
        Stage stage = (Stage) scene.getWindow();
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        scene.setRoot(root);
        stage.setMaximized(true);
        stage.show();
    }

    @FXML
    private void mkrawprice(javafx.scene.input.MouseEvent event){
        activePage = "activeRawPrice";
        try{
            Node source =(Node) event.getSource();
            Scene scene = source.getScene();
            Stage stage = (Stage) scene.getWindow();

            Parent root = FXMLLoader.load(getClass().getResource("/com/example/ayeyarricemill/RawPage.fxml"));
            scene.setRoot(root);
            stage.setMaximized(true);
            stage.show();
//            SceneController.switchCenter("/com/example/ayeyarricemill/RawPrice.fxml");
//            // ဒီနေရာမှာ highlight ပြောင်းဖို့ manual ခေါ်ပေးရမယ်
//            highlightActiveMenu();
        }catch(Exception e){
            System.err.println("Error loading to vital scene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void mkgoodprice(javafx.scene.input.MouseEvent event){
        activePage = "activeGoodPrice";
        try{
            Node source =(Node) event.getSource();
            Scene scene = source.getScene();
            Stage stage = (Stage) scene.getWindow();

            Parent root = FXMLLoader.load(getClass().getResource("/com/example/ayeyarricemill/GoodPage.fxml"));
            scene.setRoot(root);
            stage.setMaximized(true);
            stage.show();
//            SceneController.switchCenter("/com/example/ayeyarricemill/GoodPrice.fxml");
//            // ဒီနေရာမှာ highlight ပြောင်းဖို့ manual ခေါ်ပေးရမယ်
//            highlightActiveMenu();
        }catch(Exception e){
            System.err.println("Error loading to vital scene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void logout(javafx.scene.input.MouseEvent event){
        try{
            Node source =(Node) event.getSource();
            Scene scene = source.getScene();
            Stage stage = (Stage) scene.getWindow();

            Parent root = FXMLLoader.load(getClass().getResource("/com/example/ayeyarricemill/AniLogSign.fxml"));
            scene.setRoot(root);
            stage.setMaximized(true);
            stage.show();
        }catch(Exception e){
            System.err.println("Error loading to vital scene: " + e.getMessage());
            e.printStackTrace();
        }
    }
}