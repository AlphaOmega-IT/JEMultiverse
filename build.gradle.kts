import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "de.jexcellence.multiverse"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    // JExcellence Implementations
    implementation("me.blvckbytes:BBConfigMapper:0.1") {
        isTransitive = false
    }
    implementation("me.blvckbytes:BukkitEvaluable:0.1") {
        isTransitive = false
    }
    implementation("me.blvckbytes:GPEEE:0.1") {
        isTransitive = false
    }
    implementation("de.jexcellence.commands:JECommands:1.0.0") {
        isTransitive = false
    }
    implementation("de.jexcellence.je18n:JE18n:1.0.0") {
        isTransitive = false
    }
    implementation("de.jexcellence.jeplatform:JEPlatform:1.0.0") {
        isTransitive = false
    }
    implementation("de.jexcellence.hibernate:JEHibernate:1.0.0") {
        isTransitive = false
    }
    implementation("de.jexcellence:inventory-framework-platform-bukkit:3.2.0")
    implementation("de.jexcellence:inventory-framework-platform-paper:3.2.0")

    compileOnly(platform("org.hibernate.orm:hibernate-platform:6.6.4.Final"))
    compileOnly("org.hibernate.orm:hibernate-core")
    compileOnly("jakarta.transaction:jakarta.transaction-api")
    compileOnly("com.github.ben-manes.caffeine:caffeine:3.1.8")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.register<ShadowJar>("cleanShadowJar") {
    dependsOn("clean", "shadowJar")
}

tasks.withType<ShadowJar> {
    destinationDirectory.set(file("C:\\Users\\Justin\\Desktop\\JExcellence\\JExcellence-Server\\plugins"))

    dependencies {
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/*.RSA")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = "Multiverse"
            version = project.version.toString()
        }
    }
    repositories {
        mavenLocal()
    }
}
