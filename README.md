[download]: https://img.shields.io/github/downloads/Mvndi/TreeFalls/total
[downloadLink]: https://modrinth.com/plugin/treefalls
[discord-shield]: https://img.shields.io/discord/728592434577014825?label=discord
[discord-invite]: https://discord.gg/RPNbtRSFqG

[ ![download][] ][downloadLink]
[ ![discord-shield][] ][discord-invite]

[**Discord**](https://discord.gg/RPNbtRSFqG) | [**Hangar**](https://hangar.papermc.io/Hydrolien/TreeFalls) | [**Modrinth**](https://modrinth.com/plugin/treefalls) | [**GitHub**](https://github.com/Mvndi/TreeFalls)

# TreeFalls
Smooth tree falling Paper/Folia plugin.

<img src="TreeFalls.gif" alt="tree falls" width="640" height="360" loop=infinite>

## Usage

Download the latest version from [the releases][downloadLink]. Start your server.

## Config

You can change the configuration in the `config.yml` file.
Each time you change the configuration, reload with `/tf reload` or restart your server.

In the config you can set :
- The number of blocks that the player can break at once.
- The list of gamemode where the player is can cut trees by breaking a single log.
- The list of materials that are concidered as trees.
- The list of tools that are concidered as breaking tree tools.


## Statistics
[![bStats Graph Data](https://bstats.org/signatures/bukkit/treefalls.svg)](https://bstats.org/plugin/bukkit/TreeFalls/29518)

## Build
Clone the project from [GitHub](https://github.com/Mvndi/TreeFalls).

Run `./gradlew assemble`
The plugin jar file will be in build/libs/

You can also start a test server directly from Gradle with `./gradlew runPaper`

## Thanks
Thanks to [SmoothTimber](https://github.com/SourceWriters/SmoothTimber) that inspired this project.
This project is kind of a simplified version of SmoothTimber with modern tools (Maven -> Gradle, Spigot -> Paper, Manual run -> auto run with runPaper), much less dependencies and no legacy support (1.20+).