set term postscript eps enhanced color 24
unset key
set tic scale 0
set xrange [0:100]
set yrange [0:150]
set palette rgbformula -7,2,-7
set cbrange [0:2000]
unset cbtics
set log cb
set xlabel "Time"
set ylabel "Degree"
set cblabel "Frequency"
set title "Time-degree frequency plot of RP network"
set view map
splot "experiments/netsci18/rankdegrees.dat" matrix with image