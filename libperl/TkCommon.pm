#!/usr/bin/perl
#==============================================================================
#
#    FILE: TkCommon.pm
#
#-PURPOSE----------------------------------------------------------------------
# EDA ToolKit Perl module 
#------------------------------------------------------------------------------
#
#-CHANGE LOG-------------------------------------------------------------------
# 04/12/2011 GFS  Initial coding.
#==============================================================================

package TkCommon;

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

#------------------------------------------------------------------------------
# FUNCTION: runcmd(command, debug)
#  PURPOSE: Runs the given command.  If debug is true then don't actually run the
#           command just display the command string.
#  PARAMS IN: command = command to run
#             debug   = 0 or 1 (If true run command otherwiase display it)
# PARAMS OUT: n/a
#
# RETURN CODES: 1 = OK
#               0 = error running command
#------------------------------------------------------------------------------
sub runcmd {
	
  my($command, $debug) = @_;
  chomp($command);
  print "Command: \"$command\"\n";

  my $rc = $ERROR;
  my @results = ();
  if (! $debug) {
		
    # Run the command
    @results = `$command`;
    $rc = $? >> 8;
		
    # Show the results
    print "Results from command:\n";
    print "@results\n";
		
    if ($rc) {
      print "sysRC: $rc\n";
      print "ERROR: Return code $rc from $command.\n";
    }
    else {
      print "Return code: $rc\n";
      print "Command, \"$command\", executed successfully.\n";
    }
  }
  return $rc;
}

#------------------------------------------------------------------------------
#   FUNCTION: read_compdef_files (tkVersion, defPath, compDefType, debug)
#    PURPOSE: Reads the TK version-specific component definition files.
#
#  PARAMS IN: tkVersion   = Toolkit version (e.g. 14.1.0)
#             defPath     = first part of AFS path containing the component 
#                           definition files.
#             compDefType = defines which files to process.
#                           "both"  means combine both definition files.
#                           "support" means only process the build_support def file.
#                           "deliver" means only process the deliver def file. 
#             debug       = 0 or 1 (If true, display the component definition array
#                           contents.
# PARAMS OUT: rc          = return code
#             %compList   = hash containing the component definition to use.
#
# RETURN CODES: 0 = OK
#               8 = invalid compDefType
#------------------------------------------------------------------------------
sub read_compdef_files {
	
  my ($tkVersion, $defPath, $compDefType, $debug) = @_;
  my $rc = $OK;
  my $comp_deliver_def = "${defPath}/tk${tkVersion}.component.delivered.def";
  my $comp_support_def = "${defPath}/tk${tkVersion}.component.build_support.def";
  my $compCnt = 0;
  my $comp = "";
	
  my %comh = ();       # hash containing all components
  my %delh = ();       # hash containing only "deliver" components
  my %suph = ();       # hash containing only "build support" components
	
  # Read the files and load the hashes
  open (COMPDEF1, "<$comp_deliver_def") or die "cannot open $comp_deliver_def...exiting\n";
  open (COMPDEF2, "<$comp_support_def") or die "cannot open $comp_support_def...exiting\n";

  # Components defined for "deliver"
  my (@del_comps) = <COMPDEF1>;
  foreach $comp (@del_comps) {
    chomp ($comp);
    $delh{$comp} = $comp;
  }
  close (COMPDEF1);
	
  # Components defined for "build support"
  my (@sup_comps) = <COMPDEF2>;
  foreach $comp (@sup_comps) {
    chomp ($comp);
    $suph{$comp} = $comp;
  }
  close (COMPDEF2);
	
  # Return the hash of component names based on its definition type 
  # (deliver, support, or both)
  #
  if ($compDefType eq "both") {
		
    # Merge the two hashes
    %comh = (%delh, %suph);
		
    if ($debug) {
      print "\n***** TK $tkVersion COMPONENT LIST *****\n";
      foreach $comp (sort keys %comh) {
        $compCnt++;
        print "$compCnt) $comp\n";
      }
    }
    return ($rc, %comh);

  } elsif ($compDefType eq "deliver") {
    if ($debug) {
      print "\n***** TK $tkVersion COMPONENT LIST *****\n";
      foreach $comp (sort keys %delh) {
        $compCnt++;
        print "$compCnt) $comp\n";
      }
    }
    return ($rc, %delh);
    
  } elsif ($compDefType eq "support") {
    if ($debug) {
      print "\n***** TK $tkVersion COMPONENT LIST *****\n";
      foreach $comp (sort keys %suph) {
        $compCnt++;
        print "$compCnt) $comp\n";
      }
    }
    return ($rc, %suph);
  }
  else {
    $rc = $ERROR;
    print "ERROR: Invalid component definition file type, $compDefType, was input!\n";
    return ($rc, %comh);
  }
} # end read_compdef_files

