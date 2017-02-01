#!/usr/bin/perl
#==============================================================================
#
#    FILE: SVNutils.pm
#
#-PURPOSE----------------------------------------------------------------------
# EDA TK script utilies.
#------------------------------------------------------------------------------
#
#-CHANGE LOG-------------------------------------------------------------------
# 03/20/2012 GFS  Initial coding.
#==============================================================================

package SVNutils;

use strict;
use warnings;
use Sys::Hostname;
use FindBin qw($RealBin);

use lib "$RealBin/../libperl";
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

my $SVN_FSH = "/afs/eda/tools/bin/svn";
my $SVN_BTV = "/afs/btv/data/subversion/bin/svn";
my $SVN_GJ_URL = "https://greenjiffy.fishkill.ibm.com/eda/tk";
my $SVN_BJ_URL = "https://bluejiffy.fishkill.ibm.com/eda";


#------------------------------------------------------------------------------
# Name   : GetSvn()
# Purpose: Set the correct path to subversion
# RCs    : Path to SVN.
#------------------------------------------------------------------------------
sub GetSVN {

  my $host = hostname;
  my $svn = $SVN_FSH;
  $svn = $SVN_BTV if (lc($host) =~ /\.btv\./);

  return $svn;

}


#------------------------------------------------------------------------------
# Name   : GetLastRevision($comp, $branch, $ref_revision)
# Purpose: Read the last changed revision for the specified url
# RCs    : $OK or $ERROR
#------------------------------------------------------------------------------
sub GetLastRevision {

  my ($comp, $branch, $revision) = @_;

  $branch = "branches/$branch" if ($branch ne "trunk");

  # Construct the command 
  my $command = GetSVN() . " info";
  $command .= " $SVN_GJ_URL/$comp/$branch";

  # Run the command
  my @results;
  my $rc = TKutils::RunCommand($command, \@results, 0, 1);

  # Parse the results
  foreach my $line (@results) {
    next if ($line !~ /Last Changed Rev/);
    chomp $line;
    my @tokens = split(/: /, $line);
    $$revision = $tokens[1];
    last;
  }

  return $rc;

}


#------------------------------------------------------------------------------
# Name   : IsSvnTkBranch($component, $branch)
# Purpose: Check for presence of branch in SVN repos for this component.
# RCs    : 0 = branch is NOT present
#          1 = branch in is present
#------------------------------------------------------------------------------
sub IsSvnTkBranch {

  my ($comp, $branch) = @_;

  # Construct the command 
  my $command = GetSVN() . " info";
  $command .= " $SVN_GJ_URL/$comp/branches/$branch";
  $command .= " > /dev/null";
  $command .= " 2>&1";

  # Run the command
  my @results;
  my $rc = TKutils::RunSystem($command, 0, 1);

  my $exists = 0;
  $exists = 1 if ($rc == 0);

  return $exists;

}


#------------------------------------------------------------------------------
# Name   : DeleteTkBranch($component, $branch, $message, $dryRun, $verbose)
# Purpose: Delete a branch from the TK component 
# RCs    : 0 = OK
#          8 = ERROR
#------------------------------------------------------------------------------
sub DeleteTkBranch {

  my ($comp, $branch, $message, $dryRun, $verbose) = @_;

  # Construct the command
  my $command = GetSVN() . " delete";
  $command .= " --force";
  $command .= " -m \"$message\"";
  $command .= " $SVN_GJ_URL/$comp/branches/$branch";
  $command .= " 2>&1";

  # Run the command
  my @results;
  my $rc = TKutils::RunCommand($command, \@results, $dryRun, $verbose);
  if ($rc > 0) {
    print "ERROR: Unable to delete the branch in SVN ..\n";
    print "       Command: $command\n";
    print join("\n", @results) . "\n";
    return $ERROR;
  }

  return $OK;

}

