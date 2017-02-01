# ********************************************************************************************************
# Program Name: cq2db2.pl
#
# Purpose: Synchronize the EDA TK database and CQ/MDCMS
#
# Inputs: See the application help
#
# Outputs:
#
# Return Codes:
#
# Change Log:
#	2011/09/01 RM Bell		Initial Release
#	2011/10/14 RM Bell		Support Complete to Releases transition
#       2011/11/03 RM Bell              Fun with Quotes and escape characters
#                                       Lowercase the -feature and -defect option
#	2011/11/14 RM Bell		Support going from Reserved to Complete.
#	2011/11/17 RM Bell		More fun with Quotes
#	2011/11/18 RM Bell		Support Interim Injection Process:
#						Added keywords problem_introduction and communication_method to the xml
#       2011/11/28 RM Bell              Add a threshold on the number of restarts
#       2011/11/30 RM Bell              When updating DB2, only update fields that are different
#       2011/11/30 RM Bell              Work around the fact that the user data in the ETREE database is not correct
#                                            For now, I will leave submitter and developer as BPROCESS and make BPROCESS a tester
#       2011/12/07 RM Bell              Work around the fact that the trxid is being reset. Need to get a schema update for this.
#                                       Perform an case insensitive compare on user
#       2011/12/13 RM Bell              Dump out some hashes into email sent on a fatal error
#       2011/12/19 RM Bell              Work around for not getting a withdrawn reason
#       2012/02/07 RM Bell              Add threshold for how often restart emails are sent
#       2012/02/14 RM Bell		Log the start and end time.
#  	2012/02/20 RM Bell		Added the noendtime. This allows the application to be run for an time slice without updating the last run time.
#       2012/03/08 RM Bell              Allow modifying of "onhold" Injection/Change Requests
#                                       Eliminate check that the trxid is null
#                                       Stop setting trxid in second update. Schema has been fixed
#                                       Temporarily remove the code to move it to approved state
#                                       Phase one of improved conflict checking. Add what fields conflict to the email.
#      2012/03/22 RM Bell		uncomment out the availability check on the release
#      2012/03/30 RM Bell		Capture results from DB2 api cr.getUpdated
#      2012/05/03 RM Bell               Put back checking if trxid is null. The lack of this check was causing too
#                                       many conflict updates. This had been eliminated because the cr.create was not
#                                       auto-reviewing change requests.
#      2012/05/08 RM Bell               Added code to eliminate any EDA TK DB updates that were performed due to the
#                                       sync process
#      2012/05/30 RM Bell               Log the person making the update (because all updates in CQ are from BPROCESS)
#      2012/07/20 RM Bell               Change the unknown user message from an error to a warning
#                                       Improved the handling of conflicted fields
#                                       Retrieve the IIP (IBM Intranet ID) of the developer when retrieving the Change/Injection Request
#      2012/11/16 RM Bell               Support xtinct processing of 14/18 toolkit
#      2012/12/10 RM Bell		Change from -cqtk to -t
#      2012/12/10 RM Bell		Support customer impacted field
#      2016/11/17 NAVEEN                Fix to allow CQ Release Change to be Synced with DB if CQ state is SUBMITTED
#
# ****************************************************************************************************************************************

# TODO: Fix up logerror when unable to open the error file
# TODO: Some type of cleanup of restart files once things are working again

use strict;

use File::Basename;
use FindBin qw($Bin);
use lib "$Bin/../libperl";
use CQPerlExt;
use Time::Local;
use Data::Dumper 'Dumper';
use Getopt::Long;
use XML::Simple;
use email;
use Carp;
use Storable qw(dclone);

# Used to hold the invocation of this application
my $invocation;

# Parameters passed in
my $uid='';
my $pw='';
my $db='';
my $schema='';
my $start_time;
my $last_time;
my $indir='';
my $outdir='';
my $dfile='';
my $email='Y';
my $appmode;
my $dbmode='';
my $help='';
my $restart_threshold=999;
my $restartemail_threshold;
my $noendtime='';

# Flag to determine if the error message file has been opened or not
my $error_open=0;



my     $db2_xs = XML::Simple->new(KeyAttr=>{record=>'clearquest_id'},
		                        forcearray=>['record','component','release',
		                                     'state','description',
		                                     'severity','type','impacted_customer',
						     'problem_introduction','communication_method',
           	                                     'cq_releasename',
		                                     'updated_timestamp',
                        			     'updated_by'],
#			                        	     'codeupdate_count','codeupdate_count_since'],
			KeepRoot => 1
			);

my $cq_xs=XML::Simple->new(KeyAttr=>{record=>'clearquest_id'},
	                        forcearray=>['record','component','release',
	                                     'state','description',
	                                     'severity','type','impacted_customer',
					     'developer_iip','tk_release',
	                                     'modify_date',
				    	     'trxid'],
#						 ,'action_performed']
			KeepRoot => 1
			);


# my $out_xs;

my $db2_cr_cu_hash;
my $update_db2_cr_cu_hash;
my $cq_ir_hash;
my $cq_rel_hash;

my $sync_id='iipmds2@us.ibm.com';

# This file is used to ensure that only one process is running.
# It also contains the date/time of the last successful run
# The date time is in the format YYYY-MM-DD HH:MM:SS
my $lockfile='CQ2DB2.LOCK';

my $cq_rel_xml = 'cq_rel.xml';
my $cq_ir_xml = 'cq_ir.xml';
my $db2_cr_cu_xml = 'db2_cr_cu.xml';

my $path='/afs/eda/data/edainfra/tools/enablement';

# Names of the "java APIs"
my $changerequpdate='bin/changeReqUpdateAll';
my $changereqcreate='bin/changeReqCreate';
my $changereqgetbycq='bin/cr.getByCQ';
my $changereqgetupdated='bin/cr.getUpdated';

# This hash contains all of the transitions that are possible in CQ
# Any that we don't want to support via the synchronization  process are commented out.
    my %transition_hash = (
#     'SUBMITTED;DECLINED'      => 'Decline',
     'SUBMITTED;WITHDRAWN'     => 'Withdraw',
#     'SUBMITTED;REVIEWED'      => 'Reviewed',
     'SUBMITTED;SUBMITTED'     => 'Modify',
#     'APPROVED;DECLINED'       => 'Decline',
     'APPROVED;WITHDRAWN'      => 'WIthdraw',
#     'APPROVED;ONHOLD'         => 'Reviewed',
      'APPROVED;COMPLETE'       => 'Complete',
      'APPROVED;RELEASED'       => 'Release',
      'APPROVED;TRANSMITTED'       => 'Transmit',
     'APPROVED;APPROVED'       => 'Modify',
#     'DECLINED;SUBMITTED'      => 'ReSubmit',
     'DECLINED;DECLINED'       => 'Modify',
     'WITHDRAWN;SUBMITTED'     => 'ReSubmit',
     'WITHDRAWN;WITHDRAWN'     => 'Modify',
#     'ONHOLD;SUBMITTED'        => 'ReSubmit',
#     'ONHOLD;DECLINED'         => 'Decline',
#     'ONHOLD;WITHDRAWN'        => 'Withdraw',
     'ONHOLD;ONHOLD'           => 'Modify',
#     'REVIEWED;APPROVED'       => 'Approve',
#     'REVIEWED;DECLINED'       => 'Decline',
     'REVIEWED;WITHDRAWN'      => 'Withdraw',
     'REVIEWED;REVIEWED'       => 'Modify',
     'RESERVED;RESERVED'       => 'Modify',
     'RESERVED;SUBMITTED'      => 'ReSubmit',
     'RESERVED;APPROVED'       => 'ReSubmit',
     'RESERVED;COMPLETE'       => 'Complete',  # I need to trust that Gregg is doing appropriate checking that this is valid
     'COMPLETE;TRANSMITTED'    => 'Transmit',
     'COMPLETE;RELEASED'       => 'Release',
     'COMPLETE;APPROVED'       => 'UnComplete',
     'COMPLETE;COMPLETE'       => 'Modify',
     'TRANSMITTED;RELEASED'    => 'Release',
     'TRANSMITTED;COMPLETE'    => 'UnTransmit',
     'TRANSMITTED;TRANSMITTED' => 'Modify',
     'RELEASED;TRANSMITTED'    => 'UnRelease',
     'RELEASED;RELEASED'       => 'Modify',
     );


