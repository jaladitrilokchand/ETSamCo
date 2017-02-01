#--------------------------------------------------------------------------
# Example of how to use ...
#--------------------------------------------------------------------------
#
# use email;
#
# my %emailObj;
# email::createobj(\%emailObj, "plain");

# email::addto(\%emailObj, $toAddress);
# email::addfrom(\%emailObj, $fromAddress);
# email::addsubject(\%emailObj, $subject);
# email::addbody(\%emailObj, $body);

# my $reply = email::sendit(\%emailObj);
# if ($reply == 0) {
#   print "Email sent successfully!\n";
#   $rc = $OK;
# }
# else {
#   print "ERROR: unable to send email ... $reply\n";
# }
#
#--------------------------------------------------------------------------

use strict;


package email;
require Exporter;
my @ISA = qw(Exporter);
my @EXPORT = qw(createobj addbody  addsubject addfrom
                addto addcc addbcc addfile sendit);
my @EXPORT_OK = qw();


#*****************************************************************************
# INVOCATION: createobj(\%emailobj,$type)
# INPUTS:
#    $emailobj_ref: A parameter that contains the email data
#    $type: The type of email. Either 'plain' or 'html'.
# OUTPUTS:
#   The new email object, %emailobj, is created and initialized.
# RETURN CODES:
#    None
# PROCESSING:
#    Initializes the email object for use.
#******************************************************************************
sub createobj {
  use strict;
  my ($emailobj_ref, $type) = @_;

  # Clean out the "object"
# %$emailobj_ref=();
  $emailobj_ref->{type} = $type;       #plain or html
  $emailobj_ref->{filename}=[];        #path and name of file to attach
  $emailobj_ref->{filetype}=[];        #type of data in file
  $emailobj_ref->{newname}=[];         #name of file in email
  # mime boundary marker
  $emailobj_ref->{boundary} = "_----------=_".int(time).$$;
  $emailobj_ref->{subject}='';
  $emailobj_ref->{from}=[];
  $emailobj_ref->{to}=[];
  $emailobj_ref->{cc}=[];
  $emailobj_ref->{bcc}=[];
  $emailobj_ref->{body}='';
}


#******************************************************************************
# INVOCATION: addbody(\%emailobj,$body)
# INPUTS:
#    %emailobj: The email object
#    $body: Text to be added to the email body.
# OUTPUTS:
#    Updated email body.
# RETURN CODES:
#    None
# PROCESSING:
#    Adds content to the body of the email.
#******************************************************************************
sub addbody {
  use strict;

  my ($emailobj_ref, $data) = @_;
  $emailobj_ref->{body} .= $data;

}


#******************************************************************************
# INVOCATION: addsubject(\%emailobj,$subject)
# INPUTS:
#    %emailobj: The email object
#    $subject: Text that is the subject of the email.
# OUTPUTS:
#    Updated email body.
# RETURN CODES:
#    None
# PROCESSING:
#    Sets the content of the email subject.
#******************************************************************************
sub addsubject {
  use strict;

  my ($emailobj_ref, $data) = @_;
  $emailobj_ref->{subject} = $data;

}


#******************************************************************************
# INVOCATION: addfrom(\%emailobj,$from)
# INPUTS:
#    %emailobj: The email object
#    $from: The email address of the sender.
# OUTPUTS:
#    Updated email body.
# RETURN CODES:
#    None
# PROCESSING:
#    Sets the email address of the sender.
#******************************************************************************
sub addfrom {
  use strict;

  my ($emailobj_ref, $data) = @_;
  $emailobj_ref->{from} = $data;

}


#******************************************************************************
# INVOCATION: addto(\%emailobj,$to)
# INPUTS:
#    %emailobj: The email object
#    $to: An email address of a primary recipient.
# OUTPUTS:
#    Updated email body.
# RETURN CODES:
#    None
# PROCESSING:
#    Adds the email address to the distribution list.
#******************************************************************************
sub addto {
  use strict;

  my ($emailobj_ref, $data) = @_;
  push @{$emailobj_ref->{to}},$data;

}


