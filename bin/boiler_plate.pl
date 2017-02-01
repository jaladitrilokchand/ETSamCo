a#!/usr/bin/perl
# =============================================================================
#
#    FILE: builer_plate.pl
#  SYNTAX: See "printHelp" methods below or execute the program with -h
#
# CREATOR: Gregg Stadtlander
#    DATE: 04/10/2012
#
#  INPUTS: See "printHelp" methods below
#
# OUTPUTS: n/a
#
# RETURN CODES: See "printSyntax" method below.
#
#
# PURPOSE ---------------------------------------------------------------------
# See "printHelp" method.
# -----------------------------------------------------------------------------
#
#
# CHANGE LOG ------------------------------------------------------------------
# 04/10/2012 GS  Initial coding.
# =============================================================================

use warnings;
use strict;
use Getopt::Std;
use Getopt::Long;
Getopt::Long::Configure ("no_ignore_case");

# Global variables



# Program variables
my $appName = "boiler_plate.pl";
my $progInvocation = $appName;
foreach (@ARGV) {
    $progInvocation .= " " . $_;
}

# Program arguments
my $help         = "";
my $verbose      = 0;
my $toolKit      = "";

# Return codes
my $OK    = 0;
my $ERROR = 8;

# Generic application variables
my @tokens = ();
my $entry  = "";
my $rc  = $ERROR;
my $startDir = $ENV{"PWD"};


#-----------------------------------------------------------------------------#
#                 M A I N   P R O G R A M                                     #
#-----------------------------------------------------------------------------#

# Deal nicely with unexpected exits
$SIG{'INT'}   = sub { handleInterruption(); };
$SIG{'QUIT'}  = sub { handleInterruption(); };
$SIG{__DIE__} = sub { handleDIE(); };


# Read the command line arguments
$progRC = processArgs();
if ($progRC == $ERROR) {
    print "\n[$appName] Use -h to get more details.\n";
    exit $ERROR;
}


#
# Add your main here ...
#


CleanExit("Application complete", $OK);


#-----------------------------------------------------------------------------#
#                F U N C T I O N  D E F I N I T I O N S                       #
#-----------------------------------------------------------------------------#

#------------------------------------------------------------------------------
# Name   : handleInterruption()
# Purpose: To handle Ctrl+C interrruption
# RCs    : none
#------------------------------------------------------------------------------
sub handleInterruption {
  CleanExit("\n[$appName] Caught interruption - exiting\n", $ERROR);
}


#------------------------------------------------------------------------------
# Name   : handleDIE()
# Purpose: To handle an unexpected die().
# RCs    : none
#------------------------------------------------------------------------------
sub handleDIE {
  CleanExit("\n[$appName] Caught an unexpected die - exiting\n", $ERROR);
}


#------------------------------------------------------------------------------
# Name   : cleanup($message, $rc)
# Purpose: Performs any applicatin clean up and exits with rc
# RCs    : none
#------------------------------------------------------------------------------
sub CleanExit {
  my ($message, $rc) = @_;
  print "$message\n";
  print "Return code: $rc\n";
  chdir($startDir) if (defined($startDir));
  exit $rc;
}


#------------------------------------------------------------------------------
# Name   : printHelp()
# Purpose: Prints discription and full usage to console
# RCs    : none
#------------------------------------------------------------------------------
sub printHelp {

  my $progDescription = "
This script executes some TBD process ...
";

  my $syntax = "
SYNTAX:
  $appName <-t tool_kit> [-h] [-y]

RETURN CODES:
  0(ok) 8(error)
";

  my $where ="
WHERE:
  tool_kit = Tool Kit version (ie, 14.1.1, 14.1.2 ...
  -h         (optional) application help
  -y         (optional) Verbose
";

  print $progDescription;
  print $syntax;
  print $where;
}


#------------------------------------------------------------------------------
# Name   : processArgs()
# Purpose: Read and verify command line arguments
# RCs    : 0 = OK
#          8 = ERROR
#------------------------------------------------------------------------------
sub processArgs {

  # Parse command line arguments
  GetOptions ('tk|t=s'     => \$toolKit,
	      'y'          => \$verbose,
	      'help|h|?'   => \$help) || return $ERROR;

  # If help requested, show usage
   if ($help) {
     printHelp();
     return $ERROR;
   }

  # Validate required input
  if (! $toolKit) {
    print "ERROR: Tool Kit (-t) is a required parameter\n";
    return $ERROR;
  }

  return $OK;

}