my $cqrestart_file;
my $db2restart_file;
my $conflict_file;
my $error_file='error.txt';
my $log_file;


my $conflict_hash;
my $cq_fail_hash;
my $db2_fail_hash;


my $cq_restart_hash;
my $db2_restart_hash;

my $emailbody='';

my $CQSession;
my $retcode;

my $usage  = "\n";
$usage .= "Description:\n";
$usage .= "  Synchronizes the EDA TK database and CQ/MDCMS.\n\n";
$usage .= "Parameters:\n";
$usage .= "  cqperl cq2db2.pl   [-stime starttime] [-ltime lasttime]\n";
$usage .= "                     [-uid userid] [-pw password] [-db database] [-schema schema]\n";
$usage .= "                     [-indir indir] [-outdir outdir] [-dfile debugfile] [-email email]\n";
$usage .= "                     -appmode appmode -dbmode dbmode\n";
$usage .= "                     [-help]";
$usage .= "\n\n";
$usage .= "  where: starttime  = overrides the current date/time as the start time for this invocation (yyyy-mm-dd hh:mm:ss)\n";
$usage .= "         lasttime   = overrides the last runtime for this invocation (yyyy-mm-dd hh:mm:ss)\n";
$usage .= "         userid     = ClearQuest userid\n";
$usage .= "         password   = ClearQuest password\n";
$usage .= "         database   = ClearQuest database name\n";
$usage .= "         schema     = ClearQuest schema repository\n";
$usage .= "         indir      = \n";
$usage .= "         outdir     = \n";
$usage .= "         debugfile  = Name of a file to write debug statements to\n";
$usage .= "         email      = Controls if email is sent. Defaults to 'Y'. Set to 'N' to stop email from being sent.\n";
$usage .= "         appmode    = used to construct the path (dev/prod/etc) to the 'Java APIs'\n";
$usage .= "         dbmode     = database mode passed into the 'Java APIs' \n";
$usage .= "         help       = displays this help\n";
$usage .= "\n";

eval{  # Try

  ProcessArgs();

  start_synch_proc();

  retrieve_cq_rels();

  read_retry_data();

  retrieve_cq_data($last_time,$start_time);

  retrieve_db2_data($last_time,$start_time);

  process_cq_data();

  process_db2_data();

  write_retry_data();

  finish_synch_proc();

  1;
}
or do { # Catch
  my $retmsg = $@;
  fatal_error($retmsg);
};

# ********************************************************************************************************
# Purpose:
#    Perform the necessary initialization
#
# Return codes:   NA
#
# ********************************************************************************************************
sub start_synch_proc {

  unless ($start_time) {
    #capture the current time
    my ($sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $time) = localtime();
    $start_time = sprintf("%4d-%2.2d-%2.2d %2.2d:%2.2d:%2.2d",
    			  $year + 1900, $mon + 1, $mday,  $hour, $min, $sec);
  }

  my ($date,$time) = split(/ /,$start_time);
  my ($year,$month,$day) = split(/-/,$date);
  my ($hh,$mm,$ss) = split(/:/,$time);
  my $tmon = $month-1;
  my $tyear = $year-1900;
   my $timex = timelocal($ss,$mm,$hh,$day,$tmon,$tyear) -1;

  my ($sec, $min, $hour, $mday, $mon, $yr, $wday, $yday, $time) = localtime($timex);
  my $fake_start_time = sprintf("%4d-%2.2d-%2.2d %2.2d:%2.2d:%2.2d", $yr+1900, $mon + 1, $mday, $hour, $min, $sec);

  my $filename=$start_time;
  $filename =~ s/://g;
  $filename =~ s/\///g;
  my ($t_date,$t_time) = split(/ /,$filename);
  $t_date =~s/\///g;
  $filename =~ s/ /_/g;

  my $filename = 'data/' . $filename;
  $cqrestart_file = $filename . "_cqrestart.xml";
  $db2restart_file = $filename . "_db2restart.xml";
  $conflict_file = $filename . "_conflict.xml";
  $log_file = 'data/' . $t_date . "_log.txt";

  open(LOG_FILE, ">>$log_file") || fatal_error("Could not open $log_file\n");
  print LOG_FILE "\n************************************************************************************\n";
  LogMsg("$invocation\n");

  # Open the file for read and write
  open(LOCK_FILE, "+<$lockfile") || fatal_error("Could not open file $lockfile\n");

   # obtain an exclusive lock and exit if cannot get it
   flock(LOCK_FILE, 6) || fatal_error("Could not lock file $lockfile");

  # read the last run time for the file
  # assume it is the first and only line in the file
  unless ($last_time) {
    $last_time = readline(LOCK_FILE) or fatal_error("could not read file $lockfile\n $!");
    chomp $last_time;
  }

  my $l_filename=$start_time;
  $l_filename =~ s/://g;
  $l_filename =~ s/\///g;
  my ($t_date,$t_time) = split(/ /,$l_filename);
  $t_date =~s/\///g;
  $l_filename =~ s/ /_/g;
  $l_filename = 'data/' . $l_filename;

  LogMsg("Using a start time of: $last_time and an end time of: $start_time\n");

# $out_xs= XML::Simple->new(
#                        KeyAttr=>{record=>'clearquest_id'},
#                        KeepRoot => 1
#                         );




  eval{  # Try
    $CQSession= CQPerlExt::CQSession_Build();
    $CQSession->UserLogon($uid,$pw,$db,$schema);
    1;
  }
  or do { # Catch
    my $retmsg = $@;
    fatal_error($retmsg);
  }
}

# ********************************************************************************************************
# Purpose:
# Read in any retry data from the previous run.
#
# Return codes: NA
#
# ********************************************************************************************************
sub read_retry_data {
  LogMsg("Reading retry data\n");

  my $l_filename=$last_time;
  $l_filename =~ s/://g;
  $l_filename =~ s/\///g;
  my ($t_date,$t_time) = split(/ /,$l_filename);
  $t_date =~s/\///g;
  $l_filename =~ s/ /_/g;

  my $cq_restart_file = 'data/' . $l_filename . '_cqrestart.xml';
  my $db2_restart_file = 'data/' . $l_filename . '_db2restart.xml';;

  if (-e $db2_restart_file) {
     $db2_restart_hash = $db2_xs->XMLin($db2_restart_file);
   }

   if (-e $cq_restart_file) {
    $cq_restart_hash = $cq_xs->XMLin($cq_restart_file);
   }

}


