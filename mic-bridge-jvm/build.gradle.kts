// Bu modül Android modülü DEĞİL: sıradan bir Java kütüphanesi olarak derlenir
// (Android'in D8/R8 ile DEX'e çevirdiği kod, gömülü/misafir JVM'in çalıştırdığı
// standart JVM bytecode'u ile UYUMLU DEĞİLDİR). Üretilen micbridge.jar,
// :LisyLauncher modülünün assets/components/micbridge/ klasörüne kopyalanır
// ve oyun başlatılırken -Xbootclasspath/a: ile misafir JVM'e eklenir
// (bkz. Launcher.kt / getCacioJavaArgs).
plugins {
    `java-library`
}

java {
    // Oyunun kullanabileceği en eski Java sürümüyle (Java 8) bile çalışsın diye
    // bytecode hedefi düşük tutuldu; modern JVM'ler eski bytecode'u sorunsuz çalıştırır.
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.jar {
    archiveBaseName.set("micbridge")
    archiveVersion.set("")
    archiveClassifier.set("")
}
