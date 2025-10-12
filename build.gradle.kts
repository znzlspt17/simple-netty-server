plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0"
}

group = "com.znzlspt17"
version = "1.0-SNAPSHOT"

allprojects {
    group = "com.znzlspt17"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
}

dependencies {
    implementation(project(":netcore"))
    implementation(project(":server"))
}

tasks.shadowJar {
    archiveBaseName.set("server")
    archiveClassifier.set("jar-with-dependencies")

    manifest {
        attributes(
            "Main-Class" to "com.znzlspt.Main"
        )
    }

    mergeServiceFiles()

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