# ********************************************************************************************************
# Purpose:
#    Retrieve the updates since the last run from db2
#    If an input xml file exists, it is used instead of reading directly from db2
#    This input xml file allows a person to create many different testcases.
#    Merge any retry data with the data retrieved from db2
#
# Return codes: NA
#
# ********************************************************************************************************
sub retrieve_db2_data {

  LogMsg("Retrieve db2 data\n");
  my $rc =0;
  my $results;
  my $cmd;
  # ##############################################################
  #
  # ##############################################################
  if ($indir && -e $db2_cr_cu_xml) {
     if ($dfile) {print_debug("Reading db2 data from a file\n")};
     my $clearquest_id;
     my $updated_by;
     my $updated_timestamp;
     my $trxid;

     $update_db2_cr_cu_hash=$db2_xs->XMLin($db2_cr_cu_xml);

     $db2_cr_cu_hash = $db2_xs->XMLin($db2_cr_cu_xml);

     if ($dfile) {print_dumper('\$update_db2_cr_cu_hash',\$update_db2_cr_cu_hash)};
     foreach $clearquest_id (keys %{$update_db2_cr_cu_hash->{ChangeRequest}->{record}}) {
      $updated_by = $update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{updated_by}[0];
      $updated_timestamp = $update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{updated_timestamp}[0];
       unless ($updated_timestamp ge $last_time && $updated_timestamp lt $start_time ) {
         delete($update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id});
       }
     }
     if ($update_db2_cr_cu_hash->{ChangeRequest}->{updt_retrieve_rc}) {
      $rc = $update_db2_cr_cu_hash->{ChangeRequest}->{updt_retrieve_rc};
     }

  }
  # ##############################################################
  #
  # ##############################################################
  else {
     my $outfile = 'getupdated.xml';
     if ($dfile) {print_debug("Reading db2 data directly from database\n")};
      $cmd = "$path/$appmode/$changereqgetupdated -y -db $dbmode -detail -startdate '" . $last_time . "' -enddate '" . "$start_time" . "' -output $outfile";
      LogMsg("$cmd\n");
      if ($dfile) {print_debug("$cmd\n");}
      $results = `$cmd`;
      $rc = $?;
      $rc >>=8;
      if ($rc ==0) {
       $update_db2_cr_cu_hash=$db2_xs->XMLin($outfile);
     }
  }

  remove_sync_updates($update_db2_cr_cu_hash);

  if ($dfile) {print_dumper('\$db2_cr_cu_hash',\$db2_cr_cu_hash)};
  if ($dfile) {print_dumper('\$update_db2_cr_cu_hash',\$update_db2_cr_cu_hash)};

  unless ($rc == 0) {
   my $msg ="\t\tUnable to retrieve information from DB2 rc: $rc\n";
   $msg .= "\t\tcommand: $cmd\n";
   $msg .= "\t\tresults: $results\n";
   fatal_error($msg);
  }

  #Merge the restart hash here
  foreach my $clearquest_id (keys %{$db2_restart_hash->{ChangeRequest}->{record}}) {
    unless (exists($update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id})) {
      $update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}=$db2_restart_hash->{ChangeRequest}->{record}{$clearquest_id};
    }
  }
}

# ********************************************************************************************************
# Purpose:
#    Remove updates made by the sync process
#
# Return codes: NA
#
# ********************************************************************************************************
sub remove_sync_updates {
 my($db2_hash) = @_;
 my $clearquest_id;


 foreach $clearquest_id (keys %{$db2_hash->{ChangeRequest}->{record}}) {
   if ( $db2_hash->{ChangeRequest}->{record}{$clearquest_id}->{updated_by}[0] eq $sync_id) {
      LogMsg("\tRemoving $clearquest_id because the last update was a sync update\n");
      delete($update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id});
   }
 }

}

# ********************************************************************************************************
# Purpose:
#    Get all of the releases from ClearQuest
#    If an input xml file exists, it is used instead of reading directly from ClearQuest
#
# Return codes: NA
#
# ********************************************************************************************************
sub retrieve_cq_rels {
  LogMsg("Retrieve CQ releases\n");

  my $rc=0;
  if ($indir && -e $cq_rel_xml) {
    $cq_rel_hash = XMLin($cq_rel_xml,
  		   KeyAttr=>{release=>'num'},
                        forcearray=>['release','name'],
			KeepRoot => 1
			);

# TODO allow for a return code in the xml
#    if ($cq _rel_hash->{ClearQuest}->{updt_retrieve_rc}) {
#      $rc = $cq_ir_hash->{ClearQuest}->{updt_retrieve_rc};
#    }


  }
  else {
    my $CQQuery = $CQSession->BuildQuery("release");
    $CQQuery->BuildField('num');
    $CQQuery->BuildField('name');
    my $CQFilter = $CQQuery->BuildFilterOperator($CQPerlExt::CQ_BOOL_OP_AND);
    # for now, I will assume that the database knows all. Probably need to fix this someday.
    $CQFilter->BuildFilter('available', $CQPerlExt::CQ_COMP_OP_EQ, ['Yes']);
    my $CQResultSet = $CQSession->BuildResultSet($CQQuery) ;
    $CQResultSet->Execute();
    while (  $CQResultSet->MoveNext() == $CQPerlExt::CQ_SUCCESS ) {
      my $num = $CQResultSet->GetColumnValue(1);
      my $name = $CQResultSet->GetColumnValue(2);
      $cq_rel_hash->{ClearQuest}->{release}{$num}->{name}[0]=$name;
     }
  }

  if ($dfile) {print_dumper('\$cq_rel_hash',\$cq_rel_hash)};

  unless ($rc == 0) {
    fatal_error("Unable to retrieve release information from CQ\n");
  }
}

# ********************************************************************************************************
# Purpose:
#    Retrieve the updates since the last run from ClearQuest
#    If an input xml file exists, it is used instead of reading directly from db2
#    This input xml file allows a person to create many different testcases.
#    Merge any retry data with the data retrieved from ClearQuest
#
# Return codes: NA
#
# ********************************************************************************************************
sub retrieve_cq_data {
  LogMsg("Retrieve CQ data\n");

   my $rc=0;
  # ##############################################################
  #
  # ##############################################################
  if ($indir && -e $cq_ir_xml) {
     if ($dfile) {print_debug("Reading cq data from a file\n")};
     my $clearquest_id;
     my $trxid;
     my $modify_date;

    $cq_ir_hash = $cq_xs->XMLin($cq_ir_xml);

     if ($dfile) {print_dumper('\$cq_ir_hash',\$cq_ir_hash)};
     foreach $clearquest_id (keys %{$cq_ir_hash->{ClearQuest}->{record}}) {
      $modify_date = $cq_ir_hash->{ClearQuest}->{record}{$clearquest_id}->{modify_date}[0];
      $trxid = $cq_ir_hash->{ClearQuest}->{record}{$clearquest_id}->{trxid}[0];
     unless ($modify_date ge $last_time && $modify_date lt $start_time && !$trxid ) {
        delete($cq_ir_hash->{ClearQuest}->{record}{$clearquest_id});
      }
    }
    if ($cq_ir_hash->{ClearQuest}->{updt_retrieve_rc}) {
      $rc = $cq_ir_hash->{ClearQuest}->{updt_retrieve_rc};
    }

  }
  # ##############################################################
  #
  # ##############################################################
  else {
    if ($dfile) {print_debug("Reading cq data directly from ClearQuest\n")};
    my $CQQuery = $CQSession->BuildQuery("tk_injectionrequest");
    $CQQuery->BuildField('id');
    $CQQuery->BuildField('tk_component');
    $CQQuery->BuildField('tk_release_num');
    $CQQuery->BuildField('modify_date');
    $CQQuery->BuildField('description');
    $CQQuery->BuildField('state');
    $CQQuery->BuildField('severity');
    $CQQuery->BuildField('cr_type');
    $CQQuery->BuildField('developer.login_name');
    $CQQuery->BuildField('tk_release');
    $CQQuery->BuildField('customer_impacted');

    my $CQFilter = $CQQuery->BuildFilterOperator($CQPerlExt::CQ_BOOL_OP_AND);
    $CQFilter->BuildFilter('modify_date', $CQPerlExt::CQ_COMP_OP_GTE, [$last_time]);
    $CQFilter->BuildFilter('modify_date', $CQPerlExt::CQ_COMP_OP_LT, [$start_time]);
    $CQFilter->BuildFilter('trxid', $CQPerlExt::CQ_COMP_OP_IS_NULL, []);
    $CQFilter->BuildFilter('vc_system', $CQPerlExt::CQ_COMP_OP_EQ, ['SVN']);

    my $CQResultSet = $CQSession->BuildResultSet($CQQuery) ;
    $CQResultSet->Execute();
    while (  $CQResultSet->MoveNext() == $CQPerlExt::CQ_SUCCESS ) {
      my $cq_id = $CQResultSet->GetColumnValue(1);
      my $component = $CQResultSet->GetColumnValue(2);
      my $release = $CQResultSet->GetColumnValue(3);
      my $modify_date = $CQResultSet->GetColumnValue(4);
      my $description = $CQResultSet->GetColumnValue(5);
      my $state = $CQResultSet->GetColumnValue(6);
      my $severity = $CQResultSet->GetColumnValue(7);
      my $type = $CQResultSet->GetColumnValue(8);
      my $developer_iip = $CQResultSet->GetColumnValue(9);
      my $tk_release = $CQResultSet->GetColumnValue(10);
      my $customer_impacted = $CQResultSet->GetColumnValue(11);
      $cq_ir_hash->{ClearQuest}->{record}{$cq_id}->{component}[0]->{name}=$component;
      $cq_ir_hash->{ClearQuest}->{record}{$cq_id}->{modify_date}[0]=$modify_date;
      $cq_ir_hash->{ClearQuest}->{record}{$cq_id}->{description}[0]=$description;
      $cq_ir_hash->{ClearQuest}->{record}{$cq_id}->{state}[0]=$state;
      $cq_ir_hash->{ClearQuest}->{record}{$cq_id}->{developer_iip}[0]=$developer_iip;
      $cq_ir_hash->{ClearQuest}->{record}{$cq_id}->{tk_release}[0]=$tk_release;
      if ($severity) {
        $cq_ir_hash->{ClearQuest}->{record}{$cq_id}->{severity}[0]=$severity;
      }
      if ($type) {
        $cq_ir_hash->{ClearQuest}->{record}{$cq_id}->{type}[0]=$type;
      }
      if ($customer_impacted) {
        $cq_ir_hash->{ClearQuest}->{record}{$cq_id}->{impacted_customer}[0]=$customer_impacted;
      }
    }

  }


  if ($dfile) {print_dumper('\$cq_ir_hash',\$cq_ir_hash)};
  unless ($rc == 0) {
    fatal_error("Unable to retrieve information from CQ\n");
  }

  #Merge the restart hash here
  foreach my $clearquest_id (keys %{$cq_restart_hash->{ClearQuest}->{record}}) {
    unless (exists($cq_ir_hash->{ClearQuest}->{record}{$clearquest_id})) {
      $cq_ir_hash->{ClearQuest}->{record}{$clearquest_id}=$cq_restart_hash->{ClearQuest}->{record}{$clearquest_id};
    }
  }



}

