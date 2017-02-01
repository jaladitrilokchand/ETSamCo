#!/usr/bin/perl
#==============================================================================
#
#    FILE: LL_ET.pm
#
#-PURPOSE----------------------------------------------------------------------
# Simple Load Leveler perl module used for einstimer and family
# compiles and regression tests.
#
# Files are written to ...
#   /afs/eda/data/edainfra/LL/$release/$component/$location
#   ie, /afs/eda/data/edainfra/LL/1401/einstimer/build
#
#------------------------------------------------------------------------------
#
#-CHANGE LOG-------------------------------------------------------------------
# 06/28/2012 GFS  Initial coding.
# 10/03/2012 GFS  Updated LL class to 4hour
# 10/19/2012 GFS  Updated CPUs to 4 for kalafala test cases.
# 11/09/2012 GFS  Updated LL class to 8hour
# 12/31/2012 GFS  Updated the memory to 32 gb for kalafala2 reg tests.
# 04/12/2013 GFS  Updated the memory to 12 gb for pds reg tests.
#==============================================================================

package LL_ET;

use strict;
use warnings;
use File::Path qw(mkpath);


#------------------------------------------------------------------------------
# Perl module configuration
#------------------------------------------------------------------------------
use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);
require Exporter;

@ISA = qw(Exporter AutoLoader);
# Items to export into callers namespace by default. Note: do not export
# names by default without a very good reason. Use EXPORT_OK instead.
# Do not simply export all your public functions/methods/constants.
@EXPORT = qw();

$VERSION = '1.0';


#------------------------------------------------------------------------------
# Globals
#------------------------------------------------------------------------------
my $OK = 0;
my $ERROR = 8;
my $TRUE = 1;
my $FALSE = 0;
my $entry = "";
my @LOCS = ( "build", "shipb", "tkb",  "xtinct" );
my @PLATS = ( "AIX", "LINUX" );
my $LL_DIR = "/afs/eda/data/edainfra/LL";
my $LL_BIN = "/afs/eda/tools/bin";


#------------------------------------------------------------------------------
# Name: getPlatforms()
# Purpose: Return a list of valid platforms
# RCs: n/a
#------------------------------------------------------------------------------
sub getPlatforms {

  return join(" ", @PLATS);

}


#------------------------------------------------------------------------------
# Name: getLocations()
# Purpose: Return a list of valid locations
# RCs: n/a
#------------------------------------------------------------------------------
sub getLocations {

  return join(" ", @LOCS);

}


#------------------------------------------------------------------------------
# Name: validatePlatform(platform)
# Purpose: Verifies the platform is aix or linux
# RCs: 1 = true
#      0 = false
#------------------------------------------------------------------------------
sub validatePlatform {
	
  my($plat) = @_;
  $plat = uc($plat);

  my $rc = $FALSE;
  foreach $entry (@PLATS) {
    $rc = $TRUE if ($entry eq $plat);
  }

  return $rc;

}


#------------------------------------------------------------------------------
# Name: setLlPlatform($platform, $plat_ref)
# Purpose: Converts the build/compile platform name to LL platform name
# RCs: n/a
#------------------------------------------------------------------------------
sub setLlPlatform {
	
  my($plat, $plat_ref) = @_;
  $plat = lc($plat);

  if ($plat =~ /aix/) {
    $$plat_ref = "AIX";
  }
  else {
    $$plat_ref = "LINUX";
  }

}


#------------------------------------------------------------------------------
# Name: validateLocation(location))
# Purpose: Verifies the locatin is build, shipb, tkb or xtinct
# RCs: 1 = true
#      0 = false
#------------------------------------------------------------------------------
sub validateLocation {
	
  my($loc) = @_;
  $loc = lc($loc);

  my $rc = $FALSE;
  foreach $entry (@LOCS) {
    $rc = $TRUE if ($entry eq $loc);
  }

  return $rc;

}


#------------------------------------------------------------------------------
# Name: getLlDir($appName, $release, $component, $location, $platform);
# Purpose: Returns the directory to store the LL files
# RCs: n/a
#------------------------------------------------------------------------------
sub getLlDir {

  my($name, $rel, $comp, $loc, $plat) = @_;

  my $dir = "$LL_DIR/$rel/$comp/$loc";
  mkpath($dir) if (! -e $dir);

  return $dir;

}


#------------------------------------------------------------------------------
# Name: getLlFile($appName, $release, $component, $location, $platform,
#                 $jobId, $extentsion);
# Purpose: Returns the full path to input, error, output or ll file
# RCs: n/a
#------------------------------------------------------------------------------
sub getFilePath {

  my($app, $rel, $comp, $loc, $plat, $id, $ext) = @_;

  my $dir = getLlDir($app, $rel, $comp, $loc, $plat);
  my $file = getUserName() . "_" . $app . "_" . "$id.$ext";

  return "$dir/$file";

}


