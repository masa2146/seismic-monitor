package com.hubbox.seismicmonitor.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class SeismicData {
    double value;
    double staLtaRatio;
    Instant timestamp;
    String network;
    String station;
    String channel;
    boolean eventDetected;
}
