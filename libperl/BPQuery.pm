#############################################################################
# Package:     perl BluePages library
# Module:      HTTP API library
# Filename:    BPQuery.pm
# Author:      Marcel Kinard/Raleigh/IBM
# RCS Id:      $Id: BPQuery.pm,v 1.19 2009/10/12 18:57:26 marcelk Exp $
# Web site:    http://w3.opensource.ibm.com/projects/perlbp
# License:     IBM Internal open source
# Copyright:   1998, 2005 IBM
# Description: This is a perl interface to BluePages. This interface hides 
#              all the server and network specifics, making it really easy
#              to use. All data is accessed via perl5 objects. Note that 
#              this uses the bluepages HTTP interface on port 80, not LDAP,
#              but the data is the same. You can get the latest version
#              of this file, sample programs, and other resources at the
#              web site listed above.
#
# This also requires BPRecord.pm
#
# Function calls are:
#
# new BPQuery(QUERY_TYPE, SEARCH_ARGUMENT)
#   creates a new BPQuery object, executes the query, and loads the results
#   into the object. The search argument should be in plain text format,
#   because this API will automatically escape all the special URL characters.
#   A wildcard may be specified using the percent (%) character.
#   i.e., $my_query = new BPQuery('allByName', 'Kinard, %Marcel%');
#   Here is a list of each query type in a sample invocation:
#       $my_query = new BPQuery('byCnum', '532416897');
#       $my_query = new BPQuery('bySerial', '532416');
#       $my_query = new BPQuery('byInternetAddr', 'marcelk@us.ibm.com');
#       $my_query = new BPQuery('allByName', 'Kinard, %Marcel%');
#       $my_query = new BPQuery('allByNameLite', 'Kinard, %Marcel%');
#       $my_query = new BPQuery('allByNameFuzzy', 'Kinard,M');
#       $my_query = new BPQuery('allByNameFuzzyLite', 'Kinard,M');
#       $my_query = new BPQuery('allByNotesID',
#                               'CN=Marcel Kinard/OU=Raleigh/O=IBM@IBMUS');
#       $my_query = new BPQuery('allByNotesIDLite',
#                               'CN=Marcel Kinard/OU=Raleigh/O=IBM@IBMUS');
#       $my_query = new BPQuery('directReportsOf', '096127897');
#           096127897 is the cnum of a manager, not a standard employee
#       $my_query = new BPQuery('directReportsOfLite', '096127897');
#       $my_query = new BPQuery('managerChainFor', '532416897');
#       $my_query = new BPQuery('membersOfDept', '68 EXDA');
#           the search argument must be division and dept separated by a space
#       $my_query = new BPQuery('membersOfDeptLite', '68 EXDA');
#       $my_query = new BPQuery('depts', '68 EXDA');
#           returns the department title, does not return any people
#       $my_query = new BPQuery('orgCodes', '68');
#           returns the division information, does not return any people
#       $my_query = new BPQuery('cCodes', '897');
#           returns the country name for the specified country code,
#           does not return any people
#       $my_query = new BPQuery('workLoc', 'SMB');
#           returns the work location details for that work location code,
#           does not return any people
#       $my_query = new BPQuery('eCodes', 'P');
#           returns the employee type description (ie, Regular, Supplemental)
#           for that employee code, does not return any people
#
# The search results are loaded into memory, so beware of large results.
#
# After a BPQuery object is instantiated, embedded in it is a collection of 
# BPRecord objects. The following BPQuery methods are available for managing 
# the collection:
#
# $my_query->status()
#   returns the string indicating the query completion status. A successful
#   query should have an empty string.
# $my_query->numRecords()
#   returns the integer number of BPRecord objects in the collection (usually
#   this is the number of people found, unless you queried for a different
#   type of object).
# $my_query->recordKeys()
#   returns an array of the primary keys of each of the BPRecord objects
#   in the collection.
# $my_query->record(KEY)
#   returns a BPRecord object using a primary key that was returned
#   from the recordKeys() method.
#   i.e., $my_query->record('532416897')
#   It is possible to integrate the BPRecord methods on one line,
#   i.e., $my_query->record('532416897')->valueOf('NAME')
# $my_query->dump()
#   returns a text string of all the BPRecord objects in human-readable format
#
# Look at the comments in BPRecord.pm for a list of the methods available
# on the BPRecord object.
#
# sample program:
#
#   use BPRecord;
#   use BPQuery;
#   $query = new BPQuery('allByName', 'Kinard%');
#   print "status = " . $query->status() . "\n";
#   print $query->numRecords() . " records retrieved.\n";
#   for $recordKey ($query->recordKeys()) {
#     print "--------------------------------------\n";
#     print "record for key $recordKey:\n";
#     for $valueKey ($query->record($recordKey)->valueKeys()) {
#       print  "$valueKey = " . 
#              $query->record($recordKey)->valueOf($valueKey) . "\n";
#     }
#   }
#
# A cnum is the concatenation of the employee serial number and the three
# digit country code. For example, my employee serial is 532416 and the
# country code for the USA is 897. Then my cnum is 532416897. An employee 
# serial is unique within a single country, but not worldwide. This is why the 
# country code must be used with the employee serial number to generate a 
# primary key for a worldwide employee. To find the cnum of any employee, goto 
# w3.ibm.com/bluepages, search for them, and in the result page mouse-over the 
# "Report to chain" link without clicking it. If your browser is configured to 
# display a URL in the status bar, you can see the URL of the link 
# which should contain in it the employee's cnum, usually following 
# "uid=". You can test this by looking at your own entry on 
# w3.ibm.com/bluepages
# 
# Please also review the service documentation at
# http://bluepages.ibm.com/BpHttpApisv3/bpwebapidoc.html
# Descriptions of all the valueKeys may be found there by clicking on 
# "WSAPI" in the navbar and then in the section titled "Directory Tables".
#
# This tool conforms to version 3 of the WSAPI interface to Bluepages.
# Note that v1.0 of the WSAPI/BPWSAPI interface to Bluepages will be sunset
# on 12/31/2009. Because perlbp version <= 1.9 used v1.0 of WSAPI/BPWSAPI,
# users of perlbp will need to upgrade to at least perlbp version 1.10
# before the end of 2009.
#
# This code will query the APILocator to get the fully-qualified
# URL of each of the possible queries. The APILocator is guaranteed to be
# constant, but the URL for each type of query can be changed (it has
# changed already once, this will protect you from further changes.)
# 
# There also is an option to stream the query results to a comma-delimited
# file. Doing so will put all the output in the file, and none will be in
# memory; this is because some queries can be quite large. This can be handy
# when you want to import the query results into your favorite database.
# Here is an example for doing this:
#
#   use BPQuery;
#   use BPRecord;
#   $query = new BPQuery('allByName', 'Kinard%', 'toDelFile', 'myfile.del');
#   print "status = " . $query->status() . "\n";
#   print $query->numRecords() . " records retrieved.\n";
#
# Notice that two additional parameters exist when creating the BPQuery 
# object: a flag to output the results to a comma-delimited file, and the 
# filename that you want the results written to. If you are considering
# using this to download large pieces of Bluepages, then you should consider
# instead using the Extract Services described at
# http://tst2bluepages.mkm.can.ibm.com/
#
# If you would like a Java implementation of this functionality, please
# refer to the "w3java" project on the IIOSB (IBM Internal OpenSource Bazaar)
# at https://w3.opensource.ibm.com/projects/w3java
#
#############################################################################

