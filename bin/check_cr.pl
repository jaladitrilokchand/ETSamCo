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
use Getopt::Long;
use DBI;


use FindBin qw($Bin);
use lib "$Bin/../libperl";
use  XML::Simple;

use Data::Dumper 'Dumper';

my $invocation;
my $help;
my $usage;


my $db;
my $hostname;
my $port;
my $user;
my $pass;
my $update_date;
my $xml_file;

my $end_date;

my $db2_hash;

my $dbh;
 

 ProcessArgs();
 init();

 get_db2_data();
 term();


 # ********************************************************************************************************
# Purpose:
#    Something
#
# Return codes:   NA
#
# ********************************************************************************************************


 sub get_db2_data {
 my $stmt;
 my @table;
 my $t_row;
 my @data;
 my $data;

# my $stmt2;
# my @table2;
# my $t2_row;





my $string;

my $opsys=$^O;


  if ($opsys eq 'MSWin32') {
    #windows where the db is catalog using the odbc driver
    $string="dbi:ODBC:$db";
   }
   elsif ($opsys eq 'linux') {
    # Linux where the db is not catalog using the db2 driver
    $string = "dbi:DB2:DATABASE=$db; HOSTNAME=$hostname; PORT=$port; PROTOCOL=TCPIP; UID=$user; PWD=$pass;";
   }
   else {
     print "Currently do not support $opsys\n";
     exit 99;
   }

   $dbh = DBI->connect($string, $user, $pass, {RaiseError => 1});

#   $dbh = DBI->connect("dbi:ODBC:etreedb", "etreeadm", "etreedb2", {RaiseError => 1});


  # get the failing components from the database
  $stmt ="SELECT cr.clearquest_id, cmp.component_name, tkr.tkrelease_name, tkr.alt_tkrelease_name, tkv.tkversion_name, ";
  $stmt .= "crtype.changerequest_type, crsev.changerequest_severity, crstatus.changerequest_status, ";
  $stmt .= "cr.description, cr.updated_by, cr.updated_tmstmp ";
  $stmt .= "from tk.changerequest cr, tk.component_tkversion_x_changerequest cmp2cr, tk.component_tkversion cmp_tkv, ";
  $stmt .= "tk.component_tkrelease cmp_tkr, tk.component cmp, tk.tkrelease tkr, tk.tkversion tkv, ";
  $stmt .= "tk.changerequest_type crtype, tk.changerequest_severity crsev, tk.changerequest_status crstatus ";
  $stmt .= "where cr.updated_tmstmp >= {ts'$update_date'} ";
  $stmt .= "and cr.updated_tmstmp <= {ts'$end_date'} ";
#  $stmt .= "and tk_ctkv.component_tkrelease_id = tk_ctkr.component_tkrelease_id ";
  $stmt .= "and cmp2cr.changerequest_id = cr.changerequest_id ";
  $stmt .= "and cmp_tkv.component_tkversion_id = cmp2cr.component_tkversion_id ";
  $stmt .= "and cmp_tkr.component_tkrelease_id = cmp_tkv.component_tkrelease_id ";
  $stmt .= "and cmp.component_id = cmp_tkr.component_id ";
  $stmt .= "and tkr.tkrelease_id = cmp_tkr.tkrelease_id ";
  $stmt .= "and tkv.tkversion_id = cmp_tkv.tkversion_id and tkv.tkrelease_id = cmp_tkr.tkrelease_id ";
  $stmt .= "and crtype.changerequest_type_id = cr.changerequest_type_id ";
  $stmt .= "and crsev.changerequest_severity_id = cr.changerequest_severity_id ";
  $stmt .= "and crstatus.changerequest_status_id = cr.changerequest_status_id ";
#  $stmt .= "and tk_ctkv.deleted_by is NULL ";
  $stmt .= "order by cr.clearquest_id";
  @table = run_stmt($stmt);
  undef @data;
  for $t_row (@table) {
     push(@data,@$t_row);
     my $clearquest_id=@$t_row[0];
#     $db2_hash->{ChangeRequest}->{record}->{$clearquest_id}->{component}[0]=@$t_row[1];
     $db2_hash->{ChangeRequest}->{record}->{$clearquest_id}->{component}[0]->{name}=@$t_row[1];

  # release information
  $db2_hash->{ChangeRequest}->{record}{$clearquest_id}->{release}[0]->{releasename}=@$t_row[2] . "." . @$t_row[4];
  $db2_hash->{ChangeRequest}->{record}{$clearquest_id}->{release}[0]->{alt_releasename}=@$t_row[3] . "." . @$t_row[4];

  $db2_hash->{ChangeRequest}->{record}{$clearquest_id}->{updated_by}[0]=@$t_row[9];
  $db2_hash->{ChangeRequest}->{record}{$clearquest_id}->{updated_timestamp}[0]=@$t_row[10];
   $db2_hash->{ChangeRequest}->{record}{$clearquest_id}->{description}[0]=@$t_row[8];
    $db2_hash->{ChangeRequest}->{record}{$clearquest_id}->{state}[0]=@$t_row[7];
     $db2_hash->{ChangeRequest}->{record}{$clearquest_id}->{type}[0]=@$t_row[5];
 $db2_hash->{ChangeRequest}->{record}{$clearquest_id}->{severity}[0]=@$t_row[6];


    # push(@data,@$t_row[0]);
  }

  # print "data is: \n@data\n";

   foreach $data (@data) {
 #    print "{$data}\n";
    # runit($component);
  }

 # print Dumper($db2_hash);
     my $outxml = XMLout($db2_hash,
                        KeyAttr=>{record=>'clearquest_id'},
                        KeepRoot => 1
                         );
  # print $outxml;
  print XML_FILE $outxml;

   $dbh->disconnect();
 }


