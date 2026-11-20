plugins {
    java
    id("org.springframework.boot") version "4.1.2"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.mpazneb"
version = "1.0.0"
description = "Microservicio de sesiones de aula y resultados de MPAZ NEB"

repositories {
    mavenCentral()
}

dependencies {
    // API REST (controladores, JSON) y validación de los datos que llegan
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Persistencia con JPA/Hibernate sobre una base de datos H2 guardada en archivo
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-h2console")
    runtimeOnly("com.h2database:h2")

    // Lombok: genera getters, setters y constructores de las entidades
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Pruebas: JUnit 5, Mockito, AssertJ y MockMvc
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    options.release = 21
    options.encoding = "UTF-8"
    // Conserva los nombres de los parámetros (@PathVariable, @RequestParam)
    options.compilerArgs.add("-parameters")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