#------------------------------------------------------------------------------
#   FUNCTION: validateCompFromList (compName, tkVersion, debug, %compList)
#    PURPOSE: Validate component name against the TK version-specific component definition files.
#
#  PARAMS IN: compName    = component name
#             tkVersion   = Toolkit version (e.g. 14.1.0)
#             debug       = 0 or 1 (If true, display messages)
#             %compList   = hash containing the component definition to use.
#
# PARAMS OUT: rc          = return code
#
# RETURN CODES: 0 = component name found in list
#               2 = component name not found in list
#------------------------------------------------------------------------------
sub validateCompFromList {
	#
  # Determine if component is in the component definition list (Abort, if not)
  #
	my ($compName, $tkVersion, $debug, %compList) = @_;
	my $rc = $OK;
	
	if (exists $compList{$compName}) {
		if ($debug) {
			print "Component name, $compName, exists in list associated with TK version $tkVersion.\n";
		}
	}
	else {
		$rc = 2;
		if ($debug) {
			print "Component \"$compName\", is not associated with TK version $tkVersion.\n";
		}
	}	
	return ($rc);
} # end validateCompFromList

#------------------------------------------------------------------------------
#   FUNCTION: check_tkVersion (tkVersion)
#    PURPOSE: Validate format of TK version
#
#  PARAMS IN: tkVersion   = Toolkit version (e.g. 14.1.0)
#
# PARAMS OUT: rc          = return code
#
# RETURN CODES: 0 = TK version format is correct
#               1 = TK version format is invalid
#------------------------------------------------------------------------------
sub check_tkVersion {
  #
  # Check TK version input format (e.g. 14.1.n)
  #
  my ($tkVer) = @_;
  my $rc = $ERROR;

  if ($tkVer =~ /(14)\.(1)\.(\d{1,2}?)/) {
    $rc = $OK;
  }
  if ($tkVer =~ /(15)\.(1)\.(\d{1,2}?)/) {
    $rc = $OK;
  }
  return ($rc);

} # sub check_tkVersion

#------------------------------------------------------------------------------
#   FUNCTION: convert_tkVersion (tkVer, verType)
#    PURPOSE: Convert tkVersion into various formats based on verType
#
#  PARAMS IN: tkVer    = Toolkit version (e.g. 14.1.{n})
#             verType  = type to convert into:
#                        "release"   : 14.1
#                        "selfstore" : 1401
#                        "underscore": 14_1_{n}
#
# PARAMS OUT: rc       = return code
#             newVer   = converted format 
#
# RETURN CODES: 0 = TK version was converted
#               1 = TK version was not converted
#------------------------------------------------------------------------------
sub convert_tkVersion {
	#
	# Convert from 14.1.n format to other formats
	#
	my ($tkVer, $verType) = @_;
	
	my ($maj, $min, $ver);
	my $newVer;
	my $rc = $ERROR;      # Default: not converted
	my $us = "_";
	my $dot = ".";
	my $zero = "0";
	
	# Is tkVer in correct format?
	if ($tkVer =~ m/(\d\d).(\d).(\d)/) {
		
		# Split into three parts
		($maj, $min, $ver) = split (/\./, $tkVer);
		
		# Reassemble based on verType
		if ($verType eq "underscore") {
			$newVer = "${maj}${us}${min}${us}${ver}";
			$rc = 0;
		}
		elsif ($verType eq "selfstore") {
			$newVer = "${maj}${zero}${min}";
			$rc = 0;	
		}
		elsif ($verType eq "release") {
			$newVer = "${maj}${dot}${min}";
			$rc = 0;	
		}
		return ($rc, $newVer);
	}
	else {
		print "TK version, \"$tkVer\", is not in correct format for conversion.\n";	
	}
} # sub convert_tkVersion

1;
__END__
