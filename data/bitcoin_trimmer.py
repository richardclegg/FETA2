#!/usr/bin/env python3

yearfrom = "2009"
yearto = "2012"

file1 = 'data/bitcoin/bitcoin_ordered.txt'
file2 = 'data/bitcoin/bitcoin_'+yearfrom+'-'+yearto+'.txt'

year1 = int(yearfrom + "0000000000")
year2 = int(yearto + "0000000000")

with open(file1,'r') as fread:
    with open(file2,'w') as fwrite:
        for i, line in enumerate(fread):
            linestr = line.strip().split()
            year = int(linestr[2])
            if year < year1:
                continue
            if year > year2:
                break
            fwrite.write(line)
    fwrite.close()