#------------------------------------------------------------------------------
# Name: getErrFile($appName, $release, $component, $location, $platform,
#                  $jobId");
# Purpose: Returns the full path to the error file
# RCs: n/a
#------------------------------------------------------------------------------
sub getErrFile {

  my($app, $rel, $comp, $loc, $plat, $id) = @_;
  return getFilePath($app, $rel, $comp, $loc, $plat, $id, "err");

}


#------------------------------------------------------------------------------
# Name: getOutFile($appName, $release, $component, $location, $platform,
#                  $jobId");
# Purpose: Returns the full path to the output file
# RCs: n/a
#------------------------------------------------------------------------------
sub getOutFile {

  my($app, $rel, $comp, $loc, $plat, $id) = @_;
  return getFilePath($app, $rel, $comp, $loc, $plat, $id, "out");

}


#------------------------------------------------------------------------------
# Name: getInFile($appName, $release, $component, $location, $platform
#                 $jobId");
# Purpose: Returns the full path to the LL input file
# RCs: n/a
#------------------------------------------------------------------------------
sub getInFile {

  my($app, $rel, $comp, $loc, $plat, $id) = @_;
  return getFilePath($app, $rel, $comp, $loc, $plat, $id, "in");

}


#------------------------------------------------------------------------------
# Name: getJobFile($appName, $release, $component, $location, $platform,
#                  $jobId");
# Purpose: Returns the full path to the LL job file
# RCs: n/a
#------------------------------------------------------------------------------
sub getJobFile {

  my($app, $rel, $comp, $loc, $plat, $id) = @_;
  return getFilePath($app, $rel, $comp, $loc, $plat, $id, "job");

}


#------------------------------------------------------------------------------
# Name: getJobName($appName, $release, $component, $loc, $plat, $jobId)
# Purpose: Creates the LL job name
# RCs: n/a
#------------------------------------------------------------------------------
sub getJobName {

  my($app, $rel, $comp, $loc, $plat, $id) = @_;
  return "$rel.$comp.$loc.$plat." . getUserName() . ".$app.$id";

}


#------------------------------------------------------------------------------
# Name: getUser()
# Purpose: Returns the user name
# RCs: n/a
#------------------------------------------------------------------------------
sub getUserName {

  return $ENV{LOGNAME} || $ENV{USER} || getpwuid($<);

}


