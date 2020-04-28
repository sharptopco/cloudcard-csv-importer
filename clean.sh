#!/usr/bin/env bash

clear

[ -e "../input/test-people.csv" ] && echo "../input/test-people.csv is already replaced" || mv completed/test-people*.csv input/.

cd output
cat *.csv
rm *.csv
cd ..
