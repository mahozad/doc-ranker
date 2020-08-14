plugins {
    java
    kotlin("jvm") version "1.3.72"
}

group = "ir.parsijoo"
version = "1.0-RC"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(files("lib/parsi-analyzer-1.2.0.jar"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.elasticsearch:elasticsearch:7.1.1")

    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.6.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.6.2")
    testImplementation("org.junit.platform:junit-platform-engine:1.6.2")
    testImplementation("org.junit.platform:junit-platform-commons:1.6.2")
    testImplementation("com.opencsv:opencsv:5.2")
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
        // Enable running Junit5 tests.
        useJUnitPlatform()

        // NOTE: If the tests are built and run using gradle (either executing the gradle task directly or when Intellij is set
        //  to build and run the tests using 'gradle' in settings -> build -> gradle), the gradle default encoding seems
        //  to be something other than 'UTF-8', so the tests that contain UTF characters do not run correctly.
        //  To fix it set gradle java argument org.gradle.jvmargs=-Dfile.encoding=UTF-8 in gradle.properties file.
        //  See https://stackoverflow.com/q/21267234 for more info.
    }
}
