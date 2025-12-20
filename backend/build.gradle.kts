plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
}

group = "com.example"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

dependencies {
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.gson)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)

    implementation("org.jetbrains.exposed:exposed-core:0.42.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.42.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.42.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.42.1")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("com.zaxxer:HikariCP:5.0.1")

    // Security
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")

    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("io.ktor:ktor-server-status-pages-jvm")
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.11")

    // Testing
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    implementation("io.ktor:ktor-server-cors:2.3.7")
}
