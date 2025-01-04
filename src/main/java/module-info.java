module com.hubbox.seismicmonitor {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires org.slf4j;
    requires edu.sc.seis.seisFile;


    opens com.hubbox.seismicmonitor to javafx.fxml;
    exports com.hubbox.seismicmonitor;
    exports com.hubbox.seismicmonitor.config;
    exports com.hubbox.seismicmonitor.model;
    exports com.hubbox.seismicmonitor.service;
    exports com.hubbox.seismicmonitor.view;
    exports com.hubbox.seismicmonitor.viewmodel;
}
