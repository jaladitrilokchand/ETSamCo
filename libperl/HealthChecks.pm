#!/usr/bin/perl
#==============================================================================
#
#    FILE: HealthChecks.pm
#
#-PURPOSE----------------------------------------------------------------------
# Health Check utilies.
#------------------------------------------------------------------------------
#
#-CHANGE LOG-------------------------------------------------------------------
# 02/19/2013 GFS  Initial coding.
#==============================================================================

package HealthChecks;

use strict;
use warnings;
use File::Path qw(mkpath);

# Define custom perl libs path and modules
use lib '/afs/eda/data/edainfra/tools/enablement/dev/libperl';
use IcofFile;
use TKutils;


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
my $DATA_DIR = "/afs/eda/data/edainfra/tools/enablement/dev/data";
my $BIN = "/afs/eda/data/edainfra/tools/enablement/prod/bin";


#------------------------------------------------------------------------------
# Name   : GetDataFile($release, $location)
# Purpose: Create the data file name
# RCs    : data file name
#------------------------------------------------------------------------------
sub GetDataFile {

  my ($rel, $loc) = @_;
  return "$DATA_DIR//HealthCheck_" . $rel . "_" . $loc . ".data";

}


#------------------------------------------------------------------------------
# Name   : ReadDataFile($file, \@contents)
# Purpose: Reads the Health Check data file and puts file contents into
#          contents ref list
# RCs    : $OK or $ERROR
#------------------------------------------------------------------------------
sub ReadDataFile {

  my ($file, $refContents) = @_;

  if (! IcofFile::ReadFileToList($file, $refContents, 0)) {
    print "ERROR: unable to read Health Chech data file\n";
    print "       File: $file\n";
    return $ERROR;
  }

  return $OK

}


#------------------------------------------------------------------------------
# Name   : GetToolKit($release, $location, $alt, $ref_tk)
# Purpose: Loads $ref_tk with the Tool Kit version for this release/location
# RCs    : $OK or $ERROR
#------------------------------------------------------------------------------
sub GetToolKit {

  my ($rel, $loc, $alt, $ref_tk) = @_;

  # Determine the stage from the location
  my $stage = "DEVELOPMENT";
  $stage = "PREVIEW" if ($loc eq "shipb");
  $stage = "PRODUCTION" if ($loc eq "tkb");

  # Construct the command
  my $command = "$BIN/getToolKits";
  $command .= " -r $rel";
  $command .= " -s $stage";
  $command .= " -q";
  $command .= " -alt" if ($alt == 1);

  # Run the command
  my @results;
  my $rc = TKutils::RunCommand($command, \@results, 0, 0);
  if ($rc != $OK) {
    print "ERROR: unable to update database\n";
    print "       Command: $command\n";
    return $ERROR;
  }

  # Process the results
  $$ref_tk = $results[0];
  chomp $$ref_tk;

  return $OK;

}


#------------------------------------------------------------------------------
# Name   : GetField($field, \@contents, \$value)
# Purpose: Reads the line starting with $field and saves the value to $value
# RCs    : $OK or $ERROR
#------------------------------------------------------------------------------
sub GetField {

  my ($field, $refContents, $refValue) = @_;

  my @list = grep(/^$field;/, @$refContents);
  if (scalar(@list) > 0) {
    my @tokens = split(";", $list[0]);
    $$refValue = $tokens[1];
  }
  else {
    $$refValue = "";
    return $ERROR;
  }

  return $OK

}


#------------------------------------------------------------------------------
# Name   : GetLegend(\@contents, \%legend)
# Purpose: Reads the LEGEND from the contents and set legend collection
# RCs    : $OK or $ERROR
#------------------------------------------------------------------------------
sub GetLegend {

  my ($refContents, $refLegend) = @_;

  my $entry;
  my @list = grep(/^LEGEND;/, @$refContents);
  foreach $entry (@list) {
    my @tokens = split(";", $entry);
    my $key = $tokens[1];
    my $value = $tokens[2];
    $$refLegend{$key} = $value;
  }

  if (scalar(@list) < 1) {
    return $ERROR;
  }

  return $OK

}


#------------------------------------------------------------------------------
# Name   : GetChecks(\@contents, \%help, \%owners, \%mgrs, \@checks)
# Purpose: Reads the CHECKS from the contents and set collections
# RCs    : $OK or $ERROR
#------------------------------------------------------------------------------
sub GetChecks {

  my ($refContents, $refHelp, $refOwners, $refMgrs, $refChecks) = @_;

  my $entry;
  foreach $entry (@$refContents) {

    # Process only check entries
    next if ($entry !~ /^CHECK;/);

    my @tokens = split(/;/, $entry);
    my $checkName = $tokens[1];
    my $colText = $tokens[2];
    my $colUrl = $tokens[3];
    my $processThisCheck = $tokens[5];

    # Skip checks that should not be in the report
    next if (uc($processThisCheck) eq "NO");

    $$refHelp{$checkName} = "$colText;$colUrl";
    push(@$refChecks, $checkName); # Save order so table has the same order

    # Determine if owners and/or managers should be notified
    $$refOwners{$checkName} = $tokens[6];
    $$refMgrs{$checkName} = $tokens[7];
  }

  return $OK;

}


#------------------------------------------------------------------------------
# Name   : GetCheckFiles($checkDir, \@components, \@compChkFiles)
# Purpose: Reads the check files from the checkDir and adds them to the
#          checkFiles list.  The check files are filter by the components so
#          we are adding only checks in the components lists
# RCs    : $OK or $ERROR
#------------------------------------------------------------------------------
sub GetCheckFiles {

  my ($dir, $refComps, $refCheckFiles) = @_;

  my @files;
  IcofFile::ReadDirToList($dir, \@files, 0);

  my $comp;
  foreach $comp (@$refComps) {
    my @matches = grep(/^$comp/, @files);

    my $match;
    foreach $match (@matches) {
      my @tokens = split("_", $match);
      pop @tokens;   # remove _PASS.html from file name
      my $myComp = join("_", @tokens);

      if ($myComp eq $comp) {
	push(@$refCheckFiles, $match);
	last;
      }

    }

  }

}


1;


__END__