# ********************************************************************************************************
# Purpose:
#    Loop through all of the updated Change Requests in db2 and
#    If the Change Request has been updated in CQ, mark as a conflict. Otherwise update CQ
#
# Return codes: NA
#
# ********************************************************************************************************
sub process_db2_data {
  LogMsg("Process DB2 data\n");
  my $db2_record;
  my $clearquest_id;

  foreach $clearquest_id (keys %{$update_db2_cr_cu_hash->{ChangeRequest}->{record}}) {
      LogMsg("\tProcessing $clearquest_id from DB2\n");
      if (exists($cq_ir_hash->{ClearQuest}->{record}{$clearquest_id})) {
      update_conflict($clearquest_id,$update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id},
      		$cq_ir_hash->{ClearQuest}->{record}{$clearquest_id});
    }
    else {
      update_cq($clearquest_id,$update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id});
    }
  }
}


# ********************************************************************************************************
# Purpose:
#    Loop through all of the updated Change Requests in CQ and
#    If the Change Request has not been updated in db2, either update the record in db2 or insert a new record in db2
#
# Return codes: NA
#
# ********************************************************************************************************
sub process_cq_data {
  LogMsg("Process CQ data\n");
  my $db2_cr_record;
  my $is_there_new_cu_record;
  my $is_there_cu_record;
  my $rc;
  my $cq_id;
  my $component;
#  my $action_performed;
  my $trxid;

  foreach $cq_id (keys %{$cq_ir_hash->{ClearQuest}->{record}}) {
    LogMsg("\tProcessing $cq_id from CQ\n");
    unless (exists($update_db2_cr_cu_hash->{ChangeRequest}->{record}{$cq_id})) {
      $component=$cq_ir_hash->{ClearQuest}->{record}{$cq_id}->{component}[0]->{name};
#      $action_performed=$cq_ir_hash->{ClearQuest}->{record}{$cq_id}->{action_performed}[0];
      $trxid=$cq_ir_hash->{ClearQuest}->{record}{$cq_id}->{trxid}[0];
      $rc=0;
      ($rc,$db2_cr_record)=get_db2_record($cq_id);
      if ($rc == 0) {
        if (exists($db2_cr_record->{ChangeRequest}->{record})) {
          update_db2($cq_id,$cq_ir_hash->{ClearQuest}->{record}{$cq_id},$db2_cr_record->{ChangeRequest}->{record}{$cq_id});
        }
        else {
          insert_db2($cq_id,$cq_ir_hash->{ClearQuest}->{record}{$cq_id});
        }
      }
    }
  }
}


