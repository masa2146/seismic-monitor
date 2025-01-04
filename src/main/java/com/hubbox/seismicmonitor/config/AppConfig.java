package com.hubbox.seismicmonitor.config;

import lombok.Builder;

@Builder(toBuilder = true)
public record AppConfig(
    String host,
    Integer port,
    String network,
    String station,
    String channel,
    Integer staWindow,
    Integer ltaWindow,
    Double threshold,
    Integer maxDataPoints,
    Double normalizationFactor
) {
    public AppConfig() {
        this(
            "rtserve.iris.washington.edu",
            18000,
            "IU",
            "ANMO",
            "00BHZ",
            100,
            5000,
            0.1,
            1000,
            100000.0
        );
    }
}
