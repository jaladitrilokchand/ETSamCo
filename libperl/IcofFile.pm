#!/usr/bin/perl
#==============================================================================
#
#    FILE: IcofFile.pm
#
# CREATOR: Gregg F. Stadtlander  (stadtlag)
#    DEPT: C4U
#    DATE: 05/21/2002
#
#-PURPOSE----------------------------------------------------------------------
# Perl module for locking, unlocking, and reading files
#------------------------------------------------------------------------------
#
#-CHANGE LOG-------------------------------------------------------------------
# 05/21/2002 GFS  Initial coding.
# 09/30/2003 SMP  Added additional function ReadDirToList, with similar
#                 functionality as ReadFileToList.
# 11/05/2007 GFS  Updated to require vars to be declared (use strict).
#==============================================================================

use strict;

# Set this file up as a PERL module
package IcofFile;
require Exporter;
my @ISA = qw(Exporter);
my @EXPORT = qw(DoIOwnFileLock IsFileLocked LockFile ReadFileToList ReadDirToList TryToLockFile TryToUnlockFile UnlockFile WaitForFile WriteListToFile);
my @EXPORT_OK = qw();

my $OK = 1;
my $ERROR = 0;
my $TRUE = 1;
my $FALSE = 0;
my $WAIT_TIMEOUT = 20;


#print "Hello - testing IcofFile.pm\n";
#print " IsFileLocked() = ", IsFileLocked("test"), "\n";
#print " LockFile() = ", LockFile("test"), "\n";
#print " IsFileLocked() = ", IsFileLocked("test"), "\n";
#print " ReadFileToList() = ", ReadFileToList("test", \@list, 1), "\n";
#print " ReadDirToList() = ", ReadDirToList("test", \@list, 1), "\n";
#print " UnlockFile() = ", UnlockFile("test"), "\n";
#print " WriteListToFile() = ", WriteListToFile("file", 1, @list), "\n";
#print " IsFileLocked() = ", IsFileLocked("test"), "\n";

#exit 0;

#------------------------------------------------------------------------------
# FUNCTION: DoIOwnFileLock(file)
#  PURPOSE: Returns true if the current user and process id match the one 
#           in the lock file
#  PARAMS IN: file = input file
# PARAMS OUT: n/a
#
# RETURN CODES: 1 = true
#               0 = false
#------------------------------------------------------------------------------
sub DoIOwnFileLock {

  my($file) = @_;

  # Return error if the file is not locked
  return $FALSE if (IsFileLocked($file) == $ERROR);

  # Read the lock file and return true if the current user and process id
  #  match the lock file contents
  my $lockfile = "$file.lock";
  my @content = ();
  return $FALSE if (! ReadFileToList($lockfile, \@content, 0));

  return $TRUE if ($content[0] =~ /^$ENV{USER};/);

  return $FALSE;

}


#------------------------------------------------------------------------------
# FUNCTION: IsFileLocked(file)
#  PURPOSE: Returns true if the file exists and file.lock exists
#  PARAMS IN: file = input file
# PARAMS OUT: n/a
#
# RETURN CODES: 1 = true
#               0 = false
#------------------------------------------------------------------------------
sub IsFileLocked {

  my($file) = @_;

  # Return false if lock file doesn't exist
  my $lockfile = "$file.lock";
  return $FALSE if (! -e $lockfile);

  return $TRUE;

}


#------------------------------------------------------------------------------
# FUNCTION: LockFile(file, user)
#  PURPOSE: Locks a file by creating a "lock file" which contains information
#           about who the file was locked by and when.  Returns error if the
#           file is already locked.
#  PARAMS IN: file = input file
#             user = userid of person locking file
# PARAMS OUT: n/a
#
# RETURN CODES: 1 = ok
#               0 = error
#------------------------------------------------------------------------------
sub LockFile {

  my($file, $user) = @_;

  # If the file is locked then wait until the file is free.  If it times out
  #  then return error.
  if (IsFileLocked($file) == $TRUE) {
    return $ERROR if (WaitForFile($file) == $ERROR);
  }

  my $lockfile = "$file.lock";
  my $lockdate = `date`;
  my $pid      = $<;

  # Open the lock file for writing
  if (! open(FILE, "> $lockfile")) {
    print "(lockFile) unable to open $lockfile for writing.\n";
    return $ERROR;
  }

  # Write to the lock file
  print FILE "$ENV{USER};$pid;$lockdate";

  # Close the lock file
  close FILE;

  return $OK;

}