#******************************************************************************
# INVOCATION: addcc(\%emailobj,$cc)
# INPUTS:
#    %emailobj: The email object
#    $cc: An email address of a carbon copied recipient.
# OUTPUTS:
#    Updated email body.
# RETURN CODES:
#    None
# PROCESSING:
#    Adds the email address to the cc distribution list.
#******************************************************************************
sub addcc {
  use strict;

  my ($emailobj_ref, $data) = @_;
  push @{$emailobj_ref->{cc}},$data;

}


#******************************************************************************
# INVOCATION: addbcc(\%emailobj,$bcc)
# INPUTS:
#    %emailobj: The email object
#    $bcc: An email address of a blind carbon copied recipient.
# OUTPUTS:
#    Updated email body.
# RETURN CODES:
#    None
# PROCESSING:
#    Adds the email address to the blind cc distribution list.
#******************************************************************************
sub addbcc {
  use strict;

  my ($emailobj_ref, $data) = @_;
  push @{$emailobj_ref->{bcc}},$data;

}


#******************************************************************************
# INVOCATION: addcc(\%emailobj,$type,$name,$filename)
# INPUTS:
#    %emailobj: The email object
#    $type: The MIME type of the file. If unknown pass in ''.
#    $name: The name of the file to be encoded into the email.
#    $filename: The fully qualified name of the file
# OUTPUTS:
#    Updated email body.
# RETURN CODES:
#    None
# PROCESSING:
#    Adds the attachment to be sent to the email object.
#******************************************************************************
sub addfile {
  use strict;

  my ($emailobj_ref,$type,$name,$filename) = @_;
  push @{$emailobj_ref->{filename}}, $name;
  push @{$emailobj_ref->{filetype}}, $type;
  push @{$emailobj_ref->{newname}}, $filename;

}