package BPQuery;

use strict;
use Socket;
use FileHandle;

# URL to the API Locator. The API Locator will tell us what query types
# are supported, and what server to call for each query type.
$BPQuery::apiLocatorUrl = "http://bluepages.ibm.com/BpHttpApisv3/apilocator";

# identifier for the HTTP User Agent of the perlbp release.
$BPQuery::version = '1.10';

# valid query types, and the primary key of each type of query.
# The APILocator is really for use internal to this script.
%BPQuery::keyTypes = ('byCnum'              => 'CNUM',
                      'bySerial'            => 'CNUM',
                      'byInternetAddr'      => 'CNUM',
                      'allByName'           => 'CNUM', 
                      'allByNameLite'       => 'CNUM',
                      'allByNameFuzzy'      => 'CNUM',
                      'allByNameFuzzyLite'  => 'CNUM',
                      'allByNotesID'        => 'CNUM',
                      'allByNotesIDLite'    => 'CNUM', 
                      'directReportsOf'     => 'CNUM',
                      'directReportsOfLite' => 'CNUM',
                      'managerChainFor'     => 'CNUM',
                      'membersOfDept'       => 'CNUM',
                      'membersOfDeptLite'   => 'CNUM',
                      'depts'               => 'DEPT',
                      'orgCodes'            => 'ORGCODE',
                      'cCodes'              => 'CC',
                      'workLoc'             => 'WORKLOC',
                      'eCodes'              => 'CODE',
                      'APILocator'          => 'NAME');

