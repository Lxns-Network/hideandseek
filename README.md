Hide And Seek
======

Highly customizable hide and seek plugin, forked from Kenshin's Hide and Seek (discontinued).

## New Changes
- **Latest minecraft version only.** Compatibility purposed codes are removed.
- **Skills** Hiders have their skills to tackle with Seekers.
- **Dynamic Timer** The timer will be extended when killing hiders.
- Location of hiders' heartbeat sounds will be obfuscated.
- New taunt feature.
- Louder heartbeat sound in last 10s
- Removed glow power up. (now as a skill)

## Fixes
- **Cannot hit solidified/moving blocks.** DID THEY EVEN PLAY THIS PLUGIN?
- Seekers now no longer see what is held by hiders.
- Performance improvement, significantly.
- Unexpected behavior when loading config.
- Seeker's health is low after respawn
- Entity hider doesn't work
- A bug in setting players' slots
- A bug in distance detection
- .. and more.

However It's not production ready unless you are able to develop it. I may publish a user friendly version in the future.

License
-----------

This project is licensed under the GPL v3 license.

Download
-----------

You can download the plugin precompiled on the releases page.

Compilation
-----------

We use gradle to handle our dependencies.

* Install Gradle
* Java 21 Required
* Clone this repo and: `gradle shadowJar`


