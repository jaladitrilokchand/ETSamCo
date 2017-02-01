#!/bin/ksh
#========================================================================================
# Program Name: CM_parallel.ll
#
# Purpose     : Runs parallel(LoadLeveler) build and regression flows on 
#               14.1 EDA tool components.
#
# Notes       : Can be run during the day or as a cron in the overnight build process
#               It can handle the two 14.1 platforms (64-rs_aix61 and 64-linux50)
#               This script requires AFS tokens and can run only from ketch (using the
#               einslib ID) and mogul (using the hdplib ID). 
#
# Change History :
# 08/17/12 GS  Copied CM_parallel.svn script as starting point for CM_parallel.ll script
#               * Converted RSH calls to LoadLevelder
#               * Removed processing *.gone files (not needed for svn)
#               * Added support for LEVEL vs build variable 
# 08/21/12 GS  Updated to support locations (-l level) other than build
# 10/01/12 GS  Updated logic to test/set AFS tokens since crons will be moving off ketch
# 10/04/12 GS  Updated COMPILE_OK to look for file in 14.1 when running as 14.0
# 10/16/12 GS  Updated to work with shipb/tkb injects.
# 12/31/12 GS  Updated logic to get the list of components to compile.  The old method
#              did not check the COMPLIST results and sometimes the COMPLIST was empty
#              or contained "LL Job not started".
# 01/11/12 GS  Updated to support xtinct TKs.
#=========================================================================================
#=========================================================================================
#                             FUNCTIONS
#=========================================================================================
function help
{
cat << \EOF
# Usage  CM_parallel.ll <-r release> <-p platform> [-A] [-C] [-l level] [-O] [-P] [-R] [-W]
#         -r release to process
#         -p platform to build
#         -A indicates an "as-needed" build 
#         -C compile and link
#         -l location to compile/test [build = default] [build, shipb ot tkb]
#         -O (indicates overnight build; please do not use this flag during daytime,
#            it'll blowaway the regression results directories)
#         -P forces into PRODUCTION flow vs default (DEVELOPMENT flow).
#         -R (run regressions only and run the whole suite default)
#	  -W do NOT update the web page for rebuild
#
# Function: Build an EinsTimer component parallelly during day time.
#           (calling ET_domake)
#
# NOTE: Whenever this script is invoked it will remove the platform_regressions
#       results directory. Please make sure to save the old results into some
#       dir. if u need them.
EOF
}

