#!/bin/bash

COLORS=`cat $1`
INPUT_FILE=$2

for COLOR in $COLORS; do
  cat $INPUT_FILE | gsed -e "s/REPLACE/${COLOR}_/g" > ${COLOR}_${INPUT_FILE}
done

