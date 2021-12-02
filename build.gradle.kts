plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.0" apply false
    id("io.papermc.paperweight.patcher") version "1.3.1"
}

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/") { content { onlyForConfigurations("paperclip") } }
}

dependencies {
    remapper("net.fabricmc:tiny-remapper:0.7.0:fat")
    decompiler("net.minecraftforge:forgeflower:1.5.498.22")
    paperclip("io.papermc:paperclip:3.0.2")
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    java { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }

    tasks.withType<JavaCompile>().configureEach {
        options.isFork = true
        options.isIncremental = true
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    tasks.withType<Javadoc>().configureEach {
        options.encoding = Charsets.UTF_8.name()
    }

    tasks.withType<ProcessResources>().configureEach {
        filteringCharset = Charsets.UTF_8.name()
    }
    
    repositories {
        mavenCentral()
        maven("https://libraries.minecraft.net/")
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://ci.emc.gs/nexus/content/groups/aikar/")
        maven("https://repo.aikar.co/content/groups/aikar")
        maven("https://repo.md-5.net/content/repositories/releases/")
        maven("https://hub.spigotmc.org/nexus/content/groups/public/")
        maven("https://jitpack.io")
    }

    configure<PublishingExtension> {
        repositories.maven {
            name = "maven"
            url = uri("https://mvn.sugarcanemc.org/repository/maven-snapshots/")
            credentials(PasswordCredentials::class)
        }
    }
}

paperweight {
    serverProject.set(project(":Sugarcane-Server"))

    useStandardUpstream("Purpur") {
        url.set(github("pl3xgaming", "Purpur"))
        ref.set(providers.gradleProperty("purpurRef"))

        withStandardPatcher {
            baseName("Purpur")

            apiOutputDir.set(layout.projectDirectory.dir("Sugarcane-API"))
            serverOutputDir.set(layout.projectDirectory.dir("Sugarcane-Server"))

            remapRepo.set("https://maven.fabricmc.net/")
            decompileRepo.set("https://files.minecraftforge.net/maven/")
        }

        reobfPackagesToFix.addAll(
            "org.sugarcanemc",
            "net.pl3x",
            "ca.spottedleaf",
            "me.jellysquid.mods"
        )
    }
}

tasks.register<Copy>("copyReobfPaperclipJar") {
    from(tasks.createReobfPaperclipJar) {
      rename("Sugarcane-paperclip-${providers.gradleProperty("version").get().trim()}-reobf.jar", "sugarcane-paperclip.jar")
    }
    into(layout.projectDirectory)
}


// copy git hooks task
tasks.register<Copy>("installGitHooks") {
    from(layout.projectDirectory.dir("hooks"))
    into(layout.projectDirectory.dir(".git/hooks"))
}

tasks.wrapper {
	distributionType = Wrapper.DistributionType.ALL
}

tasks.generateDevelopmentBundle {
    apiCoordinates.set("org.sugarcanemc.sugarcane:sugarcane-api")
    mojangApiCoordinates.set("io.papermc.paper:paper-mojangapi")
    libraryRepositories.set(
        listOf(
            "https://libraries.minecraft.net/",
            "https://maven.quiltmc.org/repository/release/",
            "https://repo.aikar.co/content/groups/aikar",
            "https://ci.emc.gs/nexus/content/groups/aikar/",
            "https://papermc.io/repo/repository/maven-public/"
        )
    )
}

tasks.register("printMinecraftVersionAP") {
    doLast {
        println("Applying Patches to: Sugarcane for Minecraft " + providers.gradleProperty("mcVersion").get().trim())
    }
}

tasks.register("printMinecraftVersionBD") {
    doLast {
        println("Building: Sugarcane for Minecraft " + providers.gradleProperty("mcVersion").get().trim())
    }
}


tasks.register("printMinecraftVersion") {
    doLast {
        println("Minecraft: " + providers.gradleProperty("mcVersion").get().trim())
    }
}