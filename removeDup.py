#!/usr/bin/env python

#netmodel class

import sys, random, math

filename=sys.argv[1]
f= open(filename,'r')


alllinks=dict()
f.readline()
f.readline()
for line in f.readlines():
    if line.strip() == '' or line[0] == '#':
        continue
    parms=line.split()
    if len(parms) <3 or len(parms) > 4:
        continue
    p1= parms[0]
    p2= parms[1]
    if p1 == p2:
        continue;
    if (p1 < p2):
        link=(parms[0],parms[1])
    else:
        link=(parms[1],parms[0])
    try:
        alllinks[link]
        continue
    except:
        alllinks[link]=1
        if len(parms) == 4:
            print parms[0],parms[1],parms[3]
        else:
            print parms[0],parms[1],parms[2]
f.close()
