package com.hubbox.seismicmonitor.viewmodel;

import com.hubbox.seismicmonitor.config.AppConfig;
import com.hubbox.seismicmonitor.model.SeismicData;
import com.hubbox.seismicmonitor.model.SeismicEvent;
import com.hubbox.seismicmonitor.service.SeedLinkService;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MonitorViewModel {
    private static final int BATCH_SIZE = 20; // UI güncelleme batch boyutu
    private static final long UI_UPDATE_INTERVAL = 50; // milisaniye

    private final SeedLinkService seedLinkService;
    @Getter private final AppConfig config;

    @Getter private final ObservableList<SeismicData> seismicDataList;
    @Getter private final ObservableList<SeismicEvent> eventList;
    @Getter private final StringProperty statusMessage;
    @Getter private final BooleanProperty monitoring;
    @Getter private final DoubleProperty currentStaLtaRatio;
    @Getter private final BooleanProperty eventDetected;

    private final Queue<SeismicData> dataBuffer;
    private final List<SeismicData> batchBuffer;
    private long lastUIUpdate;

    public MonitorViewModel(SeedLinkService seedLinkService, AppConfig config) {
        this.seedLinkService = seedLinkService;
        this.config = config;

        this.seismicDataList = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
        this.eventList = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
        this.statusMessage = new SimpleStringProperty("Hazır");
        this.monitoring = new SimpleBooleanProperty(false);
        this.currentStaLtaRatio = new SimpleDoubleProperty(0.0);
        this.eventDetected = new SimpleBooleanProperty(false);

        this.dataBuffer = new ConcurrentLinkedQueue<>();
        this.batchBuffer = new ArrayList<>(BATCH_SIZE);

        initialize();
    }

    private void initialize() {
        seedLinkService.latestDataProperty().addListener((obs, old, newData) -> {
            if (newData != null) {
                handleNewData(newData);
            }
        });

        // Event sıfırlama için ayrı thread
        eventDetected.addListener((obs, oldValue, newValue) -> {
            if (Boolean.TRUE.equals(newValue)) {
                CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS).execute(() ->
                    Platform.runLater(() -> eventDetected.set(false))
                );
            }
        });
    }

    private void handleNewData(SeismicData data) {
        dataBuffer.add(data);

        // Buffer boyut kontrolü
        while (dataBuffer.size() > config.maxDataPoints()) {
            dataBuffer.poll();
        }

        // Batch update kontrolü
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUIUpdate >= UI_UPDATE_INTERVAL) {
            updateUIBatch();
            lastUIUpdate = currentTime;
        }

        // Event kontrolü
        if (data.isEventDetected()) {
            Platform.runLater(() -> handleEventDetection(data));
        }
    }

    private void updateUIBatch() {
        batchBuffer.clear();

        // Batch için veri topla
        for (int i = 0; i < BATCH_SIZE && !dataBuffer.isEmpty(); i++) {
            SeismicData data = dataBuffer.poll();
            if (data != null) {
                batchBuffer.add(data);
            }
        }

        if (!batchBuffer.isEmpty()) {
            Platform.runLater(() -> {
                // Son veriyi al
                SeismicData lastData = batchBuffer.get(batchBuffer.size() - 1);

                // UI güncellemelerini yap
                seismicDataList.addAll(batchBuffer);
                while (seismicDataList.size() > config.maxDataPoints()) {
                    seismicDataList.remove(0);
                }

                currentStaLtaRatio.set(lastData.getStaLtaRatio());
                updateStatus(lastData);
            });
        }
    }

    private void handleEventDetection(SeismicData data) {
        SeismicEvent event = new SeismicEvent(
            data.getTimestamp(),
            data.getValue(),
            data.getStaLtaRatio(),
            data.getNetwork(),
            data.getStation(),
            data.getChannel()
        );

        eventList.add(0, event);
        if (eventList.size() > 100) {
            eventList.remove(100, eventList.size());
        }

        eventDetected.set(true);
        log.info("Seismic event detected: {}", event);
    }

    private void updateStatus(SeismicData data) {
        String status = String.format("Son Veri - Ağ: %s, İstasyon: %s, Kanal: %s, STA/LTA: %.2f",
            data.getNetwork(), data.getStation(), data.getChannel(), data.getStaLtaRatio());
        statusMessage.set(status);
    }

    public void startMonitoring() {
        if (!monitoring.get()) {
            try {
                seedLinkService.startDataCollection();
                monitoring.set(true);
                statusMessage.set("İzleme başlatıldı");
                log.info("Monitoring started");
            } catch (Exception e) {
                log.error("Failed to start monitoring", e);
                statusMessage.set("İzleme başlatılamadı: " + e.getMessage());
            }
        }
    }

    public void stopMonitoring() {
        if (monitoring.get()) {
            try {
                seedLinkService.stop();
                monitoring.set(false);
                statusMessage.set("İzleme durduruldu");
                log.info("Monitoring stopped");
            } catch (Exception e) {
                log.error("Failed to stop monitoring", e);
                statusMessage.set("İzleme durdurulamadı: " + e.getMessage());
            }
        }
    }

    public void clearData() {
        dataBuffer.clear();
        seismicDataList.clear();
        eventList.clear();
        currentStaLtaRatio.set(0.0);
        statusMessage.set("Veriler temizlendi");
        log.info("Data cleared");
    }

    public void updateConfig(AppConfig newConfig) {
        boolean wasMonitoring = monitoring.get();
        if (wasMonitoring) {
            stopMonitoring();
        }

        // Update config
        config.toBuilder().host(newConfig.host());
        config.toBuilder().port(newConfig.port());
        config.toBuilder().network(newConfig.network());
        config.toBuilder().station(newConfig.station());
        config.toBuilder().channel(newConfig.channel());
        config.toBuilder().staWindow(newConfig.staWindow());
        config.toBuilder().ltaWindow(newConfig.ltaWindow());
        config.toBuilder().threshold(newConfig.threshold());
        config.toBuilder().maxDataPoints(newConfig.maxDataPoints());
        config.toBuilder().normalizationFactor(newConfig.normalizationFactor());

        clearData();

        if (wasMonitoring) {
            startMonitoring();
        }

        log.info("Configuration updated");
    }

    public double getLastValue() {
        if (seismicDataList.isEmpty()) {
            return 0.0;
        }
        return seismicDataList.get(seismicDataList.size() - 1).getValue();
    }

    public boolean isEventInProgress() {
        return eventDetected.get();
    }

    public void saveCurrentConfig() {
        log.info("Configuration saved");
    }

    public void loadConfig() {
        log.info("Configuration loaded");
    }
}
