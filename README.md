# Aspect Recipe Index
A new and improved mod that adds NEI support for Thaumcraft, originally based on TCNEIAdditions by TimeConqueror.

## Required Dependencies
* [NotEnoughItems](https://github.com/GTNewHorizons/NotEnoughItems)
* [UniMixins](https://github.com/LegacyModdingMC/UniMixins)
* [Thaumcraft 4](https://www.curseforge.com/minecraft/mc-mods/thaumcraft/files/all?page=1&pageSize=20&version=1.7.10&showAlphaFiles=hide)
* TC4RecipeLib (part of [TC4Tweaks](https://github.com/Glease/TC4Tweaks/))

## Features:
* Items representing each aspect that are visible in NEI.
  * Searching for their recipes and uses shows the relevant recipes using that aspect as vis or essentia in all handlers.
  * Aspect items are automatically generated based on all existing aspects and automatically account for any aspects added by Thaumcraft's addons.
  * Right-click with one in hand to consume it and instantly gain a research point. Works to discover undiscovered aspects. They are unobtainable without cheating by default, but it should be possible for modpack makers to add real recipes for them if desired.
* All handlers show the required research for their recipes
  * If research has not been discovered, its parents will be listed in a tooltip shown while hovering over its icon.
  * The research widget may be disabled for all handlers by setting `showResearchKey` to false.
  * Recipes are hidden if required research has not been learned by default.
    * All recipes may be shown even without research by setting `showLockedRecipes` to true.
* Shaped and Shapeless Arcane Crafting handlers, as well as a new handler just for Wand Crafting.
  * All three support automatically moving stacks into the correct slots with NEI's overlay button.
  * Wands without enough vis to complete the craft will be swapped out if one with enough vis is found in your inventory.
  * The Wand Recipe handler will also automatically find recipes for any item that extends ItemWandCasting, such as the bracelets from Thaumic Bases.
  * Recipes for wands will be shown when looking at uses for their components (e.g. searching for uses of Gold Caps will show you how to combine them with every type of rod).
  * The Wand Recipe handler also supports custom wand recipes. If a mod adds custom recipes for wands and removes the original ArcaneWandRecipe and ArcaneSceptreRecipe, it will display the new recipes instead.
* Aspect Combination handler, to see what an aspect's parents are (or show if it is primal) and what aspects it can be combined with to create new ones.
  * Combinations are hidden until learned by default.
    * Enable `showUndiscoveredAspectNames` to see names of undiscovered aspects when hovered anywhere in NEI.
    * Enable `showUndiscoveredAspectRecipes` to show combination recipes for undiscovered aspects.
* Items Containing Aspect handler, to quickly find any items you have scanned that contain a specific aspect.
* Alchemy handler for recipes made in the Crucible and Thaumatorium.
* Infusion handler which shows the required items and essentia as well as the infusion's instability.
  * Runic Shielding Augmentation recipes are also displayed in this handler.
    * Each unique tier (0 → 1, 5 → 6, etc.) is shown only once with all valid items included in its input to reduce recipe bloat, but searching for uses (default U) of an item that accepts runic shielding will always show the cost to upgrade it another time.

### Planned Features and Contribution
Click [here](https://github.com/GTNewHorizons/AspectRecipeIndex/issues/1) to see the tracking issue with a list of planned future features. If there's something not on that list that you wish to see added to the mod, please add a comment on that issue or make a new one suggesting it.

### Language Support
The mod currently mostly only contains localization for English. If you know another language, please feel free to contribute. Things that need localization include the names of handlers, the text shown on them for research (research names are automatically pulled from the Thaumonomicon, but the rest of the tooltip needs to be localized), infusion instability,

### Other Supported Mods
Normal features added by most Thaumcraft addons (e.g. new aspects, recipes, and wand components) should automatically be supported without any extra changes. The following mods have special features that need explicit support:
* Automagy
  * Kill + Scan tasks are properly shown in the research widget if required.

Aspect Recipe Index **will** function in an environment with the original TCNEIPlugin (with or without TCNEIAdditions), but you will see **duplicate recipes** for items due to both mods registering their own handlers. For best results, remove TCNEIPlugin and TCNEIAdditions if they are present. If there are any features missing, please feel free to make a feature suggestion on the Issues tab.

Mods written to support features from the original TCNEIPlugin (interacting with its aspect items, autofilling items in crafting grids with NEI's overlay button for Arcane Workbench handlers, etc.) ***will need explicit support added for Aspect Recipe Index***.

### License

Copyright © 2020 TimeConqueror

GTNH Modifications (C) 2020-2025 The GTNH Team

Licensed under GPL-3 or later - use this however you want, but please give back any modifications!
