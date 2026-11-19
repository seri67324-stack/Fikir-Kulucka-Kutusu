package com.fikirkulucka.app.data

data class SuggestedIdea(
    val id: Int,
    val category: String,
    val title: String,
    val description: String,
    val howToDoIt: List<String>,
    val duration: String,
    val difficulty: String
)

object IdeaCatalogEngine {

    val categories: List<String> = listOf(
        "Tümü",
        "Yemek & Mutfak",
        "Yazılım & Teknoloji",
        "Kişisel Gelişim",
        "Ev & Yaşam",
        "Sanat & Tasarım",
        "Üretkenlik",
        "Doğa & Keşif",
        "Girişim & İş",
        "Öğrenme & Bilim",
        "Eğlence & Hobi"
    )

    private data class SeedTemplate(
        val category: String,
        val baseTitle: String,
        val detail: String,
        val steps: List<String>,
        val duration: String,
        val difficulty: String
    )

    private val seeds: List<SeedTemplate> = listOf(
        SeedTemplate(
            category = "Yemek & Mutfak",
            baseTitle = "Sarımsaklı Tereyağlı Ekşi Mayalı Kruton & Çorba İkilisi",
            detail = "Bayatlayan ekmekleri değerlendirip aromatik yağ ile fırınlayarak restoran kalitesinde çıtır kruton ve sıcak çorba hazırlama konsepti.",
            steps = listOf(
                "Ekmekleri 1.5 cm küpler halinde doğra ve geniş bir kaseye al.",
                "Eritilmiş tereyağı, zeytinyağı, ezilmiş sarımsak, kekik ve tuz ile harmanla.",
                "180°C fırında 10-12 dakika altın sarısı olana kadar çevirerek kızart.",
                "Kıvamlı sebze veya mercimek çorbasının üzerine servis anında ekle."
            ),
            duration = "25 dk",
            difficulty = "Kolay"
        ),
        SeedTemplate(
            category = "Yemek & Mutfak",
            baseTitle = "Ev Yapımı Fermente Acı Sos & Baharat Yağı",
            detail = "Taze biber, sarımsak ve seçili baharatlarla kendi damak tadına göre dengelenmiş acılık ve aroma profili oluşturma.",
            steps = listOf(
                "Biberleri yıka, saplarını ayıkla ve sarımsak dişleriyle birlikte iri parçalar halinde doğra.",
                "Cam kavanoza koyup %2 oranında kaya tuzu eklenmiş içme suyu ile üzerini kapla.",
                "Güneş görmeyen serin yerde 5-7 gün bekletip gazını günlük kontrol et.",
                "Süzüp az miktarda sirke ve zeytinyağı ile pürüzsüz kıvama getir."
            ),
            duration = "7 gün (aktif 20 dk)",
            difficulty = "Orta"
        ),
        SeedTemplate(
            category = "Yemek & Mutfak",
            baseTitle = "Tek Tavada Akdeniz Usulü Nohutlu & Ispanaklı Kase",
            detail = "Hızlı, dengeli, yüksek lifli ve tek bulaşıkla hazırlanan sıcak öğün tabağı.",
            steps = listOf(
                "Geniş tavada zeytinyağını ısıtıp soğan ve sarımsağı şeffaflaşana kadar sotele.",
                "Haşlanmış nohut, kimyon, kırmızı toz biber ve karabiber ekleyip 3 dakika kavur.",
                "Yıkanmış ıspanak yapraklarını ekleyip hacmi inene kadar çevir.",
                "Limon suyu ve dilersen yoğurt veya tahin sosu ile sıcak servis et."
            ),
            duration = "15 dk",
            difficulty = "Kolay"
        ),
        SeedTemplate(
            category = "Yazılım & Teknoloji",
            baseTitle = "Çevrimdışı Kişisel Bilgi & Hızlı Not Kartları Aracı",
            detail = "İnternet bağımlılığı olmadan çalışan, etiketli not ve tekrar kartı mantığıyla bilgi kalıcılığı sağlayan mikro araç.",
            steps = listOf(
                "Veri modeli için başlık, not gövdesi, etiket ve son tekrar tarihi alanlarını tanımla.",
                "Yerel veritabanı (Room veya SQLite) ile ekleme, arama ve filtreleme sorgularını yaz.",
                "Tek ekranda hızlı not girişi ve aralıklı tekrar listesini görünür kıl.",
                "Yedekleme için JSON dışa/içe aktarma özelliği ekle."
            ),
            duration = "2 gün",
            difficulty = "Orta"
        ),
        SeedTemplate(
            category = "Yazılım & Teknoloji",
            baseTitle = "Günlük Komut & Kısayol Otomasyon Betiği",
            detail = "Tekrarlayan dosya düzenleme, temizlik veya yedekleme işlerini tek komutla otomatikleştiren betik.",
            steps = listOf(
                "Her gün elle yaptığın en az 3 adımlı bir bilgisayar/telefon işlemini seç.",
                "Hangi klasörlerin veya dosyaların etkileneceğini net kurallarla sınırla.",
                "Önce kuru çalıştırma (dry-run) ile hangi dosyaların değişeceğini ekrana yazdır.",
                "Güvenli olduğundan emin olduktan sonra zamanlanmış görev veya kısayola bağla."
            ),
            duration = "1 saat",
            difficulty = "Kolay"
        ),
        SeedTemplate(
            category = "Kişisel Gelişim",
            baseTitle = "20 Dakikalık Akşam Zihin Boşaltma & Yarın Planı",
            detail = "Yatmadan önce zihindeki açık döngüleri kağıda döküp ertesi günün ilk 2 işini netleştirme alışkanlığı.",
            steps = listOf(
                "Telefonu sessize alıp boş bir sayfa aç ve aklındaki her şeyi filtresiz yaz.",
                "Yazdıklarından bugün kontrolün dışında olanları çiz.",
                "Yarın sabah yapılacak en kritik 1-2 adımı yıldızla işaretle.",
                "Defteri kapatıp ekran süresini sonlandır."
            ),
            duration = "20 dk",
            difficulty = "Kolay"
        ),
        SeedTemplate(
            category = "Ev & Yaşam",
            baseTitle = "Haftalık Dolap & Kiler Sıfır İsraf Envanteri",
            detail = "Evdeki mevcut malzemeleri görünür kılarak gereksiz alışverişi ve gıda israfını azaltma düzeni.",
            steps = listOf(
                "Buzdolabı, dondurucu ve kilerdeki açılmış ürünleri 5 dakikada listele.",
                "Önce tüketilmesi gereken ürünleri listenin en başına koy.",
                "Bu malzemelerle çıkacak 2-3 öğünlük menü planla.",
                "Eksik kalan yalnızca temel ihtiyaçları alışveriş listesine yaz."
            ),
            duration = "20 dk",
            difficulty = "Kolay"
        ),
        SeedTemplate(
            category = "Sanat & Tasarım",
            baseTitle = "Sınırlı 3 Renk Paletiyle Minimalist Poster Serisi",
            detail = "Yalnızca üç renk tonu ve geometrik formlar kullanarak görsel denge ve tipografi çalışması.",
            steps = listOf(
                "Bir koyu zemin, bir ana vurgu ve bir açık kontrast tonu belirle.",
                "Izgara (grid) sistemi kurup boşlukları (negative space) bilinçli koru.",
                "Tek bir kısa tema veya kavram seçip 3 farklı varyasyon tasarla.",
                "Yüksek çözünürlükte dışa aktar ve yan yana karşılaştır."
            ),
            duration = "1.5 saat",
            difficulty = "Orta"
        ),
        SeedTemplate(
            category = "Üretkenlik",
            baseTitle = "Tek Konulu 45 Dakikalık Derin Odak Bloğu",
            detail = "Bildirimleri kapatıp tek bir çıktıya odaklanarak yüzeysel işleri bölme yöntemi.",
            steps = listOf(
                "45 dakika içinde bitirilebilir somut tek bir çıktı belirle.",
                "Tüm sekme ve bildirimleri kapat; gerekirse uçak moduna geç.",
                "Zamanlayıcıyı başlat ve süre bitene kadar konu dışına çıkma.",
                "Süre dolduğunda 5-10 dakika mola verip ilerlemeyi not et."
            ),
            duration = "55 dk",
            difficulty = "Kolay"
        ),
        SeedTemplate(
            category = "Doğa & Keşif",
            baseTitle = "Yakın Çevrede Bilinmeyen Rota & Gözlem Yürüyüşü",
            detail = "Haritada hiç yürümediğin bir sokağı veya park hattını seçip çevre detaylarını gözlemleme.",
            steps = listOf(
                "Evden veya bulunduğun noktadan 1-3 km çapında hiç gitmediğin bir güzergah seç.",
                "Kulaklık takmadan sesleri, ışığı, mimariyi ve bitkileri fark etmeye çalış.",
                "İlginç bulduğun 3 noktayı not al veya fotoğrafını çek.",
                "Farklı bir dönüş yolu kullanarak rotayı tamamla."
            ),
            duration = "1 saat",
            difficulty = "Kolay"
        ),
        SeedTemplate(
            category = "Girişim & İş",
            baseTitle = "Küçük Bir Kitleye Odaklanan MVP (En Yalın Ürün) Taslağı",
            detail = "Aylarca kodlamak veya hazırlamak yerine tek bir sorunu çözen en küçük prototipi çıkarma.",
            steps = listOf(
                "Hedeflediğiniz kişinin canını en çok sıkan tek bir problemi net yaz.",
                "Bu problemi çözen ama ekstra özellik barındırmayan en basit akışı çiz.",
                "1 hafta içinde kullanılabilir ilk sürümü hazırla.",
                "Gerçek geri bildirim alıp ikinci iterasyona karar ver."
            ),
            duration = "1 hafta",
            difficulty = "İleri"
        ),
        SeedTemplate(
            category = "Öğrenme & Bilim",
            baseTitle = "Feynman Yöntemiyle Karmaşık Konuyu Basit Anlatma",
            detail = "Öğrendiğin bir konuyu jargon kullanmadan sanki ilk kez duyan birine anlatır gibi özetleme.",
            steps = listOf(
                "Anlamak istediğin kavramı veya konuyu seçip temel kaynaklardan oku.",
                "Kendi cümlelerinle, teknik terime boğmadan kağıda anlat.",
                "Takıldığın veya açıklarken zorlandığın boşlukları tespit et.",
                "Yalnızca o boşlukları tekrar inceleyip anlatımı sadeleştir."
            ),
            duration = "35 dk",
            difficulty = "Orta"
        ),
        SeedTemplate(
            category = "Eğlence & Hobi",
            baseTitle = "Arkadaş Grubuna Özel Mini Bilgi & Tahmin Yarışması",
            detail = "Kısa sürede hazırlanabilen, eğlenceli ve etkileşimli mini oyun veya soru seti kurgulama.",
            steps = listOf(
                "Ortak ilgi alanına göre 10-12 yaratıcı soru veya tahmin maddesi hazırla.",
                "Puanlama ve süre kuralını basit tut (örneğin 30 saniye düşünme süresi).",
                "Soru kartlarını veya ekran sunumunu hazırla.",
                "Kısa bir ısınma turuyla oyunu başlat ve puanları kaydet."
            ),
            duration = "40 dk",
            difficulty = "Kolay"
        )
    )

