![OpenMissileWars banner](https://user-images.githubusercontent.com/20552946/110436923-82d65600-8100-11eb-8670-f163814b46b6.png)

> **Help wanted**
> 
> Unfortunately I have less time than I would like to keep up with the continuous development that this project demands.
> If you have an interest in working on the project, or if you need a hand with anything, feel free to reach out to me on [my website](https://llew.netlify.app).

# OpenMissileWars

[![Build Status](https://travis-ci.com/LlewVallis/OpenMissileWars.svg?branch=master)](https://travis-ci.com/LlewVallis/OpenMissileWars)
[![Lines of Code](https://tokei.rs/b1/github/LlewVallis/OpenMissilewars?category=code)](https://github.com/LlewVallis/OpenMissileWars)
[![Files](https://tokei.rs/b1/github/LlewVallis/OpenMissilewars?category=files)](https://github.com/LlewVallis/OpenMissileWars)

OpenMissileWars is a free and open source recreation of the popular Missile Wars Minecraft minigame.
This project encompasses both a plugin to emulate Missile Wars, and the systems required to install, launch and manage OpenMissileWars servers.

# Usage
## Connecting to the public version

You can join the public version of OpenMissileWars at the IP `urmw.live`.
If you require operator permission (e.g. for testing with creative mode), ask `Llew Vallis#5734` on Discord.

## Hosting it yourself

### Using the launcher (easiest, Windows support)

The launcher will automatically download, launch, detect issues and show updates for OpenMissileWars.
The launcher itself works with Java 8+ and will notify you if your Java installation is not supported by OpenMissileWars itself.
The launcher will still allow you to run OpenMissileWars with an unsupported Java version if you wish.

#### Prerequisites

* Java 8+

#### Running

1. Download `launcher.jar` from the [the latest release](https://github.com/LlewVallis/OpenMissileWars/releases/latest).
2. Run the JAR file (by double clicking it or otherwise) to run the graphical launcher.
3. Run the JAR file on the command line with the `--nogui` (e.g. `java -jar launcher.jar --nogui`) option to run the command line launcher.
   This is useful if you prefer command line interfaces or are running it on a server without a graphical environment.
   There are several command line options you might find useful when using the launcher - use the `--help` argument to learn more.

### Run a prebuilt server (harder, Unix only)

#### Prerequisites

* Shell (`sh`)
* Java (versions 11+ are recommended, but older versions may also work)

#### Running

1. Download `server.tar.gz` from the [the latest release](https://github.com/LlewVallis/OpenMissileWars/releases/latest).
2. Extract the archive, and navigate into the extracted directory.
3. If you wish to accept the [Minecraft EULA](https://account.mojang.com/documents/minecraft_eula), create a file named `eula.txt` containing `eula=true`.
4. Run the `start.sh` script to start the server, you can set the amount of memory to allocate with the `PAPER_MEMORY` environment variable if you wish.
   It is important not to directly run the JAR file as some features will not work

### Compile and run from source (hardest, Unix only)

#### Prerequisites

* Shell (`sh`)
* Maven
* Git (or download the repository as a zip file instead of cloning)
* Java (versions 11+ are recommended, but older versions may also work)
* Curl
 
#### Running

Use the following set of commands to run the server, replacing `%RAM%` with the amount of memory you would like to run the server with (e.g. 1.5G, 512M).

```sh
git clone https://github.com/LlewVallis/OpenMissileWars
cd OpenMissileWars
PAPER_MEMORY=%RAM% ./start.sh
```

## Commands

### General usage

* `hub` - Connect to the hub world.
* `arena <name>` - Connect to the arena with the given name. When the server is started a default arena called `mw1` is created.
* `arenas` - List all arenas.
* `green` - Join the green team of your current arena.
* `red` - Join the red team of your current arena.
* `sp` - Enter spectator mode in an arena.
* `ping` - Display your current ping to the server.
* `nightvis` - Toggle whether you have night vision.
* `github` - Display a link to this repository.
* `issue` - Display a link to the issue tracker.

### Administrative

* `reset` - Restart the current arena without either team having to win.
* `arena-create <name>` - Create a new arena with the given name. This will likely lag the game for a few seconds.
* `arena-delete <name>` - Delete the arena with the given name.
* `template` - Connect to the template world, all changes made in the template will be cloned into subsequently created arenas. Use /save-all to save your changes.
* `structure-load <name> <x> <y> <z> [direction] [team]` - Load a missile or shield model in an arbitrary direction with an arbitrary color.

## Configuration

### Spawning missiles in enemy bases

By default, missiles can be spawned directly inside the enemy team's base.
To disable this behavior, the `settings.allowSpawningMissilesInEnemyBases` configuration property can be set to `false`.

### Adding new missiles

The `missiles` section in the configuration file specifies what structure (if any) is spawned when a user clicks with a
certain item.
This is best understood with an example.

```yaml
# The top level missiles section in the configuration file
missiles:
  # The Bukkit material name of the item, see https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html
  CREEPER_SPAWN_EGG:
    # The name of the structure to be spawned, without the openmissilewars author prefix
    structureName: tomahawk
    # Offsets used when determining whether to spawn the missile
    offsetX: 0
    offsetY: 4
    offsetZ: 4
    # Dimensions used when determining what blocks the spawned structure will collide with
    width: 2
    height: 2
    length: 13
```

Adding a missile does not automatically make it eligible as an item drop.
To add a custom missile, place the structure file (ends with `.nbt`) in `world/generated/openmissilewars/structures` under your server directory.
Custom missiles will automatically have terracotta, glass, ice and air mapped to team colored terracotta, glass (normal variant), glass (light variant) and structure voids.

### Adding item drops

The `items` section of the configuration file specifies which items are eligible as regular drops when playing the game.
As with the above section, this is best illustrated with an example.

```yaml
# The top level items section in the configuration file
items:
  # The Bukkit material name of the item, see https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html
  # Defaults to giving a single item
  - material: SNOWBALL
    # The custom name of the item rendered above the hotbar
    name: Shield
  # Without the name property the vanilla item names are used (no metadata is attached)
  # In "already obtained" messages, the material name is used
  - material: ARROW
    # The amount of items to give in one drop
    # If they have less than this amount of items (e.g. 2) it will be topped up
    amount: 3
```

All items have an equal probability of being given.

# Development

## Contributing

All forms of contributions are welcome, whether that be through submitting issues, providing insight on existings issues or via pull requests.
When contributing information by submitting or commenting on an issue, evidence to support any claims is appreciated, but by no means required.
If you don't have hard evidence to back up a claim, or don't have time to present it, feel free to present it regardless.
Unlike issue contributions, however, claims made in pull requests must be demonstratable or reproducible.
This requirement mostly applies to [issues requiring research](https://github.com/LlewVallis/OpenMissileWars/labels/needs%20research).

For the submission of pull requests specifically, it is usually best to target the development branch.
The master branch of this repository is automatically packaged into a major release upon each commit.

## What counts as a bug

Any discrepancy between OpenMissileWars and CubeKrowd MissileWars that affects gameplay in any non-superficial way is considered a bug.
That said, exceptions to this rule exist where the discrepancy is the result of a CubeKrowd MissileWars bug being intentionally excluded from OpenMissileWars.
A bug in CubeKrowd MissileWars is any aspect of the game that is unintentional, largely unwanted and does not have a significant impact on gameplay.