sub new {
  my($class, $type, $parm, $option, $optionParm) = @_;
  my $self = {};
  # valid options
  my @options = ('toDelFile');
  # check that the query type is valid
  die "unsupported query type: $type: supported types are " . 
      join(", ", keys(%BPQuery::keyTypes)) . ":"
    unless ($type && (grep(/^$type$/, keys(%BPQuery::keyTypes))));
  if ($type ne "APILocator") {
    # check that a parameter was specified
    die "no parameter specified"
      unless ($parm);
  }
  $self->{'queryType'} = $type;
  $self->{'keyType'} = $BPQuery::keyTypes{$type};
  $self->{'parm'} = _urlEsc($parm);
  if ($option) {
    # do the same for any special options
    die "unsupported option: $option" unless (grep(/^$option$/, @options));
    $self->{'option'} = $option;
    $self->{'optionParm'} = $optionParm;
  }
  $self->{'records'} = {};
  # the recordOrder field is an array because we need to capture the order in
  #   which the records were given to us by the server (mgr reporting chain)
  $self->{'recordOrder'} = [];
  $self->{'numRecords'} = 0;
  $self->{'status'} = "incomplete data set";
  bless $self;

  # if we haven't downloaded the APILocator info, then do it now
  # before doing the real query. We can cache the APILocator info
  # as a package variable. It's nice that the APILocator is just
  # a special query, so we can reuse the same subroutines for the
  # APILocator as for a real query. Each record in the APILocator
  # has a NAME (same as queryType above) and URL (just need to
  # append the query parm). So in the real query we will 
  # use $url = $BPQuery::apiLocator->record($queryType)->valueOf('URL');
  # If this doesn't quite make sense just open your browser to
  # http://bluepages.ibm.com/cgi-bin/APILocator.pl?API=bluepages1.0
  # and then it should become more obvious.
  if (($self->{'queryType'} ne 'APILocator') && (!$BPQuery::apiLocator)) {
    # this almost looks recursive, but we are drilling just one extra level
    $BPQuery::apiLocator = new BPQuery('APILocator', '');
    if ($BPQuery::apiLocator->status()) {
      # an error occurred, return it
      $self->{'status'} = $BPQuery::apiLocator->status();
      return($self);
    }
  }
  # we should have the APILocator info cached now

  # execute the real query
  $self->_execute;
  # return the object handle
  return($self);
}

sub status {
  my($self) = @_;
  # return the status string, which contains any error descriptions
  return ($self->{'status'});
}

