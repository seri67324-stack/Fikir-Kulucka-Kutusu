package com.fikirkulucka.app.data

data class DetailedActionPlan(
    val title: String,
    val category: String,
    val howItWorks: String,
    val whereToStart: List<String>,
    val stepByStepExecution: List<String>,
    val howToContinue: List<String>,
    val concreteExample: String,
    val pitfallsAndSolutions: List<String>
)

object ActionGuideEngine {

    fun buildPlan(idea: IdeaEntity): DetailedActionPlan {
        val cleanTitle = idea.content.substringAfter("] ").ifBlank { idea.content }
        val detectedCategory = when {
            idea.category != "Kişisel Fikir" -> idea.category
            idea.content.startsWith("[") && idea.content.contains("]") ->
                idea.content.substringAfter("[").substringBefore("]")
            cleanTitle.contains("yemek", true) || cleanTitle.contains("tarif", true) ||
                cleanTitle.contains("sos", true) || cleanTitle.contains("ekmek", true) ||
                cleanTitle.contains("nohut", true) -> "Yemek & Mutfak"
            cleanTitle.contains("uygulama", true) || cleanTitle.contains("kod", true) ||
                cleanTitle.contains("betik", true) || cleanTitle.contains("yazılım", true) -> "Yazılım & Teknoloji"
            else -> "Özel Proje & Uygulama"
        }

        val customSteps = idea.stepsSerialized
            .split("||")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val baseDescription = idea.detailText.ifBlank {
            "Bu fikir, ham bir düşünce aşamasından çıkarılıp ölçülebilir adımlara bölünmüş uygulanabilir bir eylem planıdır. Temel çalışma prensibi; küçük parçalarla hızlı sonuç almak, her adımda çıktıyı test etmek ve gereksiz karmaşıklığı elemektir."
        }

        val whereToStart = when (detectedCategory) {
            "Yemek & Mutfak" -> listOf(
                "İlk 5 dakika: Tezgahı boşalt, gerekli tüm malzemeleri ve ölçü kaplarını tek tepsiye diz (Mise en Place).",
                "Malzeme kontrolü: Eksik malzeme varsa ikame edebileceğin alternatifi pişirmeye başlamadan belirle.",
                "Isı ve ekipman hazırlığı: Fırın veya tavayı tarife başlamadan önce ideal sıcaklığa getir."
            )
            "Yazılım & Teknoloji" -> listOf(
                "İlk 15 dakika: Tek sayfalık bir kağıda yalnızca 3 temel ekranı/işlevi ve veri alanlarını çiz.",
                "Kapsam kilidi: İlk sürümde olmayacak her ekstra özelliği 'v2 Listesi'ne at ve bugün dokunma.",
                "Çalışan çekirdek: Veri girişi -> kayıt -> listeleme döngüsünü tek bir test verisiyle ayağa kaldır."
            )
            else -> listOf(
                "İlk 10 dakika: Bu fikrin bitmiş halinin tek cümlelik somut çıktısını yaz (Örn: 'Bugün 1 çalışan prototip/plan hazır olacak').",
                "Kaynak listesi: İhtiyacın olan zamanı (en az 30-45 dk kesintisiz blok) ve araçları masaya koy.",
                "En küçük ilk adım: Mükemmel olmasını beklemeden 15 dakikada tamamlanabilecek ilk parçayı başlat."
            )
        }

        val stepByStep = if (customSteps.isNotEmpty()) {
            customSteps.mapIndexed { index, step ->
                "Adım ${index + 1}: $step (Tamamlandığında sonucu kontrol edip bir sonraki aşamaya geç.)"
            }
        } else {
            listOf(
                "Adım 1 — Çerçeve Kurulumu: '$cleanTitle' fikrini 3 alt parçaya böl; her parçaya net bir bitiş kriteri yaz.",
                "Adım 2 — İlk Prototip / Taslak Üretimi: Süreyi 45 dakikaya kur ve dış dünyayı kapatıp ilk çalışan taslağı ortaya çıkar.",
                "Adım 3 — Test ve İyileştirme: Ortaya çıkan sonucu gerçek koşullarda dene; aksayan tek noktayı düzelt.",
                "Adım 4 — Paketleme ve Tamamlama: Çıktıyı kullanılabilir, kaydedilebilir veya paylaşılabilir son haline getir."
            )
        }

        val howToContinue = listOf(
            "Günlük 20-30 Dakika Kuralı: 'Devam Ediyoruz' durumundayken her gün sadece tek bir alt adımı tamamla; zinciri kırma.",
            "Haftalık Gözden Geçirme: Her 3 günde bir 'Ne çalışıyor, ne zaman kaybettiriyor?' sorusunu sorup gereksiz adımları çıkar.",
            "Sürüm Mantığı: İlk hali %100 kusursuz yapmak yerine önce %70 çalışan halini bitir, sonra üzerine cila at.",
            "Tamamlama Eşiği: Tüm kontrol adımları bittiğinde fikri başarıyla tamamlanmış projeler arşivine taşı."
        )

        val concreteExample = when (detectedCategory) {
            "Yemek & Mutfak" ->
                "Somut Örnek: 'Sarımsaklı Kruton & Çorba' fikrini onayladın. Saat 18:00'de malzemeleri çıkardın (Başladım), 18:10'da ekmekleri doğrayıp sosladın ve fırına verdin (Devam Ediyoruz), 18:25'te sıcak çorba ile servis edip tadım notunu kaydettin."
            "Yazılım & Teknoloji" ->
                "Somut Örnek: 'Çevrimdışı Not Aracı' fikrini onayladın. İlk 15 dakikada veri tablosunu çizdin (Başladım), ertesi gün ekleme ve arama ekranını kodlayıp telefonda test ettin (Devam Ediyoruz), 3. günde yedekleme butonunu ekleyip kullanıma aldın."
            else ->
                "Somut Örnek: '$cleanTitle' fikrini onayladıktan hemen sonra 'Başladım' seçip 15 dakikalık hazırlık listesini çıkardın. Ertesi gün ilk somut parçayı üreterek 'Devam Ediyoruz' aşamasına geçtin ve 3. gün sonunda somut çıktıyı tamamladın."
        }

        val pitfalls = listOf(
            "Tuzak 1 — Aşırı Planlama Felci: Günlerce araştırma yapıp hiç üretmemek. Çözüm: İlk 15 dakikada mutlaka somut bir taslak oluştur.",
            "Tuzak 2 — Kapsamı Büyütme: Başladıktan sonra fikre sürekli yeni eklemeler yapmak. Çözüm: Yeni fikirleri tekrar Kuluçka Kutusuna at, mevcut işi bölme.",
            "Tuzak 3 — Yarım Bırakma: İlk engelde motivasyon kaybı. Çözüm: Zor gelen adımı 10 dakikalık daha küçük iki parçaya böl."
        )

        return DetailedActionPlan(
            title = cleanTitle,
            category = detectedCategory,
            howItWorks = baseDescription,
            whereToStart = whereToStart,
            stepByStepExecution = stepByStep,
            howToContinue = howToContinue,
            concreteExample = concreteExample,
            pitfallsAndSolutions = pitfalls
        )
    }
}
