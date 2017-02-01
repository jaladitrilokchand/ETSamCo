# ********************************************************************************************************
# Program Name: cq2db2.pl
#
# Purpose: Compare the data that is in the EDA TK DB to the data that is in CQ/MDCMS
#
# Inputs: See the application help
#
# Outputs:
#
# Return Codes:
#
# Change Log:
#	2012/05/17 RM Bell		Initial Release
#                                       Real rough just to get something in place
#
# ********************************************************************************************************

use strict;

use CQPerlExt;
use Getopt::Long;
use FindBin qw($Bin);
use lib "$Bin/../libperl";
use XML::Simple;
use email;
use Data::Dumper 'Dumper';

my $db2_xs = XML::Simple->new(KeyAttr=>{record=>'clearquest_id'},
		                        forcearray=>['record','component','release',
		                                     'state','description',
		                                     'severity','type',
		                                     'updated_timestamp',
                        			     'updated_by'],
			KeepRoot => 1
			);

my $logmsg;

my $xml_file;

my $invocation;
my $help;
my $usage;

my $clearquest_id;

my $uid;
my $pw;
my $db;
my $schema;
my $email='N';

my $update_date;

my $cq_ir_hash;
my $update_db2_cr_cu_hash;
my $CQSession;


my %anomolies = (
#		MDCMS00119320 => "Changed from 18.1.x. Waiting for Gregg to remove from EDA TK DB",
		MDCMS00119752 => "Changed from 18.1.x. Waiting for Gregg to remove from EDA TK DB",
		MDCMS00120602 => "Changed from 18.1.x. Waiting for Gregg to remove from EDA TK DB",
		MDCMS00120706 => "Changed from 18.1.x. Waiting for Gregg to remove from EDA TK DB",
		MDCMS00121109 => "Changed from 18.1.x. Waiting for Gregg to remove from EDA TK DB",
		MDCMS00121541 => "Changed from 18.1.x. Waiting for Gregg to remove from EDA TK DB",
              );


ProcessArgs();
init();
get_cq_data();
runit();
sendemail('The current state of the databases',$logmsg);