    private val variations: List<Pair<String, String>> = listOf(
        "Hızlı Versiyon" to "Süreyi kısaltıp pratik malzemelerle hızlandırılmış uygulanış.",
        "Bütçe Dostu Mod" to "Minimum maliyetle, evdeki imkanlarla uygulanabilen yaklaşım.",
        "Hafta Sonu Derinliği" to "Acele etmeden, detaylara özen göstererek keyifle yapılan versiyon.",
        "Minimalist Dokunuş" to "Gereksiz adımları eleyip özüne odaklanan sadeleştirilmiş hal.",
        "Yaratıcı Varyasyon" to "Klasik kalıbı bozup beklenmedik bir dokunuş ekleyen alternatif.",
        "Başlangıç Seviyesi" to "İlk kez deneyecekler için adım adım güvenli başlangıç uyarlaması.",
        "İleri Seviye Ustalık" to "Temeli oturttuktan sonra kaliteyi bir üst seviyeye taşıyan aşama.",
        "Paylaşımlı / Sosyal" to "Başkasıyla birlikte yapabileceğin veya sonucu paylaşabileceğin format.",
        "Zaman Kısıtlı Sprint" to "Kısa zaman diliminde somut bir çıktı almaya odaklanan yoğunlaştırılmış plan.",
        "Aylık Rutin Formatı" to "Tek seferlik değil, düzenli aralıklarla tekrarlanabilir sistem haline getirilmiş şekil."
    )

