<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:param name="qd.mediaURL" select="''"/>

<xsl:output indent="yes"/>

<xsl:template match="/">
	<TEXT xml:lang="x-sil-TIC">
		<HEADER>
			<TITLE xml:lang="en"><xsl:text> </xsl:text></TITLE>
			<SOUNDFILE href="{$qd.mediaURL}" />
		</HEADER>
		<S>
			<FORM type="transliteration"><xsl:text> </xsl:text></FORM>
		</S>
	</TEXT>
</xsl:template>
	
</xsl:stylesheet>
