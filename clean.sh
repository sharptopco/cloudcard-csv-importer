#!/usr/bin/env bash

[ -e "../input/test-people.csv" ] && echo "../input/test-people.csv is already replaced" || mv completed/test-people*.csv input/.

cd output
rm *.csv
cd ..
