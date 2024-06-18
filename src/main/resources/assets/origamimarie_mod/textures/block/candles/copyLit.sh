#!/bin/bash

lit_candles=`ls -la | rev | cut -f1 -d' ' | rev | grep lit`

for lit_candle in $lit_candles; do
  prefix=`echo $lit_candle | cut -f1 -d'.'`
  cp $lit_candle ${prefix}_e.png
done

