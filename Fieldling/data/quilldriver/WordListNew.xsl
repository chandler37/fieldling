<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output indent="yes"/>

<xsl:param name="qd.mediaURL" select="''"/>

<xsl:template match="/">
	<WORDLIST>
		<HEADER>
			<TITLE xml:lang="en"><xsl:text> </xsl:text></TITLE>
			<SOUNDFILE href="{$qd.mediaURL}" />
		</HEADER>
		<WORD>
			<TIB><xsl:text> </xsl:text></TIB>
			<ENG><xsl:text> </xsl:text></ENG>
		</WORD>
	</WORDLIST>
</xsl:template>
	
</xsl:stylesheet>
