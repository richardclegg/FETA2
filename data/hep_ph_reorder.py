#!/usr/bin/env python3

import datetime
import operator

papertimes = 'data/cit-HepPh-dates.txt'
ordered = []

with open(papertimes) as file:
    for i, line in enumerate(file):
        line = line.strip()
        id, date = line.split()
        id = int(id)
        #print(date)
        y, m, d = map(int, date.split("-"))
        newdate = datetime.date(y,m,d)
        ordered.append(dict(id=id, date=newdate))
    file.close()

ordered = sorted(ordered, key=operator.itemgetter('date'))

cites = 'data/cit-HepPh.txt'
unorderedcites = []
orderedcites = []
citefile = 'data/cit-HepPh-ordered.txt'

with open(cites) as file2:
    for j, line2 in enumerate(file2):
        line2 = line2.strip()
        srcid, dstid = map(int, line2.split())
        unorderedcites.append(dict(srcid=srcid, dstid=dstid))
    file2.close()


for k, row in enumerate(ordered):
    paperid = int(row['id'])
    #print(paperid)
    i, tot = 0, len(unorderedcites)
    while i < tot:
        src = unorderedcites[i]['srcid']
        dst = unorderedcites[i]['dstid']
        if paperid == src:
            orderedcites.append(dict(srcid=src, dstid=dst, time=k))
            print(k)
            del unorderedcites[i]
            tot = tot - 1
        else: i = i+1

with open(citefile, 'w') as f:
    for row in orderedcites:
        f.write('%d %d %d \n' % (row['srcid'], row['dstid'], row['time']))
    f.close()