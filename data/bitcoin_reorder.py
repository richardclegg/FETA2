#!/usr/bin/env python3

file = 'data/bitcoin/bitcoin.links.csv'
output = 'data/bitcoin/bitcoin_ordered.txt'

unordered = []

def takeThird(elem):
    return elem[2]

with open(file, 'r') as f:
    for i, line in enumerate(f):
        if i == 0:
            continue
        line = line.strip();
        dststr, srcstr, count, date1str, date2str = line.split(',')
        dst = int(dststr); src = int(srcstr); time = date1str.strip('"')
        date, time = time.split('T')
        arrival = int(date + time)
        unordered.append([src, dst, arrival])
        # if i>1000:
        #     break

ordered = sorted(unordered, key = takeThird)

with open(output,'w') as outfile:
    for row in ordered:
        outfile.write('%d %d %d \n' % (row[0], row[1], row[2]))
    outfile.close()