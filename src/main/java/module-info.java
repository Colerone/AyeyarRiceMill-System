module com.example.ayeyarricemill {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;
    requires javafx.base;
    requires de.jensd.fx.glyphs.fontawesome;
    requires jbcrypt;
    requires java.net.http;
    requires com.google.gson;
    requires mysql.connector.j;
    requires java.desktop;


    opens com.example.ayeyarricemill to javafx.fxml, com.google.gson;
    exports com.example.ayeyarricemill;
}