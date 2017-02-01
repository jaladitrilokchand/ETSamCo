#!/usr/bin/perl
#==============================================================================
#
#    FILE: TKutils.pm
#
#-PURPOSE----------------------------------------------------------------------
# EDA TK script utilies.
#------------------------------------------------------------------------------
#
#-CHANGE LOG-------------------------------------------------------------------
# 12/11/2012 GFS  Initial coding.
# 02/18/2013 GFS  Added Trim, GetComponents and GetRelease functions
# 03/19/2013 GFS  Added LogComponentEvent, GetOwner and GetManager.
# 02/13/2016 NMC  Added GetShipPlatforms($toolKit) function
#==============================================================================

package TKutils;

use strict;
use warnings;

use lib '/afs/eda/data/edainfra/tools/enablement/prod/libperl';
#use lib '/afs/btv/u/stadtlag/public/ETREE/dev/libperl';
#use lib '/afs/eda/u/navechan/toolkit/dev_sandbox/libperl';
use BPRecord;
use BPQuery;


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

my $EDAINFRA = "/afs/eda/data/edainfra";
my $DEV_BIN = "$EDAINFRA/tools/enablement/dev/bin";
my $BIN = "$EDAINFRA/tools/enablement/prod/bin";
my $GET_COMPS = "$BIN/getComponents -q";
my $SVN_LOG = "$BIN/svnLog";
my $SHOW_COMP = "$BIN/showComponent";
my $TK_SHOW = "$BIN/tk.show";
my $PLAT_SHOW = "$BIN/plat.show";


#------------------------------------------------------------------------------
# Name   : RunCommand($command, \@results, $dryRun, $echo)
# Purpose: Executes the command using backticks. Writes the resutls into the
#          results reference. Will echo the command to stdout if echo is true.
# RCs    : return code
#------------------------------------------------------------------------------
sub RunCommand {

  my ($command, $resultsRef, $dryRun, $echo) = @_;

  my $rc = $OK;
  if ($dryRun) {
    print "[DRY RUN] >>> $command\n";
  }
  else {
    print "Command: $command\n" if ($echo);
    @$resultsRef = `$command`;
    $rc = $? >> 8;
  }

  return $rc;

}


#------------------------------------------------------------------------------
# Name   : RunSystem($command, $dryRun, $echo)
# Purpose: Executes the command using system(). Will echo the command to
#          stdout if echo is true.
# RCs    : return code
#------------------------------------------------------------------------------
sub RunSystem {

  my ($command, $dryRun, $echo) = @_;

  my $rc = $OK;
  if ($dryRun) {
    print "[DRY RUN] >>> $command\n";
  }
  else {
    print "Command: $command\n" if ($echo);
    system($command);
    $rc = $? >> 8;
  }

  return $rc;

}


#------------------------------------------------------------------------------
# Name   : IsAuthorized($authorizedId, $dryRun)
# Purpose: Returns 1 if the user's AFS id matches the authorized id otherwise
#          returns 0
# RCs    : 1 matches auth id
#          0 does NOT match
#------------------------------------------------------------------------------
sub IsAuthorized {

  my ($authId, $dryRun) = @_;

  my $userid = getlogin();
  print "User id: $userid\n";
  $userid = getpwuid($<);
  print "User id: $userid\n";
  return 1 if ($dryRun || ($userid eq $authId));	

  print "ABORTING - you must run this script as $authId!\n";

  return 0;

}


#------------------------------------------------------------------------------
# Name   : Trim($string)
# Purpose: Trim leading and trailing whitespace
# RCs    : string with whitespace trimmed
#------------------------------------------------------------------------------
sub Trim {

  my ($string) = @_;

  if ($string && $string =~ /\w+/) {
    $string =~ s/^\s+|\s+$//g;
  }

  return $string;

}


#------------------------------------------------------------------------------
# Name   : GetRelease($toolKit, $delimiter)
# Purpose: Determines the release from the tool kit name.  Uses $delimiter to
#          reassemble the release name after parsing apart the tk name.
#          For example GetRelease("14.1.3", "0") ==> 1401
#          For example GetRelease("14.1.3", ".") ==> 14.1
# RCs    : release name
#------------------------------------------------------------------------------
sub GetRelease {

  my ($tk, $delim) = @_;

  my @tokens = split(/\./, $tk);

  return $tokens[0] . $delim . $tokens[1];

}


#------------------------------------------------------------------------------
# Name   : GetComponents($toolKit, $component, $component_types)
# Purpose: Determine the components to process
# RCs    : List of components
#------------------------------------------------------------------------------
sub GetComponents {

  my ($tk, $comp, $compTypes) = @_;

  my @comps = ();
  if ($comp eq "") {
    my $command = "$GET_COMPS";
    $command .= " -t $tk";
    $command .= " -q";
    $command .= " -ct $compTypes" if ($compTypes ne "");


    my @results;
    my $rc = RunCommand($command, \@results, 0, 0);
    if ($rc < 1) {
      my $list = $results[0];
      chomp $list;
      @comps = split(/,/, $list);
    }
  }
  else {
    push(@comps, $comp);
  }

  return @comps;

}


