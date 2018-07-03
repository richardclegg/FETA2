#!/usr/bin/env python3

import sys

file1 = sys.argv[1]
file2 = sys.argv[2]

bit = []
bit2 = []

with open(file1,'r') as f1:
    for i,line in enumerate(f1):
        linestr = line.strip().split()
        n1, n2, year = map(int, linestr)
        bit.append([n1,n2,year])
    f1.close()

counter = 0
yearcompare=0
for row in bit:
    n1, n2, year = row[0], row[1], row[2]
    if year==yearcompare:
        bit2.append([n1,n2,counter])
    else: yearcompare = year; counter = counter+1; bit2.append([n1,n2,counter])

with open(file2,'w') as f2:
    for row in bit2:
        f2.write('%d %d %d \n' % (row[0], row[1], row[2]))
f2.close()