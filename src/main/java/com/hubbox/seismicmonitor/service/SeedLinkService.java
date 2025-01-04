package com.hubbox.seismicmonitor.service;

import com.hubbox.seismicmonitor.config.AppConfig;
import com.hubbox.seismicmonitor.model.SeismicData;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.seedlink.SeedlinkPacket;
import edu.sc.seis.seisFile.seedlink.SeedlinkReader;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SeedLinkService {
    private final AppConfig config;
    private SeedlinkReader reader;
    private final ObjectProperty<SeismicData> latestData = new SimpleObjectProperty<>();
    private volatile boolean running = false;
    private final StaLtaService staLtaService;

    public SeedLinkService(AppConfig config, StaLtaService staLtaService) {
        this.config = config;
        this.staLtaService = staLtaService;
    }

    public void startDataCollection() {
        if (running) {
            log.warn("Data collection already running");
            return;
        }
        running = true;

        CompletableFuture.runAsync(() -> {
            try {
                reader = new SeedlinkReader(true);
                setupConnection();
                processData();
            } catch (Exception e) {
                log.error("Error in data collection", e);
                running = false;
            }
        });
    }

    private void setupConnection() throws Exception {
        log.info("Setting up SeedLink connection to {}:{}", config.host(), config.port());
        String[] helloResponse = reader.sendHello();
        for (String response : helloResponse) {
            log.debug("Server response: {}", response);
        }

        reader.sendStation(config.network(), config.station());
        reader.sendSelect(config.channel());
        reader.selectTime(config.network(), config.station(),
            List.of(config.channel()),
            Instant.now().minus(1, ChronoUnit.HOURS));
        reader.endHandshake();
        log.info("SeedLink connection established");
    }

    private void processData() throws IOException {
        log.info("Starting data processing");
        while (running && reader.hasNext()) {
            try {
                SeedlinkPacket packet = reader.next();
                if (packet != null) {
                    DataRecord mseed = packet.getMiniSeed();
                    if (mseed != null) {
                        processRecord(mseed);
                    }
                }
            } catch (Exception e) {
                log.error("Error processing data", e);
            }
        }
    }

    private void processRecord(DataRecord receivedData) {
        byte[] data = receivedData.getData();
        for (byte value : data) {
            String locationIdentifier = receivedData.getHeader().getLocationIdentifier();
            log.info("Received data from {}: {}", locationIdentifier, value);
            double normalizedValue = value / config.normalizationFactor();
            boolean eventDetected = staLtaService.process(normalizedValue);

            SeismicData seismicData = new SeismicData(
                normalizedValue,
                staLtaService.getCurrentRatio(),
                Instant.now(),
                receivedData.getHeader().getNetworkCode(),
                receivedData.getHeader().getStationIdentifier(),
                receivedData.getHeader().getChannelIdentifier(),
                eventDetected
            );

            latestData.set(seismicData);
        }
    }

    public ObjectProperty<SeismicData> latestDataProperty() {
        return latestData;
    }

    public void stop() {
        running = false;
        log.info("Stopping SeedLink service");
    }
}
