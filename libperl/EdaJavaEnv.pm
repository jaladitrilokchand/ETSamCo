#!/usr/bin/perl
#==============================================================================
#
#    FILE: EdaJavaEnv.pm
#  SYNTAX: See "printSyntax" methods below or execute the program with -h
#
# CREATOR: Gregg Stadtlander
#    DEPT: AZYX
#    DATE: 06/20/2011
#
#-PURPOSE----------------------------------------------------------------------
# This perl module meant to hold some shared methods for setting up
# the java environment for EDA java applications.
# ------------------------------------------------------------------------------
#
#-CHANGE LOG-------------------------------------------------------------------
# 06/20/2011 GFS  Rewrote to use static lists of jar files to improve
#                 performance.
# 02/27/2012 GFS  Updated web service client. Added support for java 1.6. Added
#                 axis.jar to the CQ jars.
# 03/13/2012 GFS  Added com.ibm.ws.webservices.thinclient_6.1.0.jar to
#                 classpath.
# 04/04/2013 GFS  Removed soon to be depricated Shell perl module.
# 09/16/2013 GFS  Made Java 1.6 the default version.
# 06/25/2014 GFS  Made Java 1.7 the default version
#==============================================================================

use warnings;
use strict;
use IcofFile;
use File::Path;

#------------------------------------------------------------------------------
# Define it as Perl module
#------------------------------------------------------------------------------
package EdaJavaEnv;
use Exporter;
@EdaJavaEnv::ISA    = qw(Exporter);

# Export anything other than methods (do not pollute the ENV)
@EdaJavaEnv::EXPORT = qw(@UTIL_JARS @ETREE_JARS @MAIL_JARS @LEVELHIST_JARS @CLEARQUEST_JARS @JFACE_JARS);


#------------------------------------------------------------------------------
# Global variables
#------------------------------------------------------------------------------
# Program name
my $progName = "EdaJavaEnv.pm";

# Constants.
my $DIR_ERROR = "UnknownDirectory";
my $JDK_DIR_14 = "/afs/eda.fishkill.ibm.com/cte/tools/java/1.4.2";
my $JDK_DIR_15 = "/afs/eda.fishkill.ibm.com/cte/tools/java/1.5.0.20061002";
my $JDK_DIR_16 = "/afs/eda.fishkill.ibm.com/cte/tools/java/1.6.0";
my $JDK_DIR_17= "/afs/eda.fishkill.ibm.com/cte/tools/java/1.7.0";

my @JAVA_JARS = ( 'jre', 
                  'jre/lib',
                  'lib' );
my @UTIL_JARS = ( 'libx/iipmds/IcofCommon.jar',
                  'libx/iipmds/IcofMom.jar',
                  'libx/iipmds/IcofRole.jar',
                  'libx/iipmds/IcofUtil.jar',
                  'libx/log4j/log4j-1.2.14.jar' );
my @ETREE_JARS = ( 'lib/TkEtreeBase.jar', 
	               'lib/TkEtreeDb.jar',
                   'lib/TkEtreeObjects.jar',
                   'libx/db2/db2fs.jar',
                   'libx/db2/db2java.zip',
                   'libx/db2/db2jcc.jar',
                   'libx/db2/db2jcc_license_cu.jar',
                   'libx/ibm/bpjtk-v3.0.3_b20090914.jar',
                   'libx/ibm/bpwrapper.jar',
                   'libx/ibm/cwa2.jar' );
my @MAIL_JARS = ( 'libx/javamail-1.2/activation.jar',
                  'libx/javamail-1.2/mail.jar',
                  'libx/javamail-1.2/mailapi.jar',
                  'libx/javamail-1.2/smtp.jar' );
my @LEVELHIST_JARS = ( 'lib/TkLevelHist.jar',
                       'lib/TkPatch.jar' );
