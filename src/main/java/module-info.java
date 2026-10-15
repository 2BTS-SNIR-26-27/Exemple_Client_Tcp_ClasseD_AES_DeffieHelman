module com.astier.bts.client_tcp_prof {
    requires java.net.http;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires org.apache.commons.codec;
    requires totp.main;

    exports com.astier.bts.client_tcp_prof to javafx.graphics;
    opens com.astier.bts.client_tcp_prof to javafx.fxml;
}
