#!/usr/bin/perl

# Required for Blue Pages
use lib '/afs/eda/data/edainfra/tools/enablement/prod/libperl';
require BPRecord;
require BPQuery;

local ($buffer, @pairs, $pair, $name, $value, %FORM, %components);

# Read in text
$ENV{'REQUEST_METHOD'} =~ tr/a-z/A-Z/;
if ($ENV{'REQUEST_METHOD'} eq "POST") {
  read(STDIN, $buffer, $ENV{'CONTENT_LENGTH'});
}
else {
  $buffer = $ENV{'QUERY_STRING'};
}

# Split information into name/value pairs
@pairs = split(/&/, $buffer);
foreach $pair (@pairs) {
  ($name, $value) = split(/=/, $pair);
  $value =~ tr/+/ /;
  $value =~ s/%(..)/pack("C", hex($1))/eg;
  if ($name eq "components") {
    my @leads = GetOwner($value);
    $components{$value} = \@leads;
  }
  else {
    $FORM{$name} = $value;
  }
}


# Uncomment for non web test data
#$components{"etreetest1"} = "stadtlag@us.ibm.com";
#$components{"etreetest2"} = "stadtlag@us.ibm.com";
#$components{"etreetest3"} = "einslib@us.ibm.com";
#$FORM{email} = "ehull\@us.ibm.com";


# Sort the components by team lead so only 1 email sent per team lead
my %compsByLead;
foreach $comp (sort(keys(%components))) {
  my $leads_ref = $components{$comp};
  foreach my $lead (@$leads_ref) {
    if (exists($compsByLead{$lead})) {
      my $comps = $compsByLead{$lead} . " " . $comp;
      $compsByLead{$lead} = $comps;
    }
    else {
      $compsByLead{$lead} = $comp;
    }
  }
}


print "Content-type:text/html\n\n";

print <<"EOF";

<html lang="en-US" xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>EDA 14.1 - SVN Access Request Summary</title>
<meta name="DC.LANGUAGE" scheme="rfc1766" value="en-US" />
<meta name="IBM.COUNTRY" value="US" />
<meta name="CHARSET" value="ISO-8859-1" />
<meta name="DOCUMENTCOUNTRYCODE" value="US" />
<meta name="DOCUMENTLANGUAGECODE" value="en" />
<meta name="FORMAT" value="text/xhtml" />
<meta name="ROBOTS" value="index,nofollow" />
<meta name="SECURITY" value="Public" />
<meta name="SOURCE" value="v6 Template Generator" />
<meta name="TITLE" value="EDA Tool Kit - Subversion Access Request" />

<!--LOAD THREE STYLESHEETS-->
<link rel="stylesheet" type="text/css" href="http://w3.eda.ibm.com/dac2013/css/screen.css" />
<link rel="stylesheet" type="text/css" href="http://w3.eda.ibm.com/dac2013/css/interior.css" />

</head>

<body text="#000000" bgcolor="#FFFFFF" id="w3-ibm-com" class="article">

  <!-- start masthead //////////////// -->
  <div id="masthead">

    <h2 class="access">Start of masthead</h2>

     <div id="prt-w3-sitemark">
       <img src="https://w3.eda.ibm.com/dac2013/images/id-w3-sitemark-simple.gif" alt="" width="54" height="33" />
     </div>

     <div id="prt-w3-logo">
       <img src="https://w3.eda.ibm.com/dac2013/images/id-ibm-logo-black.gif" alt="IBM Logo" width="44" height="15" />
     </div>

     <div id="w3-sitemark">
       <img src="https://w3.eda.ibm.com/dac2013/images/id-w3-sitemark-large.gif" alt="" width="266" height="70" usemap="#sitemark_map" />
       <map id="sitemark_map" name="sitemark_map">
         <area shape="rect" alt="Link to W3 Home Page" coords="0,0,130,70" href="http://w3.ibm.com/" />
       </map>
     </div>

     <div id="site-title-only">EDA's Tool Kit Subversion Repository</div>

       <div id="ibm-logo">
         <a href="http://www.ibm.com">
         <img src="https://w3.eda.ibm.com/dac2013/images/id-ibm-logo.gif" alt="" width="44" height="15" /></a>
       </div>

       <div id="persistent-nav">
         <a id="w3home" href="http://w3.ibm.com/">w3 home</a>
         <a id="bluepages" href="http://w3.ibm.com/bluepages/">BluePages</a>
         <a id="helpnow" href="http://w3.ibm.com/help/">HelpNow</a>
         <a id="feedback" accesskey="9" href="http://w3.ibm.com/eworkplace/feedback/feedback.jsp?ownerid=chqadmin.ibm.com">Feedback</a>
         <a id="sitemap" href="http://w3.eda.ibm.com/dac2013/sitemap.html">SiteMap</a>
       </div>

       <div id="browser-warning">
         <img src="https://w3.eda.ibm.com/dac2013/images/icon-system-status-alert.gif" alt="Error" width="12" height="10" />
         This web page is best used in a modern browser. Since your browser is no longer supported by IBM, please upgrade your web browser at the
         <a href="http://w3.ibm.com/download/">ISSI Site</a>.
       </div>

     </div>
   </div>

   <!-- stop masthead ///////////////// -->


   <!--BEGIN MAIN BODY CONTENT-->
   <div id="content">
   <div id="content-main">

   <h2>SVN access request submitted</h2><br>
   <u>Request Summary</u><br>

