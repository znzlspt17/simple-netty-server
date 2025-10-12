plugins {
    id("java")
}

group = "com.znzlspt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // https://mvnrepository.com/artifact/io.netty/netty-buffer
    implementation("io.netty:netty-buffer:4.2.6.Final")
    // https://mvnrepository.com/artifact/io.netty/netty-handler
    implementation("io.netty:netty-handler:4.2.6.Final")
    // https://mvnrepository.com/artifact/io.netty/netty-transport
    implementation("io.netty:netty-transport:4.2.6.Final")
    // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
    implementation("org.slf4j:slf4j-api:2.0.17")
    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation("ch.qos.logback:logback-classic:1.5.19")

    implementation(project(":netcore"))

}

tasks.test {
    useJUnitPlatform()
}