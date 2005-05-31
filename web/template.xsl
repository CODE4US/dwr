<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:html="http://www.w3.org/1999/xhtml"
    xmlns="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="html jsp"
    >

<xsl:output
    method="xml"
    indent="yes"
    encoding="windows-1252"
    doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
    doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
    />

<xsl:template match="/jsp:root">
  <jsp:root version="1.2">
    <xsl:apply-templates/>
  </jsp:root>
</xsl:template>

<xsl:template match="html:html">

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <xsl:copy-of select="html:head/*"/>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
    <link rel="stylesheet" type="text/css" href="generic.css"/>
  </head>
  <body>
    <xsl:copy-of select="html:body/@*"/>

    <div id="page-logo">DWR</div>
    <div id="page-title">Direct Web Remoting - AJAX and XMLHttpRequest made easy</div>

    <div id="page-portlet-1" class="sidebody">
      <h3>GetAhead</h3>
      <a href="http://www.getahead.ltd.uk/">Home</a><br/>
      <a href="http://www.getahead.ltd.uk/sg/space/joe/">Joe Walker</a><br/>

      <h3>DWR</h3>
      <a href="index.html">Home</a><br/>
      <a href="intro.html">Introduction</a><br/>

      <h3>Java.net</h3>
      <a href="https://dwr.dev.java.net">Project Page</a><br/>
      <a href="https://dwr.dev.java.net/servlets/ProjectDocumentList">Download</a><br/>
      <a href="https://dwr.dev.java.net/servlets/ProjectMailingListList">Mailing Lists</a><br/>

      <h3>Examples</h3>
      <a href="demo-text.html">Dynamic Text</a><br/>
      <a href="demo-list.html">Selection Lists</a><br/>
      <a href="demo-form.html">Live Forms</a><br/>
      <a href="demo-table.html">Live Tables</a><br/>
      <a href="demo-validate.html">Validation</a><br/>
      <a href="demo-postcode.html">Address Entry</a><br/>

      <h3>Documentation</h3>
      <a href="intro.html">Introduction</a><br/>
      <a href="browsers.html">Browsers</a><br/>
      <a href="spring.html">DWR and Spring</a><br/>
      <a href="ref-webxml.html">web.xml Config</a><br/>
      <a href="ref-dwrxml.html">dwr.xml Config</a><br/>
      <a href="js_docs_out/">Script Docs</a><br/>
      <a href="ref-dwrdtd.html">dwr.xml DTD</a><br/>
      <a href="javadoc-ExecutionContext.html">JavaDoc</a><br/>
      <a href="issues.html">Issues</a><br/>
      <a href="upgrading.html">Upgrading</a><br/>
      <a href="changelog.html">Change Log</a><br/>
    </div>

    <div id="page-content">
      <xsl:copy-of select="html:body/*"/>
    </div>

   </body>
</html>

</xsl:template>

</xsl:stylesheet>