# ********************************************************************************************************
# Purpose:
#    Update CQ with the updates from db2
#    If output directory passed in, only print out the updates
#    If update fails, add data to the restart XML
#
# Return codes: NA
#
# ********************************************************************************************************
sub update_cq {
  my($clearquest_id,$db2_data) = @_;
  my $rc=0;
  my $results;
  my $entity;
  my $action;
  my $update =0;
  my $cq_username;
  LogMsg("\t\tUpdate CQ with $clearquest_id by $db2_data->{updated_by}[0]\n");


  # ##############################################################
  #
  # ##############################################################
  if ($outdir) {

    if ($db2_data->{update_rc}) {
      $rc=$db2_data->{update_rc};
    }

  }
  # ##############################################################
  #
  # ##############################################################
  else {
      eval{$entity=$CQSession->GetEntity('tk_injectionrequest',$clearquest_id);};
      $results = $@;
      if ($entity) {
        my $db2_state = uc $db2_data->{state}[0];
        my $cq_state = uc $entity->GetFieldValue('state')->GetValue();
        my $db2_desc = $db2_data->{description}[0];
        my $cq_desc =  $entity->GetFieldValue('description')->GetValue();
        if ($action=$transition_hash{"$cq_state;$db2_state"}) {
          eval{$CQSession->EditEntity($entity,$action)};
          $results = $@;
          if ($results) {
            $rc = 8;
          }
          else {
            my $orig_modify_date = $entity->GetFieldOriginalValue('modify_date')->GetValue();
            if ($orig_modify_date ge $start_time) {
              $rc = 12;
              $results = "Record has been updated since it was retrieved\n";
            }
            else {
              if ($db2_state ne $cq_state) {
                $update=1;
                if ($cq_state eq 'RESERVED') {
                  if ($cq_username=get_cq_user_info($db2_data->{updated_by}[0])) {
                    $entity->SetFieldValue('submitter',$cq_username);
                    $entity->SetFieldValue('developer',$cq_username);
                    # TODO: remove setting these fields temporarily until the EDA TK DB can do it
                     # print " $db2_data->{release}[0]->{long_releasename}\n";
                     if ( ! (grep/(dev)/,$db2_data->{release}[0]->{cq_releasename})) {
                       # print " $db2_data->{release}[0]->{long_releasename}\n";
                       $entity->SetFieldValue('communication_method','Release notes');
                       $entity->SetFieldValue('problem_introduction','New functionality written < 6 months ago');
                     }
                  }
                  else {
                    LogMsg("$clearquest_id was updated by an unknown user, $db2_data->{updated_by}[0]\n");
                    my $developer = $entity->GetFieldValue('developer')->GetValue();
                    # Toggle developer such that tester gets updated
                    $entity->SetFieldValue('developer','EDGE');
                    $entity->SetFieldValue('developer',$developer);
                  }
		}
		# TODO: Need to fix this. Really the user should entering this
		elsif ($db2_state eq 'WITHDRAWN') {
                  $entity->SetFieldValue('reason','Withdrawn');
                }
              }
	      if ($db2_data->{component}[0]->{name} ne $entity->GetFieldValue('tk_component')->GetValue()) {
                $entity->SetFieldValue('tk_component',$db2_data->{component}[0]->{name});
                $update=1;
              }
	      if ($db2_data->{release}[0]->{cq_releasename} ne $entity->GetFieldValue('tk_release')->GetValue()) {
                $entity->SetFieldValue('tk_release',$db2_data->{release}[0]->{cq_releasename});
                $update=1;
              }
	      if (substr($db2_desc,0,length($db2_desc)) ne substr($cq_desc,0,length($db2_desc))) {
                 $entity->SetFieldValue('description',$db2_desc);
              	 $update=1;
              }
	      if ($db2_data->{severity}[0] ne $entity->GetFieldValue('severity')->GetValue()) {
                $entity->SetFieldValue('severity',$db2_data->{severity}[0]);
                $update=1;
              }
	      if ($db2_data->{type}[0] ne $entity->GetFieldValue('cr_type')->GetValue()) {
                $entity->SetFieldValue('cr_type',$db2_data->{type}[0]);
                $update=1;
              }

              # TODO: Temporarily see if the value is set in CQ. If so, do not update CQ because we are getting the same value default value from the API
	      if (($db2_data->{problem_introduction}[0]) && ($entity->GetFieldValue('problem_introduction')->GetValueStatus() ne $CQPerlExt::CQ_HAS_VALUE) && ($db2_data->{problem_introduction}[0] ne $entity->GetFieldValue('problem_introduction')->GetValue())) {
#	      if (($db2_data->{problem_introduction}[0]) && ($db2_data->{problem_introduction}[0] ne $entity->GetFieldValue('problem_introduction')->GetValue())) {
                $entity->SetFieldValue('problem_introduction',$db2_data->{problem_introduction}[0]);
                $update=1;
              }

              # TODO: Temporarily see if the value is set in CQ. If so, do not update CQ because we are getting the same value default value from the API
	      if (($db2_data->{communication_method}[0]) && ($entity->GetFieldValue('communication_method')->GetValueStatus() ne $CQPerlExt::CQ_HAS_VALUE) && ($db2_data->{communication_method}[0] ne $entity->GetFieldValue('communication_method')->GetValue())) {
#	      if (($db2_data->{communication_method}[0]) && ($db2_data->{communication_method}[0] ne $entity->GetFieldValue('communication_method')->GetValue())) {
                $entity->SetFieldValue('communication_method',$db2_data->{communication_method}[0]);
                $update=1;
              }

	      if ($update) {
                $entity->SetFieldValue('trxid',$start_time);
                $results = $entity->Validate();
                if ($results eq '') {
                  $entity->Commit();
                  #TODO: Temporarily move it on to the approved state
                  #eval{$entity=$CQSession->LoadEntity('tk_injectionrequest',$clearquest_id);};
                  #my $cq_new_state = uc $entity->GetFieldValue('state')->GetValue();
                  #if ($db2_state eq 'APPROVED' && $cq_state eq 'RESERVED' && $cq_new_state eq 'REVIEWED') {
                  #  $action = 'Approve';
                  #  eval{$CQSession->EditEntity($entity,$action)};
                  #  # $results = $@;
                  #  $results = $entity->Validate();
                  #  if ($results eq '') {
                  #    $entity->Commit();
                  #  }
                  #  else {
                  #    $rc=16;
                  #    $results="Validation error\n $results";
                  #  }
                  #}
                }
                else {
                  $rc=16;
                  $results="Validation error\n $results";
                }
              }
	      else {
                LogMsg("\t\tCQ Update: $clearquest_id \t No fields were different\n");
              }
            }
          }
        }

        else {
	  $results="Invalid state transition $cq_state:$db2_state\n";
          $rc = 4;
        }
      }
      else {
        $rc = 4;
      }
    }


  # If bad return code, add to hash to write out for restart
  if ($rc ne 0) {
   $db2_fail_hash->{ChangeRequest}->{record}{$clearquest_id}=$db2_data;
   $emailbody .= $results;
   LogMsg("\t\tFailed CQ Update: $clearquest_id \t rc: $rc results: $results\n");
  }
}


# ********************************************************************************************************
# Purpose:
#    Given a person's intranet ID, return the persons CQ "name"
#
# Return codes: NA
#
# ********************************************************************************************************
sub get_cq_user_info {
  my($intranet_id) = @_;
  my $cq_username='';

  my $intranet_id = lc($intranet_id);

  my $CQQuery = $CQSession->BuildQuery("user_information");
  $CQQuery->BuildField('fullname');

  my $CQFilter = $CQQuery->BuildFilterOperator($CQPerlExt::CQ_BOOL_OP_AND);
  $CQFilter->BuildFilter('login_name', $CQPerlExt::CQ_COMP_OP_EQ, [$intranet_id]);
  $CQFilter->BuildFilter('isactive', $CQPerlExt::CQ_COMP_OP_EQ, ['1']);


  my $CQResultSet = $CQSession->BuildResultSet($CQQuery) ;

  $CQResultSet->Execute();
  if (  $CQResultSet->MoveNext() == $CQPerlExt::CQ_SUCCESS ) {
    $cq_username = $CQResultSet->GetColumnValue(1);
  }

   return $cq_username;


}

