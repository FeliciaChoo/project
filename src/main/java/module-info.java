module com.csc3202.lab.project {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires transitive javafx.graphics;
    requires java.rmi; 
    
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    opens com.csc3202.lab.project to javafx.fxml;
    exports com.csc3202.lab.project;
    
}