# ********************************************************************************************************
# Purpose:
#    Something
#
# Return codes:   NA
#
# ********************************************************************************************************
sub runit {
 my $t_logmsg;
 my $db2_desc;
 my $cq_desc;

foreach $clearquest_id (sort keys %{$update_db2_cr_cu_hash->{ChangeRequest}->{record}}) {
    if (exists($cq_ir_hash->{ClearQuest}->{record}{$clearquest_id})) {
         # print "processing $clearquest_id\n";
         $t_logmsg='';
         if ($update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{release}[0]->{alt_releasename}
            ne $cq_ir_hash->{ClearQuest}->{record}{$clearquest_id}->{release}[0]->{releasename}) {
              $t_logmsg .= "Mismatch release $clearquest_id: ";
              $t_logmsg .=  "db2:" . $update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{release}[0]->{alt_releasename};
              $t_logmsg .= " ";
              $t_logmsg .= "cq:" . $cq_ir_hash->{ClearQuest}->{record}{$clearquest_id}->{release}[0]->{releasename};
              $t_logmsg .= "\n";
            }
#        if ($update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{component}[0]
        if ($update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{component}[0]->{name}
            ne $cq_ir_hash->{ClearQuest}->{record}{$clearquest_id}->{component}[0]->{name}) {
              $t_logmsg .= "Mismatch component $clearquest_id: ";
              $t_logmsg .=  "db2:" . $update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{component}[0];
              $t_logmsg .= " ";
              $t_logmsg .= "cq:" . $cq_ir_hash->{ClearQuest}->{record}{$clearquest_id}->{component}[0]->{name};
              $t_logmsg .= "\n";
            }
        if ($update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{state}[0]
            ne uc $cq_ir_hash->{ClearQuest}->{record}{$clearquest_id}->{state}[0]) {
              $t_logmsg .= "Mismatch state $clearquest_id: ";
              $t_logmsg .=  "db2:" . $update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{state}[0];
              $t_logmsg .= " ";
              $t_logmsg .= "cq:" . $cq_ir_hash->{ClearQuest}->{record}{$clearquest_id}->{state}[0];
              $t_logmsg .= "\n";
            }
         if ($update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{severity}[0]
            ne  $cq_ir_hash->{ClearQuest}->{record}{$clearquest_id}->{severity}[0]) {
              $t_logmsg .= "Mismatch severity $clearquest_id: ";
              $t_logmsg .=  "db2:" . $update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{severity}[0];
              $t_logmsg .= " ";
              $t_logmsg .= "cq:" . $cq_ir_hash->{ClearQuest}->{record}{$clearquest_id}->{severity}[0];
              $t_logmsg .= "\n";
            }
          if ($update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{type}[0]
            ne  $cq_ir_hash->{ClearQuest}->{record}{$clearquest_id}->{type}[0]) {
              $t_logmsg .= "Mismatch type $clearquest_id: ";
              $t_logmsg .=  "db2:" . $update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{type}[0];
              $t_logmsg .= " ";
              $t_logmsg .= "cq:" . $cq_ir_hash->{ClearQuest}->{record}{$clearquest_id}->{type}[0];
              $t_logmsg .= "\n";
            }
            $cq_desc=$cq_ir_hash->{ClearQuest}->{record}{$clearquest_id}->{description}[0];
            $db2_desc=$update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{description}[0];
            # for today, ignore all whitespace
              $cq_desc =~ s/\s+//g;
              $db2_desc =~ s/\s+//g;
            if ($cq_desc ne $db2_desc) {
              $t_logmsg .= "Mismatch description $clearquest_id: ";
              $t_logmsg .=  "\ndb2:" . $update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{description}[0];
              $t_logmsg .= "\n";
              $t_logmsg .= "cq:" . $cq_ir_hash->{ClearQuest}->{record}{$clearquest_id}->{description}[0];
              $t_logmsg .= "\n";
            }
            if ($t_logmsg) {
              $logmsg .= $t_logmsg;
              $logmsg .= "\tupdated by: " . $update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{updated_by}[0] . "\n";
              $logmsg .= "\tupdated timestamp: " . $update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{updated_timestamp}[0] . "\n";
              $logmsg .= "\tmodified on: " . $cq_ir_hash->{ClearQuest}->{record}{$clearquest_id}->{modify_date}[0] . "\n";
              # print $logmsg . "\n";
            }


         delete($cq_ir_hash->{ClearQuest}->{record}{$clearquest_id});
    }
    else {
      if (exists($anomolies{$clearquest_id})) {
        $logmsg .= "anomoly $clearquest_id\n\t$anomolies{$clearquest_id}\n";
      }
      else {
        $logmsg .= "did not find $clearquest_id in CQ updates";
        $logmsg .= "\n\trelease: " . $update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{release}[0]->{alt_releasename};
        $logmsg .= "\n\tcomponent: " . $update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{component}[0]->{name};
        $logmsg .= "\n\tstate: " . $update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{state}[0];
        $logmsg .= "\n\ttype: " . $update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{type}[0];
        $logmsg .= "\n\tdescription: " . $update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{description}[0];
        $logmsg .= "\n\tupdated by: " . $update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{updated_by}[0];
        $logmsg .= "\n\tupdated timestamp: " . $update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{updated_timestamp}[0];
        $logmsg .= "\n";
      }
    }
#    print "$clearquest_id";
#    print "\n\t" . $update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{updated_by}[0];
#    print "\n\t" . $update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{updated_timestamp}[0];
#    print "\n\t" . $update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{release}[0]->{alt_releasename};
#    print "\n\t" . $update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{release}[0]->{releasename};
#    print "\n\t" . $update_db2_cr_cu_hash->{ChangeRequest}->{record}{$clearquest_id}->{component}[0];
#    print "\n";
}

  foreach $clearquest_id (sort keys %{$cq_ir_hash->{ClearQuest}->{record}}) {
    $logmsg .= "did not find $clearquest_id in DB2 updates\n";
    $logmsg .= "\tmodified on: " . $cq_ir_hash->{ClearQuest}->{record}{$clearquest_id}->{modify_date}[0] . "\n";
    #  if (exists($cq_ir_hash->{ClearQuest}->{record}{$clearquest_id})) {
    #  print "$clearquest_id";
    #  print "\n\t" . $cq_ir_hash->{ClearQuest}->{record}{$clearquest_id}->{modify_date}[0];
    #  print "\n\t" . $cq_ir_hash->{ClearQuest}->{record}{$clearquest_id}->{release}[0]->{releasename};
    #  print "\n\t" . $cq_ir_hash->{ClearQuest}->{record}{$clearquest_id}->{component}[0]->{name};
    #  print "\n\t" . $cq_ir_hash->{ClearQuest}->{record}{$clearquest_id}->{description}[0];
    #  print "\n\t" . $cq_ir_hash->{ClearQuest}->{record}{$clearquest_id}->{state}[0];
    #  print "\n\t" . $cq_ir_hash->{ClearQuest}->{record}{$clearquest_id}->{severity}[0];
    #  print "\n\t" . $cq_ir_hash->{ClearQuest}->{record}{$clearquest_id}->{type}[0];
    #  print "\n";
  }

  print $logmsg . "\n";

}