#------------------------------------------------------------------------------
# FUNCTION: ReadFileToList(file, list, lockFileFlag)
#  PURPOSE: Reads the specifed file into a list.
#            (usage = ReadFileToList($file, \@list, 0|1))
#  PARAMS IN: file = input file
#             list = output list
#             useFileLock = { 0 = don't lock file | 1 = lock file }
# PARAMS OUT: list = output list
#
# RETURN CODES: 1 = ok
#               0 = error
#------------------------------------------------------------------------------
sub ReadFileToList  {

  my($file, $refList, $useFileLock) = @_;

  # Return error if file does not exist
  return $ERROR if (! -e $file);

  # Lock the file
  if ($useFileLock) {
    return $ERROR if (LockFile($file) == $ERROR);
  }

  # Open it for reading
  if (! open(FILE, "$file")) {
    print "ERROR\n-----\nUnable to open file ...\n       $file\n";
    return $ERROR;
  }

  # Read the file into the list
  chomp(@$refList = <FILE>);

  # Close it
  close FILE;

  # Unlock the file
  if ($useFileLock) {
    return $ERROR if (UnlockFile($file) == $ERROR);
  }

  return $OK;

}


#------------------------------------------------------------------------------
# FUNCTION: ReadDirToList(directory, list, lockFileFlag)
#  PURPOSE: Reads the specifed directory into a list.
#            (usage = ReadDirToList($directory, \@list, 0|1))
#  PARAMS IN: directory = input directory
#             list = output list
#             useFileLock = { 0 = don't lock directory | 1 = lock directory }
# PARAMS OUT: list = output list
#
# RETURN CODES: 1 = ok
#               0 = error
#------------------------------------------------------------------------------
sub ReadDirToList {

  my($directory, $refList, $useFileLock) = @_;

  # Return error if directory does not exist
  return $ERROR if (! -e $directory);

  # Lock the directory
  if ($useFileLock) {
    return $ERROR if (LockFile($directory) == $ERROR);
  }

  # Open it for reading
  if (! opendir(DIR, "$directory")) {
    print "ERROR\n-----\nUnable to open directory ...\n       $directory\n";
    UnlockFile($directory) if ($useFileLock);
    return $ERROR;
  }

  # Read the directory into the list
  @$refList = ();
  my $currentName = "";
  while ($currentName = readdir(DIR)) {

    # Skip the listing of ".", ".." and ".OldFiles" that are included with
    #   the directory
    chomp($currentName);
    next if ($currentName =~ /^\.\.?$/);
    next if ($currentName =~ /^.OldFiles/);
    push(@$refList, $currentName);
  }

  # Close it
  closedir DIR;

  # Unlock the directory
  if ($useFileLock) {
    return $ERROR if (UnlockFile($directory) == $ERROR);
  }

  return $OK;
}


#------------------------------------------------------------------------------
# FUNCTION: UnlockFile(file)
#  PURPOSE: Unlocks a file by removing its "lock file".  Returns error if file
#           is not locked.
#  PARAMS IN: file = input file
# PARAMS OUT: n/a
#
# RETURN CODES: 1 = ok
#               0 = error
#------------------------------------------------------------------------------
sub UnlockFile {

  my($file) = @_;

  # Return error I do not own the lock file
  return $ERROR if (DoIOwnFileLock($file) == $FALSE);

  # Unlock the file
  my $lockFile = "$file.lock";
  return $ERROR if (! unlink($lockFile));

  return $OK;

}


#------------------------------------------------------------------------------
# FUNCTION: WaitForFile(file)
#  PURPOSE: Waits for the file to become unlocked.  If the file does not 
#           become unlocked within 20 seconds, a message is displayed and error
#           is returned.
#  PARAMS IN: file = input file
# PARAMS OUT: n/a
#
# RETURN CODES: 1 = ok
#               0 = error
#------------------------------------------------------------------------------
sub WaitForFile {

  my($file) = @_;

  my $i = 1;
  for ($i = 1; $i <= $WAIT_TIMEOUT; $i ++) {
    return $OK if (IsFileLocked($file) == $FALSE);
    sleep 1;
  }

  return $ERROR;

}


#------------------------------------------------------------------------------
# FUNCTION: WriteListToFile(file, lockFileFlag, list)
#  PURPOSE: Writes the list contents to the specified file
#            (usage = WriteListToFile($file, 0|1, @list))
#  PARAMS IN: file = input file
#             useFileLock = { 0 = don't lock file | 1 = lock file }
#             list = output list
# PARAMS OUT: n/a
#
# RETURN CODES: 1 = ok
#               0 = error
#------------------------------------------------------------------------------
sub WriteListToFile {

  my($file, $useFileLock, @list) = @_;

  # Lock the file
  if ($useFileLock) {
    return $ERROR if (LockFile($file) == $ERROR);
  }

  # Open it for writing
  if (! open(FILE, ">$file")) {
    print "ERROR\n-----\nUnable to open file ...\n       $file\n";
    return $ERROR;
  }

  # Read the file into the list
  print FILE join("\n", @list), "\n";

  # Close it
  close FILE;

  # Unlock the file
  if ($useFileLock) {
    return $ERROR if (UnlockFile($file) == $ERROR);
  }

  return $OK;

}


#=============================  END OF FILE  ==================================
