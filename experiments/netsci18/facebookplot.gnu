set term postscript eps enhanced color 24
unset key
set tic scale 0
set xrange [0:677]
set yrange [0:150]
set palette rgbformula 7,5,15
set cbrange [0:2000]

set log cb
set xlabel "Time"
set ylabel "Degree"
set cblabel "Frequency"
set title "Time-degree frequency plot of Facebook network"
set view map
splot "experiments/netsci18/facebookdegrees.dat" matrix with image