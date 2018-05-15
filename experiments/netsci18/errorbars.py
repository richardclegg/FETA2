#!/usr/bin/env python

import sys
import operator
import os.path
import math

filename = sys.argv[1]

results = []

with open(filename) as file:
    for i, line in enumerate(file):
        line = line.strip()
        like, beta, guess = map(float, line.split(' '))
        results.append(dict(like=like, beta=beta, guess=guess))
    
errorbars = []    
for a in [0, 0.2, 0.4, 0.6, 0.8, 1.0]:
    temp = []
    for row in results:
        if row['beta']==a:
            temp.append(dict(like = row['like'],beta=row['beta'],guess=row['guess']))
    tempnew = sorted(temp, key=operator.itemgetter('guess'))
    amin = tempnew[0]['guess']
    amax = tempnew[9]['guess']
    asum = 0.0
    for row in tempnew:
        asum = asum+row['guess']
    amean = asum / 10.0
    adev = 0.0
    for row in tempnew:
        adev += math.pow((amean - row['guess']),2)
    adev = math.sqrt(adev)
    errorbars.append(dict(beta=a, amean=amean, adev=adev))

basename = os.path.basename(filename)
resultname = 'errorbars-10000-1.txt'

with open(resultname, 'w') as f:
    for row in errorbars:
        f.write('%f %f %f \n' % (row['beta'], row['amean'], row['adev']))
