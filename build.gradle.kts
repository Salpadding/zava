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
    compileOnly("org.mybatis:mybatis:3.5.16")

    testImplementation("org.springframework.boot:spring-boot:3.2.5")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.5")
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa:3.2.5")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa:3.2.5")



    compileOnly("org.slf4j:slf4j-api:2.0.13")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.0")

    testImplementation("org.mybatis:mybatis:3.5.16")
    testImplementation("com.h2database:h2:2.2.224")
    compileOnly("org.mybatis.generator:mybatis-generator-core:1.4.2")
    testImplementation("org.mybatis.generator:mybatis-generator-core:1.4.2")

    testImplementation("commons-io:commons-io:2.16.1")
    compileOnly("commons-io:commons-io:2.16.1")

    compileOnly("org.apache.httpcomponents.client5:httpclient5:5.3.1")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.3.1")
    implementation("io.netty:netty-all:4.1.109.Final")
    compileOnly("io.netty:netty-all:4.1.109.Final")

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