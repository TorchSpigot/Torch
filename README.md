Torch 
===========
[![Build Status](https://travis-ci.org/TorchMC/Torch.svg?branch=master)](https://travis-ci.org/TorchMC/Torch)  [![Team](https://img.shields.io/badge/Powered%20by-iMinecraft-green.svg?style=flat)](https://github.com/TorchSpigot/Torch)

The main repository of TorchSpigot

High performance Spigot fork that aims to fix gameplay, inconsistencies and multi-threaded computing, based on TacoSpigot basically.
* Current developing version:  1.9.4
* Recommanded(stable) version: R0.1-RELEASE

####How To (Server Admins)
Clone this repo, and use *Torch-1.9.4-R0.x.jar*  to start server directly.
* Just enjoy Torch!:)

####How To (Compiling From Source)
To compile Torch, you need JDK8(or above), git, bash, maven, and an internet connection.

* First clone and build *[TacoSpigot](https://github.com/TacoSpigot/TacoSpigot/)*,
run `git clone --branch version/1.9.4 https://github.com/TacoSpigot/TacoSpigot.git`
* then clone this repo,
run `git clone https://github.com/TorchSpigot/Torch.git` and copy files to the main folder
* last, run `./applypatches.sh`,
run `mvn clean install`, and get final jar in *./TacoSpigot-Server/target/*

