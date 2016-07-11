Torch 
===========
![Torch](https://i.imgur.com/cJWj0we.png) 

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg?style=flat)](http://pan.baidu.com/s/1hsBEdxU/) [![Minecraft](https://img.shields.io/badge/Minecraft-1.9.4-green.svg?style=flat)](https://www.minecraft.net/) [![Java](https://img.shields.io/badge/Java_JDK-v1.8-blue.svg?style=flat)](https://www.java.com/) [![Team](https://img.shields.io/badge/Powered_by-iMinecraft-green.svg?style=flat)](https://github.com/TorchSpigot/Torch) [![MCStats](https://img.shields.io/badge/MCStats-TorchSpigot-blue.svg?style=flat)](http://mcstats.org/plugin/TorchSpigot) 


The main repository of TorchSpigot

A powerful high performance Spigot server fork with backported and more, 
that aims to fix gameplay and inconsistencies, optimizations and multi-threaded computing included, based on TacoSpigot basically.
* Current developing version:  1.9.4
* Recommanded(stable) version: R1.0-RELEASE

(If you're running a previous 1.9.4 Beta, upgrading is highly recommended.)

####How To (Server Admins)
* Get Jar file(download), *[Click Here](http://t.im/torch)*,

and use *Torch-1.9.4-R0.x.jar*, to start server directly.
* Just enjoy Torch! :)

####How To (Compiling From Source)
To compile Torch, you need JDK8(or above), git, bash, maven, and an internet connection.

* First clone and build *[TacoSpigot](https://github.com/TacoSpigot/TacoSpigot/)*,
run `git clone --branch version/1.9.4 https://github.com/TacoSpigot/TacoSpigot.git`
* then clone this repo,
run `git clone https://github.com/TorchSpigot/Torch.git` and copy files to the main folder
* last, run `./applypatches.sh`,
run `mvn clean install`, and get final jar in *./TacoSpigot-Server/target/*


* Feel free to open a PR!