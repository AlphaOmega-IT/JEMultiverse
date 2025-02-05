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
    // JExcellence libraries
    implementation("de.jexcellence.commands:JECommands:1.0.0") { isTransitive = false; }
    implementation("de.jexcellence.config:Evaluable:1.0.0") { isTransitive = false; }
    implementation("de.jexcellence.config:GPEEE:1.0.0") { isTransitive = false; }
    implementation("de.jexcellence.config:ConfigMapper:1.0.0") { isTransitive = false; }
    implementation("de.jexcellence.je18n:JE18n:1.0.0") { isTransitive = false; }
    implementation("de.jexcellence.hibernate:JEHibernate:1.0.0") { isTransitive = false; }
    implementation("de.jexcellence.platform:JEPlatform:1.0.0") { isTransitive = false; }

    // Platform dependencies
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    // Inventory framework
    compileOnly("me.devnatan:inventory-framework-platform-bukkit:3.2.0")
    compileOnly("me.devnatan:inventory-framework-platform-paper:3.2.0")

    // Hibernate dependencies
    compileOnly(platform("org.hibernate.orm:hibernate-platform:6.6.4.Final"))
    compileOnly("org.hibernate.orm:hibernate-core")
    compileOnly("jakarta.transaction:jakarta.transaction-api")
    compileOnly("com.github.ben-manes.caffeine:caffeine:3.2.0")
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
    //destinationDirectory.set(file("C:\\Users\\Justin\\Desktop\\JExcellence\\JExcellence-Server\\plugins"))

    // Add versioning to the shadow jar
    archiveFileName.set("Multiverse.jar")

    dependencies {
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
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