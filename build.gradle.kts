plugins {
    id("java")
}

group = "com.github.zava"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-console")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    testCompileOnly("org.projectlombok:lombok:1.18.32")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.32")


    implementation("org.apache.commons:commons-jexl3:3.3")
    implementation("net.bytebuddy:byte-buddy:1.14.15")
    implementation("com.google.guava:guava:33.0.0-jre")
    implementation("org.apache.commons:commons-lang3:3.14.0")


    compileOnly("org.springframework:spring-core:6.1.6")
    compileOnly("org.springframework:spring-context:6.1.6")

    testImplementation("org.springframework.boot:spring-boot:3.2.5")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.5")

    compileOnly("org.slf4j:slf4j-api:2.0.13")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.0")
}


tasks.register("cp") {
    println(sourceSets["main"].compileClasspath.asPath)
    println(sourceSets["main"].runtimeClasspath.asPath)
    println(sourceSets["test"].compileClasspath.asPath)
    println(sourceSets["test"].runtimeClasspath.asPath)
    println(project.group)
}

tasks.test {
    useJUnitPlatform()
}