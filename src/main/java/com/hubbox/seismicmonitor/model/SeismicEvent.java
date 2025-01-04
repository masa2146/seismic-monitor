package com.hubbox.seismicmonitor.model;

import java.time.Instant;
import lombok.Builder;

@Builder
public record SeismicEvent(
    Instant detectionTime,
    double magnitude,
    double staLtaRatio,
    String network,
    String station,
    String channel) {
}