#------------------------------------------------------------------------------
# Name: loadJobFile($release, $component, $location, $platform,
#                   $app, $args, $initDir, $jobId)
# Purpose: Creates the LL job file
# RCs: n/a
#------------------------------------------------------------------------------
sub loadJobFile {

  my($rel, $comp, $loc, $plat, $app, $args, $initDir, $id) = @_;

  # Get the app name from llApp
  my @tokens = split(/\//, $app);
  my $appName = pop @tokens;

  # Alter number of CPUs for specific regression tests
  my $cpus = 1;
  if ($rel eq "1401" && $comp eq "einstimer") {
    $cpus = 8 if ($id =~ /kalafala/);
  }
  if ($rel eq "1400" && $comp eq "einstimer") {
    if ($plat =~ /aix/ || $plat =~ /AIX/ ) {
      # set cpu to 3 to reduce the number of tlt jobs run simultaneously on AIX machines
      $cpus = 3;
    }
  }

  # Alter the memory for specific regression tests
  my $mem = "2 gb";
  if ($rel eq "1401" && $comp eq "einstimer") {
    $mem = "32 gb" if ($id =~ /kalafala2/);
    $mem = "36 gb" if ($id =~ /pds/);
    $mem = "32 gb" if ($id =~ /logos/);
  }

  # Set some parameters
  my $llClass = "8hour";
  my $llGroup = "timing";
  my $llAccount = "EDA";

  my $jobName = getJobName($appName, $rel, $comp, $loc, $plat, $id);
  my $outFile = getOutFile($appName, $rel, $comp, $loc, $plat, $id);
  my $errFile = getErrFile($appName, $rel, $comp, $loc, $plat, $id);

  # Prepare the LL job file
  my $ll = "";
  $ll .= "# --------------------------------------------------------------#\n";
  $ll .= "# Parameters for load leveler\n";
  $ll .= "# --------------------------------------------------------------#\n";
  $ll .= "# @ environment  = COPY_ALL; LoadL_Job_Id=$jobName\n";
  $ll .= "# @ input        = /dev/null\n";
  $ll .= "# @ output       = $outFile\n";
  $ll .= "# @ error        = $errFile\n";
  $ll .= "# @ initialdir   = $initDir\n" if ($initDir ne "");
  $ll .= "# @ requirements = (OpSys == \"$plat\") && (Feature == \"eda14.1_build\")\n";
  $ll .= "# @ preferences  = (Speed >= 5)\n";
  $ll .= "# @ resources    = ConsumableCpus($cpus) ConsumableMemory($mem) \n";
  $ll .= "# @ notify_user  = piaget\@us.ibm.com\n";
  $ll .= "# @ account_no   = $llAccount\n";
  $ll .= "# @ group        = $llGroup\n";
  $ll .= "# @ class        = $llClass\n";
  $ll .= "# @ job_name     = $jobName\n";
  #$ll .= "# @ step_name    = step1\n";
  $ll .= "# @ notification = error\n";
  $ll .= "# @ Cpu_Limit    = unlimited\n";
  $ll .= "# @ restart      = no\n";
  $ll .= "# @ executable   = $app\n";
  $ll .= "# @ arguments    = $args\n";
  $ll .= "# @ queue\n";

  # Write the LL job file
  my $file =  getJobFile($appName, $rel, $comp, $loc, $plat, $id);
  open(FH, ">$file") or die $!;
  print FH $ll;
  close(FH) or die $!;

  return $file;

}


#------------------------------------------------------------------------------
# Name: submit($jobFile, \$jobRef)
# Purpose: Submits the LL job and set jobRef to the LL job number
# RCs: n/a
#------------------------------------------------------------------------------
sub submit {

  my($file, $job_ref) = @_;
  my $command = "$LL_BIN/llsubmit $file 2>1";
  my @results = `$command`;

  $$job_ref = "";
  foreach $entry (@results) {
    #print "[submit] $entry\n";
    if (($entry =~ /llsubmit:/) && ($entry =~ /submitted/)) {
      my @tokens = split(/\"/, $entry);
      $$job_ref = $tokens[1];
    }
  }

}


#------------------------------------------------------------------------------
# Name: readOutput($outFile)
# Purpose: Returns the contents of the output file as a string
# RCs: n/a
#------------------------------------------------------------------------------
sub readOutput {

  my($outFile, $results_ref) = @_;

  # Read output file
  open(FH, "$outFile") or die $!;
  my @contents = <FH>;
  close(FH);
  #print "Reading ... $outFile\n";
  #print " Line count ... " . scalar(@contents) . "\n";

  foreach $entry (@contents) {
    #print $entry;
    $$results_ref .= $entry if ($entry !~ /YOU HAVE NEW MAIL/);
  }

}


#------------------------------------------------------------------------------
# Name: getStatus($job, \$status)
# Purpose: Returns the status of the job in the status reference
# RCs: n/a
#------------------------------------------------------------------------------
sub setStatus {

  my($job, $status_ref) = @_;

  my @results = `$LL_BIN/llq $job`;

  my $dash = 0;
  foreach $entry (@results) {
    #print "[getStatus] $entry\n";
    if ($entry =~ /no job status/) {
      $$status_ref = "";
      last;
    }
    else {
      if ($dash) {
	my @tokens = split(/ +/, $entry);
	$$status_ref = $tokens[4];
	last;
      }
      elsif ($entry =~ /^--/) {
	$dash = 1;
      }
    }
  }

  #print "Status: $$status_ref\n";

}


#------------------------------------------------------------------------------
# Name: showJobFile($jobFile)
# Purpose: Displays the contents of the job file
# RCs: n/a
#------------------------------------------------------------------------------
sub showJobFile {

  my($file) = @_;
  open(FH, "$file") or die $!;
  my @contents = <FH>;
  close(FH);

  print join("", @contents, "\n");

}


#------------------------------------------------------------------------------
# Name: getDotRelease($rel, $rel_ref)
# Purpose: Sets ref_ref to the "dot" release name (ie converts 1401 to 14.1)
# RCs: n/a
#------------------------------------------------------------------------------
sub setDotRelease {

  my($rel, $rel_ref) = @_;

  my @tokens = split(//, $rel);
  if ($tokens[2] eq ".") {
    $$rel_ref = $rel;
  }
  else {
    $$rel_ref = $tokens[0] . $tokens[1] . "." . $tokens[3];
  }

}

#------------------------------------------------------------------------------
# Name: getZeroRelease($rel, $rel_ref)
# Purpose: Sets ref_ref to the zero release name (ie converts 14.1 to 1401)
# RCs: n/a
#------------------------------------------------------------------------------
sub setZeroRelease {

  my($rel, $rel_ref) = @_;

  my @tokens = split(//, $rel);
  if ($tokens[2] eq "0") {
    $$rel_ref = $rel;
  }
  else {
    $$rel_ref = $tokens[0] . $tokens[1] . "0" . $tokens[3];
  }

}


#------------------------------------------------------------------------------
# Name: parseCommand($command, $exec_ref, $args_ref, $name_ref))
# Purpose: Parses the command to determine the full executable name
#          ($exec_ref), complete argument string ($args_ref) and
#          executable name ($name_ref).
# RCs: n/a
#------------------------------------------------------------------------------
sub parseCommand {

  my($command, $exec_ref, $args_ref, $name_ref) = @_;

  my $index = index($command, " ");
  if ($index == -1) {
    # no args
    $$exec_ref = $command;
    $$args_ref = "";
  }
  else {
    $$exec_ref = substr($command, 0, $index);
    $$args_ref = substr($command, $index + 1);
  }

  my @tokens = split(/\//, $$exec_ref);
  $$name_ref = pop(@tokens);

}


1;
__END__