# ********************************************************************************************************
# Purpose:
#    Update db2 with the updates from CQ
#    If output directory passed in, only print out the updates
#    If update fails, add data to the restart XML
#
# Return codes: NA
#
# ********************************************************************************************************
sub update_db2 {
  my($clearquest_id,$cq_data,$db2_data) = @_;
  my $rc=0;
  my $results;
  my $cmd;
  my $update = 0;
  LogMsg("\t\tUpdate DB2 data with $clearquest_id\n");

  # ##############################################################
  #
  # ##############################################################
  if ($outdir) {
    if ($cq_data->{update_rc}) {
      $rc=$cq_data->{update_rc};
    }
    else {
     print "Updating: \n";
     print "\t release: $cq_data->{tk_release}[0]\n";
     print "\t component: $cq_data->{component}[0]->{name}\n";
     print "\t severity: $cq_data->{severity}[0]\n";
     print "\t type: $cq_data->{type}[0]\n";
     print "\t description: $cq_data->{description}[0]\n";
     print "\t state: $cq_data->{state}[0]\n";
     print "\t clearquest_id: $clearquest_id\n";
    }

  }
  else {
    $cmd = "$path/$appmode/$changerequpdate -y -db $dbmode -cq $clearquest_id ";
    if ( $cq_data->{description}[0] ne $db2_data->{description}[0] ) {
      $cq_data->{description}[0] =~ s/\"/\\\"\"\"/g;
      $cq_data->{description}[0] =~ s/\'/\'\\\'\'/g;
      $cmd .= " -d \'$cq_data->{description}[0]\' ";
      $update = 1;
    }
    if ($cq_data->{impacted_customer}[0] && $cq_data->{impacted_customer}[0] ne $db2_data->{impacted_customer}[0]) {
      $cmd .= " -cust \'$cq_data->{impacted_customer}[0]\' ";
      $update = 1;
    }
    if ($cq_data->{severity}[0] && $cq_data->{severity}[0] ne $db2_data->{severity}[0]) {
      $cmd .= " -sev $cq_data->{severity}[0]";
      $update = 1;
    }
    if ($cq_data->{type}[0] && $cq_data->{type}[0] ne $db2_data->{type}[0]) {
      $cmd .= " -" . lc($cq_data->{type}[0]);
      $update = 1;
    }
    if ($cq_data->{component}[0]->{name} ne $db2_data->{component}[0]->{name} ) {
      $cmd .= " -c $cq_data->{component}[0]->{name} ";
      $update = 1;
    }
# navechan - enabling cq release change sync from CQ to ETREE, this was commented out
   # if ($cq_data->{tk_release}[0] ne $db2_data->{release}[0]->{cq_releasename}) {
   #   $cmd .= " -t \'$cq_data->{tk_release}[0]\'";
   #   $update = 1;
   # }
    if (uc $cq_data->{state}[0] ne uc $db2_data->{state}[0] ) {
      $cmd .= "  -s $cq_data->{state}[0] ";
      $update = 1;
    }

# Sync CQ Release if CQ state is SUBMITTED
   # LogMsg "\t CQ state for ID: $clearquest_id found is: $cq_data->{state}[0] \n";
    if (uc $cq_data->{state}[0] eq 'SUBMITTED') {
   #      LogMsg "\t CQ state found is Submitted, Updating the release name for ID: $clearquest_id \n";
         if ($cq_data->{tk_release}[0] ne $db2_data->{release}[0]->{cq_releasename}) {
           $cmd .= " -t \'$cq_data->{tk_release}[0]\'";
           $update = 1;
         }
    }

   if ($update) {
      LogMsg("$cmd\n");
      if ($dfile) {print_debug("$cmd\n");}
      $results = `$cmd`;
      $rc = $?;
      $rc >>=8;
   }
   else {
      LogMsg("\t\tDB2 Update: $clearquest_id \t No fields were different\n");
    }

  }



  # If bad return code, add to hash to write out for restart
  if ($rc ne 0) {
   $cq_fail_hash->{ClearQuest}->{record}{$clearquest_id}=$cq_data;
   my $msg ="\t\tFailed changeReqUpdate: $clearquest_id \t rc: $rc\n";
   $msg .= "\t\tcommand: $cmd\n";
   $msg .= "\t\tresults: $results\n";
   $emailbody .= $msg;
   LogMsg($msg);
  }
}


# ********************************************************************************************************
# Purpose:
#    Insert the data into DB2
#    If output directory passed in, only print out the updates
#    If insert fails, add data to the restart XML
#
# Return codes: NA
#
# ********************************************************************************************************
sub insert_db2 {
  my($clearquest_id,$cq_data) = @_;
  my $cmd;
  my $rc=0;
  my $results;
  LogMsg("\t\tInsert DB2 data with $clearquest_id\n");

  # ##############################################################
  #
  # ##############################################################
  if ($outdir) {
    if ($cq_data->{update_rc}) {
      $rc=$cq_data->{update_rc};
    }
    else {
     print "Inserting: \n";
     print "\t release: $cq_data->{tk_release}[0]\n";
     print "\t component: $cq_data->{component}[0]->{name}\n";
     print "\t severity: $cq_data->{severity}[0]->{name}\n";
     print "\t type: $cq_data->{type}[0]->{name}\n";
     print "\t description: $cq_data->{description}[0]\n";
     print "\t clearquest_id: $clearquest_id\n";
    }
  }
  else {
    $cq_data->{description}[0] =~ s/\"/\\\"\"\"/g;
    $cq_data->{description}[0] =~ s/\'/\'\\\'\'/g;
    $cmd = "$path/$appmode/$changereqcreate -y -db $dbmode -t \'$cq_data->{tk_release}[0]\' ";
    $cmd .= " -c  $cq_data->{component}[0]->{name}  -cq $clearquest_id -d \'$cq_data->{description}[0]\' ";
    $cmd .= " -u " . $cq_data->{developer_iip}[0];

    if ($cq_data->{impacted_customer}[0] ) {
      $cmd .= " -cust \'$cq_data->{impacted_customer}[0]\' ";
    }
    if ($cq_data->{severity}[0] ) {
      $cmd .= " -sev $cq_data->{severity}[0] ";
    }
    # TODO: Until CQ requires severity, I will default to 4
    else {
      $cmd .= " -sev 4 ";
    }
    if ($cq_data->{type}[0]) {
      $cmd .= " -" . lc($cq_data->{type}[0]);
    }
    #TODO: Until CQ requires type, I will default to feature
    else {
      $cmd .= "-feature";
    }
    if ($dfile) {print_debug("$cmd\n");}
    LogMsg("$cmd\n");
    $results = `$cmd`;
    $rc = $?;
    $rc >>=8;
    # Now I need to set the state to what ever it should be
    # TODO get this so I can set it on the insert?
    # temporarily, don't do this for reserved
    if ($rc eq 0 && $cq_data->{state}[0] ne 'Reserved'  ) {
    ##   $cmd = "$path/$appmode/$changerequpdate -y -db $dbmode -t $cq_data->{release}[0]->{releasename} -c  $cq_data->{component}[0]->{name}  -cq $clearquest_id -s $cq_data->{state}[0] -d '" . $cq_data->{description}[0] . "'";
      $cmd = "$path/$appmode/$changerequpdate -y -db $dbmode -c  $cq_data->{component}[0]->{name}  -cq $clearquest_id -s $cq_data->{state}[0] -d \'$cq_data->{description}[0]\' ";
      if ($cq_data->{severity}[0]) {
        $cmd .= " -sev $cq_data->{severity}[0] ";
      }
      if ($cq_data->{type}[0]) {
        $cmd .= " -" . lc($cq_data->{type}[0]);
      }
      LogMsg("$cmd\n");
      $results = `$cmd`;
      $rc = $?;
      $rc >>=8;
    }


  }

  # If bad return code, add to hash to write out for restart
  if ($rc ne 0) {
   $cq_fail_hash->{ClearQuest}->{record}{$clearquest_id}=$cq_data;
   my $msg ="\t\tFailed changeReqCreate: $clearquest_id \t rc: $rc\n";
   $msg .= "\t\tcommand: $cmd\n";
   $msg .= "\t\tresults: $results\n";
   $emailbody .= $msg;
   LogMsg($msg);

  }
}

# ********************************************************************************************************
# Purpose:
#    Given a Change Request number, retrieve the detail from db2
#    If an input xml file exists, it is used instead of reading directly from db2
#    This input xml file allows a person to create many different testcases.
#
# Return codes: Returns the return code from the "Java API" (or read from the "test" XML
#
# ********************************************************************************************************
sub get_db2_record {
  my($cq_id) = @_;
  my $db2_cr_record;
  my $cmd;
  my $rc = 0;
  my $results;
  LogMsg("\t\tLooking up $cq_id in DB2\n");

  # ##############################################################
  #
  # ##############################################################
  if ($indir) {
    if (exists($db2_cr_cu_hash->{ChangeRequest}->{record}{$cq_id})) {
      $db2_cr_record = $db2_cr_cu_hash->{ChangeRequest}->{record}{$cq_id};
    }
    if ($db2_cr_cu_hash->{ChangeRequest}->{record}{$cq_id}->{cr_retrieve_rc}) {
      $rc = $db2_cr_cu_hash->{ChangeRequest}->{record}{$cq_id}->{cr_retrieve_rc};
    }
  }
  # ##############################################################
  #
  # ##############################################################
  else {
      my $outfile2 = 'getbycq.xml';
      $cmd = "$path/$appmode/$changereqgetbycq  -y -db $dbmode -cqid $cq_id -output $outfile2";
      if ($dfile) {print_debug("$cmd\n");}
      $results = `$cmd`;
      $rc = $?;
      $rc >>=8;
      if ($rc ==0) {
      $db2_cr_record = $db2_xs->XMLin($outfile2);
     }
  }



  unless ($rc == 0) {
    $cq_fail_hash->{ClearQuest}->{record}{$cq_id}=$cq_ir_hash->{ClearQuest}->{record}{$cq_id};
    LogError("\t\tFailed Retrieving: $cq_id \t rc: $rc\n");
    LogError("\t\tcommand: $cmd\n");
    LogError("\t\tresults: $results\n");

  }


  return $rc, $db2_cr_record;
}