# ********************************************************************************************************
# Purpose:
#    Something
#
# Return codes:   NA
#
# ********************************************************************************************************
sub init {
   $update_db2_cr_cu_hash=$db2_xs->XMLin($xml_file);
  # print Dumper $update_db2_cr_cu_hash;

  print "Logging in\n";
  $CQSession= CQPerlExt::CQSession_Build();
  $CQSession->UserLogon($uid,$pw,$db,$schema);

}

# ********************************************************************************************************
# Purpose:
#    Something
#
# Return codes:   NA
#
# ********************************************************************************************************
sub get_cq_data {

    print "Getting CQ data\n";
    my $CQQuery = $CQSession->BuildQuery("tk_injectionrequest");
    $CQQuery->BuildField('id');
    $CQQuery->BuildField('tk_component');
    $CQQuery->BuildField('tk_release_num');
    $CQQuery->BuildField('modify_date');

    $CQQuery->BuildField('state');
    $CQQuery->BuildField('severity');
    $CQQuery->BuildField('cr_type');
   $CQQuery->BuildField('description');

    my $CQFilter = $CQQuery->BuildFilterOperator($CQPerlExt::CQ_BOOL_OP_AND);
    $CQFilter->BuildFilter('modify_date', $CQPerlExt::CQ_COMP_OP_GTE, [$update_date]);
 #   $CQFilter->BuildFilter('modify_date', $CQPerlExt::CQ_COMP_OP_LT, [$start_time]);
 #   $CQFilter->BuildFilter('trxid', $CQPerlExt::CQ_COMP_OP_IS_NULL, []);
    $CQFilter->BuildFilter('vc_system', $CQPerlExt::CQ_COMP_OP_EQ, ['SVN']);
    # I think the join of these states when querying DB1
    $CQFilter->BuildFilter('state', $CQPerlExt::CQ_COMP_OP_NEQ, ['Reserved']);

    my $CQResultSet = $CQSession->BuildResultSet($CQQuery) ;
    $CQResultSet->Execute();
    # print $CQResultSet->GetSQL();
    while (  $CQResultSet->MoveNext() == $CQPerlExt::CQ_SUCCESS ) {
      my $cq_id = $CQResultSet->GetColumnValue(1);
      my $component = $CQResultSet->GetColumnValue(2);
      my $release = $CQResultSet->GetColumnValue(3);
      my $modify_date = $CQResultSet->GetColumnValue(4);

      my $state = $CQResultSet->GetColumnValue(5);
      my $severity = $CQResultSet->GetColumnValue(6);
      my $type = $CQResultSet->GetColumnValue(7);
     my $description = $CQResultSet->GetColumnValue(8);
      # print "$component"; exit 99;
      $cq_ir_hash->{ClearQuest}->{record}{$cq_id}->{component}[0]->{name}=$component;
      $cq_ir_hash->{ClearQuest}->{record}{$cq_id}->{release}[0]->{releasename}=$release;
      $cq_ir_hash->{ClearQuest}->{record}{$cq_id}->{modify_date}[0]=$modify_date;
      $cq_ir_hash->{ClearQuest}->{record}{$cq_id}->{description}[0]=$description;
      $cq_ir_hash->{ClearQuest}->{record}{$cq_id}->{state}[0]=$state;
      if ($severity) {
        $cq_ir_hash->{ClearQuest}->{record}{$cq_id}->{severity}[0]=$severity;
      }
      if ($type) {
        $cq_ir_hash->{ClearQuest}->{record}{$cq_id}->{type}[0]=$type;
      }
    }


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

   unless ($email eq 'N') {
    $rc = email::sendit(\%emailobj,
                        $email_config_hash->{email}->{server}[0],
      		        $email_config_hash->{email}->{domain}[0],
                        $email_config_hash->{email}->{debug}[0]
		        );
  }
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
	   "uid=s" => \$uid,
	   "pw=s" => \$pw,
	   "db=s" => \$db,
	   "schema=s" => \$schema,
	   "udate=s" => \$update_date,
	   "xfile=s" =>\$xml_file,
  	   "email=s" => \$email,
	   "help|?" => \$help
	   );

  if ($help) {
    print "$usage\n";
    exit 8;
  }

  unless ($update_date) {
    $update_date = '2012-02-28 23:59:59';
 }


  if ( @ARGV) {
    fatal_error("* Invalid option specified: @ARGV.\n $invocation\n");
  }


}

