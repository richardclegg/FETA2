#Script to create plot of degree squared for different power exponents in Rank Preference model

#Properties
times <- 1;
maxDegree <- 4;
clusterCoeff <- 6;
meanDegreeSquared <- 11;
assortativity <- 13;

property <- assortativity;

#Initialise parameters

alphas <- seq(0,1,by=0.25);
rankxmlfile <- readLines("experiments/RankTest.xml")
cols = c("red","orange","green","blue","magenta");

p <- plot.new()

propertyPlot<-list();

for(i in 1:length(alphas)){
  a = alphas[i]
  rankgrow <- gsub(pattern = "XXX", replace = toString(a), x = rankxmlfile);
  writeLines(rankgrow,"experiments/rankgrow.xml");
  system("java -jar feta2-1.0.0.jar experiments/rankgrow.xml");
  system("java -jar feta2-1.0.0.jar scripts/test_measure.xml > rankresults.dat")
  varname <- paste("rankgrow",i,sep="");
  assign(varname, read.table("rankresults.dat", quote="\""));
  propertyPlot[[i]] <- eval(parse(text=varname))[,property];
}

df <- do.call("rbind",propertyPlot)

matplot(rankgrow1$V1, t(df), type = 'l', col = cols, lty = c(1,1), xlab = "Time", 
        ylab = "Assortativity")

legend("topleft", legend = c("alpha = 0", "alpha = 0.25", "alpha = 0.5", 
                             "alpha = 0.75", "alpha = 1"), col = cols, lty = c(1,1,1,1,1))
