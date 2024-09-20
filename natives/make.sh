#!/bin/bash

# 64 bit
rm -R build_linux64
mkdir build_linux64

g++  -o build_linux64/libmumble_x64.so -m64 -shared -fPIC \
     -Wl,-soname,build_linux64/libmumble_x64.so  \
     -I/usr/lib/jvm/java-17-temurin/include/ \
     -I/usr/lib/jvm/java-17-temurin/include/linux \
     -lrt \
     MumbleJniLinkDll.cpp \
       -lstdc++ \
       -m64 \
       -I/usr/include/c++/4.4/i686-linux-gnu


