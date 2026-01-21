#!/bin/bash

if [ $# != 1 ]; then
    echo "error!, \$1: csv filename"
    exit 1
fi

lines=`wc -l $1 | awk '{print $1}'`

for((i=1;i<=lines;i++))
do
    cat $1 \
        | sed -n ${i}p \
        | sed 's/,/ | /g' \
        | sed 's/^/|  /'  \
        | sed 's/$/ |/'   \
        | sed 's/SEPARATOR/ :--- /g'

    if [ $i = 1 ]; then
        num=`cat $1 | sed -n ${i}p | awk -F ',' '{print NF}'`
        printf "|"
        for((j=0;j<num;j++))
        do
            printf "-"
            printf "-"
            printf "-"
            printf "|"
        done
        printf "\n"
    fi

done