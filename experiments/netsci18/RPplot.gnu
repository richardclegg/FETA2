set term postscript eps enhanced color 24


unset key
set tic scale 0
set xrange [0:100]
set yrange [0:150]


set cbrange [0:2000]

theta(x) = x<0 ? 0 : 1
r(x) = 4*x*(1-theta(x-0.25))
g(x) = 0.5*theta(x-0.25)*(1-theta(x-0.5))
b(x) = x

set palette model RGB functions r(gray), g(gray), b(gray)

set output 'experiments/netsci18/RPplot3-grayscale.eps'

set lmargin screen 0.2
set rmargin screen 0.8

set log cb
set xlabel "Normalised Iteration Number as Percentage"
set ylabel "Degree"
set cblabel "Frequency"
set title "Time-degree frequency plot of RP network"
set view map


splot "experiments/netsci18/rankdegrees-3-10000.dat" matrix with image

q