#------------------------------------------------------------------------------
# Name   : LogComponentEvent($component, $location, $event, $message,
#          verbose, $dryRun)
# Purpose: Log a tool kit/component specific event
# RCs    : Return code
#------------------------------------------------------------------------------
sub LogComponentEvent {

  my ($tk, $comp, $loc, $event, $message, $verbose, $dryRun) = @_;

  print " Logging TK/Component event ($event) ...\n" if ($verbose);

  # Construct the command
  my $command = "$SVN_LOG";
  $command .= " -t $tk";
  $command .= " -c $comp";
  $command .= " -e $event";
  $command .= " -l $loc";
  $command .= " -m \"$message\"" if ($message);

  # Run the command
  my @results;
  my $rc = RunCommand($command, \@results, $dryRun, $verbose);

  return $rc;

}


#------------------------------------------------------------------------------
# Name   : GetOwner()
# Purpose: Look up the Component owner from the database
# RCs    : n/a
#------------------------------------------------------------------------------
sub GetOwner {

  my ($comp) = @_;

  # Construct the command
  my $command = "$SHOW_COMP";
  $command .= " -r 14.1";
  $command .= " -c $comp";
  $command .= " | grep \"TEAM LEAD\"";

  # Run the command
  my @results;
  my $rc = RunCommand($command, \@results, 0, 0);
  my ($junk, $owner) = split(/\:/, $results[0]);

  # Remove leading and trailing spaces
  return Trim($owner);

}


#------------------------------------------------------------------------------
# Name   : GetManager()
# Purpose: Determine the owner's manager
# RCs    : n/a
#------------------------------------------------------------------------------
sub GetManager {

  my ($owner) = @_;

  return "" if ($owner eq "");

  # Query BluePages for owners email address
  my $query = new BPQuery('byInternetAddr', $owner);
  my ($recordKey) = ($query->recordKeys());

  # Get serial number and country code for owners manager
  my $mgrnum = $query->record($recordKey)->valueOf('MGRNUM');
  my $mgrcc  = $query->record($recordKey)->valueOf('MGRCC');

  # Query Bluepages for managers serial number and country code (CNUM)
  $query = new BPQuery('byCnum', "$mgrnum$mgrcc");
  ($recordKey) = ($query->recordKeys());

  # Get managers email address
  return $query->record($recordKey)->valueOf('INTERNET');

}


#------------------------------------------------------------------------------
# Name   : ValidateTkComp($toolKit, $component)
# Purpose: Verifies the specified component is a member of the tool kit
# RCs    : 0 = not valid
#          1 = component is a member of tool kit
#------------------------------------------------------------------------------
sub ValidateTkComp {

  my ($tk, $comp) = @_;

  # Create a list of components for this tool kit
  my @comps = GetComponents($tk, "", "");

  # Construct the command
  my $rc = 0;
  $rc = 1 if (grep(/^$comp$/, @comps));

  return $rc;

}


#------------------------------------------------------------------------------
# Name   : GetTkStage($toolKit, $refStage)
# Purpose: Determines the specified TKs stage
# RCs    : 0 = OK
#          8 = ERROR
#------------------------------------------------------------------------------
sub GetTkStage {

  my ($tk, $refStage) = @_;

  # Construct the command
  my $command = "$TK_SHOW";
  $command .= " -t $tk";

  # Run the command
  my @results;
  my $rc = RunCommand($command, \@results, 0, 0);
  my @lines = grep(/Stage/, @results);
  my @tokens = split(/\:/, $lines[0]);
  $$refStage = Trim($tokens[1]);

  return $rc;

}


#------------------------------------------------------------------------------
# Name   : GetTkLocation($tk, \$refLocation)
# Purpose: Determines the location based on the Tool Kit
# RCs    : 0 = OK
#          8 = ERROR (no match)
#------------------------------------------------------------------------------
sub GetTkLocation {

  my($tk, $refLocation) = @_;

  # Get the TK's stage
  my $stage = "";
  my $rc = GetTkStage($tk, \$stage);
  return $rc if ($rc != $OK);

  # If development then location = prod
  # if preview then location = ship
  # if production then location = tk
  # otherwise location = customtk/tkx.y.z or xtinct/tkx.y.z
  if (lc($stage) eq "preview") {
    $$refLocation = "ship";
  }
  elsif (lc($stage) eq "production") {
    $$refLocation = "tk";
  }
  elsif (lc($stage) eq "development") {
    $$refLocation = "prod";
  }
  else {
    $$refLocation = lc($stage);
  }

  return $rc;

}


#------------------------------------------------------------------------------
# Name   : GetShipPlatforms($toolKit)
# Purpose: Determine the available platforms for a toolkit to process
# RCs    : List of platforms
#------------------------------------------------------------------------------
sub GetShipPlatforms {

  my ($tk) = @_;
     
  # Construct the command
    my $command = "$PLAT_SHOW";
    $command .= " -t $tk";
    $command .= " -ship";
    #print "myCommand: $command";

  # Run the command 
    my @platforms = ();
    my @results;
    my $rc = RunCommand($command, \@results, 0, 0);    
    my $result;
    foreach $result (@results) { 
      # remove the new line from the end of a line and then check for empty line
      chomp $result; 
      if($result ne ""){  
        push(@platforms, $result);
      }         
    }
    
    my $platSize = @platforms;
    #print "plat size: $platSize\n";
           
  	return @platforms;
  }

1;


__END__
