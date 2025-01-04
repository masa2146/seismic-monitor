# Sismik Monitör Uygulaması

## Genel Bakış
Sismik Monitör, gerçek zamanlı sismik verileri görüntülemek ve analiz etmek için geliştirilmiş bir Java uygulamasıdır. Uygulama, SeedLink protokolü üzerinden sismik istasyonlardan veri alır ve bu verileri görsel olarak sunar.

## Özellikler
- Gerçek zamanlı sismik veri görüntüleme
- STA/LTA (Short Time Average/Long Time Average) analizi
- Otomatik sismik olay tespiti
- İnteraktif grafik arayüzü (yakınlaştırma, kaydırma)
- Özelleştirilebilir konfigürasyon ayarları

## Sistem Gereksinimleri
- Java Runtime Environment (JRE) 11 veya üzeri
- JavaFX runtime
- Internet bağlantısı

## Kurulum
1. Uygulamanın JAR dosyasını indirin
2. Java 11 veya üzeri bir sürümün yüklü olduğundan emin olun
3. Terminal veya komut istemcisinde aşağıdaki komutu çalıştırın:
   ```bash
   java -jar seismic-monitor.jar
   ```

## Kullanım
1. Uygulama başlatıldığında ilk olarak konfigürasyon penceresi açılır
2. Gerekli ayarları yapılandırın
3. "İzlemeyi Başlat" butonuna tıklayarak veri akışını başlatın
4. Grafikleri yakınlaştırmak için CTRL + Fare Tekerleği kullanın
5. Grafik üzerinde seçili bir alanı yakınlaştırmak için SHIFT + Sol Tık ile seçim yapın
6. Grafiği kaydırmak için orta fare tuşunu kullanın

## Konfigürasyon Parametreleri

### Bağlantı Ayarları
- **Sunucu Adresi**: SeedLink sunucusunun adresi (Varsayılan: rtserve.iris.washington.edu)
- **Port**: Sunucu port numarası (Varsayılan: 18000)
- **Network**: Sismik ağ kodu (Örn: IU)
- **İstasyon**: İstasyon kodu (Örn: ANMO)
- **Kanal**: Veri kanalı (Örn: BHZ)

### Analiz Parametreleri
- **STA Pencere Boyutu**: Kısa dönem ortalama pencere boyutu (Varsayılan: 100)
- **LTA Pencere Boyutu**: Uzun dönem ortalama pencere boyutu (Varsayılan: 5000)
- **Tetikleme Eşiği**: Sismik olay tespiti için STA/LTA oranı eşiği (Varsayılan: 0.1)
- **Maksimum Veri Noktası**: Grafikteki maksimum veri noktası sayısı (Varsayılan: 1000)
- **Normalizasyon Faktörü**: Ham veri normalizasyon faktörü (Varsayılan: 100000.0)

## Grafik Arayüzü
Uygulama iki ana grafik içerir:
1. **Sismik Veri Grafiği**: Ham sismik sinyalleri gösterir
2. **STA/LTA Grafiği**: Sinyal/gürültü oranını ve olası sismik olayları gösterir

## Sismik Olay Tespiti
- Uygulama, STA/LTA algoritması kullanarak otomatik olarak sismik olayları tespit eder
- Tespit edilen olaylar grafikte işaretlenir ve durum çubuğunda bildirim gösterilir
- Tetikleme eşiği konfigürasyon penceresinden ayarlanabilir

## Hata Ayıklama
- Uygulama logları konsol ve log dosyasında tutulur
- Bağlantı hatası durumunda otomatik olarak yeniden bağlanmayı dener
- Hata durumları kullanıcıya bildirim olarak gösterilir

## Güvenlik Notları
- Uygulama sadece okuma amaçlı veri alır
- Sunucu bağlantısı güvenli bir protokol üzerinden yapılır
- Hassas veri içermez ve yerel depolama yapmaz