# ********************************************************************************************************
# Purpose:
#   If there are conflicting updates in the two data sources, the conflict is written to a file.
#   This file will be sent to the distribution list via email.
#
# Return codes: NA
#
# ********************************************************************************************************
sub update_conflict {
  my($clearquest_id,$db2_data,$cq_data) = @_;

  my $conflicted_fields='';

  # component information
  if ($cq_data->{component}[0]->{name} ne $db2_data->{component}[0]->{name}) {
    $conflicted_fields .= " component";
  }

  # release information
  if ($cq_data->{tk_release}[0] ne $db2_data->{release}[0]->{cq_releasename}) {
    $conflicted_fields .= " release";
  }

  # state
  if (uc $cq_data->{state}[0] ne $db2_data->{state}[0]) {
    $conflicted_fields .= " state";
  }

  #description
  if ($cq_data->{description}[0] ne $db2_data->{description}[0]) {
    $conflicted_fields .= " description";
  }


  # severity information
  if ($cq_data->{severity}[0] ne $db2_data->{severity}[0]) {
    $conflicted_fields .= " severity";
  }


  # type information
  if ($cq_data->{type}[0] ne $db2_data->{type}[0]) {
    $conflicted_fields .= " type";
  }

  #check that at least one field is really different
  if ($conflicted_fields) {

    # component information
    $conflict_hash->{Conflict}->{record}{$clearquest_id}->{component_cq}[0]->{name}=$cq_data->{component}[0]->{name};
    $conflict_hash->{Conflict}->{record}{$clearquest_id}->{component_db2}[0]->{name}=$db2_data->{component}[0]->{name};

    # release information
   $conflict_hash->{Conflict}->{record}{$clearquest_id}->{release_cq}[0]=$cq_data->{tk_release}[0];
   $conflict_hash->{Conflict}->{record}{$clearquest_id}->{release_db2}[0]->{cq_releasename}=$db2_data->{release}[0]->{cq_releasename};

    # state
    $conflict_hash->{Conflict}->{record}{$clearquest_id}->{state_db2}[0]=$db2_data->{state}[0];
    $conflict_hash->{Conflict}->{record}{$clearquest_id}->{state_cq}[0]=$cq_data->{state}[0];

    #description
    $conflict_hash->{Conflict}->{record}{$clearquest_id}->{description_db2}[0]=$db2_data->{description}[0];
    $conflict_hash->{Conflict}->{record}{$clearquest_id}->{description_cq}[0]=$cq_data->{description}[0];

    # severity information
    $conflict_hash->{Conflict}->{record}{$clearquest_id}->{severity_cq}[0]->{name}=$cq_data->{severity}[0];
    $conflict_hash->{Conflict}->{record}{$clearquest_id}->{severity_db2}[0]->{name}=$db2_data->{severity}[0];

    # type information
    $conflict_hash->{Conflict}->{record}{$clearquest_id}->{type_cq}[0]->{name}=$cq_data->{type}[0];
    $conflict_hash->{Conflict}->{record}{$clearquest_id}->{type_db2}[0]->{name}=$db2_data->{type}[0];

    # update timestamp
    $conflict_hash->{Conflict}->{record}{$clearquest_id}->{z_updated_by}[0]=$db2_data->{updated_by}[0];
    $conflict_hash->{Conflict}->{record}{$clearquest_id}->{z_updated_timestamp}[0]=$db2_data->{updated_timestamp}[0];
    $conflict_hash->{Conflict}->{record}{$clearquest_id}->{z_modify_date}[0]=$cq_data->{modify_date}[0];

    LogError("\t\tConflicting updates: $clearquest_id\n conflicted fields: $conflicted_fields\n");
    $emailbody="\t\tConflicting updates $clearquest_id\n conflicted fields: $conflicted_fields\n";
  }
  else {
    LogMsg("\t\tConflicting update was made, but no fields are in conflict");
  }
}