my @CLEARQUEST_JARS = ( 'libx/iipmds/ClearQuest.jar',
                        'libx/iipmds/ClearQuestClient.jar',
                        'libx/iipmds/axis.jar',
                        'libx/runtimes/com.ibm.ws.webservices.thinclient_6.1.0.jar',
                        'libx/runtimes/com.ibm.ws.webservices.thinclient_7.0.0.jar' );
my @JFACE_JARS = ( 'lib/JfaceBase.jar',
                   'libx/jface/org.eclipse.core.commands_3.2.0.jar',
                   'libx/jface/org.eclipse.equinox.common_3.2.0.jar',
                   'libx/jface/org.eclipse.jface_3.2.2.jar',
                   'libx/jface/org.eclipse.osgi_3.2.3.jar',
                   'libx/jface/org.eclipse.ui.workbench_3.2.2.jar' );


#-----------------------------------------------------------------------------#
#                 M E T H O D  D E F I N I T I O N S                          #
#-----------------------------------------------------------------------------#

#------------------------------------------------------------------------------
# Name   : getJavaExec(javaVersion)
# Purpose: Returns the java executable name with full path
# RCs    : N/A
#------------------------------------------------------------------------------
sub getJavaExec {
  my($javaVersion) = @_;

  my $javaExec = "UnknownJavaExec";

  # Set the path to java for linux or aix
  if ($javaVersion && $javaVersion eq "1.5") {
    $javaExec = $JDK_DIR_15 . "/jre/bin/java";
  }
  else {
    #$javaExec = $JDK_DIR_16 . "/jre/bin/java";
    $javaExec = $JDK_DIR_17 . "/jre/bin/java";
  }

  if (! -f $javaExec) {
    print "[$progName - getJavaExec] $javaExec does not exist\n$!\n";
    return "UnknownJavaExec";
  }

  return $javaExec;

}


#------------------------------------------------------------------------------
# Name   : getJavaExec(javaVersion)
# Purpose: Returns the java executable name with full path
# RCs    : N/A
#------------------------------------------------------------------------------
#sub getJavaExec {
#
#  	my($javaVersion) = @_;
#  	my $javaExec = "UnknownJavaExec";
#	my $os = $^O;
#
#  	# Set the default path to java for linux or aix but only use this for 1.5
#  	if ($javaVersion && $javaVersion eq "1.5") {
#    	$javaExec = $JDK_DIR_15 . "/jre/bin/java";
#    	return $javaExec;
#  	}	
#
#	# Set the search paths based on OS.
#	my @paths = ();
#	if ($os eq "aix") {
#		@paths = ("/usr/bin/java", "/usr/java14/bin/java",
#		         "/usr/java15/bin/java", "$JDK_DIR_16/jre/bin/java");
#    }
#    else {
#		@paths = ("/usr/bin/java", "/etc/alternatives/java", 
#		          "$JDK_DIR_16/jre/bin/java");
#    }
#
#	# Find Java on the search path.
#	foreach $javaExec (@paths) {
#		if (-f $javaExec) {
#			my $rc = system("$javaExec -version 2> /dev/null");
#			return $javaExec if ($rc == 0);
#		}
#	}
#
#  	if (! -f $javaExec) {
#    	print "[$progName - getJavaExec] $javaExec does not exist\n$!\n";
#    	return "UnknownJavaExec";
#  	}
#
#  	return $javaExec;
#
#}


#------------------------------------------------------------------------------
# Name   : getJavaPath(javaVersion)
# Purpose: Returns the java path (ex. /usr/bin/java)
# RCs    : N/A
#------------------------------------------------------------------------------
sub getJavaPath {
  my($javaVersion) = @_;

  if ($javaVersion && $javaVersion eq "15") {
    return $JDK_DIR_15;
  }
  else {
    return $JDK_DIR_16;
  }

}


#------------------------------------------------------------------------------
# Name   : getPlatformClasspathAddons(rootDir)
# Purpose: Returns classpath additions for the current platform.
# RCs    : N/A
#------------------------------------------------------------------------------
sub getPlatformClasspathAddons {
  my($rootDir) = @_;

  # Add all jar/zip files from the libx_platform directory.
  my $os = $^O;

  return $rootDir . "libx_platform/" . $os . "/swt.jar";

}


