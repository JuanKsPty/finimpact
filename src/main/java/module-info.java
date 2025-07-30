module com.juank.utp.finimpact {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    // Dependencias para base de datos
    requires java.sql;
    requires com.microsoft.sqlserver.jdbc;

    opens com.juank.utp.finimpact to javafx.fxml;
    opens com.juank.utp.finimpact.model to javafx.fxml;

    exports com.juank.utp.finimpact;
    exports com.juank.utp.finimpact.model;
    exports com.juank.utp.finimpact.repository;
    exports com.juank.utp.finimpact.utils;
}