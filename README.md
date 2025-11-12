# Draconic Embeddium Fix

## About
A fix for Draconic Evolution and Embeddium Render issues.

## How it works?
It changes the render of Brandons Core to use Embeddium's entity system.

## Features
- Fixes rendering issues with Draconic Evolution when using Embeddium.
- Have compatibility code to change Iris and ImmediatelyFast configurations to force Draconic's items and entities to be rended.
- **You can check each compatible mod settings by using /draconicembeddiumfix status command.**

## Important Information
Draconic Embeddium Fix is a Client-side mod. </br>
If Iris is installed, the mod will force "allowUnknownShaders" config to be set to true. </br>
If ImmediatelyFast is installed, the mod will force "hud_batching" config to be set to false.

>> You can disable these changes in the config file located at `config/draconicembeddiumfix-client.toml`.
> By disabling it, each mod will use its own configuration settings.

## Credits
- [Draconic Evolution](https://www.curseforge.com/minecraft/mc-mods/draconic-evolution) by brandon3055
- [Brandon's Core](https://www.curseforge.com/minecraft/mc-mods/brandons-core) by brandon3055
- [Embeddium](https://www.curseforge.com/minecraft/mc-mods/embeddium) by FiniteReality