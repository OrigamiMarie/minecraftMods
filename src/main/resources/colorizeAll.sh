#!/bin/bash

FILES=`cat $1`

for FILE in $FILES; do
  ./colorize.sh colors $FILE
done

