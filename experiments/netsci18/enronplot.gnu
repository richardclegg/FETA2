set term postscript eps enhanced color 24
unset key
set tic scale 0
set xrange [0:900]
set yrange [0:500]
set palette rgbformula 7,5,15
set cbrange [0:2000]

set log cb
set xlabel "Time"
set ylabel "Degree"
set cblabel "Frequency"
set title "Time-degree frequency plot of Enron email network"
set view map
splot "experiments/netsci18/enrondegrees.dat" matrix with image