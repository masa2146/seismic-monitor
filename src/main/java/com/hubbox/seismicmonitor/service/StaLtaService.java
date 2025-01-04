package com.hubbox.seismicmonitor.service;

import com.hubbox.seismicmonitor.config.AppConfig;
import java.util.LinkedList;
import java.util.Queue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StaLtaService {
    private final Queue<Double> staBuffer = new LinkedList<>();
    private final Queue<Double> ltaBuffer = new LinkedList<>();
    private final AppConfig config;
    private double staSum = 0.0;
    private double ltaSum = 0.0;

    public StaLtaService(AppConfig config) {
        this.config = config;
    }

    public boolean process(double sample) {
        double absValue = Math.abs(sample);
        updateBuffers(absValue);

        if (isBuffersFull()) {
            return false;
        }

        double ratio = calculateRatio();
        boolean detected = ratio > config.threshold();

        if (detected) {
            log.debug("Event detected! STA/LTA ratio: {}", ratio);
        }

        return detected;
    }

    private void updateBuffers(double value) {
        // Update STA buffer
        staSum += value;
        staBuffer.add(value);
        if (staBuffer.size() > config.staWindow()) {
            staSum -= staBuffer.remove();
        }

        // Update LTA buffer
        ltaSum += value;
        ltaBuffer.add(value);
        if (ltaBuffer.size() > config.ltaWindow()) {
            ltaSum -= ltaBuffer.remove();
        }
    }

    private boolean isBuffersFull() {
        return staBuffer.size() < config.staWindow() ||
            ltaBuffer.size() < config.ltaWindow();
    }

    public double getCurrentRatio() {
        if (isBuffersFull()) {
            return 0.0;
        }
        return calculateRatio();
    }

    private double calculateRatio() {
        double sta = staSum / config.staWindow();
        double lta = ltaSum / config.ltaWindow();
        return lta != 0 ? sta / lta : 0.0;
    }
}
