#!/usr/bin/ksh

# Functions
function nightly_build_custom_file
{
  product=$1
  release=$2
  log_file=$3

  /afs/eda/u/$USER/bin/bnr/nightly_build_custom_file.ksh $product $release $log_file linux
}

function build_gcov_beam
{
  product=$1
  release=$2
  log_file=$3

  echo "[BNR]: Changing directory to build tree top..." >> $log_file
  cd /afs/eda/build/$product/$release
  echo "[BNR]: Beginning coverage build..." >> $log_file
  make BITS=64 gcov
  echo "[BNR]: Done coverage build." >> $log_file
  echo "[BNR]: Beginning BEAM build..." >> $log_file
  make BITS=64 beam
  echo "[BNR]: Done BEAM build." >> $log_file
}

function execute_on_aix
{
  product=$1
  release=$2
  email=$3
  log_file=$4
  jobs_dir=/afs/eda/u/$USER/bin/bnr
  jobs_file=$jobs_dir/submit.on.aix.$product

  echo "# @ class = 8hour" > $jobs_file
  echo "# @ requirements = (OpSys == \"AIX61\")" >> $jobs_file
  echo "# @ account_no = EDA" >> $jobs_file
  echo "# @ group = enablement" >> $jobs_file
  echo "# @ notification = always" >> $jobs_file
  echo "# @ notify_user = $email" >> $jobs_file
  echo "# @ executable = $jobs_dir/on.aix.ksh" >> $jobs_file
  echo "# @ arguments = $product $release \"$email\"" >> $jobs_file
  echo "# @ output = $jobs_dir/submit.aix.out.$product" >> $jobs_file
  echo "# @ error = $jobs_dir/submit.aix.err.$product" >> $jobs_file
  echo "# @ queue" >> $jobs_file

  llsubmit $jobs_file
  echo "[BNR]: Submitted jobs on AIX." >> $log_file
}

# Global variables.
PRODUCT=$1
RELEASE=$2
EMAIL=$3
DATE=`date +%d`
MONTH=`date +%b`
YEAR=`date +%Y`
mkdir -p "/afs/eda/u/$USER/logs/bnr/$MONTH.$YEAR"
LOG_FILE="/afs/eda/u/$USER/logs/bnr/$MONTH.$YEAR/$PRODUCT.$RELEASE.linux.$DATE"

# Global calls
echo "[BNR]: Start of Linux26 machine logs..." > $LOG_FILE
#
#run_hdp_regression $PRODUCT $RELEASE $LOG_FILE
nightly_build_custom_file $PRODUCT $RELEASE $LOG_FILE
build_gcov_beam $PRODUCT $RELEASE $LOG_FILE
execute_on_aix $PRODUCT $RELEASE "$EMAIL" $LOG_FILE
echo "[BNR]: End of Linux26 machine logs." >> $LOG_FILE