#------------------------------------------------------------------------------
# Name   : CopyTkBranch($url, $src, $tgt, $revision, $dryRun, $verbose)
# Purpose: Creates a branch called $tgt from $src at the specified revision.
#          If $revision is "" then use the HEAD.
# RCs    : 0 = OK
#          8 = ERROR
#------------------------------------------------------------------------------
sub CopyTkBranch {

  my ($url, $src, $tgt, $revision, $dryRun, $verbose) = @_;

  $src = "branches/$src" if ($src ne "trunk");
  $tgt = "branches/$tgt" if ($tgt !~ /^branches/);

  # Construct the command
  my $command = GetSVN() . " copy";
  $command .= " $url/$src";
  $command .= "@" . "$revision" if ($revision ne "");
  $command .= " $url/$tgt";
  $command .= " -m \"DEV: Branch creation\"";
  $command .= " 2>&1";

  # Run the command
  my @results;
  my $rc = TKutils::RunCommand($command, \@results, $dryRun, $verbose);
  if ($rc > 0) {
    print "ERROR: Unable to create new branch in SVN ..\n";
    print "       Command: $command\n";
    print join("\n", @results) . "\n";
    return $ERROR;
  }

  return $OK;

}


#------------------------------------------------------------------------------
# Name   : GetBranch($toolKit, $component, $location, $refBranch)
# Purpose: Determine the branch for the working copy
# RCs    : trunk or branches/*
#------------------------------------------------------------------------------
sub GetBranch {

  my ($tk, $comp, $loc, $refBranch) = @_;

  my $rel = TKutils::GetRelease($tk, ".");
  my $wc = "/afs/eda/$loc/$comp/$rel";

  # Need to always run svn inf on the real SVN wc copy so need to replace /afs/eda/customtkb/tk$tk link
  # with path to real WC (/afs/eda/tk$tk)
  $wc =~ s/customtkb\///g;   # substitute => s/old_str/new_str/g

  # Construct the command
  my $command = GetSVN() . " info $wc";

  # Run the command
  my @results;
  my $rc = TKutils::RunCommand($command, \@results, 0, 0);
  if ($rc > 0) {
    print "ERROR: Unable to create query SVN working copy for branch - $wc\n";
    print "       Command: $command\n";
    print join("\n", @results) . "\n";
    $$refBranch = "";
    return $ERROR;
  }

  foreach my $line (@results) {
    next if ($line !~ /URL/);
    chomp $line;
    my @tokens = split(/\/$comp\//, $line);
    $$refBranch = $tokens[1];
  }

  return $OK;

}


#------------------------------------------------------------------------------
# Name   : GetBranchDb($toolKit, $component, $revision, $refBranch)
# Purpose: Determine the branch for the tk, comp and revision from the DB
# RCs    : trunk or branches/*
#------------------------------------------------------------------------------
sub GetBranchDb {

  my ($tk, $comp, $rev, $refBranch) = @_;

 # Construct the command
  my $command = "$RealBin/../bin/branch4rev";
  $command .= " -t $tk";
  $command .= " -c $comp";
  $command .= " -r $rev";

  # Run the command
  my @results;
  my $rc = TKutils::RunCommand($command, \@results, 0, 0);
  if ($rc > 0) {
    print "ERROR: Unable to create query DB for branch - $tk, $comp and $rev\n";
    print "       Command: $command\n";
    print join("\n", @results) . "\n";
    $$refBranch = "";
    return $ERROR;
  }

  chomp $results[0];
  $$refBranch = $results[0];

  return $OK;

}


#------------------------------------------------------------------------------
# Name   : GetURL($toolKit, $component, $location, $refURL)
# Purpose: Determine the SVN URL for theworking copy
# RCs    : trunk or branches/*
#------------------------------------------------------------------------------
sub GetURL {

  my ($tk, $comp, $loc, $refURL) = @_;

  my $rel = TKutils::GetRelease($tk, ".");
  my $wc = "/afs/eda/$loc/$comp/$rel";

  # Construct the command
  my $command = GetSVN() . " info $wc";

  # Run the command
  my @results;
  my $rc = TKutils::RunCommand($command, \@results, 0, 0);
  if ($rc > 0) {
    print "ERROR: Unable to create query SVN working copy for URL - $wc\n";
    print "       Command: $command\n";
    print join("\n", @results) . "\n";
    $$refURL = "";
    return $ERROR;
  }

  foreach my $line (@results) {
    next if ($line !~ /URL/);
    chomp $line;
    my @tokens = split(/ +/, $line);
    $$refURL = $tokens[1];
  }

  return $OK;

}

1;


__END__
