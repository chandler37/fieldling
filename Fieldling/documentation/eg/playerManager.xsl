<?xml version="1.0" encoding="iso-8859-1"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output 
	method="html" 
	indent="yes" 
	encoding="iso-8859-1"/>

<!-- player should have one of these 2 values: jmf|qt4j -->
<xsl:param name="player" select="'jmf'"/> 
<xsl:param name="mediafile" select="'BAC.mp3'"/>

<xsl:template match="/">
	<HTML>
		<HEAD>
			<script language="Javascript" src="playerManager.js">.</script>
			<xsl:choose>
				<xsl:when test="$player='jmf'"><xsl:call-template name="forJMF"/></xsl:when>
				<xsl:when test="$player='qt4j'"><xsl:call-template name="for4QT4J"/></xsl:when>
			</xsl:choose>
		</HEAD>
		<BODY>
			<OL><xsl:apply-templates select=".//S"/></OL>
		</BODY>
	</HTML>
</xsl:template>  


<xsl:template match="S[./AUDIO]">
	<LI>
		<a href="javascript:void(0)" style="text-decoration:none;color:black;">
		<xsl:attribute name="ID"><xsl:value-of select="generate-id(.)"/></xsl:attribute>
		<xsl:attribute name="onclick">playFrom('<xsl:value-of select="generate-id(.)"/>')</xsl:attribute>
		<xsl:apply-templates/>
		</a>
	</LI>
</xsl:template>

<xsl:template name="forJMF">
	<APPLET 	CODEBASE="../../dist" 
			ARCHIVE="mediaplayer.jar" 
			CODE="fieldling.mediaplayer.JMFPlayerApplet.class"
			WIDTH="300" HEIGHT="20" NAME="player" MAYSCRIPT="">
		<PARAM>
			<xsl:attribute name="NAME">Sound</xsl:attribute>
			<xsl:attribute name="VALUE"><xsl:value-of select="$mediafile"/></xsl:attribute>
		</PARAM>
		<PARAM>
			<xsl:attribute name="NAME">IDS</xsl:attribute>
			<xsl:attribute name="VALUE">
				<xsl:for-each select="//S"><xsl:value-of select="generate-id(.)"/>,</xsl:for-each>
			</xsl:attribute>
		</PARAM>
		<PARAM>
			<xsl:attribute name="NAME">STARTS</xsl:attribute>
			<xsl:attribute name="VALUE">
				<xsl:for-each select="//S/AUDIO"><xsl:value-of select="@start"/>,</xsl:for-each>
			</xsl:attribute>
		</PARAM>
		<PARAM>
			<xsl:attribute name="NAME">ENDS</xsl:attribute>
			<xsl:attribute name="VALUE">
				<xsl:for-each select="//S/AUDIO"><xsl:value-of select="@end"/>,</xsl:for-each>
			</xsl:attribute>
		</PARAM>
	</APPLET>
</xsl:template>
<xsl:template name="for4QT4J">
	<APPLET 	CODEBASE="../../dist" 
			ARCHIVE="mediaplayer.jar" 
			CODE="fieldling.mediaplayer.QT4JPlayerApplet.class"
			WIDTH="320" HEIGHT="16" NAME="player" MAYSCRIPT="">
		<PARAM>
			<xsl:attribute name="NAME">Sound</xsl:attribute>
			<xsl:attribute name="VALUE"><xsl:value-of select="$mediafile"/></xsl:attribute>
		</PARAM>
		<PARAM>
			<xsl:attribute name="NAME">IDS</xsl:attribute>
			<xsl:attribute name="VALUE">
				<xsl:for-each select="//S"><xsl:value-of select="generate-id(.)"/>,</xsl:for-each>
			</xsl:attribute>
		</PARAM>
		<PARAM>
			<xsl:attribute name="NAME">STARTS</xsl:attribute>
			<xsl:attribute name="VALUE">
				<xsl:for-each select="//S/AUDIO"><xsl:value-of select="@start"/>,</xsl:for-each>
			</xsl:attribute>
		</PARAM>
		<PARAM>
			<xsl:attribute name="NAME">ENDS</xsl:attribute>
			<xsl:attribute name="VALUE">
				<xsl:for-each select="//S/AUDIO"><xsl:value-of select="@end"/>,</xsl:for-each>
			</xsl:attribute>
		</PARAM>
	</APPLET>
</xsl:template>

</xsl:stylesheet>
		