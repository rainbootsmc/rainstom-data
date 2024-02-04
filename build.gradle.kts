plugins {
    `java-library`
//    id("net.kyori.blossom") version "2.0.0"
//    alias(libs.plugins.blossom)
    alias(libs.plugins.vanilla.gradle) apply false

    `maven-publish`
}

val branch = System.getenv()["GITHUB_REF_NAME"] ?: "unknown"
val buildNumber = System.getenv()["BUILD_NUMBER"] ?: "local-SNAPSHOT"

group = "net.rainbootsmc"
version = "$branch+build.$buildNumber"
description = "Generator for Minecraft game data values"

java {
    withSourcesJar()
    withJavadocJar()

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

//blossom {
//    val gitFile = "src/main/java/net/minestom/data/MinestomData.java"
//
//    val gitCommit = System.getenv("GIT_COMMIT")
//    val gitBranch = System.getenv("GIT_BRANCH")
//
//    replaceToken("\"&COMMIT\"", if (gitCommit == null) "null" else "\"${gitCommit}\"", gitFile)
//    replaceToken("\"&BRANCH\"", if (gitBranch == null) "null" else "\"${gitBranch}\"", gitFile)
//}

tasks.register("generateData") {
    logger.warn("Mojang requires all source-code and mappings used to be governed by the Minecraft EULA.")
    logger.warn("Please read the Minecraft EULA located at https://account.mojang.com/documents/minecraft_eula.")
    logger.warn("In order to agree to the EULA you must create a file called eula.txt with the text 'eula=true'.")
    val eulaTxt = File("${rootProject.projectDir}/eula.txt")
    logger.warn("The file must be located at '${eulaTxt.absolutePath}'.")
    if ((eulaTxt.exists() && eulaTxt.readText(Charsets.UTF_8).equals("eula=true", true))
        || project.properties["eula"].toString().toBoolean()
        || System.getenv("EULA")?.toBoolean() == true
    ) {
        logger.warn("")
        logger.warn("The EULA has been accepted and signed.")
        logger.warn("")
    } else {
        throw GradleException("Data generation has been halted as the EULA has not been signed.")
    }
    logger.warn("It is unclear if the data from the data generator also adhere to the Minecraft EULA.")
    logger.warn("Please consult your own legal team!")
    logger.warn("All data is given independently without warranty, guarantee or liability of any kind.")
    logger.warn("The data may or may not be the intellectual property of Mojang Studios.")
    logger.warn("")

    // Simplified by Sponge's VanillaGradle
    dependsOn(project(":DataGenerator").tasks.getByName<JavaExec>("run") {
        args = arrayListOf(rootDir.resolve("src/main/resources").absolutePath)
    })
}

tasks.register<Jar>("dataJar") {
    dependsOn("generateData")

    archiveBaseName.set("rainstom-data")
    archiveVersion.set(libs.versions.minecraft)
    destinationDirectory.set(layout.buildDirectory.dir("dist"))
    from(rootDir.resolve("src/main/resources").absolutePath)
}

tasks.processResources.get().dependsOn("generateData")

publishing {
    val uri = System.getenv()["S3_URI"]
    val accessKey = System.getenv()["S3_ACCESS_KEY"]
    val secretKey = System.getenv()["S3_SECRET_KEY"]

    publications {
        create<MavenPublication>("maven") {
            groupId = "net.rainbootsmc"
            artifactId = "rainstom-data"
            version = project.version.toString()

            artifact(tasks.getByName("dataJar"))
        }
    }
    repositories {
        maven {
            if (uri != null) {
                url = uri(uri)
            }
            credentials(AwsCredentials::class) {
                this.accessKey = accessKey
                this.secretKey = secretKey
            }
        }
    }
}