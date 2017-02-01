#!/usr/bin/perl

print "Content-type: text/html\n\n";

print <<"EOF";
<html xmlns="" xml:lang="en" lang="en">
<head>
<title>EDA 14.1 - SVN Access Request Form</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
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

<script language="javascript" type="text/javascript">
function validateForm(f) {
  var msg = "";

  var a = f.access;
  var isChecked = false;
  for ( var i = 0; i < a.length; i++) {
    if (a[i].checked) {
       isChecked = true;
    }
  }
  if (! isChecked) {
    msg += " * An Access Type must be selected\\n";
  }

  var e = f.email.value;
  if (e == null || e == "") {
    msg += " * Intranet ID must be specified\\n";
  }

  var c = f.components.selectedIndex;
  if (c == -1) {
    msg += " * One or more Components must be selected\\n";
  }

  if (msg != "") {
    var m = "The following errors were found while verifying this form.\\n";
    m += msg;
    m += "\\nPlease correct these errors and resubmit. Thank you!";
    alert(m);
    return false;
  }

}
</script>


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
<
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

   <a name="navskip"></a>
   <table width="100%" cellpadding="0" cellspacing="0" border="0">
     <tr>
       <h2>SVN Access Request Form</h2>
<br>
Complete this form and click Submit to request access to EDA's Tool Kit Subversion(SVN) repository. 
To view your current SVN access click on <i>SVN Access List</i> in the left navigation.<br>

       <form name="myForm" onsubmit="return validateForm(this);" action="./svn_register2.pl" method="POST">
         <br>
           <b>Access Type:</b> <br>
          <input type="radio" name="access" value="WRITE" checked> Write<br>
          <input type="radio" name="access" value="READ"> Read only<br>
          <input type="radio" name="access" value="NONE"> None (delete all access)<br>
          <br>
          <b>Apply request to this Intranet ID:</b><br>
          <input type="text" name="email"><i> (e.g. johndoe\@us.ibm.com)</i><br><br>

EOF


print '<b>Apply request to these Tool Kit Component(s):</b> <br>';
print '<select name="components" size=10 multiple >';

# Fill in the Component list
$dbAPIbin = '/afs/eda/data/edainfra/tools/enablement/prod/bin';
$complst = `$dbAPIbin/getComponents -q -t 14.1.build`;
@comparr = split (/,/ , $complst);

foreach $cmpnt (sort(@comparr)) {
  print "\<option value=\"$cmpnt\"\>$cmpnt\<\/option\> \n";
}
print '</select>';
print '<br><br>';
print "</p>";

print '<p><input type="submit" value="Submit"> </p></form>' ;

print '<br><i>This request will be sent to Tool Kit Component owners for review and approval. 
Their approval is required before an SVN administrator can grant the requested access.</i>';
print "\n";

print '</tr></table>';

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


