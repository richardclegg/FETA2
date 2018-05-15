set term postscript eps enhanced color 24
unset key
set tic scale 0
set xrange [0:100]
set yrange [0:150]
set palette rgbformula 7,5,15
set cbrange [0:2000]

set output 'experiments/netsci18/BAplot3-grayscale.eps'

set lmargin screen 0.2
set rmargin screen 0.8

set log cb
set xlabel "Normalised Iteration Number as Percentage"
set ylabel "Degree"
set cblabel "Frequency"
set title "Time-degree frequency plot of BA network"
set view map
splot "experiments/netsci18/BAdegrees-3-10000.dat" matrix with image

q