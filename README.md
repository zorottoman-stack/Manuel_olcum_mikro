# Mikro Ölçüm Merkezi – sabit XLS şablonu

Bu proje, kullanıcının her seferinde Excel yüklemesini gerektirmez. Orijinal `sablon.xls` dosyası uygulamanın içine bir kez eklenmiştir. Backend Apache POI HSSF ile gerçek BIFF `.xls` dosyasını açar, yalnızca ölçüm ve tarih hücrelerini değiştirir ve aynı `.xls` biçiminde indirir.

## Önemli şablon eşleştirmeleri (yüklenen orijinal dosya incelendi)
- Sayfa: `KGY ÖLÇÜM RAPORU`
- Tarih: `F1`
- Seri numarası: `F11` hücresindeki orijinal formül dosya adından seri numarasını üretir. Bu formüle dokunulmaz. İndirilen dosyanın adı seri numarası olur.
- Ölçümler: K31:F13, K34:F14, K41:F15, K43:F16, B:F17, K32:F18, K33:F19, K36:F20, A:F21, C:F22, D:F23, H:F24, K35:F25, K42:F26, K44:F27.

## Yerel çalıştırma
Java 17 ve Maven gerekir:
`mvn spring-boot:run`
Ardından `http://localhost:8080` aç.

## Render üzerinde yayınlama
1. Bu klasörün içeriğini GitHub deposuna yükle.
2. Render'da New > Web Service > Build and deploy from a Git repository seç.
3. Depoyu bağla.
4. Runtime Docker seç; Render Dockerfile'ı otomatik algılar.
5. Deploy tamamlanınca Render adresini aç.
6. İstersen daha sonra kendi alan adını bu servise bağlayabilirsin.

## Güvenlik ve veri
Bu örnek servis gelen ölçüm isteğini bellekte işler; rapor dosyasını diske veya veritabanına kaydetmez. Ancak ölçüm verileri HTTP isteği sırasında barındırma sağlayıcısına ulaşır. Kurum verisi için şirket onayı olmadan kullanmayın; HTTPS kullanın.

## Biçim koruma notu
Apache POI, `.xls` dosyasını `.xls` olarak düzenler ve var olan hücre stillerini değiştirmeden hedef hücre değerlerini yazar. Excel'in tüm özel nesneleri, baskı ayarları, makrolar veya nadir çizim öğeleri için mutlak birebir garanti verilemez. İlk dağıtımdan sonra indirilen örnek rapor orijinal dosyayla görsel olarak karşılaştırılmalıdır.
