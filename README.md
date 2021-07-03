# Sugarcane
Highly optimized Airplane fork focusing on stability and performance.

##### NOTE: We borrow some patches from Yatopia

# Reminder
This project is still **work in progress**, so there will not have stable build for production for a while, until we did enough test to ensure it's already stable for production.

We **HIGHLY RECOMMEND TO NOT USE the development builds for any kind of production**
If you find any issue or have some suggestion to this project, feel free to [open a issue](https://github.com/SugarcaneMC/Sugarcane/issues/new).

## Please join our [Discord Server](https://sugarcanemc.org/discord) to get support!

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
    compileOnly "org.sugarcanemc.sugarcane:sugarcane-api:1.17-R0.1-SNAPSHOT"
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
    compileOnly("org.sugarcanemc.sugarcane:sugarcane-api:1.17-R0.1-SNAPSHOT")
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
    <version>1.17-R0.1-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

</p>
</details>

# Try it out 
You can download the latest development build for 1.17 in [GitHub Actions](https://github.com/SugarcaneMC/Sugarcane/actions?query=branch%3Aver%2F1.17). There is not stable build for production yet
