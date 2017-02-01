################# Run /printwith CQPERL and not PERL    ##################


####################################################################
#
######################################################################

use strict;

use FindBin qw($Bin);

use lib "$Bin/../libperl";

use CQPerlExt;
use Time::Local;
use Data::Dumper 'Dumper';
use Getopt::Long;
use email;
use Carp;



# my ($uid,$pw,$schema,$db,$update) = @ARGV;

my $email='Y';
my $uid='';
my $pw='';
my $db='';
my $schema='';
my $min='';
my $max='';
my $error_open=0;

my $count;

my $error_file='error_resthem.txt';
my $log_file;

my $start_time;


my $CQSession;


eval{  # Try

  ProcessArgs();
  init();
  $count = GetCount();
  LogMsg("*** COUNT $count MAX $max MIN $min ****\n");
  if ($count < $min) {
    CreateIt($count);
  }

  1;
}
or do { # Catch
  my $retmsg = $@;
  fatal_error($retmsg);
};



#############################################################################
#
#
#############################################################################
sub init
{


 my ($sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $time) = localtime();
 $start_time = sprintf("%4d-%2.2d-%2.2d %2.2d:%2.2d:%2.2d",
    			  $year + 1900, $mon + 1, $mday,  $hour, $min, $sec);

  my $filename=$start_time;
  $filename =~ s/://g;
  $filename =~ s/\///g;
  my ($t_date,$t_time) = split(/ /,$filename);
  $t_date =~s/\///g;
  $log_file = 'data/' . $t_date . "_restthem_log.txt";

  open(LOG_FILE, ">>$log_file") || fatal_error("Could not open $log_file\n");
  print LOG_FILE "\n************************************************************************************\n";

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

sub term
{
  close LOG_FILE;
  close ERROR_FILE;

  if (-e $error_file) {
    LogMsg("Sending email\n");
    sendemail("Error running");
  }

}

#############################################################################
#
#
#############################################################################
sub GetCount
{
  my $count;
  my $querydef = $CQSession->BuildQuery('tk_injectionrequest');
  $querydef->BuildField('id');
  my $QueryFilter = $querydef->BuildFilterOperator($CQPerlExt::CQ_BOOL_OP_AND);
  $QueryFilter->BuildFilter('state', $CQPerlExt::CQ_COMP_OP_EQ, ['Reserved']);
  my $queryfielddefs = $querydef->GetQueryFieldDefs();
  my $idfield = $queryfielddefs->ItemByName("id");
  $idfield->SetAggregateFunction($CQPerlExt::CQ_DB_AGGR_COUNT);
  my $ResultSet = $CQSession->BuildResultSet($querydef);
 $ResultSet->Execute();


  if ($ResultSet->MoveNext() == $CQPerlExt::CQ_SUCCESS) {
    $count = $ResultSet->GetColumnValue(1);
  }
  return $count;


}



#############################################################################
#
#
#############################################################################
sub CreateIt
{
  my ($count) = @_;  print STDOUT "*** COUNT $count MAX $max MIN $min ****\n";

  for (my $i=$count; $i<$max;$i++) {
    my $CQEntity = $CQSession->BuildEntity('tk_injectionrequest');
    $CQEntity->SetFieldValue('tk_component','NULL');

    # this needs to be in the database as unavailable
    $CQEntity->SetFieldValue('tk_release','NULL');
    # Force this to NULL
    $CQEntity->SetFieldValue('tk_release_num','NULL');


    $CQEntity->SetFieldValue('description','RESERVE');

    $CQEntity->SetFieldRequirednessForCurrentAction('autoaction',$CQPerlExt::CQ_OPTIONAL);
    $CQEntity->SetFieldValue('autoaction','Reserve');

    $CQEntity->SetFieldValue('cr_type','FEATURE');

    $CQEntity->SetFieldValue('severity','4');

    $CQEntity->SetFieldValue('problem_introduction','NA');
    $CQEntity->SetFieldValue('communication_method','NA');
    $CQEntity->SetFieldValue('customer_impacted','NA');
    $CQEntity->SetFieldValue('no_delivery','Y');

#   $CQEntity->SetFieldRequirednessForCurrentAction('communication_method',$CQPerlExt::CQ_OPTIONAL);
#   $CQEntity->SetFieldRequirednessForCurrentAction('problem_introduction',$CQPerlExt::CQ_OPTIONAL);
  # $CQEntity->SetFieldRequirednessForCurrentAction('cr_type',$CQPerlExt::CQ_OPTIONAL);
  # $CQEntity->SetFieldRequirednessForCurrentAction('severity',$CQPerlExt::CQ_OPTIONAL);

    # Validate and commit the updates
    my $CQStatus = $CQEntity->Validate();
    if ($CQStatus eq "") {
      $CQEntity->Commit();
      LogMsg("Created: " . $CQEntity->GetFieldValue('id')->GetValue() . "\n");
    }
    else {
      fatal_error($CQStatus);
      $CQEntity->Revert();
    }
  }


}

# *********************************************************************
sub ProcessArgs {
  my @stime;
  Getopt::Long::Configure("pass_through");
   GetOptions (
	   "min=s" => \$min,
	   "max=s" => \$max,
	   "uid=s" => \$uid,
	   "pw=s" => \$pw,
	   "db=s" => \$db,
	   "schema=s" => \$schema,
	   "email=s" => \$email,
	   );

  if ( @ARGV) {
    fatal_error("* Invalid option specified: @ARGV.\n");
  }


}


  # *********************************************************************
#
# *********************************************************************
sub fatal_error {
  my ($text) = @_;
  my $temp = Carp::longmess();
  LogError('',$text);
  LogError('',$temp);

  close ERROR_FILE;

  sendemail("fatal error running","$text $temp");

  exit 99;

}

# *********************************************************************
#
# *********************************************************************
sub LogError {
  my ($var,$text) = @_;

  unless ($error_open) {
    unless (open(ERROR_FILE, ">$error_file"))   {
      print STDOUT "Could not open $error_file\n";
      sendemail("Could not open $error_file");
    }
    $error_open=1;
  }

  print ERROR_FILE "$start_time $text \n" . $var;
  LogMsg("$var $text");
}


# *********************************************************************
#
# *********************************************************************
sub LogMsg {
  my ($text) = @_;
  print LOG_FILE "$start_time $text";
  print STDOUT "$start_time $text";
}

sub sendemail {
   my ($subject,$body) = @_;

    my $rc;

    my %emailobj;

    my $email_address;
    my $filename;
    my @filenames;

    my $email_config_hash ;

    my $email_config_xml = 'data/email_config.xml';

   if (-e $email_config_xml) {
     $email_config_hash = XMLin($email_config_xml,
                        forcearray=>['debug','server','domain','from','type','to','cc','bcc'],
			KeepRoot => 1
			);

   }
   else {
     $email_config_hash->{email}->{server}[0]='postoffice.btv.ibm.com';
     $email_config_hash->{email}->{domain}[0]='btv.ibm.com';
     $email_config_hash->{email}->{from}[0]='richbell@us.ibm.com';
     $email_config_hash->{email}->{to}[0]='richbell@us.ibm.com';
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

