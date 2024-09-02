#!/bin/bash
#curl https://github.com/twbs/bootstrap/archive/v5.3.2.zip -o  ./build/bootstrap.zip
rm -rf build/bootstrap-5.3.2
yes|unzip bootstrap-5.3.2.zip -d ./build
cd build/bootstrap-5.3.2
rm -rf dist
#sed -i "1i@import \"../../custom.scss\";" ./scss/_variables.scss
npm install
npm run dist
