/*
 * Copyright (c) 2026 Michaël Moser.
 *
 * Auteur : Michaël Moser
 * Tous droits réservés.
 */

///////////////////////////////////////////
/*
Pour un installateur Windows (--type exe ou --type msi)
    •Un outil tiers est indispensable : WiX Toolset (version 3.x).
    •jpackage délègue la compilation du paquet d'installation Windows aux utilitaires candle.exe et light.exe fournis par WiX v3.
    •Si WiX n'est pas installé, la tâche de génération d'installateur échouera systématiquement.

winget install WiX.Toolset
*/

///////////////////////////////////////////
/*
Pour un installateur Windows (--type exe ou --type msi)
    •Un outil tiers est indispensable : WiX Toolset (version 3.x).
    •jpackage délègue la compilation du paquet d'installation Windows aux utilitaires candle.exe et light.exe fournis par WiX v3.
    •Si WiX n'est pas installé, la tâche de génération d'installateur échouera systématiquement.

winget install WiX.Toolset
*/

import java.nio.file.Files

plugins {
    java
    application
    id("org.javamodularity.moduleplugin") version "1.8.15"
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "fr.btsciel"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
}

val junitVersion = "6.1.3"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("com.astier.bts.client_tcp_prof")
    mainClass.set("com.astier.bts.client_tcp_prof.HelloApplication")
}

javafx {
    version = "21.0.6"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation("com.google.code.gson:gson:2.14.0")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:3.0.8")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val appName = "ClientTCP"
val mainClassName = "com.astier.bts.client_tcp_prof.HelloApplication"
val appIcon = file("icons/icon.ico").absolutePath

// Copie du fichier JSON de la racine vers le dossier input de jpackage (build/libs)
val copyConfigFile = tasks.register<Copy>("copyConfigFile") {
    from(file("configuration_json.json"))
    into(layout.buildDirectory.dir("libs"))
}

val copyDependencies = tasks.register<Copy>("copyDependencies") {
    from(configurations.runtimeClasspath)
    into(layout.buildDirectory.dir("libs"))
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = mainClassName
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    finalizedBy(copyConfigFile,copyDependencies)
}

// Copie le fichier JSON directement à côté du .exe après la génération de jpackageImage
val copyJsonToImageRoot = tasks.register<Copy>("copyJsonToImageRoot") {
    from(file("configuration_json.json"))
    into(layout.buildDirectory.dir("jpackage-image/$appName"))
    dependsOn("jpackageImage")
}

tasks.register<Exec>("jpackageImage") {
    group = "distribution"
    description = "Crée une application portable console avec jpackage"

    dependsOn(tasks.jar, copyConfigFile)
    finalizedBy(copyJsonToImageRoot)

    doFirst {
        val outputDir = layout.buildDirectory.dir("jpackage-image").get().asFile
        if (outputDir.exists()) {
            outputDir.walkBottomUp().forEach { file ->
                try {
                    Files.setAttribute(file.toPath(), "dos:readonly", false)
                } catch (_: Exception) {}
                file.setWritable(true)
                file.delete()
            }
        }
    }

    commandLine(
        "jpackage",
        "--type", "app-image",
        "--name", appName,
        "--icon", appIcon,
        "--input", tasks.jar.get().archiveFile.get().asFile.parentFile.absolutePath,
        "--main-jar", tasks.jar.get().archiveFileName.get(),
        "--main-class", mainClassName,
        "--dest", layout.buildDirectory.dir("jpackage-image").get().asFile.absolutePath,
//        "--win-console"
    )
}

tasks.register<Exec>("jpackage") {
    group = "distribution"
    description = "Crée un installateur Windows .exe console avec jpackage"

    // L'installateur est construit à partir de l'app-image (qui contient déjà le JSON à côté du .exe)
    dependsOn(copyJsonToImageRoot)

    doFirst {
        val outputDir = layout.buildDirectory.dir("installer").get().asFile
        if (outputDir.exists()) {
            outputDir.walkBottomUp().forEach { file ->
                try {
                    Files.setAttribute(file.toPath(), "dos:readonly", false)
                } catch (_: Exception) {}
                file.setWritable(true)
                file.delete()
            }
        }
    }

    val cleanVersion = project.version.toString().substringBefore('-')

    commandLine(
        "jpackage",
        "--type", "exe",
        "--name", appName,
        "--app-version", cleanVersion,
        "--vendor", "fr.btsciel",
        "--description", "Client TCP de base",
        "--app-image", layout.buildDirectory.dir("jpackage-image/$appName").get().asFile.absolutePath,
        "--dest", layout.buildDirectory.dir("installer").get().asFile.absolutePath,
//        "--win-console",
        "--win-dir-chooser",
        "--win-menu",
        "--win-shortcut"
    )
}
