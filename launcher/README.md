# OpenMissileWars Launcher

The OpenMissileWars launcher is a graphical and console application which allows users to do the following:

* Install OpenMissileWars releases.
  The launcher downloads these releases from GitHub and then configures them locally.
* Manage OpenMissileWars installations.
  The launcher allows viewing and uninstalling installed versions.
* View changelogs and new releases.
  The graphical launcher shows the latest version when started accompanied by the release notes for that version.
  
The launcher is designed to operate both with and without an internet connection.
Java versions 8+ are fully supported for the launcher, but attempting to launch OpenMissileWars without at least Java 11 will show a warning.

The launcher checks for a `launcher-settings.json` file in each release it attempts to run.
If one is present, the launcher assumes that it is outdated and will refuse to run that version.
This feature is designed to allow for clean forced updates to the launcher in the future.

The launcher installs downloaded versions into the `./omw-installations` directory.