# ********************************************************************************************************
# Purpose:
#    When an error has occurred, it will be written to a restart file.
#    This file is read in the next time the application is run and an attemp is made to re-process the data.
#    An email will be sent when ever a restart file is created.
#
# Return codes:
#   Application exits with a fatal error if any of the files cannot be opened
#
# ********************************************************************************************************
sub write_retry_data {

  if ($conflict_hash) {
    open(CONFLICT_FILE, ">$conflict_file") || fatal_error("Could not open $conflict_file");

    my $outxml = XMLout($conflict_hash,
                        KeyAttr=>{record=>'clearquest_id'},
                        KeepRoot => 1
                         );
    print CONFLICT_FILE $outxml;
    close CONFLICT_FILE;
  }

  if ($cq_fail_hash) {
    open(CQRESTART_FILE, ">$cqrestart_file") || fatal_error("Could not open $cqrestart_file");
    my $outxml = XMLout($cq_fail_hash,
                        KeyAttr=>{record=>'clearquest_id'},
                        KeepRoot => 1
                         );
    print CQRESTART_FILE $outxml;
    close CQRESTART_FILE;
  }


  if ($db2_fail_hash) {
    open(DB2RESTART_FILE, ">$db2restart_file") || fatal_error("Could not open $db2restart_file");
    my $outxml = XMLout($db2_fail_hash,
                        KeyAttr=>{record=>'clearquest_id'},
                        KeepRoot => 1
                         );

    print DB2RESTART_FILE $outxml;
    close DB2RESTART_FILE;
  }


     if ($dfile) {print_dumper('\$conflict_hash',\$conflict_hash)};
     if ($dfile) {print_dumper('\$cq_fail_hash',\$cq_fail_hash)};


  my @files = <data/*restart*>;
  my $restart_count = scalar(@files);

  if ($restart_count > $restart_threshold) {
    rename('ok2run', 'notok2run');
  }

}





# ********************************************************************************************************
# Purpose:
#    Final cleanup before a normal exit of the application
#
# Return codes: NA
#
# ********************************************************************************************************
sub finish_synch_proc {
    my ($sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $time) = localtime();
    my $end_time = sprintf("%4d-%2.2d-%2.2d %2.2d:%2.2d:%2.2d",
    			  $year + 1900, $mon + 1, $mday,  $hour, $min, $sec);
    my $file;
    my $newname;

  my @cq_restart_files = <data/*cqrestart*>;
  my $cq_restart_count = scalar(@cq_restart_files);
  my @db2_restart_files = <data/*db2restart*>;
  my $db2_restart_count = scalar(@db2_restart_files);

 # rename  anyold restart files
   unless (-e $cqrestart_file) {
     foreach $file (@cq_restart_files) {
         $newname = $file;
         $newname =~ s/cqrestart/cqok/;
         rename($file,$newname);
	 LogMsg("Renaming: $file $newname\n");
     }
   }

  # rename  anyold restart files
   unless (-e $db2restart_file) {
     foreach $file (@db2_restart_files) {
         $newname = $file;
         $newname =~ s/db2restart/db2ok/;
         rename($file,$newname);
	 LogMsg("Renaming: $file $newname\n");
     }
   }

  LogMsg("Finish up: $end_time\n");

  #Close any open files
  close LOG_FILE;
  close ERROR_FILE;
  close DEBUG_FILE;



  if (-e $error_file || -e $conflict_file
    || (-e $cqrestart_file && (($cq_restart_count + $restartemail_threshold)%$restartemail_threshold == 1))
    || (-e $db2restart_file && (($db2_restart_count + $restartemail_threshold)%$restartemail_threshold == 1))) {
    LogMsg("Sending email\n");
    sendemail("Error running",$emailbody);
  }




  unless ($noendtime) {
    # clear out the file
    truncate(LOCK_FILE,0);
    # start writing at the beginning
    seek(LOCK_FILE,0,0);

    print LOCK_FILE "$start_time";
  }

  close LOCK_FILE;

}


# ********************************************************************************************************
# Purpose:
#    Write messages to error file
#
# Return codes: NA
#
# ********************************************************************************************************
sub LogError {
  my ($var,$text) = @_;

  unless ($error_open) {
    unless (open(ERROR_FILE, ">$error_file"))   {
      print STDOUT "Could not open $error_file\n";
      sendemail("Could not open $error_file",$emailbody);
    }
    $error_open=1;
  }

  print ERROR_FILE "$start_time $text \n" . $var;
  LogMsg("$var $text");
}


# ********************************************************************************************************
# Purpose:
#    Write messages to STDOUT and the log file
#
# Return codes: NA
#
# ********************************************************************************************************
sub LogMsg {
  my ($text) = @_;
  print LOG_FILE "$start_time $text";
  print STDOUT "$start_time $text";
}




# ********************************************************************************************************
# Purpose:
#    Using the Perl Data:Dumper module, write varaible values to the debug file
#
# Return codes: NA
#
# ********************************************************************************************************
sub print_dumper {
  my($text,$data) = @_;
  my ($package, $filename, $line, $func) = caller(0);
  use Data::Dumper 'Dumper';
  print DEBUGFILE "******************************************************\n";
  print DEBUGFILE "($package, $filename, $line, $func)  $text=> \n" .  Dumper($data);
  print DEBUGFILE "******************************************************\n";
}

# ********************************************************************************************************
# Purpose:
#    Write to the debug file
#
# Return codes: NA
#
# ********************************************************************************************************
sub print_debug {
  my($text) = @_;
  my ($package, $filename, $line, $func) = caller(0);
  print DEBUGFILE "******************************************************\n";
  print DEBUGFILE $text;
  print DEBUGFILE "******************************************************\n";

}

# ********************************************************************************************************
# Purpose:
#    Log the error message, send an error email, and exit the application
#
# Return codes:
#   Exit the application with a RC=99
#
# ********************************************************************************************************
sub fatal_error {
  my ($text) = @_;
  my $temp = Carp::longmess();
  LogError('',$text);
  LogError('',$temp);

  close ERROR_FILE;
  close CONFLICT_FILE;
  close CQRESTART_FILE;
  close DB2RESTART_FILE;

  sendemail("fatal error running",
            "$emailbody \n $text $temp\n"
            . "***** data from DB2 (update_db2_cr_cu_hash) *****\n" . Dumper($update_db2_cr_cu_hash)
            . "***** data from CQ (cq_ir_hash) *****\n" . Dumper($cq_ir_hash)
#            . "***** rel data from CQ (cq_rel_hash *****\n" . Dumper($cq_rel_hash)
  );

  exit 99;

}




# ********************************************************************************************************
# Purpose:
#   Send the email
#
# Return codes: NA
#
# ********************************************************************************************************
sub sendemail {
   my ($subject,$body) = @_;

    my $rc;

    my %emailobj;

    my $email_address;
    my $filename;
    my @filenames;

    my $email_config_hash ;

    my $email_config_xml = 'data/email_config.xml';

   # Determine who to send the email to, who it is from, and how to send it
   if (-e $email_config_xml) {
     $email_config_hash = XMLin($email_config_xml,
                        forcearray=>['debug','server','domain','from','type','to','cc','bcc'],
			KeepRoot => 1
			);

   }
   else {
     $email_config_hash->{email}->{server}[0]='na.relay.ibm.com';
     $email_config_hash->{email}->{domain}[0]='btv.ibm.com';
     $email_config_hash->{email}->{from}[0]='stadtlag@us.ibm.com';
     $email_config_hash->{email}->{to}[0]='stadtlag@us.ibm.com';
     $email_config_hash->{email}->{cc}[0]='trjaladi@in.ibm.com';
     $email_config_hash->{email}->{type}[0]='plain';
     $email_config_hash->{email}->{debug}[0]='0';

   }

   email::createobj(\%emailobj,$email_config_hash->{email}->{type}[0]);

   my $temparray =$email_config_hash->{email}->{to};
   foreach $email_address (@{$email_config_hash->{email}->{to}}){
     email::addto(\%emailobj,$email_address);
   }

   foreach $email_address (@{$email_config_hash->{email}->{cc}}){
     email::addcc(\%emailobj,$email_address);
   }

   foreach $email_address (@{$email_config_hash->{email}->{bcc}}){
     email::addbcc(\%emailobj,$email_address);
   }

    email::addfrom(\%emailobj,$email_config_hash->{email}->{from}[0]);

    email::addsubject(\%emailobj,$subject);

    email::addbody(\%emailobj,$body);

   # attach any files that exist
   if (-e $cqrestart_file) {email::addfile(\%emailobj,'',$cqrestart_file,$cqrestart_file);};
   if (-e $db2restart_file) {email::addfile(\%emailobj,'',$db2restart_file,$db2restart_file);};
   if (-e $conflict_file) {email::addfile(\%emailobj,'',$conflict_file,$conflict_file);};
   if (-e $error_file) {email::addfile(\%emailobj,'',$error_file,$error_file);};

   unless ($email eq 'N') {
    $rc = email::sendit(\%emailobj,
                        $email_config_hash->{email}->{server}[0],
      		        $email_config_hash->{email}->{domain}[0],
                        $email_config_hash->{email}->{debug}[0]
		        );
  }

   unlink $error_file;

}

# ********************************************************************************************************
# Purpose:
#   Read in the parameters
#
# Return codes:
#     Application exit with RC=8 when help is requested
#     Application exit with RC=99 when invalid option specified or required options are missing
#
# ********************************************************************************************************
sub ProcessArgs {
  my @stime;
  $invocation = "$0 @ARGV";
   # Getopt::Long::Configure("pass_through");
   GetOptions (
	   "stime=s" => \$start_time,
	   "ltime=s" => \$last_time,
	   "uid=s" => \$uid,
	   "pw=s" => \$pw,
	   "db=s" => \$db,
	   "schema=s" => \$schema,
	   "indir=s" => \$indir,
	   "outdir=s" => \$outdir,
	   "dfile=s" => \$dfile,
	   "email=s" => \$email,
	   "appmode=s" => \$appmode,
	   "dbmode=s" => \$dbmode,
	   "rthreshold=s" => \$restart_threshold,
           "emailthreshold=s" => \$restartemail_threshold,
           "noendtime" => \$noendtime,
	   "help|?" => \$help
	   );

  if ($help) {
    print "$usage\n";
    exit 8;
  }


  if ( @ARGV) {
    fatal_error("* Invalid option specified: @ARGV.\n $invocation\n");
  }


  if ($indir) {
    $lockfile =$indir . '/' . $lockfile;
    $cq_rel_xml =$indir . '/' . $cq_rel_xml;;
    $cq_ir_xml =$indir . '/' . $cq_ir_xml;;
    $db2_cr_cu_xml =$indir . '/' . $db2_cr_cu_xml;
  }
  else {
    $lockfile ='data/' . $lockfile;
  }

  unless ($dbmode) {fatal_error("* Option dbmode is required\n");}
  unless ($appmode) {fatal_error("* Option appmode is required\n");}

  if ($dfile) {
    if ($outdir) {
      $dfile = $outdir . '/' . $dfile;
    }
    open(DEBUGFILE, ">$dfile") || fatal_error("Could not open file $dfile");
  }

}
