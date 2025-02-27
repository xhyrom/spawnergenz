plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta9"
}

group = "me.xhyrom.spawnergenz"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.purpurmc.org/snapshots")
    maven("https://jitpack.io")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://hub.jeff-media.com/nexus/repository/jeff-media-public/")
    maven("https://repo.jopga.me/releases")
    maven("https://repo.essentialsx.net/releases/")
}

dependencies {
    compileOnly("org.purpurmc.purpur", "purpur-api", "1.21.4-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("dev.xhyrom.peddlerspocket:PeddlersPocket:1.1.0") {
        exclude(group = "net.kyori")
    }

    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    implementation("dev.jorel:commandapi-bukkit-shade:9.7.0")
    implementation("de.tr7zw:item-nbt-api:2.14.1")
    implementation("com.jeff-media:MorePersistentDataTypes:2.4.0")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group = "org.bukkit")
    }
    compileOnly("net.essentialsx:EssentialsX:2.20.1") {
        exclude(group = "org.bukkit")
        exclude(group = "org.spigotmc")
    }
    compileOnly("com.github.Gypopo:EconomyShopGUI-API:1.7.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks {
    shadowJar {
        relocate("dev.jorel.commandapi", "me.xhyrom.spawnergenz.libs.commandapi")
        relocate("com.jeff-media.morepersistentdatatypes", "me.xhyrom.spawnergenz.libs.morepersistentdatatypes")
        relocate("de.tr7zw.changeme.nbtapi", "me.xhyrom.spawnergenz.libs.nbtapi")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}