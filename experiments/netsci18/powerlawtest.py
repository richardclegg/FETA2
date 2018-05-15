#!/usr/bin/env python3

import powerlaw

badgrees = "experiments/netsci18/BAtransp.dat"

badata = []

i=1;

with open(badgrees) as file:
    for j, line in enumerate(file):
        line = line.strip()
        freq = int(line)
        for j in range(1,freq):
            badata.append(i)
        i=i+1

fit = powerlaw.Fit(badata, discrete=True)
print(fit.xmin)
print(fit.power_law.alpha)
fit = powerlaw.Fit(badata, xmin=4.0, discrete=True)

R, p = fit.distribution_compare('power_law', 'exponential', normalized_ratio=True)
print(R, p)
print(fit.distribution_compare('power_law','lognormal'))
fit.plot_ccdf(linewidth=3)

rankdegrees = "experiments/netsci18/ranktransp.dat"

rankdata = []

i=1

with open(rankdegrees) as file:
    for j, line in enumerate(file):
        line = line.strip()
        freq = int(line)
        for j in range(1,freq):
            rankdata.append(i)
        i=i+1

fit2 = powerlaw.Fit(rankdata, discrete=True)
print(fit2.xmin)
print(fit2.power_law.alpha)
fit2 = powerlaw.Fit(rankdata, xmin=fit2.xmin, discrete=True)

R2, p2 = fit2.distribution_compare('power_law', 'exponential', normalized_ratio=True)
print(R2, p2)
print(fit2.distribution_compare('power_law','lognormal'))
fit2.plot_ccdf(linewidth=3)

TPAdegrees = "experiments/netsci18/TPAtransp.dat"

TPAdata = []

i=1;

with open(TPAdegrees) as file:
    for j, line in enumerate(file):
        line = line.strip()
        freq = int(line)
        for j in range(1,freq):
            TPAdata.append(i)
        i=i+1

fit3 = powerlaw.Fit(TPAdata, discrete=True)
print(fit3.xmin)
print(fit3.power_law.alpha)
fit3 = powerlaw.Fit(TPAdata, xmin=fit3.xmin, discrete=True)

R3, p3 = fit3.distribution_compare('power_law', 'exponential', normalized_ratio=True)
print(R3, p3)
print(fit3.distribution_compare('power_law','lognormal'))
fit3.plot_ccdf(linewidth=3)