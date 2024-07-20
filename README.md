

## Why?

Forking this since the original plugin is heavily outdated. I'm not very well versed in Java so if you see any issues or bad practices please reach out or open an issue!
Enjoy!

## WirelessRedstone
The official page of the Wireless Redstone plugin. [Spigot page.](https://www.spigotmc.org/resources/wirelessredstone.8251/)

### Changes
- Changed native API version to Spigot 1.21.1
- Fixed Version check to recognize Paper 1.21-57-0e02aa5 (MC: 1.21) as a valid build.
- Fixed most of the issues with Depreciated Bukkit API classes/functions.
- Fixed Redstone Wire Not Working!
- Added missing MC Versions to fix server logs stating unknow server version


https://github.com/user-attachments/assets/169c6f80-e1b4-4f3f-9168-ff0cf0baeb88


https://github.com/user-attachments/assets/50ca65b9-068c-44e6-b891-351f51e6cbf4


## Known Issues
- Performance issues on windows machines, when server first starts and you interact with a sign the server will hang for couple of seconds. It only does it once.
- ~~Signs don't turn off when plugged into redstone wire, the workaround for this is to use redstone torches in-between the signs and redstone wires as in video below.~~

## ToDo
- Add MySql Support (I know it isn't probably needed for a small plugin but it is nice to have the option)
- Add a new sign functionality, basically allow a single transmitter sign to have multiple channels that you can go through.
- Add Removal of all old channels (from database and yml files) when all signs are destroyed.

## Config

| Key                         | Description                                                                                                                                                                                                                                                                                                                                                  |
|-------------------------    |----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------    |
| ConfigVersion               | You should NOT change this value yourself!                                                                                                                                                                                                                                                                                                                   |
| Language                    | Change the language of the plugin. It falls back to English if the language is not found. For more info see the Translate section.                                                                                                                                                                                                                           |
| ColourfulLogging            | Add colours to logging.                                                                                                                                                               |
| SilentMode                  | Disable most of the feedback if the user doesn't have permissions.                                                                                                                                                                                                                                                                                           |
| DebugMode                   | Print more information to the console.                                                                                                                                                                                                                                                                                                                       |
| DropSignWhenBroken          | Drop a sign item if a WirelessChannel is destroyed.                                                                                                                                                                                                                                                                                                          |
| InteractTransmitterTime     | Amount of time (in milliseconds) a WirelessChannel will be active if there's an interaction with a transmitter.                                                                                                                                                                                                                                              |
| CacheRefreshFrequency       | Frequency (in seconds) of refreshing the database. You should leave this to the default value.                                                                                                                                                                                                                                                               |
| gateLogic                   | The logic of the transmitters;  OR: If one of the transmitters is powered the channel will be activated. All transmitters must be off to power the channel down.  IGNORE: If one of the transmitters is powered the channel will be activated. If a transmitter is no longer powered the channel will be deactivated ignoring other transmitters.            |
| saveOption                  | Save WirelessRedstone data in YML or SQLITE.                                                                                                                                                                                                                                                                                                                 |

## License

WirelessRedstone is released under the [GPLv3](LICENSE.txt).

```
Copyright (C) 2016  WirelessRedstoneGroup

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see http://www.gnu.org/licenses.
```

### Used licenses

Gson is released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0).

```
Copyright 2008 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
