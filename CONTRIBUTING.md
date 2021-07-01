# Contributing to Sugarcane

SugarcaneMC is happy you're willing to contribute to our projects. We are usually very lenient with all submitted PRs, but there are still some guidelines you can follow to make the approval process go more smoothly.

⚠ This file is W.I.P⚠

**Table of contents:**
<!-- vscode-markdown-toc -->
* [Requirements](#Requirements)
* [PR Policy](#PRPolicy)
  * [Formatting](#Formatting)
* [Guidelines](#Guidelines)

<!-- vscode-markdown-toc-config
    numbering=false
    autoSave=true
    /vscode-markdown-toc-config -->
<!-- /vscode-markdown-toc -->

## <a name='Requirements'></a>Requirements

* JDK 16 [AdoptOpenJDK](https://adoptopenjdk.net/)
* Git `git` on all package managers

## <a name='PRPolicy'></a>PR Policy

We'll accept changes that make sense. You should be able to justify their existence, along with any maintenance costs that come with them.
Remember that these changes will affect everyone who runs Sugarcane, not just you and your server.

While we will fix minor formatting issues, you should stick to the guide below when making and submitting changes.

### <a name='Formatting'></a>Formatting

All modifications to non-Sugarcane files should be marked.

* Multi-line changes need start with // Sugarcane start and end with // Sugarcane end
  * However adding a reason to // Sugarcane start would be very appreciated.

* The comments should generally be about the reason the change was made, what it was before, or what the change is.
  * Multi-line messages should start with `// Sugarcane start` and use `/*Multi line message here*/` for the message itself.
  * One-line changes should have `// Sugarcane` or `// Sugarcane - reason.`

Here's an example of how to mark changes by Sugarcane:

```java
entity.getWorld().dontbeStupid(); // Sugarcane - was beStupid() which is bad
entity.getFriends().forEach(Entity::explode);
// Sugarcane start - use plugin-set spawn
// entity.getWorld().explode(entity.getWorld().getSpawn());
Location spawnLocation = ((CraftWorld)entity.getWorld()).getSpawnLocation();
entity.getWorld().explode(new BlockPosition(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ()));
// Sugarcane end
```

We generally follow usual Java style (aka. Oracle style), or what is programmed into most IDEs and formatters by default. There are a few notes, however:

It is fine to go over 80 lines as long as it doesn't hurt readability.
There are exceptions, especially in Spigot-related files.

When in doubt or the code around your change is in a clearly different style, use the same style as the surrounding code.

## <a name='Guidelines'></a>Guidelines

* Intentionally harmfull/Troll PRs will cause you to be blocked.
* Pull requests are to be aimed at the 1.17/dev branch.
* Ports of Fabric Mods and other forks are acceptable.

  * When porting a fabric mod that uses Yarn mappings , you can use `https://wagyourtail.xyz/Projects/Minecraft%20Mappings%20Viewer/App?version=1.17&mapping=yarn,yarnIntermediary,mojang` to easily figure out how yarn names are named in mojang mappings