sub _execute {
  my($self) = @_;
  my($proto, $addr, $sin, $request, $inHeader, $line, $cnum, $record,
     $discard, $recordsSent, $url, $protocol, $hostname, $relativeUrl, $port,
     $name, $aliases, $type, $len, $iaddr, $specialPort, $hasStatus);

  # there are slightly different formats between the APILocator 
  # and a real query. So handle that difference here.
  if ($self->{'queryType'} eq 'APILocator') {
    $url = $BPQuery::apiLocatorUrl . "?format=text";
  } else {
    # first check that the requested queryType is known in the APILocator
    if (!defined($BPQuery::apiLocator->record($self->{'queryType'}))) {
      $self->{'status'} = "APILocator does not contain queryType " .  
        $self->{'queryType'};
      return;
    }
    # the APILocator contains everything except the parameter value
    $url = $BPQuery::apiLocator->record($self->{'queryType'})->valueOf('URL') .
      $self->{'parm'};
  }

  # figure out where to connect to
  ($protocol, $hostname, $relativeUrl, $specialPort) = _parseUrl($url);

  $proto = getprotobyname('tcp');
  if (defined($specialPort) && ($specialPort)) {
    $port = $specialPort;
  } else {
    ($name, $aliases, $port) = getservbyname($protocol, 'tcp');
    if ((!defined($port)) || ($port == 0)) {
      if ($protocol eq "http") {
        $port = 80;
      } else {
        $self->{'status'} = "unknown protocol '$protocol'";
        return;
      }
    }
  }

  # create a TCP client socket connection to the BluePages server.
  
  # create the local socket endpoint
  unless (socket(SOCKET, PF_INET, SOCK_STREAM, $proto)) {
    $self->{'status'} = "local socket: $!";
    return;
  }

  $addr = inet_aton($hostname);
  if (!defined($addr) || (!$addr)) {
    $self->{'status'} = "inet_aton (get hostname): $hostname: $!";
    return;
  }

  # connect the local endpoint to the remote endpoint.
  # set up the sin structure - just like C code
  $sin = sockaddr_in($port, $addr);
  unless (connect(SOCKET, $sin)) {
    $self->{'status'} = "connect to $hostname port $port: $!";
    return;
  }

  # must flush the buffer immediately instead of waiting the data to be
  # sent in larger chunks. Without doing this, the server may not receive
  # the request because it is stuck in a buffer here on the client.
  autoflush SOCKET 1;

  # build and send the HTTP request to the server
  $request = "GET $relativeUrl HTTP/1.0" . "\n";
  $request .= "User-Agent: perlbp $BPQuery::version (unknown; perl5)" . "\n";
  # don't forget the blank line to indicate the end of our request
  print SOCKET $request . "\n";

  # parse the response headers that the server sends to us
  $inHeader = 1;
  $line = <SOCKET>;
  if (!defined($line)) {
    $self->{'status'} = "connected but unable to read from socket (firewall?) (hostname=$hostname, port=$port, queryType=$self->{'queryType'})";
    return;
  }
  $line = _stripNewline($line);
  # verify that we receive a return code 200 from the server
  unless ($line =~ m|^HTTP/1.\d 200|) {
    $self->{'status'} = "HTTP header: $line";
    return;
  }
  # discard the rest of the HTTP headers. Just look for the blank line that
  # indicates the end of the headers
  while ($inHeader) {
    $line = <SOCKET>;
    $line = _stripNewline($line);
    $inHeader = 0 if ($line eq "");
  }

  # check if the toDelFile option was requested
  if ((defined($self->{'option'})) && 
      ($self->{'option'} eq "toDelFile")) {
    $self->{'fileHandle'} = *DELFILE;
    unless ((defined($self->{'fileHandle'})) &&
            (open($self->{'fileHandle'}, ">".$self->{'optionParm'}))) {
      $self->{'status'} = "unable to open file $self->{'optionParm'}: $!";
      return;
    }
  }

  # here comes the data
  $discard = 0;
  $hasStatus = 0;
  while ($line = <SOCKET>) {
    $line = _stripNewline($line);
    if (($line =~ /^$self->{'keyType'}:/) && !$discard) {
      # a keyType field indicates the beginning of a new record
      # if there is an old record, add it to the list
      $self->_saveRecord($record) if ($record);
      # create a new record
      $record = new BPRecord();
    } elsif ($line =~ m|^#|) {
      # this is the end-of-data indicator, which includes a return code,
      # the number of records it thinks it sent, plus a status message
      $hasStatus = 1;
      $line =~ m|count=(\d+)|;
      $recordsSent = "$1";
      $line =~ m|rc=(\d+)|;
      if ($1 == 0) {
          $self->{'status'} = 0;
      } else {
          $self->{'status'} = "BluePages API encountered return code $1 from server.";
          $line =~ m|message=(.*)|;
          $self->{'status'} .= " " . $1;
      }
      $discard = 1;
      $self->_saveRecord($record) if ($record);
    }
    if (!$discard) {
      # add the field data to the current record
      $record->addData($line);
    }
  }

  # no more data on the socket - that's the end. Start our cleanup.
  close(SOCKET);

  unless ($self->{'status'} == 0) {
    return;
  }

  unless ($hasStatus == 1) {
    $self->{'status'} = "No status received from Bluepages server for query.";
    return
  }

  # verify that the number of records that the server thinks it sent
  # matches the number we received
  if ($recordsSent == $self->{'numRecords'}) {
    $self->{'status'} = "";
  } elsif ($recordsSent == "") {
    $self->{'status'} = "did not receive final record count";
  } else {
    $self->{'status'} = "not all records received: expected $recordsSent, got "
			. $self->{'numRecords'};
  }

  # clean up any options that were used
  if ((defined($self->{'option'})) && 
      ($self->{'option'} eq "toDelFile") &&
      (defined($self->{'fileHandle'}))) {
    close($self->{'fileHandle'});
    undef($self->{'fileHandle'});
  }
}

sub dump {
  my($self) = @_;
  my($result) = "";
  for ($self->recordKeys) {
    $result .= $self->record($_)->dump();
  }
  return($result);
}

sub recordKeys {
  my($self) = @_;
  # return a list of all the cnums of the records
  # return(keys(%{$self->{'records'}}));
  # but also do it in the order that we received them from the server
  return(@{$self->{'recordOrder'}});
}