    private val angles: List<String> = listOf(
        "Akdeniz aroması & taze otlarla",
        "Baharat dengesi & isli notalarla",
        "Klasik tarifi modernleştirerek",
        "Az malzeme / yüksek etki prensibiyle",
        "Mevsimsel taze ürünlerle",
        "Hızlı hazırlık & temiz çalışma düzeniyle",
        "Katmanlı doku & kontrast lezzetlerle",
        "Kişiselleştirilebilir porsiyon mantığıyla",
        "Pratik şablon & kontrol listesiyle",
        "Ölçülebilir adım & net hedefle",
        "Günlük alışkanlığa entegre ederek",
        "Deneysel & keşif odaklı yaklaşımla",
        "Yüksek verim & düşük karmaşıklıkla",
        "Estetik sunum & düzen odağıyla",
        "Kendi tarzına göre uyarlayarak",
        "Adım adım tekrarlanabilir reçeteyle",
        "Hızlı prototip & test mantığıyla",
        "Zihin açıcı yeni bakış açısıyla",
        "Sade, net ve uygulanabilir planla",
        "Uzun vadeli fayda hedefiyle"
    )

    private val allIdeas: List<SuggestedIdea> by lazy {
        val list = ArrayList<SuggestedIdea>(10500)
        var counter = 1
        // 13 seeds * 10 variations * 21 angle slots (20 angles + 1 base) = 2730 per cycle -> 4 cycles = 10,920 ideas
        for (cycle in 1..4) {
            for (seed in seeds) {
                for ((varTitle, varDesc) in variations) {
                    for (angleIdx in 0..20) {
                        if (list.size >= 10500) break
                        val angleText = if (angleIdx == 0) seed.detail else "${seed.detail} (${angles[angleIdx - 1]})"
                        val titleSuffix = if (angleIdx == 0) "" else " • ${angles[angleIdx - 1]}"
                        val stepsWithCycle = if (cycle == 1) {
                            seed.steps
                        } else {
                            seed.steps + "Tur #$cycle notu: Önceki denemendeki tek bir detayı iyileştirerek uygula."
                        }
                        list.add(
                            SuggestedIdea(
                                id = counter++,
                                category = seed.category,
                                title = "${seed.baseTitle} — $varTitle$titleSuffix",
                                description = "$angleText $varDesc",
                                howToDoIt = stepsWithCycle,
                                duration = seed.duration,
                                difficulty = seed.difficulty
                            )
                        )
                    }
                    if (list.size >= 10500) break
                }
                if (list.size >= 10500) break
            }
            if (list.size >= 10500) break
        }
        list
    }

    fun getTotalCount(): Int = allIdeas.size

    fun queryIdeas(category: String, searchQuery: String): List<SuggestedIdea> {
        val q = searchQuery.trim().lowercase()
        return allIdeas.asSequence()
            .filter { item -> category == "Tümü" || item.category == category }
            .filter { item ->
                q.isEmpty() ||
                    item.title.lowercase().contains(q) ||
                    item.description.lowercase().contains(q) ||
                    item.category.lowercase().contains(q)
            }
            .toList()
    }
}
