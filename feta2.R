#Constructor for feta model
fetamodel<-function(input.data, 
        nullOffset= TRUE, 
        deg= TRUE, 
        rand= TRUE,
        silent=FALSE,
        pfp= FALSE, 
        antitri=FALSE, 
        degpow=FALSE, 
        alpha=1.0,
        single=FALSE, 
        double= FALSE,
        delta= 0.0, 
        tri= FALSE,
        family= FALSE,
        newcount= 0,
        hotcount= 0)
{
    if (class(input.data) == "character") {
        input.data<-readfile.fetamodel(input.data)
        if (!silent) {
            cat("Data read!\n");
        }
    } else if (class(input.data) == "data.frame") {
        if (!silent) {
            cat("Data accepted!\n");
        }
    } else {
        cat("Expected file name or data as first input!\n")
        return(NULL)
    }
    object<-list (
        input.data= input.data, 
        nullOffset=nullOffset, 
        deg= deg, 
        rand= rand,
        silent= silent,
        pfp= pfp, 
        antitri= antitri, 
        degpow= degpow, 
        alpha= alpha,
        single= single, 
        double= double,
        delta= delta, 
        tri= tri,
        family= family,
        newcount= newcount,
        hotcount= hotcount
    )
    class(object)<-"fetamodel"
    return(object)  

}

# Merges lists with no name duplication -- first occurence takes priority
merge.list<-function(list1,list2)
{
    l<-append(list1,list2)
    l[duplicated(names(l))]<-NULL
    return(l)
}

#Helper function performs normalisation of selections
normByLevels<-function(col1,col2)
{
  sums<-tapply(col1,factor(col2),sum) 
  sums[sums==0]<-1
  levs<-tapply(col1,factor(col2))
  col1/sums[][levs]
}

#gets rid of NAs in columns
replaceNA<-function(col1,col2)
{
   col1[is.na(col1)] <- col2[which(is.na(col1))]
   col1
}

findhot.fetamodel<-function (x,range=seq(1,10),...)
{
  for (i in range) {
    g<-linearfit.fetamodel(x,hotcount=i,...,silent=TRUE)
    cat (i," ",g$deviance)
    cat ("\n")
    rm(g)
  }
}

finddelta.fetamodel<-function (x,range=seq(0.01,0.1,0.01),...)
{
    for (i in range) {
        g<-linearfit.fetamodel(x,delta=i,...,silent=TRUE,pfp=TRUE,deg=FALSE)
        cat (i," ",g$deviance)
        cat ("\n")
        rm(g)
    }
}

findalpha.fetamodel<-function (x,range=seq(0.1,2.0,0.1),...)
{
    args<-list(...)
    for (i in range) {
        g<-linearfit.fetamodel(x,alpha=i,...,deg=FALSE,silent=TRUE,degpow=TRUE)
        cat (i," ",g$deviance)
        cat ("\n")
        rm(g)
    }
}

print.fetamodel<-function(x) {
    
    if ("formula" %in% names(x)) {
        cat("Formula: ",x$formula,"\n");
    }
    if ("glm" %in% names(x)) {
        summary(glm)
    }
}



readfile.fetamodel<-function(dataFiles)
{
   flag<-0
   
   for (d in dataFiles) {
      if (flag == 0) {
         flag <-1
         input.data<-read.table(d)
         colnames(input.data) <-
            c("nodeid","choiceid","chosen","batchsize",
           "nonodes", "degcount","tricount","hotcount")
      } else {
         input.data<-rbind(input.data,read.table(d))
      }
   }
   input.data
}



linearfit.fetamodel<-function (x, ...)
{
    if (class(x) != "fetamodel") {
        cat("Require fetamodel object created by fetamodel()\n");
        return(0)
    }
    #attach(x,warn.conflicts=FALSE)
    args<-list(...)
    #attach(args,warn.conflicts=FALSE)
    x<-merge.list(args,x)
    if (!x$family) {
        if (max(x$input.data$batchsize) == 1) {
            family<-binomial(link="identity")
        } else {
            family<-gaussian(link="identity")
        }
    }
    if (x$nullOffset) {
        myForm<-"x$input.data$chosen ~ 0"
    } else {
        scount<-1
        myForm<-"x$input.data$chosen ~ 1"
    }
    scount<-0
    if (x$rand) {
        myForm<-paste(myForm,"+ randFact")
        randFact<-x$input.data$batchsize
        randFact<-normByLevels(randFact,x$input.data$choiceid)
        scount<-scount+1
    }
    if (x$deg) {
        degrees<-x$input.data$degcount
        degrees<-normByLevels(degrees,x$input.data$choiceid)
        degrees<-degrees*x$input.data$batchsize
        myForm<-paste(myForm,"+ degrees")
        scount<-scount+1
    }
    if (x$hotcount > 0) {
        hotcol<-1*(x$input.data$hotcount <= x$hotcount & x$input.data$hotcount > 0)
        hotcol<-normByLevels(hotcol, x$input.data$choiceid)
        hotcol<- hotcol*x$input.data$batchsize
        myForm<-paste(myForm,"+ hotcol")
        scount<-scount+1
    } 
    if (x$newcount > 0) {
        newcol<-1*( (max(x$input.data$nodeid) -x$input.data$nodeid) < x$newcount)
        newcol<-normByLevels(newcol, x$input.data$choiceid)
        newcol<- newcol*x$input.data$batchsize
        myForm<-paste(myForm,"+ newcol")
        scount<-scount+1
    }

    if (x$pfp) {
        pfpcol<-x$input.data$degcount^(1.0 + x$delta * log10(x$input.data$degcount))
        pfpcol<-normByLevels(pfpcol,x$input.data$choiceid)
        pfpcol<-pfpcol*x$input.data$batchsize
        myForm<-paste(myForm,"+ pfpcol")
        scount<-scount+1
    }
    if (x$tri) {
        tricol<-x$input.data$tricount
        tricol<-normByLevels(tricol,x$input.data$choiceid)
        myForm<-paste(myForm,"+ tricol")
        scount<-scount+1
        tricol<-tricol*x$input.data$batchsize
    }
    if (x$degpow) {
        degpowcol<-x$input.data$degcount^(x$alpha)
        degpowcol<-normByLevels(degpowcol,x$input.data$choiceid)
        degpowcol<-degpowcol*x$input.data$batchsize
        myForm<-paste(myForm,"+ degpowcol")
        scount<-scount+1
    }
    if (x$antitri) {
        antitricol<-max(x$input.data$tricount)-(x$input.data$tricount)
        antitricol<-normByLevels(antitricol,x$input.data$choiceid)
        myForm<-paste(myForm,"+ antitricol")
        scount<-scount+1
    }
    if (x$single) {
        singlecol<-(1*(x$input.data$degcount==1));
        singlecol<-normByLevels(singlecol,x$input.data$choiceid)
        singlecol<-singlecol*x$input.data$batchsize
        myForm<-paste(myForm,"+ singlecol")
        scount<-scount+1
    }
    if (x$double) {
        doublecol<-(1*(x$input.data$degcount==2));
        doublecol<-normByLevels(doublecol,x$input.data$choiceid)
        doublecol<-doublecol*x$input.data$batchsize
        myForm<-paste(myForm,"+ doublecol")
        scount<-scount+1
    }  

    if(!x$silent) {
        cat ("Now fitting ",myForm," with ",scount," variables\n")
    }
    fmla<-as.formula(myForm)
    mystart<-rep(1/scount,scount)
    g<-glm(fmla,family=family,start=mystart)
    x$formula<-fmla
    x$glm<-g
    return(g)
}


