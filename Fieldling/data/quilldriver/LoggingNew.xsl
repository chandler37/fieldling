<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output indent="yes"/>

<xsl:param name="qd.mediaURL" select="''"/>

<xsl:template match="/">
	<LOG>
		<HEADER>
			<TITLE xml:lang="en"><xsl:text> </xsl:text></TITLE>
			<LOGGER><xsl:text> </xsl:text></LOGGER>
			<SOUNDFILE href="{$qd.mediaURL}" />
			<TEXTFILE href=" " />
		</HEADER>
		<DIV>
			<DESC><xsl:text> </xsl:text></DESC>
		</DIV>
	</LOG>
</xsl:template>
	
</xsl:stylesheet>
