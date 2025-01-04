package com.hubbox.seismicmonitor;


import com.hubbox.seismicmonitor.config.AppConfig;
import com.hubbox.seismicmonitor.service.SeedLinkService;
import com.hubbox.seismicmonitor.service.StaLtaService;
import com.hubbox.seismicmonitor.view.ConfigurationDialog;
import com.hubbox.seismicmonitor.view.MonitorView;
import com.hubbox.seismicmonitor.viewmodel.MonitorViewModel;
import java.util.Optional;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SeismicMonitorApp extends Application {
    private AppConfig config;
    private SeedLinkService seedLinkService;
    private MonitorViewModel viewModel;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        try {
            initializeApplication();
        } catch (Exception e) {
            showErrorAndExit("Uygulama başlatılamadı", e);
        }
    }

    private void initializeApplication() {
        config = new AppConfig();

        if (!showConfigurationDialog()) {
            Platform.exit();
            return;
        }

        initializeServices();

        showMainWindow();

        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            handleApplicationClose();
        });

        log.info("Application started successfully");
    }

    private boolean showConfigurationDialog() {
        ConfigurationDialog dialog = new ConfigurationDialog(config);
        Optional<AppConfig> result = dialog.showAndWait();

        if (result.isPresent()) {
            config = result.get();
            return true;
        }
        return false;
    }

    private void initializeServices() {
        StaLtaService staLtaService;
        try {
            staLtaService = new StaLtaService(config);
            seedLinkService = new SeedLinkService(config, staLtaService);
            viewModel = new MonitorViewModel(seedLinkService, config);

            log.info("Services initialized successfully");
        } catch (Exception e) {
            showErrorAndExit("Servisler başlatılamadı", e);
        }
    }

    private void showMainWindow() {
        try {
            MonitorView monitorView = new MonitorView(viewModel);
            Scene scene = new Scene(monitorView, 1024, 768);

            primaryStage.setTitle("Sismik Monitör");
            primaryStage.setScene(scene);
            primaryStage.show();

            viewModel.startMonitoring();

            log.info("Main window displayed successfully");
        } catch (Exception e) {
            showErrorAndExit("Ana pencere oluşturulamadı", e);
        }
    }

    private void handleApplicationClose() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Çıkış");
        alert.setHeaderText("Uygulamadan çıkmak istiyor musunuz?");
        alert.setContentText("Tüm izleme işlemleri durdurulacak.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            shutdown();
        }
    }

    private void shutdown() {
        try {
            if (viewModel != null) {
                viewModel.stopMonitoring();
            }
            if (seedLinkService != null) {
                seedLinkService.stop();
            }

            log.info("Application shutdown successfully");
            Platform.exit();
        } catch (Exception e) {
            log.error("Error during shutdown", e);
            Platform.exit();
        }
    }

    private void showErrorAndExit(String message, Exception e) {
        log.error(message, e);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Hata");
        alert.setHeaderText(message);
        alert.setContentText("Hata detayı: " + e.getMessage());

        alert.showAndWait();
        Platform.exit();
    }

    @Override
    public void stop() {
        shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