#******************************************************************************
# INVOCATION: addcc(\%emailobj,$SeverName,$DomainName $DebugLevel)
# INPUTS:
#    %emailobj: The email object
#    $ServerName: The SMTP server
#    $DomainName: The domain name of the SMPT server.
#    $DebugLevel: Either 0 for none or 1 for debug messages.
# OUTPUTS:
#    The email is sent.
# RETURN CODES:
#    0: No errors
#    Error message when problems
# PROCESSING:
#    Connect to the SMTP server
#    Send the data
#    Close the connection
#    Clean out the email object
#******************************************************************************
sub sendit {
  use strict;

  # Hide from UNIX/AIX
  use MIME::Base64 qw(encode_base64);
  use Net::SMTP;

  eval("use LWP::MediaTypes qw(guess_media_type);");

  my ($emailobj_ref) = @_;

  my $crlf="\x0d\x0a";
  my @days = ("Sun","Mon","Tue","Wed","Thu","Fri","Sat");
  my @months = ("Jan","Feb","Mar","Apr","May","Jun",
                "Jul","Aug","Sep","Oct","Nov","Dec");

  my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = gmtime(time());

  my ($timestamp)= sprintf("%2.2d:%2.2d:%2.2d", $hour, $min, $sec);
  $year +=1900;

  my $to;

  my @filepieces;
  my $filetype;
  # my %mimetypes = (
  #    "doc"  => "application/winword",
  #    "zip"  => "application/zip"
  #   );

  # Create a new SMTP object
  my $smtp=Net::SMTP->new("mailrelay.fishkill.ibm.com");
  return "Unable to create an SMTP Object" if ($smtp eq "" );

  $smtp->mail($emailobj_ref->{from});

  foreach $to (@{$emailobj_ref->{to}}) {
    $smtp->to("$to");
  }

  foreach $to (@{$emailobj_ref->{cc}}) {
    $smtp->to("$to");
  }

  foreach $to (@{$emailobj_ref->{bcc}}) {
    $smtp->to("$to");
  }

  $smtp->data();

  $smtp->datasend("From: $emailobj_ref->{from}$crlf");

  if (scalar(@{$emailobj_ref->{to}}) > 0) {
    $smtp->datasend("To: <" . join(">,<",@{$emailobj_ref->{to}}) . ">$crlf");
  }

  if (scalar(@{$emailobj_ref->{cc}}) > 0) {
    $smtp->datasend("cc: <" . join(">,<",@{$emailobj_ref->{cc}}) . ">$crlf");
  }

  if (scalar(@{$emailobj_ref->{bcc}}) > 0) {
    $smtp->datasend("bcc: <" . join(">,<",@{$emailobj_ref->{bcc}}) . ">$crlf");
  }

  $smtp->datasend("Subject: $emailobj_ref->{subject}$crlf");

  $smtp->datasend("Date: $days[$wday], $mday $months[$mon] $year $timestamp UT$crlf");
  $smtp->datasend("X-Mailer: ClearQuest/MDCMS$crlf");

  # Attachment with either plain or html text

  if (scalar(@{$emailobj_ref->{filename}}) >0) {
    $smtp->datasend("Content-Transfer-Encoding: 7bit$crlf");
    $smtp->datasend("Content-Type: multipart/mixed;$crlf");
    $smtp->datasend("              boundary=\"$emailobj_ref->{boundary}\"$crlf");
    $smtp->datasend("MIME-Version: 1.0$crlf");
    $smtp->datasend("$crlf");
    $smtp->datasend("This is a multi-part message in MIME format.$crlf");
    $smtp->datasend("$crlf");
    $smtp->datasend("--$emailobj_ref->{boundary}$crlf");
    $smtp->datasend("Content-Type: text/".$emailobj_ref->{type}."$crlf");
    # The following line confuses eudora
    # $smtp->datasend("Content-Transfer-Encoding: 7bit$crlf");
    $smtp->datasend("$crlf");
  }
  # html text without attachment
  elsif ($emailobj_ref->{type} eq "html" ) {
    $smtp->datasend("MIME-Version: 1.0$crlf");
    $smtp->datasend("Content-Type: text/html$crlf");
    $smtp->datasend("$crlf");
  }
  #else it is just plain text and no mime is required

  $smtp->datasend($emailobj_ref->{body});

  # Do I want to try to put the attachments on separate lines
  # Currently all are one one line
  my $indx=0;
  my $filename;
  foreach $filename (@{$emailobj_ref->{filename}}) {
    $smtp->datasend("$crlf");
    $smtp->datasend("--$emailobj_ref->{boundary}$crlf");
    $smtp->datasend("Content-Transfer-Encoding: base64$crlf");
    # If the filetype is null, try to determine the mime type from the
	# type of file (piece after the last period)
	# If not found, the default is applicaiton/octect-stream
    if ($emailobj_ref->{filetype}[$indx] eq "") {
	 $filetype = guess_media_type($emailobj_ref->{newname}[$indx]);
 	 # (@filepieces) = split(/\./,$emailobj_ref->{newname}[$indx]);
     # if (scalar(@filepieces) and exists ($mimetypes{lc($filepieces[scalar(@filepieces)-1])}))
	 # {
     #    $filetype = $mimetypes{lc($filepieces[scalar(@filepieces)-1])};
     # }
     # else   {
     #   $filetype = "application/octet-stream";
     # }
    }
    else {
      $filetype = $emailobj_ref->{filetype}[$indx];
  	}
    $smtp->datasend("Content-Type: $filetype;$crlf");
    $smtp->datasend("              name=\"$emailobj_ref->{newname}[$indx]\"$crlf");
    $smtp->datasend("Content-Disposition: attachment;$crlf");
    $smtp->datasend("              name=\"$emailobj_ref->{newname}[$indx]\"$crlf");
    $smtp->datasend("$crlf");

    my $buf;
    open(FILE, $filename) or return "Could not open file: $filename";
    binmode FILE;
    while (read(FILE, $buf, 60*57)) {
       $smtp->datasend(encode_base64($buf));
    }
    close FILE;
    $smtp->datasend("$crlf");
    $indx++;
  }

  $smtp->dataend();

  # Close the connection
  $smtp->quit();

  # Clean out the "object"
# %$emailobj_ref=();
  $emailobj_ref->{type}='';
  $emailobj_ref->{filename}=[];
  $emailobj_ref->{filetype}=[];
  $emailobj_ref->{newname}=[];
  $emailobj_ref->{boundary}='';
  $emailobj_ref->{subject}='';
  $emailobj_ref->{from}=[];
  $emailobj_ref->{to}=[];
  $emailobj_ref->{cc}=[];
  $emailobj_ref->{bcc}=[];
  $emailobj_ref->{body}='';

  return 0;

}


1;



