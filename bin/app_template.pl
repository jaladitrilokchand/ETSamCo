#!/usr/bin/perl
# =============================================================================
#
#    FILE: app_template.pl
# CREATOR: Gregg Stadtlander
#
# PURPOSE ---------------------------------------------------------------------
# See "printHelp" method.
# -----------------------------------------------------------------------------
#
#
# CHANGE LOG ------------------------------------------------------------------
# 02/13/2014 GS  Initial coding.
# =============================================================================
use warnings;
use strict;
use Getopt::Long;
Getopt::Long::Configure ("no_ignore_case");

use lib '/afs/eda/data/edainfra/tools/enablement/prod/libperl';
#use lib '/afs/eda/data/edainfra/tools/enablement/dev/libperl';
use TKutils;


# Global variables
my $BIN_DIR = "/afs/eda/data/edainfra/tools/enablement/dev/bin";
my $LOGS_DIR = "/afs/eda/data/edainfra/logs";

# Program variables
my $appName = "app_template.pl";
my $progInvocation = $appName;
foreach (@ARGV) {
  $progInvocation .= " " . $_;
}

# Program arguments
my $help            = "";
my $verbose         = 0;
my $dbMode          = "";  # default is prod if empty
my $dryRun          = 0;
my $toolKit         = "";
my $startDir;

# Return codes
my $OK    = 0;
my $ERROR = 8;

# Generic application variables
my @tokens = ();
my $entry  = "";
my $rc     = $ERROR;


#-----------------------------------------------------------------------------#
#                 M A I N   P R O G R A M                                     #
#-----------------------------------------------------------------------------#

# Deal nicely with unexpected exits
$SIG{'INT'}   = sub { handleInterruption(); };
$SIG{'QUIT'}  = sub { handleInterruption(); };
$SIG{__DIE__} = sub { handleInterruption(); };

# Read the command line arguments
$rc = processArgs();
if ($rc == $ERROR) {
  print "\n[$appName] Use -h to get more details.\n";
  exit $ERROR;
}


CleanExit("\nApplication complete", $OK);


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
# Name   : cleanup($message, $rc)
# Purpose: Performs any applicatin clean up and exits with rc
# RCs    : none
#------------------------------------------------------------------------------
sub CleanExit {
  my ($message, $rc) = @_;

  # Log application end event
  #LogAppEvent("end");

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
Script description here ...
";

  my $syntax = "
SYNTAX:
  $appName <-t tk>
                  [-dryrun] [-y] [-db dbMode] [-h]

RETURN CODES:
  0(ok) 8(error)
";

  my $where ="
WHERE:
  tk        = Tool Kit version which must exist (ie, 14.1.1 ...)
  -dryrun   = Run script in dryrun mode
  dbMode    = Database mode [default = PROD] (PROD, DEV, TEST)
  -h        = Application usage help
  -y        = Verbose mode
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
  GetOptions ('t=s'       => \$toolKit,
	      'db=s'      => \$dbMode,
	      'dryrun'    => \$dryRun,
	      'y'         => \$verbose,
	      'help|h|?'  => \$help)
    || return $ERROR;

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

  print "Running in DRY RUN mode ...\n" if ($dryRun);

  return $OK;

}


#------------------------------------------------------------------------------
# Name   : SetComponents()
# Purpose: Create a list of components ready for this Tool Kit
# RCs    : List of component names or empty list
#------------------------------------------------------------------------------
sub SetComponents {

  my ($tk, $comp) = @_;
  print " Querying for components in $tk ...\n" if ($verbose);

  # Query the DB for tk/components for the src tool kit
  my @list = TKutils::GetComponents($tk, $comp, "");
  print "Found " . scalar(@list) . " components\n" if ($verbose);

  return @list;

}


#------------------------------------------------------------------------------
# Name   : GetStatus($tgtToolKit, $component)
# Purpose: Query the DB for this component's CTK status
# RCs    : n/a
#------------------------------------------------------------------------------
sub GetStatus {

  my ($tk, $comp) = @_;

  print " Reading xtinxt/customtk status ...\n" if ($verbose);

  # Construct the command
  my $command = "$BIN_DIR/statusXtinct";
  $command .= " -t $tk";
  $command .= " -c $comp";
  $command .= " -q";
  $command .= " -db $dbMode" if ($dbMode);

  # Run the command and show results
  my $status = "";
  my @results;
  my $rc = TKutils::RunCommand($command, \@results, 0, $verbose);
  if ($rc == 0) {
    foreach my $line(@results) {
      next if ($line !~ /$comp/);
      my @tokens = split(/ +/, $line);
      $status = $tokens[2];
    }
  }

  print " Status: $status\n" if ($verbose);

  return $status;

}