sub record {
  my($self, $cnum) = @_;
  # return a object handle to the record of the specified cnum
  if (defined($self->{'records'}->{$cnum})) {
    return($self->{'records'}->{$cnum})
  } else {
    return(undef);
  }
}

sub numRecords {
  my($self) = @_;
  # return the number of records we have
  return($self->{'numRecords'});
}

sub _deltaQueryTypes {
  my($self) = @_;
  my($queryType, $differences, @apiTypes);
  # check to see if the keyTypes hardcoded above agrees with the APILocator.
  $differences = 0;
  if (!($BPQuery::apiLocator)) {
    print "APILocator info is not present\n";
    return;
  }
  # get a simple list of the queryTypes from the APILocator
  foreach $queryType ($BPQuery::apiLocator->recordKeys()) {
    push(@apiTypes, $BPQuery::apiLocator->record($queryType)->valueOf('NAME'));
  }
  # see if APILocator has queryTypes not in our hardcoded list
  foreach $queryType (@apiTypes) {
    if (!grep(/^$queryType$/, keys(%BPQuery::keyTypes))) {
      # I choose not to support SLAPHAPI
      next if ($queryType eq "SLAPHAPI");
      print "APILocator contains " . $queryType . 
        "that is not in known keyTypes (new type?)\n";
      $differences++;
    }
  }
  # see if our hardcoded list has queryTypes not in APILocator
  foreach $queryType (keys(%BPQuery::keyTypes)) {
    if (!grep(/^$queryType$/, $BPQuery::apiLocator->recordKeys())) {
      # APILocator is expected to be found here, it is a special internal value
      next if ($queryType eq "APILocator");
      print "keyTypes contains " . $queryType . 
        "that is not in APILocator (deleted type?)\n";
      $differences++;
    }
  }
  print "differences: $differences\n";
  return;
}

sub _parseUrl {
  my($url) = @_;
  my($protocol, $hostname, $relativeUrl, $specialPort);
  $url =~ /^([^:]+):\/\/([^\/]+)(\/.*)/;
  $protocol = $1;
  $hostname = $2;
  $relativeUrl = $3;

  # look for nondefault port after hostname
  if ($hostname =~ /^([^:]+):(\d+)/) {
    $hostname = $1;
    $specialPort = $2;
  } else {
    $specialPort = undef;
  }
  return($protocol, $hostname, $relativeUrl, $specialPort);
}

sub _stripNewline {
  my($line) = @_;
  # an internal function to strip <cr><lf> characters from socket data
  $line =~ s/\r//g;
  $line =~ s/\n//g;
  return($line);
}

sub _saveRecord {
  my($self, $record) = @_;
  # an internal function to write/save existing records
  if ((defined($self->{'option'})) && 
      ($self->{'option'} eq "toDelFile") &&
      (defined($self->{'fileHandle'}))) {
    # write list to disk as a .del file
    $record->toDelFile($self->{'fileHandle'});
  } else {
    # regular list in memory
    my($key) = $record->{$self->{'keyType'}};
    $self->{'records'}->{$key} = $record;
    # now capture the key value for the recordOrder
    push(@{$self->{'recordOrder'}}, $key);
  }
  $self->{'numRecords'}++;
}

sub _urlEsc {
  my($value) = @_;
  # we are handling % specially, since it is used to escape special characters.
  # Note that because there will be arguments to s// that perl requires
  # pattern matching operators be escaped with a backslash
  my($unescaped, $escaped);
  my %urlEscChars = (' '  => '%20',
                     '"'  => '%22',
                     '#'  => '%23',
                     '&'  => '%26',
                     '\+' => '%2B',
                     '/'  => '%2F',
                     ':'  => '%3A',
                     ';'  => '%3B',
                     '<'  => '%3C',
                     '='  => '%3D',
                     '>'  => '%3E',
                     '\?' => '%3F',
                     '\@' => '%40',
                     '\[' => '%5B',
                     '\]' => '%5D',
                     '\^' => '%5E',
                     '\{' => '%7B',
                     '\|' => '%7C',
                     '\}' => '%7D',
                     '\~' => '%7E');
  # hide all the percent signs %, we need to do it first so not to interfere
  # with the rest of the special characters listed in the array above.
  $value =~ s/\%/\%25/g;
  for $unescaped (keys(%urlEscChars)) {
    $escaped = $urlEscChars{$unescaped};
    $value =~ s/$unescaped/$escaped/g;
  }
  return($value);
}

1;