# ********************************************************************************************************
# Purpose:
#    Something
#
# Return codes:   NA
#
# ********************************************************************************************************
  sub init {
      my $time = time() - 60 * 10; #subtract 10 minutes from current time
      my ($sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $time) = localtime();
      $end_date = sprintf("%4d-%2.2d-%2.2d %2.2d:%2.2d:%2.2d",
    			  $year + 1900, $mon + 1, $mday,  $hour, $min, $sec);

     open(XML_FILE, ">>$xml_file") || die("Could not open $xml_file\n");
  }


  # ********************************************************************************************************
# Purpose:
#    Something
#
# Return codes:   NA
#
# ********************************************************************************************************
  sub term  {
      close XML_FILE;
  }


# ********************************************************************************************************
# Purpose:
#    Something
#
# Return codes:   NA
#
# ********************************************************************************************************
    sub run_stmt {
      my ($stmt) = @_;
      my @row;
      my @table;
      # print "$stmt\n";
      my $sth = $dbh->prepare($stmt) or   die "Cannot prepare: " . $dbh->errstr;
      $sth->execute()  or   die "Cannot execute: " . $sth->errstr;;
      while (@row = $sth->fetchrow_array) {
       #  print "@row\n";
        push(@table,[@row]);
      }
     $sth->finish();

     # for my $t_row (@table) {
     #   for my $t_column (@$t_row) {
     #    print "$t_column\t";
     #  }
     #  print "\n";
     # }

    return @table;
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
	   "db=s" => \$db,
	   "hostname=s" => \$hostname,
	   "port=s" => \$port,
	   "user=s" => \$user,
	   "pass=s" => \$pass,
	   "udate=s" => \$update_date,
	   "xfile=s" =>\$xml_file,
	   "help|?" => \$help
	   );

  if ($help) {
    print "$usage\n";
    exit 8;
  }



  unless ($update_date) {
    $update_date = '2011-01-01 04:59:59';
 }
 


  if ( @ARGV) {
    die("* Invalid option specified: @ARGV.\n $invocation\n");
  }

}
