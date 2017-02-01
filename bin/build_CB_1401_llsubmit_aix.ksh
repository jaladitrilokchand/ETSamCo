#!/usr/bin/ksh

# Functions
function nightly_build_custom_file 
{
  product=$1
  release=$2
  log_file=$3

  /afs/eda/u/$USER/bin/bnr/nightly_build_custom_file.ksh $product $release $log_file aix
}

function build_beam_lint
{
  product=$1
  release=$2
  log_file=$3

  echo "[BNR]: Changing directory to build tree top..." >> $log_file
  cd /afs/eda/build/$product/$release
  echo "[BNR]: Beginning BEAM build..." >> $log_file
  make BITS=64 beam
  echo "[BNR]: Done BEAM build." >> $log_file
  echo "[BNR]: Beginning lint build..." >> $log_file
  make BITS=32 lint
  echo "[BNR]: Done lint build." >> $log_file
}

function promote_build_to_dev
{
  product=$1
  release=$2
  email=$3
  log_file=$4

  if [[ $product = "hdp" || $product = "model" || $product = "modelio" || $product = "modelutil" ]]; then
    path=/afs/eda/build/$product/$release/build/complaints
  else
    path=/afs/eda/build/$product/$release
  fi

  today=`date +%Y-%M-%d`
  last_error=""
  if [[ -f $path/build_errors* ]]; then
    latest=`ls -tr $path/build_errors* | tail -1`
    last_error=`stat -c %y $latest | cut -d ' ' -f1`
  fi
  if [[ $last_error -ne $today ]]; then
    echo "[BNR]: Promoting $product/$release to dev..." >> $log_file
    cd /afs/eda/build/$product/$release/build/bin-64-amd64_linux26
    /afs/eda/build/$product/$release/build/bin-64-amd64_linux26/BuildStatus -FBp_ALL_
    /afs/eda/build/$product/$release/build/bin-64-amd64_linux26/BuildStatus -F
    cd /afs/eda/build/$product/$release/build/bin-64-rs_aix61
    /afs/eda/build/$product/$release/build/bin-64-rs_aix61/BuildStatus -F

# Weirdly, this chunk of code which sends the e-mail has to start from column 0.
# If it does not, an error will be issued while executing the file!
mail -s "Successfully promoted $product/$release to dev" $email <<EOF
Notification e-mail
EOF

    echo "[HBR]: Successfully promoted $product/$release to dev" >> $log_file
  else

# Weirdly, this chunk of code which sends the mail has to start from column 0.
# If it does not, an error will be issued while executing the file!
mail -s "Error while promoting $product/$release to dev" $email <<EOF
Notification e-mail
EOF

    echo "[BNR]: Error while promoting $product/$release to dev" >> $log_file
  fi
}

# Global variables.
PRODUCT=$1
RELEASE=$2
EMAIL=$3
DATE=`date +%d`
MONTH=`date +%b`
YEAR=`date +%Y`
mkdir -p "/afs/eda/u/$USER/logs/bnr/$MONTH.$YEAR"
LOG_FILE="/afs/eda/u/$USER/logs/bnr/$MONTH.$YEAR/$PRODUCT.$RELEASE.aix.$DATE"

# Global calls
echo "[BNR]: Start of AIX61 machine logs..." > $LOG_FILE
#run_hdp_regression $PRODUCT
nightly_build_custom_file $PRODUCT $RELEASE $LOG_FILE
build_beam_lint $PRODUCT $RELEASE $LOG_FILE
promote_build_to_dev $PRODUCT $RELEASE "$EMAIL" $LOG_FILE
echo "[BNR]: End of AIX61 machine logs." >> $LOG_FILE
