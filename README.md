<div align=center>
    <img src="https://cdn.discordapp.com/attachments/517734448008134686/857649078409887784/sugar2.png" width="512">
    <br /><br />
    <h3>✅ Highly optimized <a href="https://github.com/TECHNOVE/Airplane">Airplane</a> fork that focuses on stability and performance.</h3>
    <a href="https://sugarcanemc.org/discord">
        <img alt="Discord" src="https://img.shields.io/discord/855918593497759754?color=green&label=discord&logo=discord&style=for-the-badge">
    </a>
    <a href="https://sugarcanemc.org/">
        <img alt="Website" src="https://img.shields.io/website?style=for-the-badge&up_color=red&up_message=SugarcaneMC&url=https%3A%2F%2Fsugarcanemc.org%2F">
    </a>
</div>

# We use patches from the following projects

* [Paper](https://github.com/PaperMC/Paper)
* [Tuinity](https://github.com/Tuinity/Tuinity)
* [Airplane](https://github.com/TECHNOVE/Airplane)
* [Purpur](https://github.com/pl3xgaming/Purpur)
* NOTE: We also borrow some patches from [Yatopia.](https://github.com/YatopiaMC/Yatopia)

# ❗ Reminder
This project is still **work in progress**, so there will not have stable builds for production for a while, until we perform enough tests to ensure it's stable enough for production.

We **HIGHLY RECOMMEND TO NOT USE the development builds for any kind of production enviroment**.

If you find any issue or have some suggestion, feel free to [open a issue](https://github.com/SugarcaneMC/Sugarcane/issues/new).

## How To (Plugin developers)

<details><summary>Gradle</summary>
<p>

> ##### Groovy DSL

First, add the SugarcaneMC repository

```groovy
repositories {
    maven {
        url 'https://mvn.sugarcanemc.org/repository/maven-public/'
    }
}
```
Then, add the Sugarcane-API dependency:

```groovy
dependencies {
    compileOnly "org.sugarcanemc.sugarcane:sugarcane-api:1.17.1-R0.1-SNAPSHOT"
}
 ```
 
> #### Kotlin DSL

First, add the SugarcaneMC repository

```kotlin
repositories {
    maven("https://mvn.sugarcanemc.org/repository/maven-public/")
}
```

Then, add the Sugarcane-API dependency:

```kotlin
dependencies {
    compileOnly("org.sugarcanemc.sugarcane:sugarcane-api:1.17.1-R0.1-SNAPSHOT")
}
```

</p>
</details>

<details><summary>Maven</summary>
<p>
    
First, add the SugarcaneMC repository

```xml
<repositories>
    <repository>
        <id>sugarcanemc-repo</id>
        <url>https://mvn.sugarcanemc.org/repository/maven-public/</url>
    </repository>
</repositories>
```

And then add the Sugarcane-API dependency:

```xml
<dependency>
    <groupId>org.sugarcanemc.sugarcane</groupId>
    <artifactId>sugarcane-api</artifactId>
    <version>1.17.1-R0.1-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

</p>
</details>

## We also have a [Discord Server](https://sugarcanemc.org/discord) for support

# Try it for yourself

* **[GitHub Actions](https://github.com/SugarcaneMC/Sugarcane/actions?query=branch%3Aver%2F1.17)** - Download latest developmemt build for Minecraft 1.17 (**not for production**).

# Contributing

**See [Contributing.md](https://github.com/SugarcaneMC/Sugarcane/blob/main/CONTRIBUTING.md)**
