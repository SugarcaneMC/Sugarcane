plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
    id("io.papermc.paperweight.patcher") version "1.3.9"
}

val paperMavenPublicUrl = "https://repo.papermc.io/repository/maven-public/"

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
}

subprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }
    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }

    repositories {
        mavenCentral()
        maven(paperMavenPublicUrl)
        maven("https://jitpack.io")
        maven("https://repo.codemc.org/repository/maven-public/")
    }
}

repositories {
    mavenCentral()
    maven(paperMavenPublicUrl) {
        content {
            onlyForConfigurations(configurations.paperclip.name)
        }
    }
}

dependencies {
    remapper("net.fabricmc:tiny-remapper:0.8.6:fat")
    decompiler("net.minecraftforge:forgeflower:1.5.605.7")
    paperclip("io.papermc:paperclip:3.0.2")
}

paperweight {
    serverProject.set(project(":Sugarcane-Server"))

    remapRepo.set(paperMavenPublicUrl)
    decompileRepo.set(paperMavenPublicUrl)

    usePaperUpstream(providers.gradleProperty("purpurCommit")) {
        withPaperPatcher {
            apiPatchDir.set(layout.projectDirectory.dir("patches/api"))
            apiOutputDir.set(layout.projectDirectory.dir("Sugarcane-API"))

            serverPatchDir.set(layout.projectDirectory.dir("patches/server"))
            serverOutputDir.set(layout.projectDirectory.dir("Sugarcane-Server"))
        }
    }
}

val projectName = rootProject.name
val version = providers.gradleProperty("version").get().trim()

tasks.register("paperclipJar") {
    finalizedBy("createReobfPaperclipJar")
}

tasks.createReobfPaperclipJar {
    finalizedBy("copyReobfPaperclipJar")
}

tasks.register<Copy>("copyReobfPaperclipJar") {
    from(tasks.createReobfPaperclipJar) {
        rename("${projectName}-paperclip-${version}-reobf.jar", "${projectName.toLowerCase()}-paperclip.jar")
    }
    into(layout.projectDirectory)
}

// Copy Git Hooks Task
tasks.register<Copy>("installGitHooks") {
    from(layout.projectDirectory.dir("hooks"))
    into(layout.projectDirectory.dir(".git/hooks"))
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

tasks.generateDevelopmentBundle {
    apiCoordinates.set("org.sugarcanemc.sugarcane:susgarcane-api")
    mojangApiCoordinates.set("io.papermc.paper:paper-mojangapi")
    libraryRepositories.set(
        listOf(
            "https://libraries.minecraft.net/",
            "https://maven.quiltmc.org/repository/release/",
            "https://repo.aikar.co/content/groups/aikar",
            "https://ci.emc.gs/nexus/content/groups/aikar/",
            "https://repo.maven.apache.org/maven2/",
            "https://repo.papermc.io/repository/maven-public/",
            "https://repo.purpurmc.org/snapshots",
            "https://repo.codemc.org/repository/maven-public/"
        )
    )
}

allprojects {
    publishing {
        repositories {
            maven("https://mvn.sugarcanemc.org/repository/maven-snapshots/") {
                name = "maven"
                credentials(PasswordCredentials::class)
            }
        }
    }
}

publishing {
    publications.create<MavenPublication>("devBundle") {
        artifact(tasks.generateDevelopmentBundle) {
            artifactId = "dev-bundle"
        }
    }
}

val mcVersion = providers.gradleProperty("mcVersion").get().trim()
tasks.register("printMinecraftVersion") {
    doLast {
        println("Minecraft: $mcVersion")
    }
}