# What is this?

OpenMissileWars is a free and open source recreation of the popular Missile Wars Minecraft minigame.
This project encompasses both a plugin to emulate Missile Wars, and some scripts to setup a server running the plugin.

# Hosting

## Connect to my existing server

I've already installed OpenMissileWars on a public server which you can join with the IP `94.130.231.179`.
Your ping should be roughly the same as it is on Cubekrowd, since it uses the same host.
If you'd like to be OP on the server (for example for creative mode testing), ask `Llew Vallis#5734` on Discord.

## Run your own copy

### Prerequisites

 * Shell (`sh`)
 * Maven
 * Git (or download the repository as a zip file instead of cloning)
 * Java (versions 11+ are recommended, but older versions may also work)
 * Curl
 
### Running

Use the following set of commands to run the server, replacing `%RAM%` with the amount of memory you would like to run the server with (e.g. 1.5G, 512M).

```sh
git clone https://github.com/LlewVallis/OpenMissileWars
cd OpenMissileWars
PAPER_MEMORY=%RAM% ./start.sh
```

# Commands

## General usage

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

## Administrative

* `arena-create <name>` - Create a new arena with the given name. This will likely lag the game for a few seconds.
* `arena-delete <name>` - Delete the arena with the given name.
* `template` - Connect to the template world, all changes made in the template will be cloned into subsequently created arenas. Use /save-all to save your changes.
* `structure-load <name> <x> <y> <z> [direction] [team]` - Load a missile or shield model in an arbitrary direction with an arbitrary color.