EOF

print "Access needed: $FORM{access}<br>";
print "Requester: $FORM{email}<br>";
print "TK Release: 14.1<br>";
print "<br>";
print "Email has been sent to these Tool Kit Component Owner(s) for authorization<br>";
print "<ul>";
foreach $lead (keys(%compsByLead)) {
  $emails .= MailIt($lead, $FORM{email}, $compsByLead{$lead}, $FORM{access});
  print "<li>$lead - $compsByLead{$lead}";
}
print "</ul>";
print "<br>Thank you";

print <<"EOF";

</div>
</div>

  <div id="navigation">
    <h2 class="access">Start of left navigation</h2>
      <!-- left nav -->
       <div id="left-nav">
         <div class="top-level">
           <a href="http://w3.ibm.com/">w3 Home</a>
           <a href="http://w3.eda.ibm.com/eda/">EDA Internal Page</a>
           <a href="https://w3sse.btv.ibm.com/wiki/wiki.html?wiki_id=1945">ETREE Wiki</a>
           <a href="http://w3.eda.ibm.com/afs/eda/data/edainfra/reports/eda-14.1-access.conf.html">SVN Access List</a>
         </div>
       </div>
      <!-- end left nav -->
  </div>

</body></html>

EOF



#------------------------------------------------------------------------------
# Name   : MailIt($to_whom, $requester, $comps, $access)
# Purpose: Sends email to component owners
# RCs    : email message
#------------------------------------------------------------------------------
sub MailIt() {
  my ($to, $requester, $comps, $access) = @_;

  $subject = "Action Required: approve SVN access request";
  my $from = "jwcolem\@us.ibm.com";
  my $user = GetNameFromBP($requester);
  #my $user = "";

  my $email = "To: $to\n";
  $email .= "From: $from\n";
  $email .= "Cc: $requester\n";
  $email .= "Subject: $subject\n\n";
  $email .= "This user has requested SVN access to TK component(s) you own\n";
  $email .= "\n";
  $email .= "User         : $user\n" if ($user ne "");
  $email .= "Access needed: $access\n";
  $email .= "Email        : $requester\n";
  $email .= "Component(s) : $comps\n";
  $email .= "Release      : 14.1\n";
  $email .= "\n";
  $email .= "Component Owner: $to\n";
  $email .= "\n";
  $email .= "If you approve this user's request to be given the specified";
  $email .= "access to your component in EDA's SVN repository, please ";
  $email .= "reply-all with your approval.  If you don't approve of this ";
  $email .= "access request, please reply-all and state that it is denied.\n";
  $email .= "\n";
  $email .= "This email is generated by an automated process, please ";
  $email .= "contact jwcolem@us.ibm.com if you have any questions.\n";
  $email .= "\n";
  $email .= "Thank you\n";

  # Uncomment for non web testing
  #print $email;

  open(MAIL, "|/usr/sbin/sendmail -t");
  print MAIL $email;
  close(MAIL);

  return $email;

}


#------------------------------------------------------------------------------
# Name   : GetOwner()
# Purpose: Look up the Component owner from the database
# RCs    : Returns a list of owners
#------------------------------------------------------------------------------
sub GetOwner {

  my ($comp) = @_;

  # Construct the command
  my $command = "/afs/eda/data/edainfra/tools/enablement/prod/bin/showComponent";
  $command .= " -r 14.1";
  $command .= " -c $comp";
  $command .= " | grep OWNER";

  # Run the command
  my @owners = ();
  my @results = `$command`;
  return @owners if (scalar(@results) < 1);

  foreach my $entry (@results) {
    my @tokens = split(/\: +/, $entry);
    my $owner = $tokens[1];
    $owner =~ s/(^\s+|\s+$)//g;
    push(@owners, $owner) if (! grep(/^$owner$/, @owners));
  }

  return @owners;

}


#------------------------------------------------------------------------------
# Name   : GetNameFromBP()
# Purpose: Determine the intranet id's full name ... "Cho, Minsik  (MINSIK)"
# RCs    : n/a
#------------------------------------------------------------------------------
sub GetNameFromBP {

  my ($owner) = @_;
  return "" if ($owner eq "");

  # Query BluePages for owners email address
  my $query = new BPQuery('byInternetAddr', $owner);
  my ($recordKey) = ($query->recordKeys());

  # Get serial number and country code for owners manager
  my $name = $query->record($recordKey)->valueOf('NAME');

  # Get managers email address
  return $name;

}


1;
