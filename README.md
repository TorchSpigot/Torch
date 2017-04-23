# Torch (火苣)

[![PayPayl donate button](https://img.shields.io/badge/paypal-donate-yellow.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=caowenkang1@qq.com&lc=US&item_name=Torch&currency_code=USD&bn=PP%2dDonationsBF%3apaypal%2ddonate%2dyellow%2esvg%3aNonHostedGuest) [![Build Status](https://travis-ci.org/TorchSpigot/Torch.svg?branch=master)](https://travis-ci.org/TorchSpigot/Torch) [![Minecraft](https://img.shields.io/badge/Minecraft-1.11.2-green.svg?style=flat)](https://www.minecraft.net/) [![Java](https://img.shields.io/badge/Java_JDK-v1.8-green.svg?style=flat)](https://www.java.com/) [![bStats](https://img.shields.io/badge/bStats-Torch-blue.svg?style=flat)](https://bstats.org/plugin/bukkit/Torch)


### Introduction
Torch is a fork of [Paper](https://github.com/PaperMC/Paper) with multi-thread computing and further optimizations.
We aims to be stable and fast. 

Features:
+ All features of Paper/Spigot are included
+ Multi-threading computing, enjoy the server with multi-core CPU
+ Dupe glitch fixes


### Building Requirements:
+ Java JDK 8 or above(8u131 is recommended)
+ Maven


### Download
+ [![Build Status](https://circleci.com/gh/TorchSpigot/Torch/tree/master.svg?style=svg) **CircleCI**](https://circleci.com/gh/TorchSpigot/Torch/tree/master) - Checkout the "Artifacts" tab of the latest build (**login required**)

You can also build from source by yourself if the latest building is failed, but passing via Travis-CI :/

### Building/Compile

#### Setup
```sh
git clone https://github.com/TorchSpigot/Torch
cd Torch
git checkout ver/1.11.2
```

#### Build
```sh
./scripts/build.sh --init
```


### Contributing
The sources are easy to modify, checkout `sources` folder

+ Feel free to open a PR or issue.
