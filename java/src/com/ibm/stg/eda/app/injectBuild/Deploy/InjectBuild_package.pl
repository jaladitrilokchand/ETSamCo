#!/usr/bin/perl
# =============================================================================
#
#    FILE: InjectBuild_package.pl
#  SYNTAX: Execute program with -? or -help.
#
# CREATOR: Gregg Stadtlander
#    DATE: 02/05/2010
#
#  INPUTS: See application help message.
#
#
# PURPOSE ---------------------------------------------------------------------
# Packages all jar and class files for the injectBuild application into a tar
# file that can be exploded in the /afs/eda/u/einslib/bin.
# -----------------------------------------------------------------------------
#
# CHANGE LOG ------------------------------------------------------------------
# 02/05/2010 GS  Initial coding.
# =============================================================================

use warnings;
use strict;

use Getopt::Long;
Getopt::Long::Configure ("pass_through");

use English;
use Cwd;
use File::Path;
use File::Copy;
use Sys::Hostname;


# Application constants.
my $APP_NAME = "InjectBuild_package.pl";
my $OK = 0;
my $ERROR = 8;
my $entry = "";
my @tokens = ();
my $test;
my $verbose;
my $help;

my $USAGE = "\n";
$USAGE .= "$APP_NAME [-h] [-test]\n";
$USAGE .= "where\n";
$USAGE .= "  -h = Show application help\n";
$USAGE .= " -test = Use \$aes/apps/test instead of \$aes/apps/prod as source location\n";
$USAGE .= "\n";


# Get environment data.
my $host = hostname;
my $startDir = cwd();
my $userID = (getpwuid($<)) [0];


# Read the command line args
if (ProcessArgs() != $OK) {
    return $ERROR;
}


# Find the InjectBuild_contents_for_EDA.txt input file.
my $file = "InjectBuild_contents_for_EDA.txt";
if (! -f $file) {
    print "ERROR: unable to locate input file - $file\n";
    exit $ERROR;
}


# Specify the source directory.
my $sourceDir = "/afs/btv/data/aes/apps/prod/";
if ($test) {
    $sourceDir = "/afs/btv/data/aes/apps/test/";
}
print "Copying files from $sourceDir ...\n";


# Read teh input file
open(DAT, $file) || die("Could not open file!"); 
my @contents = <DAT>;
close(DAT);


# Copy each entry in the input file from $aes to this directory 
# creating new directories along the way.
my $src = "";
my $dh;
foreach $entry (@contents) {
    chomp($entry);

    @tokens = split(/\//, $entry);
    pop(@tokens);
    my $subDir = join("/", @tokens);
    my $directory = $startDir . "/" . $subDir;
    my $fullPath = $startDir . "/" . $entry;

    # Create the target directory if it doesn't exist.
    if (! -d $directory) {
	mkpath($directory);
    }

    # Copy the file(s).
    if ($entry !~ /\*/) {
	$src = $sourceDir . $entry;
	copy($src, $fullPath);
	print " Copied: $src to $fullPath\n" if ($verbose);
    }
    else {
	my $dh;
	chop($entry);
	chop($fullPath);
	my $myDir = $sourceDir . $entry;
	opendir($dh, $myDir) || warn "ERROR: can't opendir $myDir: $!";
	my @dirContents = readdir($dh);
	closedir $dh;
	my $file;
	foreach $file (@dirContents) {
	    $src = $myDir . $file;
	    next if (! -f $src);
	    my $tgt = $fullPath . $file;
	    copy($src, $tgt);
	    print " Copied: $src to $tgt\n" if ($verbose);
	}

    }

}


# Create the tar ball.
print "\nCreating tar ball ...\n";
my $date = `date +%Y%m%d`;
chomp($date);
my $myPublic = $ENV{HOME} . "/" . "public";
my $tarball = "$myPublic/injectBuild_" . $date . ".tar";

`tar -cvf $tarball iipmds/*`;

print " Created $tarball\n";

print "\nDone!\n";



# ------------------------------------------------------------------------------
# Name   : processArgs()
# Purpose: Read command line parameters.
# RCs    : none
# ------------------------------------------------------------------------------
sub ProcessArgs {

  # Parse command line arguments
  GetOptions ('test|t'    => \$test,
              'verbose|y' => \$verbose,
              'help|h'    => \$help
             );

  # If help requested show usage and exit.
  if ($help) {
    print $USAGE;
    return $ERROR;
  }

  return $OK

}
