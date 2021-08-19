plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.0.0" apply false
    id("io.papermc.paperweight.patcher") version "1.1.9"
}

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/") { content { onlyForConfigurations("paperclip") } }
    maven("https://maven.fabricmc.net/") { content { onlyForConfigurations("remapper") } }
}

dependencies {
    remapper("net.fabricmc:tiny-remapper:0.5.0:fat")
    paperclip("io.papermc:paperclip:2.0.1")
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    java { toolchain { languageVersion.set(JavaLanguageVersion.of(16)) } }

    tasks.withType<JavaCompile>().configureEach {
        options.isFork = true
        options.isIncremental = true
        options.encoding = Charsets.UTF_8.name()
        options.release.set(16)
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
        }

        reobfPackagesToFix.addAll(
            "org.sugarcanemc",
            "gg.airplane",
            "net.pl3x",
            "com.tuinity",
            "ca.spottedleaf",
            "me.jellysquid.mods"
        )
    }
}

tasks.paperclipJar {
    destinationDirectory.set(rootProject.layout.projectDirectory)
    archiveFileName.set("sugarcane-paperclip.jar")
}

// copy git hooks task
tasks.register<Copy>("installGitHooks") {
    from(layout.projectDirectory.dir("hooks"))
    into(layout.projectDirectory.dir(".git/hooks"))
}
