plugins {
    java
    kotlin("jvm") version "1.3.72"
}

group = "ir.parsijoo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(files("lib/stemmer-complete-0.1.0.jar"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.elasticsearch:elasticsearch:7.1.1")
    implementation("org.junit.platform:junit-platform-commons:1.6.2")
    implementation("org.opentest4j:opentest4j:1.2.0")
    implementation("org.apiguardian:apiguardian-api:1.1.0")

    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.6.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.6.2")
    testImplementation("org.junit.platform:junit-platform-engine:1.6.2")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    test {
        useJUnitPlatform()
    }
}