#------------------------------------------------------------------------------
# Name   : getClassDirectory($rootDir)
# Purpose: Returns the full path of the classes directory
# RCs    : N/A
#------------------------------------------------------------------------------
sub getClassDirectory {
  my($rootDir) = @_;

  return $rootDir . "java/classes/";

}


#------------------------------------------------------------------------------
# Name   : getArgs(@ARGV)
# Purpose: List the command line args and add quotes to args if necessary.
# RCs    : arg list as text.
#------------------------------------------------------------------------------
sub getArgs {
  my (@argv) = @_;

  my $arg = "";
  my $argText = "";
  foreach $arg (@argv) {
    $arg = "\"" . $arg . "\"" if ($arg =~ / +/);
    $argText .= $arg . " ";
  }

  return $argText;

}


#------------------------------------------------------------------------------
# Name   : startClassPath($javaExec)
# Purpose: Returns the UTIL jar files
# RCs    : N/A
#------------------------------------------------------------------------------
sub startClassPath {
    my($javaExec) = @_;

    my @tokens = split("/", $javaExec);
    pop @tokens;
    pop @tokens if ($tokens[scalar(@tokens)-1] eq "bin");
    my $javaDir = join("/", @tokens);

    my $paths = $javaDir . ":" . getJars($javaDir, @JAVA_JARS);
    #my $paths = "";
    #return "CLASSPATH=$paths";

    return $paths;

}


#------------------------------------------------------------------------------
# Name   : getUtilJars($directory)
# Purpose: Returns the UTIL jar files
# RCs    : N/A
#------------------------------------------------------------------------------
sub getUtilJars {
    my($directory) = @_;

    return getJars($directory, @UTIL_JARS);

}


#------------------------------------------------------------------------------
# Name   : getEtreeJars($directory)
# Purpose: Returns the ETREE jar files
# RCs    : N/A
#------------------------------------------------------------------------------
sub getEtreeJars {
    my($directory) = @_;

    return getJars($directory, @ETREE_JARS);

}


#------------------------------------------------------------------------------
# Name   : getMailJars($directory)
# Purpose: Returns the Mail jar files
# RCs    : N/A
#------------------------------------------------------------------------------
sub getMailJars {
    my($directory) = @_;

    return getJars($directory, @MAIL_JARS);

}


#------------------------------------------------------------------------------
# Name   : getLevelHistJars($directory)
# Purpose: Returns the LevelHist jar files
# RCs    : N/A
#------------------------------------------------------------------------------
sub getLevelHistJars {
    my($directory) = @_;

    return getJars($directory, @LEVELHIST_JARS);

}


#------------------------------------------------------------------------------
# Name   : getClearQuestJars($directory)
# Purpose: Returns the ClearQuest jar files
# RCs    : N/A
#------------------------------------------------------------------------------
sub getClearQuestJars {
    my($directory) = @_;

    return getJars($directory, @CLEARQUEST_JARS);

}


#------------------------------------------------------------------------------
# Name   : getJfaceJars($directory)
# Purpose: Returns the JFace jar files
# RCs    : N/A
#------------------------------------------------------------------------------
sub getJFaceJars {
    my($directory) = @_;

    my $path = getJars($directory, @JFACE_JARS);

    my $os = $^O;
    $path .= ":" . "$directory/libx_platform/$os/swt.jar";

    return $path;

}


#------------------------------------------------------------------------------
# Name   : getJars($directory, @jarList)
# Purpose: Returns the ETREE jar files
# RCs    : N/A
#------------------------------------------------------------------------------
sub getJars {
    my($myPath, @jarList) = @_;

    my @myJars = ();
    my $jar;
    foreach $jar (@jarList) {
      if ($myPath =~ /\/$/) {
	push(@myJars, $myPath . $jar);
      }
      else {
	push(@myJars, $myPath . "/". $jar);
      }
    }

    return join(":", @myJars);

}
