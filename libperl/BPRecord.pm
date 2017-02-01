#############################################################################
# Package:     perl BluePages library
# Module:      HTTP API library
# Filename:    BPRecord.pm
# Author:      Marcel Kinard/Raleigh/IBM
# RCS Id:      $Id: BPRecord.pm,v 1.5 2005/02/16 17:53:02 marcelk Exp $
# Web site:    http://w3.opensource.ibm.com/projects/perlbp
# License:     IBM Internal open source
# Copyright:   1998, 2004 IBM
# Description: Manipulate records from a BPQuery object. This file should
#              be used with BPQuery.pm, it won't do much without it.
#              You can get the latest version of this file, sample programs,
#              and other resources at the web site listed above.
#
#
# Please read the instructions at the top of BPQuery.pm first.
#
# $my_record->valueKeys()
#   returns an array of all the keys of the values in this record
#   (equivalent to database column names)
# $my_record->valueOf(KEY)
#   returns a string value for the specified key.
#   i.e., $my_record->valueOf('NAME')
# $my_record->dump()
#   returns the contents of the record as a human-readable text string
#
#############################################################################

package BPRecord;

use strict;

sub new {
  my($class) = @_;
  my $self = {};
  bless $self;
  return($self);
}

sub addData {
  my($self, $line) = @_;
  # with a line of data from the server, add it to the current record
  my($key, $value);
  # make sure it is of the form KEY:VALUE
  return unless ($line =~ /^(\w+):(.*)/);
  $key = $1;
  $value = $2;
  # remove leading and trailing space from the value
  $value =~ s/^\s+//;
  $value =~ s/\s+$//;
  $self->{$key} = $value;
}

sub valueOf {
  my($self, $key) = @_;
  # return the value of a specific key in this record
  if (defined($self->{$key})) {
    return($self->{$key})
  } else {
    return(undef);
  }
}

sub valueKeys {
  my($self) = @_;
  # return a list of all the keys in this record
  return(keys(%$self));
}

sub toDelFile {
  my($self, $fileHandle) = @_;
  my($key, $value, $line);
  # always output the columns in alpabetical order
  for $key (sort($self->valueKeys)) {
    $line .= "," if ($line);
    $value = $self->{$key};
    # double quote marks are our string delimiter. Make sure they don't
    # exist as part of the value or DB2 will get confused
    $value =~ s/"//g;
    $value = qq|"$value"|;
    $line .= $value;
  }
  print $fileHandle $line . "\n";
}

sub dump {
  my($self) = @_;
  my($result);
  $result = "BPRecord dump:\n";
  for (sort($self->valueKeys)) {
    $result .= "  $_: " . $self->valueOf($_) . "\n";
  }
  return($result);
}

1;
