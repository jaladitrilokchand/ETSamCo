#!/usr/bin/perl
#==============================================================================
#
#    FILE: TkPkgDels.pm
#
#-PURPOSE----------------------------------------------------------------------
# Contains functions necessary to package a collection of deliverables
#
#------------------------------------------------------------------------------
#
#-CHANGE LOG-------------------------------------------------------------------
# 04/12/2013 GFS  Initial coding.
#==============================================================================

package TkPkgDels;

use strict;
use warnings;


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
my @LOCS = ( "build", "shipb", "tkb",  "xtinct" );
my @PLATS = ( "AIX", "LINUX" );
my $EDA_DIR = "/afs/eda";


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





1;
__END__
