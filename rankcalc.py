#!/usr/bin/env python

N=10000
i=1
alpha = -0.5
sum = 0

while i<N+1:
    sum += i**alpha
    i+=1

prob = 1/sum
prob2 = (2**alpha)/sum
print(prob)
print(prob2)