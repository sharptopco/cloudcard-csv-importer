#!/usr/bin/env bash

[ -e "../input/test-people.csv" ] && echo "../input/test-people.csv is already replaced" || mv completed/test-people.csv input/test-people.csv

for f in output/*.csv; do
    [ -e "$f" ] && rm "$f" || echo "$f is already clean"
    break
done
