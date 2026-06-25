# Draconic Evolution Render Patcher

## About
A fix for Draconic Evolution and Embeddium/Sodium Render issues.

## How it works?
It changes the render of Brandons Core to use Embeddium's or Sodium's entity system.

## Features
- Fixes rendering issues with Draconic Evolution when using Embeddium/Sodium.
- Has compatibility code to override Iris and ImmediatelyFast runtime settings so Draconic's items and entities render correctly.
- **You can check each compatible mod settings by using "/derender status" command.**

## Important Information
Draconic Evolution Render Patcher is a Client-side mod. </br>
Remember to use either Sodium or Embeddium. </br>
If Iris is installed, the mod will force "allowUnknownShaders" config to be set to true. </br>
If ImmediatelyFast is installed, the mod will disable runtime HUD and experimental screen batching.

> You can disable these changes in the config file located at `config/derenderpatcher-client.toml`.
> By disabling it, each mod will use its own configuration settings.

## Credits
- [Draconic Evolution](https://www.curseforge.com/minecraft/mc-mods/draconic-evolution) by brandon3055
- [Brandon's Core](https://www.curseforge.com/minecraft/mc-mods/brandons-core) by brandon3055
- [Embeddium](https://www.curseforge.com/minecraft/mc-mods/embeddium) by FiniteReality
- [Sodium](https://www.curseforge.com/minecraft/mc-mods/sodium) by JellySquid
