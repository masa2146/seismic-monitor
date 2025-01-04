package com.hubbox.seismicmonitor.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MonitorViewText {

    @UtilityClass
    public static final class Chart {
        public static final String TIME_AXIS = "Zaman";
        public static final String AMPLITUDE_AXIS = "Genlik";
        public static final String SEISMIC_CHART_TITLE = "Sismik Veri";
        public static final String SEISMIC_SERIES_NAME = "Sismik Sinyal";
        public static final String STA_LTA_RATIO_AXIS = "STA/LTA Oranı";
        public static final String STA_LTA_CHART_TITLE = "STA/LTA Oranı";
        public static final String STA_LTA_SERIES_NAME = "STA/LTA";
    }

    @UtilityClass
    public static final class Button {
        public static final String RESET_ZOOM = "Zoom Sıfırla";
        public static final String START_MONITORING = "İzlemeyi Başlat";
        public static final String STOP_MONITORING = "İzlemeyi Durdur";
        public static final String SETTINGS = "Ayarlar";
        public static final String CLEAR_CHARTS = "Grafikleri Temizle";
    }

    @UtilityClass
    public static final class Status {
        public static final String READY = "Hazır";
        public static final String MONITORING = "İzleniyor";
        public static final String CONNECTION_WAITING = "Bağlantı: Bekliyor";
        public static final String CONNECTION_ACTIVE = "Bağlantı: Aktif";
        public static final String SEISMIC_EVENT_DETECTED = "!!! SİSMİK OLAY TESPİT EDİLDİ !!!";
    }

    @UtilityClass
    public static final class Dialog {
        public static final String CONFIG_TITLE = "Sismik Monitör Konfigürasyonu";
        public static final String CONFIG_HEADER = "Bağlantı ve Analiz Parametrelerini Ayarlayın";
        public static final String SAVE = "Kaydet";
        public static final String CONNECTION_SETTINGS = "Bağlantı Ayarları";
        public static final String ANALYSIS_SETTINGS = "Analiz Parametreleri";
    }

    @UtilityClass
    public static final class Form {
        public static final String HOST_LABEL = "Sunucu Adresi:";
        public static final String HOST_PROMPT = "Sunucu adresi girin";
        public static final String PORT_LABEL = "Port:";
        public static final String NETWORK_LABEL = "Network:";
        public static final String NETWORK_PROMPT = "Network kodu girin";
        public static final String STATION_LABEL = "İstasyon:";
        public static final String STATION_PROMPT = "İstasyon kodu girin";
        public static final String CHANNEL_LABEL = "Kanal:";
        public static final String CHANNEL_PROMPT = "Kanal kodu girin";
        public static final String STA_WINDOW_LABEL = "STA Pencere Boyutu:";
        public static final String LTA_WINDOW_LABEL = "LTA Pencere Boyutu:";
        public static final String THRESHOLD_LABEL = "Tetikleme Eşiği:";
        public static final String MAX_DATA_POINTS_LABEL = "Maksimum Veri Noktası:";
        public static final String NORMALIZATION_FACTOR_LABEL = "Normalizasyon Faktörü:";
    }

    @UtilityClass
    public static final class Tooltip {
        public static final String HOST = "SeedLink sunucusunun adresi";
        public static final String PORT = "SeedLink sunucusunun port numarası";
        public static final String NETWORK = "Network kodu (örn: IU)";
        public static final String STATION = "İstasyon kodu (örn: ANMO)";
        public static final String CHANNEL = "Kanal kodu (örn: BHZ)";
        public static final String STA_WINDOW = "Kısa dönem ortalama pencere boyutu";
        public static final String LTA_WINDOW = "Uzun dönem ortalama pencere boyutu";
        public static final String THRESHOLD = "STA/LTA tetikleme eşik değeri";
        public static final String MAX_DATA_POINTS = "Grafikte gösterilecek maksimum veri noktası sayısı";
        public static final String NORMALIZATION_FACTOR = "Ham veri normalizasyon faktörü";
    }

    @UtilityClass
    public static final class Error {
        public static final String CONFIG_ERROR_TITLE = "Konfigürasyon hatası";
    }
}