function goodbye
{
[[ -n $ENTIRE ]] && print "\n$SHELL_NAME: EinsTimer.${LOG_VER} ending at $(date +" %a %D %T") on $(hostname)" >> $BUILDLOG
FILELIST=$(/usr/bin/ls /tmp/*.$PID 2>/dev/null)
[[ -n $FILELIST ]] && /bin/rm -f $FILELIST  2>/dev/null

FILELIST=$HOME/tmp/logfile.${PRODUCT}.${LOG_VER}.${PLATFORM}
[[ -a $FILELIST ]] && /bin/rm -f $FILELIST 2>/dev/null

FILELIST=$HOME/tmp/compload.${PRODUCT}.${LOG_VER}.${PLATFORM}
[[ -a $FILELIST ]] && /bin/rm -f $FILELIST 2>/dev/null

rc="$1"
[[ -n $2 ]] && print "$2"
FILELIST=$(find $BUILDDIR -name core)
[[ -n $FILELIST ]] && /bin/rm $FILELIST
print "\n$(date +"%D %T"): Build has exited<br>" >> $COMPERROR
exit "${rc:-0}"
}

function check_complock
{
  ALLDONE=
  while [[ -z $ALLDONE ]]
  do
    /bin/ls ${COMPLOCKDIR}/* 2>/dev/null >/dev/null
    if [[ $? = 0 ]]
    then
      print "\n$(date +"%D %T") Waiting for complock to clear..." >> $BUILDLOG
      sleep $NAPTIME
    else
      ALLDONE=TRUE
    fi
  done
}


#
# Force the DLL symlinks to point at the "-O.dll" file versions.
#  
function build_DLL_symlinks
{

  curDir=`pwd`
  case $PLATFORM in
    64-rs_aix61) PLATDIR="$BUILDDIR/.dll-aix64"
	         ;;
     64-linux50) PLATDIR="$BUILDDIR/.dll-linux64"
	         ;;
              *) return 4
	         ;;
  esac

  if [[ -d $PLATDIR ]]
  then
    cd $PLATDIR
    count=0
    for file in $(ls *-O.dll)
    do
      strDel="-O"
      strLeft=${file%$strDel*}
      strRight=${file#*$strDel}
      newFile=${strLeft}${strRight}
      print "\nBuilding symlinks..." >> $BUILDLOG
      print "ln -sf $file $newFile"  >> $BUILDLOG
      ln -sf $file $newFile
      ((count = count + 1)) 
    done
    print "\nCreated $count symlinks in directory $PLATDIR."  >> $BUILDLOG
    print "Operation complete.\n"  >> $BUILDLOG
  else
    print "ERROR: Target dir, \"$PLATDIR\", does not exist! \n"  >> $BUILDLOG
  fi
  cd $curDir
}

function get_tkversion
{
  STAGENAME="development"
  if [[ $LEVEL = "shipb" ]]
  then
    STAGENAME="preview"
  elif [[ $LEVEL = "tkb" ]]
  then
    STAGENAME="production"
  elif [[ $LEVEL = *+(xtinct/tk1)* ]]
  then
    STAGENAME=$LEVEL
  fi

  DBSWITCH="-db PROD"
  if [[ $PRODMODE = FALSE ]]
  then
    DBSWITCH="-db DEV"
  fi

  myCmd="$SVNCLIB/getToolKits -r $R_V -s $STAGENAME -q $DBSWITCH"
  TKVERSION=$($myCmd)

}


#=========================================================================================
#                             MAIN PROGRAM
#=========================================================================================
PRODMODE=FALSE
MAX_MACH=50 # Was 20 when using RSH
LEVEL=build

while getopts :hr:p:ACl:OPRSW OPTION
do
  case "$OPTION" in
      r) RELEASE=$OPTARG;;
      p) PLATFORM=$OPTARG;;
      A) AS_NEEDED=TRUE;;
      C) DO_COMPILE=TRUE;;
      l) LEVEL=$OPTARG;;
      O) OVERNIGHT=TRUE;;
      P) PRODMODE=TRUE;;
      R) DO_REGRESSION=TRUE;;
      R) DO_REGRESSION=TRUE;;
      W) NO_WEB=TRUE;;
      :) help; goodbye 1 "\n*** Option -$OPTARG requires a paramter."; exit;;
      ?) help; goodbye 1 "\n*** Option -$OPTARG is not recognized."; exit;;
  esac
done
shift $(($OPTIND - 1))

[[ -z $RELEASE ]] && { help; goodbye 1 "\n*** release required!"; exit; }
[[ -z $PLATFORM ]] && { help; goodbye 1 "\n*** platform required!"; exit; }

LEVEL_STR=$LEVEL
if [[ $LEVEL = @(build|shipb|tkb) ]]
then
    print "Valid location specified: $LEVEL"
elif [[ $LEVEL = *+(xtinct/tk1)* ]]
then
    print "Valid location specified: $LEVEL"
    # convert xtinct/tk14.1.2 with xtinct_tk14.1.2
    LEVEL_STR=$(echo $LEVEL_STR|sed 's/\//_/g')
else 
    print "INVALID location specified: $LEVEL"
    exit
fi


# The default operation of the OVERNIGHT switch is to run both COMPILE and REGRESSIONS.
# We want just COMPILE for the model* components. 
if [[ $OVERNIGHT = TRUE ]]
then
    DO_COMPILE=TRUE
    case $RELEASE in
	model.1401|modelutil.1401|modelio.1401) ;;
	*) DO_REGRESSION=TRUE;;
    esac
fi

# dot in the common build script
. /afs/eda/u/cmlib/bin/CM_common.14

# Use parse_rel function to get PRODUCT name and convert from zero-based release
# version to dot version (e.g. 1401 -> 14.1)
parse_rel $RELEASE

# on the sun when 'rsh'ing USER is set but LOGNAME is not (hp_ux102 is opposite)
# this can probably be fixed by using whoami!!!
[[ -z $LOGNAME && -n $USER ]] && LOGNAME=$USER
[[ -z $USER && -n $LOGNAME ]] && USER=$LOGNAME

# AFS authentication
# Look up the user's password in the hidden password file.
if [[ -f /tmp/local/$USER ]]
then
    PASS=$(cat /tmp/local/$USER)
    if [[ -z $PASS || $PASS = "" ]]
    then 
	print "Password file for $USER is empty. Exiting with errors ..."
	exit
    fi
    print "Password found ..."
    /usr/afsws/bin/klog -pass $PASS
    # /usr/afsws/bin/klog -c btv -pass $PASS
    export KAUTH=afs
else 
    tokeCount=$(/usr/afsws/bin/tokens | grep -c "(AFS ID")
    if (( $tokeCount > 0 ))
    then
	print "AFS tokens found ..."
	/usr/afsws/bin/tokens
    else 
	print "No AFS tokens and can't find password file for $USER. Exiting with errors ..."
	exit
    fi
fi

#
# Setting the environment variables
#

# What the heck do we need all these for?
# ARCHPLAT refers to the subdirectory name used per platform in the build, applies to Arch-<platform> dirs,
#          and .lib-, .dll-, and .bin-
# REGPLAT  refers to the regression results subdirectory
# REGFILE  refers to the regression list prefix <arch>.reglist
# map platform to machine arch's in CM_getbestmach
# GMPLAT   (getmach_platform) refers to the machinename to use when using CM_getbestmach, 
#          it needs an extra oslevel digit, otherwise all is the same
# GMPLAT1  refers to the machine to use if you are building an n.0 build and
#          need the DIRECTORIES from the n.1 build 
# 
GMPLAT=$PLATFORM  # set a default unless specified otherwise
GMPLAT1=rs_aix61  # this is good for the indefinite future, but we will have to override
	  # by release level at some time
case $PLATFORM in
64-rs_aix61) GMPLAT=64-rs_aix61
       GMREGPLAT=64-rs_aix61
       REGPLAT=AIX64
       ARCHPLAT=aix64
       REGFILE=64rs
       ;;
64-linux50) GMPLAT=64-linux50
       REGPLAT=Linux64
       ARCHPLAT=linux64
       REGFILE=64linux
       ;;
esac

# if the regression platform isn't set, set it to the same as the compile
if [[ ! -n $GMREGPLAT ]]
then
    GMREGPLAT=$GMPLAT
fi

SHELL_NAME=${0##*/}
PID=$$
CMLIB=/afs/eda.fishkill.ibm.com/u/cmlib
EINSLIB=/afs/eda.fishkill.ibm.com/u/einslib
HDPLIB=/afs/eda.fishkill.ibm.com/u/hdplib

#
# Get the hour based on 24-hour time
# If it is before midnight, use tomorrow's date for the log file
#
integer HOUR=$(date +%H)
if [[ $HOUR -ge 18 ]]
then
# The normal setting for TZ in Fishkill is EST5EDT (GMT+5 and DST),
# telling the system we are 5 hours behind Greenwich, which is the time
# it is physically keeping.
# Pretend we are in Middle Europe, where the new day begins 6 pm our time.
    typeset -l DATE=$(TZ=MET-1MEDT date +%b%d)
    typeset -l MONTH=$(TZ=MET-1MEDT date +%b)
else
    typeset -l DATE=$(date +%b%d)
    typeset -l MONTH=$(date +%b)
fi


if [[ $PRODMODE = FALSE ]]
then
    SVNCLIB=/afs/eda/data/edainfra/tools/enablement/dev/bin
    BUILDDIR=/afs/eda.fishkill.ibm.com/${LEVEL}/svn_test/${PRODUCT}/${R_V}
    BUILDLOG=$HOME/logs/${MONTH}/${DATE}.${PRODUCT}.${LOG_VER}.${PLATFORM}.$LEVEL_STR.devmode
else
    SVNCLIB=/afs/eda/data/edainfra/tools/enablement/prod/bin
    BUILDDIR=/afs/eda.fishkill.ibm.com/${LEVEL}/${PRODUCT}/${R_V}
    BUILDLOG=$HOME/logs/${MONTH}/${DATE}.${PRODUCT}.${LOG_VER}.${PLATFORM}.$LEVEL_STR
fi
print "SVNCLIB  : $SVNCLIB"
print "BUILD LOG: $BUILDLOG"
print "BUILD DIR: $BUILDDIR"

[[ ! -d $BUILDDIR ]] && { print "\n*** $BUILDDIR could not be read!"; exit; }

get_tkversion
print "TKVERSION: $TKVERSION" | tee -a $BUILDLOG
if [[ $TKVERSION = No* ]]
then
    print "Unable to determine TK Version"
    exit
fi


ETCDIR=$BUILDDIR/build/etc
EINCOM=eincom

BLOWAWAYFILE=$ETCDIR/blowaway.$PLATFORM
COMPERROR=$ETCDIR/cmperr.${PLATFORM}.html
RESULTFILE=$ETCDIR/badlibs.$PLATFORM

COMPLOCKDIR=$ETCDIR/complock/${PLATFORM}
[[ ! -d $COMPLOCKDIR ]] && mkdir -p $ETCDIR/complock/${PLATFORM}

LOGDIR=$ETCDIR/log/$PLATFORM
[[ ! -d $LOGDIR ]] && mkdir -p $ETCDIR/log/${PLATFORM}

REGLOGDIR=$ETCDIR/reglog/$PLATFORM
EINCOMLOG=$LOGDIR/$EINCOM
REGRESSIONDIR=/afs/eda.fishkill.ibm.com/project/$PRODUCT/regression/$LOG_VER
LOCKFILE=$ETCDIR/lock.prebuild
LOCK_EXTRACT=$BUILDDIR/lock.extract
NO_EXTRACT=$BUILDDIR/no.extract
COMPILE_OK=/afs/eda.fishkill.ibm.com/${LEVEL}/${PRODUCT}/14.1/build/etc/compile.ok.${PLATFORM}
# wait up to ten minutes for the extract
MAXWAIT=600

SHORTNAP=30 
NAPTIME=60
LONGNAP=180
#GETMACH="$CMLIB/bin/CM_getbestmach.14"
GET_CONFIG="$CMLIB/bin/cm_get_config"
MAKEGEN="$CMLIB/bin/CM_mkgen.14" 
# RSH=/usr/afsws/bin/rsh
# RSH=/usr/bin/ssh
GETVAL="$CMLIB/bin/cm_getval"

LL_results="$SVNCLIB/ll.run_results"
LL_wait="$SVNCLIB/ll.run_wait"
LL_forget="$SVNCLIB/ll.run_forget"

BTLEVEL="prod"
if [[ $LEVEL != "build" ]]
then
  BTLEVEL=$LEVEL
fi

NUT_VER=$(grep "^NUTSH_VERSION" $BUILDDIR/Make.rules | awk '{print $3}')
NUTSHELL=/afs/eda/$BTLEVEL/nutshell/$NUT_VER
BTOOLS=$NUT_VER
BLDTOOLS=/afs/eda/$BTLEVEL/tools/$BTOOLS/bin
print "NUTSHELL: $NUTSHELL"
print "BTLEVEL : $BTLEVEL"

export PATH=$BLDTOOLS:$PATH

[[ $PLATFORM = linux?? ]] && ULIMIT=ulimit || ULIMIT=/bin/ulimit


#
# Using  Flex for rs_aix4 platform
#
export FLEXFLAG=
if [[ $PLATFORM = rs_aix4? ]]
then
  [[ $PRODUCT = einstimer ]] && export FLEXFLAG=" -f"
  [[ $PRODUCT = ess ]] && export FLEXFLAG=" -f"
  [[ $PRODUCT = edautils ]] && export FLEXFLAG=" -f"
  [[ $PRODUCT = cre ]] && export FLEXFLAG=" -f"
fi

######################################################################
#
# Start Processing
#
######################################################################

######################################################################
#
# Do the prebuild processing
print "\n\n$SHELL_NAME: $RELEASE ${PLATFORM} starting $(date +"%a %D %T") on $(hostname)<br>" | tee -a $BUILDLOG >> $COMPERROR

# check for extract completion, build on demand
if [[ -n $AS_NEEDED && -a $NO_EXTRACT ]]
then
  print "Build on Demand: there was no extract...exiting." >> $BUILDLOG
  print "Build on Demand: there was no extract...exiting.<br>" >> $COMPERROR
  exit
fi

WAIT=
while [[ -a $LOCK_EXTRACT  && -z $DONE_WAITIN ]]
do
  # see if a NO_extract file appears while we wait
  if [[ -n $AS_NEEDED && -a $NO_EXTRACT ]]
  then
    print "Build on Demand: there was no extract...exiting." >> $BUILDLOG
    print "Build on Demand: there was no extract...exiting.<br>" >> $COMPERROR
    exit
  fi
  sleep 30
  ((WAIT=$WAIT+30))
  (( $WAIT > $MAXWAIT )) && DONE_WAITIN=TRUE
done

# check to see which condition failed
if [[ -a $LOCK_EXTRACT ]]
then
  print "Extract lock still exists, waited $MAXWAIT, assuming extract failed...exiting." >> $BUILDLOG
  print "Extract lock still exists, waited $MAXWAIT, assuming extract failed...exiting.<br>" >> $COMPERROR
  exit
fi

# Remove the no_build file (indicates to web page that a build was attempted)
print "$(date +'%D %T'): /bin/rm -f $ETCDIR/no_build.${PLATFORM}*" >> $BUILDLOG
/bin/rm -f $ETCDIR/no_build.${PLATFORM}*

# if the build is einstimer 14.1, remove the COMPILE_OK file if exists
if [[ $R_V = 14.1 && $PRODUCT = einstimer && -n $DO_COMPILE ]]
then
  if [[ -a $COMPILE_OK ]]
  then
    rm $COMPILE_OK
  fi
fi 

# Clean up the complock directory
print "Removing complock files" >> $BUILDLOG
/bin/rm -f $COMPLOCKDIR/* 2>/dev/null


if [[ -n $DO_COMPILE ]] 
then
  # do aix 64-bit specific pre-processing
  if [[ $ARCHPLAT = aix64 ]]
  then
    # lock out other build platforms
    print "$(date +"%a %D %T") Touching lock file" >> $BUILDLOG
    /bin/touch $LOCKFILE
  else
    print "Checking to see if $LOCKFILE exists" >> $BUILDLOG
    while [[ -a $(print $LOCKFILE) ]]
    do
      print "Waiting for $LOCKFILE to clear" >> $BUILDLOG
      sleep 30
    done
  fi # 32bit aix pre-processing/lockfile wait

  [[ $PLATFORM = 64* ]] && { export BITS=64; CMRCFLAG=-b; } || CMRCFLAG=

  # symlink the files to n.1 build if this is an n.0 build
  if [[ $ARCHPLAT = aix && $R_V = *.0 ]]
  then
    LINK="$(print $R_V | cut -f1 -d.).1"
    LOGFILE=$LOGDIR/links

    if [[ $PRODMODE = FALSE ]]
    then
      PARENTDIR=/afs/eda.fishkill.ibm.com/${LEVEL}/svn_test/${PRODUCT}/${LINK}
    else
      PARENTDIR=/afs/eda.fishkill.ibm.com/${LEVEL}/${PRODUCT}/${LINK}
    fi

    print "\n*** $(date +"%a %D %T")  Creating $LINK symlink in $BUILDDIR" >> $LOGFILE
    if [[ -d $BUILDDIR/$LINK ]]
    then
      print "Removing $LINK link in $BUILDDIR" >> $BUILDLOG
      /bin/rm -fr $BUILDDIR/$LINK >> $BUILDLOG 2>&1
    fi
    print "Creating links to source code in $PRODUCT" >> $BUILDLOG
    cd $BUILDDIR
    print "ln -s . $LINK" | tee -a $LOGFILE >> $BUILDLOG
    ln -s . $LINK >> $BUILDLOG 2>&1
    
    # get a list of components to build
    print "obtaining a list of directories to symlink in parent directory " >> $BUILDLOG

    REM_CMD="$GET_CONFIG -ap $PARENTDIR -t opt $CMRCFLAG -iv DIRECTORIES"
    REM_CMD=$(echo $REM_CMD|sed 's/ /##/g') # allows correct parsing by perl script
    LL_CMD="\"$LL_results -r $R_V -c $PRODUCT -l $LEVEL_STR -p $PLATFORM -e $REM_CMD"
    
    sysRc=8
    while (( $sysRc > 0 ))
    do 
      print "Running $LL_CMD" >> $BUILDLOG
      print "Running $LL_CMD"
      COMPLIST=$($LL_CMD)
      sysRc=$?
      if (( $sysRc < 1 ))
      then
	  print "Component List: $COMPLIST" >> $BUILDLOG
	  # Set the rc to bad if the results contain "not started"
	  [[ $COMPLIST = *+(not started)* ]] && sysRc=8
      fi
    done
    
    print "Component List: $COMPLIST" >> $BUILDLOG
    print "Component List: $COMPLIST"

    # add component directories that are not in the $(DIRECTORIES) variable in
    # the make config
    COMPLIST_ADD="custom abstract abstract/ndr-64-linux abstract/preamble "
    COMPLIST="$COMPLIST $COMPLIST_ADD"
    
    print Complist: $COMPLIST >> $BUILDLOG
    # build the list of flags, filtering out certain directories
    COMPONLY=
    for COMP in $COMPLIST
    do
      [[ $COMP = @(lib*|bin|dll*|testcases|include|private) ]] && continue
      COMPONLY=" --only $COMP $COMPONLY"
    done

    # print COMPONLY=$COMPONLY >> $BUILDLOG

    # call setup sandbox
    print "\n*** $(date +"%a %D %T") running setup_sandbox to create symlinks" | tee -a $BUILDLOG >> $LOGFILE
    print "/afs/eda/$BTLEVEL/tools/14.1/bin/setup_sandbox --sync $COMPONLY --verbose  /afs/eda/$LEVEL/$PRODUCT/$LINK $BUILDDIR" | tee -a $BUILDLOG >> $LOGFILE
    /afs/eda/$BTLEVEL/tools/14.1/bin/setup_sandbox --sync $COMPONLY /afs/eda/$LEVEL/$PRODUCT/$LINK $BUILDDIR >> $LOGFILE 2>&1
    # remove the build link
    /bin/rm $BUILDDIR/$LINK

    # update the extracted file list for the .0 build
    ln -sf $(print $BUILDDIR | sed "s|$R_V|$LINK|")/build/etc/extracted.files.curr.html $ETCDIR/extracted.files.curr.html >> $BUILDLOG 2>&1

  fi  # n.0 build symlinking to n.1 build/or not

  # get a complist from the native $BUILDDIR/Make.config
  REM_CMD="$GET_CONFIG -ap $BUILDDIR -t opt $CMRCFLAG -iv DIRECTORIES"
  REM_CMD=$(echo $REM_CMD|sed 's/ /##/g') # allows correct parsing by perl script
  LL_CMD="$LL_results -r $R_V -c $PRODUCT -l $LEVEL_STR -p $PLATFORM -e $REM_CMD"

  sysRc=8
  while (( $sysRc > 0 ))
  do 
    print "Running $LL_CMD" >> $BUILDLOG
    print "Running $LL_CMD"
    COMPLIST=$($LL_CMD)
    sysRc=$?
    if (( $sysRc < 1 ))
    then
	print "Component List: $COMPLIST" >> $BUILDLOG
	# Set the rc to bad if the results contain "not started"
	[[ $COMPLIST = *+(not started)* ]] && sysRc=8
    fi
  done
    
  print "Component List: $COMPLIST" >> $BUILDLOG
  print "Component List: $COMPLIST"


  # check to see if this is a blowaway build
  if [[ -a $BLOWAWAYFILE ]]
  then
    print "\n A complete rebuild (blowaway) was requested" >> $BUILDLOG
    LIBDIR=$BUILDDIR/.lib-${PLATFORM}
    print "/bin/rm $LIBDIR/*.a" >> $BUILDLOG
    /bin/rm $LIBDIR/*.a >> $BUILDLOG 2>&1
    /bin/rm $BLOWAWAYFILE
  fi

  # Need to remove the logfiles, comperr, and resultfile files
  print "Removing the logfiles in the log directories" >> $BUILDLOG
  print "/bin/rm -f $LOGDIR/* $LOGDIR/.*" >> $BUILDLOG
  /bin/rm -f $LOGDIR/* >> $BUILDLOG 2>&1
  print "/bin/rm -f $COMPERROR >> $BUILDLOG" >> $BUILDLOG
  /bin/rm -f $COMPERROR >> $BUILDLOG 2>&1
  print "/bin/rm -f $RESULTFILE >> $BUILDLOG" >> $BUILDLOG
  /bin/rm -f $RESULTFILE >> $BUILDLOG 2>&1

  # Need to Remove the .d files in Arch directory
  print "Need to remove all generated  files" >> $BUILDLOG
  print "find $BUILDDIR -name Arch-$ARCHPLAT | xargs /bin/rm -fr"  >> $BUILDLOG
  find $BUILDDIR -name Arch-$ARCHPLAT | xargs /bin/rm -fr >> $BUILDLOG 2>&1

  # clean out build_warning*, build_error* files
  print "$(date +'%D %T'): /bin/rm -f $BUILDDIR/build_*.$ARCHPLAT" >> $BUILDLOG
  if [[ -a $BUILDDIR/build_*.$ARCHPLAT ]]
  then
    /bin/rm -f $BUILDDIR/build_*.$ARCHPLAT
  else
    print "build_ files do not exist for $ARCHPLAT" >> $BUILDLOG
  fi
  print "$(date +'%D %T'): /bin/rm -f $BUILDDIR/lint_*.$ARCHPLAT" >> $BUILDLOG
  if [[ -a $BUILDDIR/build_*.$ARCHPLAT ]]
  then
    /bin/rm -f $BUILDDIR/lint_*.$ARCHPLAT
  else
    print "lint_ files do not exist for $ARCHPLAT" >> $BUILDLOG
  fi

fi # if [[ -n $DO_COMPILE ]]


# REGRESSION pre-processing
if [[ -n $DO_REGRESSION ]]
then
  # move the golden.update
  print "Moving regression to $ETCDIR/regression.$PLATFORM" >> $BUILDLOG
  [[ -s ${REGLOGDIR}/golden.update ]] && { /bin/mv ${REGLOGDIR}/golden.update $ETCDIR/golden.update.$PLATFORM; }
  
  # Remove the reglog files
  print "Removing reglog files" >> $BUILDLOG
  print "/bin/rm -f $REGLOGDIR/.* $REGLOGDIR/* >> $BUILDLOG 2>&1" >> $BUILDLOG
  /bin/rm -f $REGLOGDIR/.* $REGLOGDIR/* >> $BUILDLOG 2>&1

  # If it is an overnight build, need to blow away results area
  if [[ -n $OVERNIGHT && -d $REGRESSIONDIR/results && $PLATFORM = rs_aix?? ]]
  then
    # clean up build/etc/reglist
    print "Cleaning up the reglist directory" >> $BUILDLOG
    $EINSLIB/bin/reglist_cleanup.pl -r $RELEASE  >> $BUILDLOG

    print "Deleting the developer results area in $REGRESSIONDIR/results" >> $BUILDLOG

    for USER in $(ls -1 $REGRESSIONDIR/results/)
    do
      print "/bin/rm -fr $REGRESSIONDIR/results/$USER" >> $BUILDLOG
      /bin/rm -fr $REGRESSIONDIR/results/$USER  2>> $BUILDLOG
    done
  else
    print "Deleting the einslib results area for $PLATFORM" >> $BUILDLOG
    # If this is einstimer, include the build directory in the regression results path (DWM)
    if [[ $PRODUCT = einstimer ]]
    then
      print "/bin/rm -fr ${REGRESSIONDIR}/results/einslib/${LEVEL}/$REGPLAT 2>/dev/null" >> $BUILDLOG
      /bin/rm -fr ${REGRESSIONDIR}/results/einslib/${LEVEL}/$REGPLAT 2>/dev/null
    else
      print "/bin/rm -fr ${REGRESSIONDIR}/results/einslib/$REGPLAT 2>/dev/null" >> $BUILDLOG
      /bin/rm -fr ${REGRESSIONDIR}/results/einslib/$REGPLAT 2>/dev/null
    fi # add build to einstimer results path 
    # print "/bin/rm -fr ${REGRESSIONDIR}/results/einslib/${LEVEL}/$REGPLAT 2>/dev/null" >> $BUILDLOG
    # /bin/rm -fr ${REGRESSIONDIR}/results/einslib/${LEVEL}/$REGPLAT 2>/dev/null
  fi # overnight
fi # [[ -n $DO_REGRESSION ]]


######################################################################
#
# Start the Compile
if [[ -n $DO_COMPILE ]]
then
  # set web page to compiling, write to underlying page
  if [[ -z $NO_WEB ]]
  then
    if [[ $PRODMODE = FALSE ]]
    then
      print "\nDEVMODE: CM_BuildStatus not being run for build" >> $BUILDLOG
    else
      if [[ $LEVEL = build ]]
      then
        print "$HDPLIB/bin/CM_BuildStatus -r $RELEASE -p $PLATFORM -l $LEVEL -B" >> $BUILDLOG
        $HDPLIB/bin/CM_BuildStatus -r $RELEASE -p $PLATFORM -l  $LEVEL -B >> $BUILDLOG 2>&1
      fi
    fi  
  fi
  print "<html>" > $COMPERROR
  print "<xmp>" >> $COMPERROR
  print "$(date +"%D %T"): Build in progress..............\n<br>" >> $COMPERROR

  # if we are 64-bit AIX, do a make generated followed by make includes
  if [[ $ARCHPLAT = aix64 ]]
  then
    print "\n*** $(date +"%a %D %T") running make generated to create MsgCat and other generated files" >> $BUILDLOG

    print "Using LL for doing 'make generated' ..." >> $BUILDLOG
    REM_CMD="$MAKEGEN $BEAMOPT -d $BUILDDIR -p $PLATFORM"
    REM_CMD=$(echo $REM_CMD|sed 's/ /##/g') # allows correct parsing by perl script

    LL_CMD="$LL_wait -r $R_V -c $PRODUCT -l $LEVEL_STR -p $PLATFORM -e $REM_CMD"
    print "Running $LL_CMD" >> $BUILDLOG
    print "Running $LL_CMD"
    $LL_CMD

    # if inject builds then convert the symlinks in private and include dirs
    # to hard files
    if [[ $LEVEL != build ]]
    then
      # link2real 14.1 einstimer tkb)
      print "Converting sym links in private & include dirs to real files ..." >> $BUILDLOG
      print "Converting sym links in private & include dirs to real files ..."
      LL_CMD="$EINSLIB/bin/link2real $R_V $PRODUCT $LEVEL"
      print "Running $LL_CMD" >> $BUILDLOG
      print "Running $LL_CMD"
      $LL_CMD >> $BUILDLOG
    fi


    # Pre LL code ...
    #MACHINE=$(getonemach $GMPLAT)
    #print "machine for doing 'make generated': $MACHINE" >> $BUILDLOG
    #print "$RSH $MACHINE $MAKEGEN $BEAMOPT -d $BUILDDIR -p $PLATFORM" >> $BUILDLOG
    #$RSH $MACHINE $MAKEGEN $BEAMOPT -d $BUILDDIR -p $PLATFORM



    # remove the lock file so other platforms can go ahead
    print "$(date +"%a %D %T") Removing lock file" >> $BUILDLOG
    print "/bin/rm -f $LOCKFILE" >> $BUILDLOG
    /bin/rm -f $LOCKFILE 2>/dev/null

  fi # ARCHPLAT = aix64

  # SPECIAL CASE PROCESSING FOR linux40 32 vs 64bit header file sourcedir differences
  if [[ $PLATFORM = linux40 || $PLATFORM = 64-linux40 ]]
  then
    print "Linux40 only: finding all .d files and removing"  >> $BUILDLOG
    for ARCHDIR in $(find $BUILDDIR -name Arch-$ARCHPLAT -print)
    do
      find $ARCHDIR -name "*.d" | xargs /bin/rm -fr >> $BUILDLOG 2>&1
    done
  fi # ARCHPLAT = linux40 or 64-linux40

  # we already have a complist, retrieved before we cleaned up the build_warnings/error files
  # sort this list so that we put the longest compiles at the top (\c means omit final newline)

  # make sure the temp file doesn't exist before starting - keep file for debug
  rm $HOME/tmp/compload.${PRODUCT}.${LOG_VER}.${PLATFORM}  

  cd $BUILDDIR 
  for COMP in $COMPLIST
  do
    if [[ $COMP = html* ]]
    then
      print "$COMP 1" >> $HOME/tmp/compload.${PRODUCT}.${LOG_VER}.${PLATFORM}
      continue
    fi
    if [[ $COMP = rlcviewer ]]
    then
      print "$COMP 1" >> $HOME/tmp/compload.${PRODUCT}.${LOG_VER}.${PLATFORM}
      continue
    fi
    [[ $COMP = @(dll*|lib*|bin|testcases) ]] && continue

#     if [[ -n $(/bin/ls $BUILDDIR/$COMP/*[Ccf] $BUILDDIR/$COMP/*tclz 2>/dev/null) ]]
#     then
#       print "$COMP\c" >> $EINSLIB/tmp/compload.${PRODUCT}.${LOG_VER}.${PLATFORM}
#       ls -l $COMP/*[Ccf] $COMP/*tclz|wc -l >> $EINSLIB/tmp/compload.${PRODUCT}.${LOG_VER}.${PLATFORM} 2>/dev/null
#     fi
#   moving '2>/dev/null' into ls -l part of check for tclz files as this generates email otherwise -mcw 11/05/08

    if [[ -n $(/bin/ls $COMP/*[Ccf] $BUILDDIR/$COMP/*tclz 2>/dev/null) ]]
    then
      print "$COMP\c" >> $HOME/tmp/compload.${PRODUCT}.${LOG_VER}.${PLATFORM}
      ls -l $COMP/*[Ccf] $COMP/*tclz 2>/dev/null|wc -l >> $HOME/tmp/compload.${PRODUCT}.${LOG_VER}.${PLATFORM}
    fi

  done

    COMPS=$(sort -t" " +1 -n -r $HOME/tmp/compload.${PRODUCT}.${LOG_VER}.${PLATFORM} | cut -f1 -d" "| uniq)

    print "$(date +"%D %T"): Starting compiles<br>" >> $COMPERROR
    print "List of COMPS:\n $COMPS" >> $BUILDLOG

# exit

    # Pre LL code ...
    #MACHINELIST=$(print $($GETMACH -sp $GMPLAT -m 2.0 -c $MAX_MACH) " ")
    #print "machines available for build:\n $MACHINELIST" >> $BUILDLOG

    # traverse the complete list of available machines (first pass)
    # thereafter, get one machine at a time, as available
    for COMP in $COMPS
    do

      if [[ $MACHINELIST = " " ]]
      then
	# first pass complete, available machines deployed
	# get the count of complocks, wait until it falls below the maxcount
	until [[ $(/bin/ls -1 $COMPLOCKDIR | wc -l) -lt  $MAX_MACH ]]
	do
	  print "$MAX_MACH components under construction...waiting\n" >> $BUILDLOG
	  sleep $LONGNAP
	done
	# okay, free to assign more component builds 
	sleep $SHORTNAP
	MACHINE=$(getonemach $GMPLAT)
	print "\n result of CM_Getbestmach is $MACHINE" >> $BUILDLOG
	if [[ -z $MACHINE ]]
	then
	  until [[ -n $MACHINE ]]
	  do
	    print "\n Job not submitted  because all machines are busy.Waiting......" >> $BUILDLOG
	    sleep $NAPTIME
	    MACHINE=$(getonemach $GMPLAT)
	  done
	fi
     else # MACHINELIST not null
       # traverse the machinelist and do the first pass
       MACHINE=${MACHINELIST%% *}
       MACHINELIST=${MACHINELIST#* }
       print "FIRST PASS: next available machine is $MACHINE" >> $BUILDLOG
     fi
     
     # turn a $SUBCOMP of /html/etutils to html.etutils
     # for complock names
     COMPFILE=$(print $COMP | sed 's!/!.!g')

     /bin/touch $COMPLOCKDIR/$COMPFILE 
     DATE=$(date +"%D %T")
     print "\*\*\* $COMP is being built on $MACHINE at $DATE" >> $BUILDLOG

     REM_CMD="$SVNCLIB/CM_domake.svn -r $RELEASE -p $PLATFORM -c $COMP $FLEXFLAG"
     if [[ $LEVEL != build ]]
     then
	 REM_CMD="$REM_CMD -l $LEVEL"
     fi
     if [[ $PRODMODE = FALSE ]]
     then
	 REM_CMD="$REM_CMD -d"
     fi
     REM_CMD=$(echo $REM_CMD|sed 's/ /##/g') # allows correct parsing by perl script
     LL_UNIQ=${COMP##*/}

     LL_CMD="$LL_forget -r $R_V -c $PRODUCT -l $LEVEL_STR -p $PLATFORM -e $REM_CMD -u $LL_UNIQ"
     print "Running $LL_CMD" >> $BUILDLOG
     print "Running $LL_CMD"
     $LL_CMD

     # Pre LL code ...
     #if [[ $PRODMODE = FALSE ]]
     #then
     #  print "\nDEVMODE: $RSH $MACHINE $SVNCLIB/CM_domake.svn -d -r ${RELEASE} -p${PLATFORM} -c ${COMP}${FLEXFLAG}&\n" >> $BUILDLOG
     #  $RSH $MACHINE $SVNCLIB/CM_domake.svn -d -r ${RELEASE} -p${PLATFORM} -c ${COMP}${FLEXFLAG}& 
     #else
     #  print "$RSH $MACHINE $SVNCLIB/CM_domake.svn -r ${RELEASE} -p${PLATFORM} -c ${COMP}${FLEXFLAG}&\n" >> $BUILDLOG
     #  $RSH $MACHINE $SVNCLIB/CM_domake.svn -r ${RELEASE} -p${PLATFORM} -c ${COMP}${FLEXFLAG}& 
     #fi
   
  done # for comp in comps

  # wait for the complock directory to empty out
  check_complock

  print "$(date +"%D %T"): End of compiles." | tee -a $BUILDLOG
  print "$(date +"%D %T"): End of compiles.<br>" >> $COMPERROR


