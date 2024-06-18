#!/bin/bash

TEMPLATE_FILE=$1
COLORS=`cat $2`
COUNTS=`cat $3`
ANGLES=`cat $4`
LITS=`cat $5`

for COLOR in $COLORS; do
  COLOR=`echo $COLOR | sed -e "s/'//g"`
  for COUNT in $COUNTS; do
    for ANGLE in $ANGLES; do
      for LIT in $LITS; do
        LIT=`echo $LIT | sed -e "s/'//g"`
        NEW_FILE_NAME=`echo $TEMPLATE_FILE | sed -e "s/COLOR/$COLOR/g" | sed -e "s/COUNT/$COUNT/g" | sed -e "s/ANGLE/$ANGLE/g" | sed -e "s/LIT/$LIT/g"`
        cat $TEMPLATE_FILE | sed -e "s/COLOR/$COLOR/g" | sed -e "s/COUNT/$COUNT/g" | sed -e "s/ANGLE/$ANGLE/g" | sed -e "s/LIT/$LIT/g" > $NEW_FILE_NAME
      done
    done
  done
done

