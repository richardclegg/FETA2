#!/usr/bin/env python3

import powerlaw
import sys
import math

degreefile=sys.argv[1]
finaldegrees=[]

with open(degreefile, 'r') as file:
    lines = file.read().splitlines()
    last_line = lines[-1]

last_line = last_line.strip()
degreestable = last_line.split(' ')
#print(degreestable)

degreesdata = []
i=1
for k in range(0,len(degreestable)):
    freq = int(degreestable[k])
    for j in range(1, freq):
        degreesdata.append(i)
    i = i+1

#print(degreesdata)

fit = powerlaw.Fit(degreesdata, discrete=True)
print('Best discrete power law fit with cutoff value xmin and exponent alpha')
print('----------')
xmin = fit.xmin
alpha = fit.alpha
print(xmin, alpha)
print('----------')
print('----------')

print('Log likelihood and p value in comparison with exponential distribution')
print('----------')
Rnorm, pnorm = fit.distribution_compare('power_law', 'exponential', normalized_ratio=True)
if Rnorm < 0:
    print('Power law less likely explanation than exponential')
elif pnorm > 0.05:
    print('Power law not a significantly better fit than exponential')
else: print('Power law a significantly better fit than exponential')
print(Rnorm,pnorm)
print('----------')
print('----------')

print('Log likelihood and p value in comparison with lognormal distribution')
print('----------')
Rlog, plog = fit.distribution_compare('power_law', 'lognormal', normalized_ratio=True)
if Rlog < 0:
    print('Power law less likely explanation than lognormal')
elif plog > 0.05:
    print('Power law not a significantly better fit than lognormal')
else: print('Power law a significantly better fit than lognormal')
print(Rlog, plog)

fig = fit.plot_pdf(color='b', linewidth=2)
fit.power_law.plot_pdf(color='b', linestyle='--', ax=fig)