if [[ $PRODUCT != @(model|modelutil) ]]
then
  ######################################################################
  #
  # Verify that the libraries have the right object count
  #

  print "$(date +"%D %T"): Verifying the libraries<br>" >> $COMPERROR
  OKTOBUILD=
  typeset -i COUNT=1

  # allow two cycles of verifylibs and rebuild
  while [[ -z $OKTOBUILD && $COUNT -lt 2 ]]
  do
    # -N flag indicates not to delete the libraries
    [[ $COUNT = 1 ]] && FLAG="-N" || FLAG=
    print "\n>>> Verifying the libraries <<<" >> $BUILDLOG
    print "/bin/touch $COMPLOCKDIR/verifylibs" >> $BUILDLOG
    /bin/touch $COMPLOCKDIR/verifylibs
        
    REM_CMD="$SVNCLIB/CM_verifylibs.svn -r $RELEASE -p $PLATFORM $FLAG"
    if [[ $LEVEL != build ]]
    then
	REM_CMD="$REM_CMD -l $LEVEL"
    fi
    if [[ $PRODMODE = FALSE ]]
    then
	REM_CMD="$REM_CMD -d"
    fi
    REM_CMD=$(echo $REM_CMD|sed 's/ /##/g') # allows correct parsing by perl script

    LL_CMD="$LL_results -r $R_V -c $PRODUCT -l $LEVEL_STR -p $PLATFORM -e $REM_CMD"
    print "Running $LL_CMD" >> $BUILDLOG
    print "Running $LL_CMD"
    ll_out=$($LL_CMD)
    print $ll_out >> $BUILDLOG
    print "Verifylibs results ... $ll_out"
    # Pre LL code ...
    #MACHINE=$(getonemach $GMPLAT)
    #if [[ $PRODMODE = FALSE ]]
    #then
    #  print "\nDEVMODE: $RSH $MACHINE $SVNCLIB/CM_verifylibs.svn -d -r$RELEASE -p$PLATFORM $FLAG" >> $BUILDLOG
    #  $RSH $MACHINE $SVNCLIB/CM_verifylibs.svn -d -r$RELEASE -p$PLATFORM $FLAG >> $BUILDLOG 2>&1
    #else
    #  print "$RSH $MACHINE $SVNCLIB/CM_verifylibs.svn -r$RELEASE -p$PLATFORM $FLAG" >> $BUILDLOG
    #  $RSH $MACHINE $SVNCLIB/CM_verifylibs.svn -r$RELEASE -p$PLATFORM $FLAG >> $BUILDLOG 2>&1
    #fi

    check_complock

    print "$(date +"%D %T"): Verify libs has completed<br>" >> $COMPERROR

    if [[ -s $RESULTFILE ]]
    then
      # need to re-try build on some components
      RESULTS=$(sort -t" " +1 -n -r $RESULTFILE | cut -f1 -d" "| uniq)

      # move the verifylibs log over and start a new one
      [[ -a $LOGDIR/verifylibs ]] && mv $LOGDIR/verifylibs $LOGDIR/verifylibs.$COUNT

      #MACHINELIST=$(print $($GETMACH -sp $GMPLAT -m 2.0 -c $MAX_MACH) " ")
      #print "machines available for rebuild:\n $MACHINELIST" >> $BUILDLOG
      print "List of rebuilds:\n $RESULTS" >> $BUILDLOG

      for LINE in $RESULTS
      do
	if [[ $MACHINELIST = " " ]]
	then
	  # first pass on available machines & comps complete
	  # check the complock count and continue when it falls below the maxcount
	  until [[ $(/bin/ls -1 $COMPLOCKDIR | wc -l) -lt  $MAX_MACH ]]
	  do
	    print "$MAX_MACH components under construction...wating\n" >> $BUILDLOG
	    sleep $LONGNAP
	  done # until [[ $(/bin/ls -1 $COMPLOCKDIR | wc -l) -lt  $MAX_MACH ]]
	  # okay to submit more rebuild components
	  sleep $SHORTNAP

	  MACHINE=$(getonemach $GMPLAT)

	else # if [[ $MACHINELIST = " " ]]
	  # traverse the machinelist and do the first pass
	  MACHINE=${MACHINELIST%% *}
	  MACHINELIST=${MACHINELIST#* }
	  print "FIRST PASS: next available machine is $MACHINE" >> $BUILDLOG
	fi # if [[ $MACHINELIST = " " ]]

	#rename the component log and start a new log
	[[ -a $LOGDIR/$LINE ]] && mv $LOGDIR/$LINE $LOGDIR/$LINE.$COUNT
	/bin/touch $COMPLOCKDIR/$LINE

        print "$(date +"%D %T"): Resubmitting $LINE for rebuild." >> $BUILDLOG
        print "$(date +"%D %T"): Resubmitting $LINE for rebuild.<br>" >> $COMPERROR

	REM_CMD="$SVNCLIB/CM_domake.svn -r $RELEASE -p $PLATFORM -c $LINE"
	if [[ $LEVEL != build ]]
	then
	    REM_CMD="$REM_CMD -l $LEVEL"
	fi
	if [[ $PRODMODE = FALSE ]]
	then
	    REM_CMD="$REM_CMD -d"
	fi
	REM_CMD=$(echo $REM_CMD|sed 's/ /##/g') # allows correct parsing by perl script
	LL_UNIQ=${LINE##*/}

	LL_CMD="$LL_forget -r $R_V -c $PRODUCT -l $LEVEL_STR -p $PLATFORM -e $REM_CMD -u $LL_UNIQ"
	print "Running $LL_CMD" >> $BUILDLOG
	print "Running $LL_CMD"
	$LL_CMD

	# Pre LL code ...
        #if [[ $PRODMODE = FALSE ]]
        #then
	#  print "\nDEVMODE: $RSH $MACHINE $SVNCLIB/CM_domake.svn -d -r$RELEASE -p${PLATFORM} -c$LINE " >> $BUILDLOG
	#  $RSH $MACHINE $SVNCLIB/CM_domake.svn -d -r$RELEASE -p${PLATFORM} -c$LINE &
        #else
	#  print "$RSH $MACHINE $SVNCLIB/CM_domake.svn -r$RELEASE -p${PLATFORM} -c$LINE " >> $BUILDLOG
	#  $RSH $MACHINE $SVNCLIB/CM_domake.svn -r$RELEASE -p${PLATFORM} -c$LINE &
        #fi

      done # for $LINE in $RESULTS

      check_complock
      COUNT=$COUNT+1
    else # [[ -z $RESULTFILE ]]
      OKTOBUILD=TRUE
    fi
  done # while [[  -z $OKTOBUILD && $COUNT -lt 2 ]]

  if [[ -z $OKTOBUILD  ]]
   then
    print "$(date +"%D %T"): Libraries were re-built." >> $BUILDLOG
    print "$(date +"%D %T"): Libraries were re-built.<br>" >> $COMPERROR 
  fi
fi # PRODUCT != model|modelutil

  ######################################################################
  #
  # build the binary, if applicable

  for COMP in $COMPLIST
  do
    if [[ $COMP = bin* ]]
    then
      print "/bin/touch $COMPLOCKDIR/$COMP " >> $BUILDLOG
      /bin/touch $COMPLOCKDIR/$COMP
      print "$(date +"%D %T"): Trying to build binaries.<br>" >> $COMPERROR

      REM_CMD="$SVNCLIB/CM_domake.svn -r $RELEASE -p $PLATFORM -c $COMP"
      if [[ $LEVEL != build ]]
      then
	REM_CMD="$REM_CMD -l $LEVEL"
      fi
      if [[ $PRODMODE = FALSE ]]
      then
	  REM_CMD="$REM_CMD -d"
      fi
      REM_CMD=$(echo $REM_CMD|sed 's/ /##/g') # allows correct parsing by perl script

      LL_CMD="$LL_forget -r $R_V -c $PRODUCT -l $LEVEL_STR -p $PLATFORM -e $REM_CMD -u $COMP"
      print "Running $LL_CMD" >> $BUILDLOG
      print "Running $LL_CMD"
      $LL_CMD      

      # Pre LL code ...
      #MACHINE=$(getonemach $GMPLAT)
      #if [[ $PRODMODE = FALSE ]]
      #then
      #  print "\nDEVMODE: $RSH $MACHINE $SVNCLIB/CM_domake.svn -d -r$RELEASE -p${PLATFORM} -c$COMP " >> $BUILDLOG
      #  $RSH $MACHINE $SVNCLIB/CM_domake.svn -d -r$RELEASE -p${PLATFORM} -c$COMP &
      #else
      #  print "$RSH $MACHINE $SVNCLIB/CM_domake.svn -r$RELEASE -p${PLATFORM} -c$COMP " >> $BUILDLOG
      #  $RSH $MACHINE $SVNCLIB/CM_domake.svn -r$RELEASE -p${PLATFORM} -c$COMP &
      #fi

    fi
  done

  check_complock

  print "$(date +"%D %T"): Building binaries has completed.<br>" >> $COMPERROR

  ######################################################################
  #
  # build the dlls, give up after two tries
  #
  OKTOBUILD=
  COUNT=0
  while [[ -z $OKTOBUILD && $COUNT -lt 2 ]]
  do
    print "/bin/touch $COMPLOCKDIR/builddlls " >> $BUILDLOG
    /bin/touch $COMPLOCKDIR/builddlls
    print "$(date +"%D %T"): Building the dlls." >> $BUILDLOG
    print "$(date +"%D %T"): Building the dlls.<br>" >> $COMPERROR

    REM_CMD="$SVNCLIB/CM_builddlls.svn -r $RELEASE -p $PLATFORM"
    if [[ $LEVEL != build ]]
    then
	REM_CMD="$REM_CMD -l $LEVEL"
    fi
    if [[ $PRODMODE = FALSE ]]
    then
	REM_CMD="$REM_CMD -d"
    fi
    REM_CMD=$(echo $REM_CMD|sed 's/ /##/g') # allows correct parsing by perl script

    LL_CMD="$LL_wait -r $R_V -c $PRODUCT -l $LEVEL_STR -p $PLATFORM -e $REM_CMD"
    print "Running $LL_CMD" >> $BUILDLOG
    print "Running $LL_CMD"
    $LL_CMD

    # Pre LL code ...
    #MACHINE=$(getonemach $GMPLAT)
    #if [[ $PRODMODE = FALSE ]]
    #then
    #  print "\nDEVMODE: $RSH $MACHINE $SVNCLIB/CM_builddlls.svn -d -r$RELEASE -p$PLATFORM" >> $BUILDLOG
    #  $RSH $MACHINE $SVNCLIB/CM_builddlls.svn -d -r$RELEASE -p$PLATFORM
    #else
    #  print "$RSH $MACHINE $SVNCLIB/CM_builddlls.svn -r$RELEASE -p$PLATFORM" >> $BUILDLOG
    #  $RSH $MACHINE $SVNCLIB/CM_builddlls.svn -r$RELEASE -p$PLATFORM
    #fi

    $GREP -E -s "Failed|Stop|Error" $LOGDIR/builddlls 2>/dev/null
    if [[ $? = 0 ]] # grep succeeded, found error
    then
      if [[ $COUNT < 2 ]]
      then
	print "dll build failed.  Resubmitting" >> $BUILDLOG

	#rename the component log and start a new log
	[[ -a $LOGDIR/builddlls ]] && mv $LOGDIR/builddlls $LOGDIR/builddlls.$COUNT
      else
	print "dll build failed." >> $BUILDLOG
      fi
      COUNT=$COUNT+1
      
      check_complock
    else # grep failed to find an error
      OKTOBUILD=TRUE
      # Temporary workaround until root cause found
      if [[ $PRODUCT = @(model|modelutil) ]]
      then
        build_DLL_symlinks
      fi
    fi
  done

  ######################################################################
  #
  # Bring up the Application
  [[ $PRODUCT = @(einstimer) ]] && RUN_APP=TRUE 
  if [[ -n $RUN_APP ]] 
  then
    # Remove the einstimer-quant.* files in the .bin-sun directory
    if [[ $PLATFORM = sun4x_5* ]]
    then
      print "Removing any einstimer-quant files" >> $BUILDLOG
      /bin/rm $BUILDDIR/.bin-sun/einstimer-quant.* 2>/dev/null
    fi

    print "Bringing up the $PRODUCT Application" >> $BUILDLOG
    LOGFILE=$LOGDIR/testcases
    touch ${COMPLOCKDIR}/testcases

    if [[ -n $(print $PLATFORM | grep 64) ]]
    then
      BIT="BITS=64" 
      EXT64="-64" 
    fi
    #invoke testcases
    MACHINE=$(getonemach $GMPLAT)

    REM_CMD="export IBMEDA_KEYFILE_PATH=/afs/eda/u/licreprt/keyfiles/edadevfsh_keyfile; $ULIMIT -n 256; $SVNCLIB/CM_domake.svn -r $RELEASE -p $PLATFORM -c testcases "
    if [[ $LEVEL != build ]]
    then
	REM_CMD="$REM_CMD -l $LEVEL"
    fi
    if [[ $PRODMODE = FALSE ]]
    then
	REM_CMD="$REM_CMD -d"
    fi
    REM_CMD=$(echo $REM_CMD|sed 's/ /##/g') # allows correct parsing by perl script

    LL_CMD="$LL_results -r $R_V -c $PRODUCT -l $LEVEL_STR -p $PLATFORM -e $REM_CMD -u testcases"
    print "Running $LL_CMD" >> $BUILDLOG
    print "Running $LL_CMD"
    ll_out=$LL_CMD
    print $ll_out >> $BUILDLOG

    # Pre LL code ...
    #if [[ $PRODMODE = FALSE ]]
    #then
    #  print "\nDEVMODE: $RSH $MACHINE export IBMEDA_KEYFILE_PATH=/afs/eda/u/licreprt/keyfiles/edadevfsh_keyfile; $ULIMIT -n 256; $SVNCLIB/CM_domake.svn -d -r$RELEASE -p${PLATFORM} -ctestcases " >> $BUILDLOG
    #  $RSH $MACHINE "export IBMEDA_KEYFILE_PATH=/afs/eda/u/licreprt/keyfiles/edadevfsh_keyfile; $ULIMIT -n 256; $SVNCLIB/CM_domake.svn -d -r$RELEASE -p${PLATFORM} -ctestcases " >> $BUILDLOG 2>&1
    #else
    #  print "$RSH $MACHINE export IBMEDA_KEYFILE_PATH=/afs/eda/u/licreprt/keyfiles/edadevfsh_keyfile; $ULIMIT -n 256; $SVNCLIB/CM_domake.svn -r$RELEASE -p${PLATFORM} -ctestcases " >> $BUILDLOG
    #  $RSH $MACHINE "export IBMEDA_KEYFILE_PATH=/afs/eda/u/licreprt/keyfiles/edadevfsh_keyfile; $ULIMIT -n 256; $SVNCLIB/CM_domake.svn -r$RELEASE -p${PLATFORM} -ctestcases " >> $BUILDLOG 2>&1
    #fi

    /bin/rm ${COMPLOCKDIR}/testcases >> $LOGFILE 2>/dev/null

    grep -Ei "_run\] error" $LOGFILE >> $BUILDLOG 2>&1
    if [[ $? = 0 ]]  # grep succeeded in detecting a failure
    then
      print "$(date +"%D %T"): $PRODUCT run failed.<br>" >> $COMPERROR
    else
      # REGRESSION=TRUE
      print "$PRODUCT ran successfully" >> $BUILDLOG
    fi
  fi  # [[ -n $RUN_APP ]]

fi # end DO_COMPILE

if [[ -n $DO_REGRESSION && -n $DO_COMPILE && $LEVEL = build ]]
then
  # checkbuild if both compile and regression
  # abort if build failure
  print "$(date +"%D %T"): Checking the build.<br>" >> $COMPERROR
  if [[ $PRODMODE = FALSE ]]
  then
    DEVSWITCH="-d"
  else
    DEVSWITCH=""
  fi
  chkbCmd="$SVNCLIB/CM_checkbuild.svn $DEVSWITCH -r $RELEASE -p $PLATFORM -t $TKVERSION -C"
  print "\n$chkbCmd" >> $BUILDLOG
  $chkbCmd 2>&1 >> $BUILDLOG

  if [[ $? != 0 ]]  # checkbuild returned non-zero 
  then
    print "$(date +"%D %T"): Build failure..regression aborted" >> $BUILDLOG

    if [[ $PRODUCT = @(einstimer|ess|cre|spam|echk|tdnoise) ]]
    then
      print "re-generating frames file for html" >> $BUILDLOG
      $EINSLIB/bin/genframe.pl -r $RELEASE -p $PLATFORM -f $ETCDIR/cmperr.$PLATFORM.html
    fi
    exit
  else 
    # build succeeded and if build is einstimer.1401 touch a file so 1400 can proceed
    if [[ $PRODUCT = einstimer && $R_V = 14.1 ]]
    then
      touch $COMPILE_OK
    fi
  fi

  # put in a delay for linux if compiling and running regression 
  # delay is present to help reduce builddll/regression time overlap 
  # due to inaccurate timekeeping on public machines
  if [[ $PLATFORM = linux* ]]
    then
      sleep 100
  fi
fi

######################################################################
#
# Start the Regression

if [[ -n $DO_REGRESSION ]]
then
  # einstimer.1400 needs to wait for a COMPILE_OK file
  if [[ $PRODUCT = einstimer && $R_V = 14.0 ]]
    then
      while [[ ! -a $COMPILE_OK  ]]
      do
	print "$(date +"%D %T") waiting for 14.1 compile to complete" >> $BUILDLOG
        print "  -> COMPILE_OK = $COMPILE_OK" >> $BUILDLOG
	sleep $LONGNAP
      done
  fi
  # set web page to running regression, write to underlying page
  if [[ -z $NO_WEB ]]
  then
    if [[ $PRODMODE = FALSE ]]
    then
      print "\nDEVMODE: CM_BuildStatus not being run for REGRESSIONS" >> $BUILDLOG
    else
      if [[ $LEVEL = build ]]
      then 
        print "$HDPLIB/bin/CM_BuildStatus -r $RELEASE -p $PLATFORM -l $LEVEL -R" >> $BUILDLOG 
        $HDPLIB/bin/CM_BuildStatus -r$RELEASE -p$PLATFORM -l $LEVEL -R >> $BUILDLOG 2>&1
      fi
    fi
  fi
  print "<html>" > $COMPERROR
  print "<xmp>" >> $COMPERROR
  print "$(date +"%D %T"): Regression in progress..............\n<br>" >> $COMPERROR
  
  # Pre LL code ...
  # get a list of machines available to run regression
  #MACHINELIST=$(print $($GETMACH -sp $GMREGPLAT -m 2.0 -c $MAX_MACH) " ")
  #print "$(date +"%D %T") machines available for build:\n $MACHINELIST" >> $BUILDLOG

  REGLIST="$(cat $REGRESSIONDIR/bin/${REGFILE}.reglist)"
  # print "${REGRESSIONDIR}/bin/${REGFILE}.reglist"  >> $BUILDLOG  #DWM debug
  print "the regression list is:\n $REGLIST" >> $BUILDLOG

  # clean up platform specific regression areac
  if [[ -d ${REGRESSIONDIR}/results/$USER/${LEVEL}/$REGPLAT ]]
  then
    print "/bin/rm -fr ${REGRESSIONDIR}/results/$USER/$LEVEL/$REGPLAT >> $BUILDLOG 2>&1" >> $BUILDLOG
    /bin/rm -fr ${REGRESSIONDIR}/results/$USER/$LEVEL/$REGPLAT >> $BUILDLOG 2>&1
  else 
    if [[ -d ${REGRESSIONDIR}/results/$USER/$REGPLAT ]]
    then
      print "/bin/rm -fr ${REGRESSIONDIR}/results/$USER/$REGPLAT >> $BUILDLOG 2>&1" >> $BUILDLOG
      /bin/rm -fr ${REGRESSIONDIR}/results/$USER/$REGPLAT >> $BUILDLOG 2>&1
    fi
  fi

  # change the web page to "Regression Running"
  #  print "$HDPLIB/bin/CM_BuildStatus -r $RELEASE -p $PLATFORM -l $LEVEL -R" >> $BUILDLOG
  #  $HDPLIB/bin/CM_BuildStatus -r $RELEASE -p $PLATFORM -l  $LEVEL -R >> $BUILDLOG 2>&1
  

    for LINE in $REGLIST
    do
      print "next regression is: $LINE" >> $BUILDLOG
      if [[ $LINE != "" ]]
      then
	if [[ $MACHINELIST = " " ]]
	then
	  # first pass complete, available machines deployed
	  # get the count of complocks, wait until it falls below the maxcount
	  until [[ $(/bin/ls -1 $COMPLOCKDIR | wc -l) -lt  $MAX_MACH ]]
	  do
	    print "$MAX_MACH tests getting regressed...waiting\n" >> $BUILDLOG
	    sleep $LONGNAP
	  done
	  # okay, free to assign more component builds 
	  sleep $NAPTIME
	  MACHINE=$(getonemach $GMREGPLAT)
	  print "\n result of CM_Getbestmach is $MACHINE" >> $BUILDLOG
	else
	  # traverse the machinelist and do the first pass
	  MACHINE=${MACHINELIST%% *}
	  MACHINELIST=${MACHINELIST#* }

	  print "FIRST PASS: next available machine is $MACHINE\n" >> $BUILDLOG
	fi

	# we have a machine, submit testcase
	print "Running the $LINE testcase" >> $BUILDLOG
	export LOGFILE=$LOGDIR/$LINE
	/bin/touch $COMPLOCKDIR/$LINE 
	
	print "\n\*\*\* $LINE is being run on $MACHINE" >> $BUILDLOG
	
	# copy a password file if it isn't there already
	# /usr/bin/rcp /tmp/local/einslib $MACHINE:/tmp/local/einslib 2>&1 >> $BUILDLOG
	
	REM_CMD="$EINSLIB/bin/ET_runregS -r $RELEASE -p $PLATFORM -t $LINE"
	if [[ $LEVEL != build ]]
        then
	    REM_CMD="$REM_CMD -l $LEVEL"
	fi
	if [[ $PRODMODE = FALSE ]]
	then
	    REM_CMD="$REM_CMD -d"
	fi
	REM_CMD=$(echo $REM_CMD|sed 's/ /##/g') # allows correct parsing by perl script

	LL_CMD="$LL_forget -r $R_V -c $PRODUCT -l $LEVEL_STR -p $PLATFORM -e $REM_CMD -u $LINE"
	print "Running $LL_CMD" >> $BUILDLOG
	print "Running $LL_CMD"
	$LL_CMD	

	# Pre LL code ...
	#print "$RSH $MACHINE $EINSLIB/bin/ET_runregS -r $RELEASE -p $PLATFORM -t ${LINE}" >> $BUILDLOG
	#$RSH $MACHINE $EINSLIB/bin/ET_runregS -r $RELEASE -p $PLATFORM -t ${LINE}&

      fi #if [[ $LINE != "" ]]
    done #for LINE in $REGLIST

    check_complock
    print "$(date +"%D %T"): End of regression." >> $BUILDLOG
    print "$(date +"%D %T"): End of regression<br>" >> $COMPERROR
    print "$(date +"%D %T"): Regression has completed." >> $BUILDLOG
    print "$(date +"%D %T"): Regression has completed.<br>" >> $COMPERROR

fi # end DO_regression

######################################################################
#
# Post build processing

# Skip check build process if not in build
if [[ $LEVEL != build ]]
then
  print "Exitting w/out running CM_checkbuild.svn ($LEVEL) ..." >> $BUILDLOG
  print "Exitting w/out running CM_checkbuild.svn ($LEVEL) ..."
  goodbye
fi


# provide -N flag to turn off email, flexflag to check for flexlint messages
CHECKFLAG="-N $FLEXFLAG"

if [[ -n $DO_COMPILE ]]
then
  CHECKFLAG="$CHECKFLAG -C"
fi
if [[ -n $DO_REGRESSION ]]
then
  CHECKFLAG="$CHECKFLAG -R"
fi

print "$(date +"%D %T"): Checking the build.<br>" >> $COMPERROR
if [[ $PRODMODE = FALSE ]]
then
  DEVSWITCH="-d"
else
  DEVSWITCH=""
fi
chkbCmd="$SVNCLIB/CM_checkbuild.svn $DEVSWITCH -r $RELEASE -p $PLATFORM -t $TKVERSION $CHECKFLAG"
print "\n$chkbCmd" >> $BUILDLOG
$chkbCmd 2>&1 >> $BUILDLOG

if [[ $? = 0 && ! -n $DO_REGRESSION ]]  # checkbuild returned zero (compile succeeded)
then
  touch $COMPILE_OK
fi

# generate a frames file if ess or einstimer, or anything else
if [[ $PRODUCT = @(einstimer|ess|cre|spam|echk|tdnoise) ]]
then
  print "re-generating frames file for html" >> $BUILDLOG
  $EINSLIB/bin/genframe.pl -r $RELEASE -p $PLATFORM -f $ETCDIR/cmperr.$PLATFORM.html
fi

#~hdplib/bin/CM_cr2br $COMPERROR
goodbye


