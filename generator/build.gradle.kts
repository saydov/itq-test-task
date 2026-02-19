plugins {
    java
    application
}

application {
    mainClass.set("com.github.saydov.generator.DocumentGenerator")
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "com.github.saydov.generator.DocumentGenerator")
    }
}
