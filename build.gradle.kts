plugins {
    java
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "com.thatgamerblue.oprs"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testCompile("junit", "junit", "4.12")
    implementation("com.google.code.gson:gson:2.8.6")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    jar {
        manifest {
            attributes(
                    "Main-Class" to "com.thatgamerblue.oprs.runewatch.Parser"
            )
        }
    }
}