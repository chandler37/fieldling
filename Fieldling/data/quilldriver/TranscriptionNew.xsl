<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:param name="qd.mediaURL" select="''"/>

<xsl:output indent="yes"/>

<xsl:template match="/">
    <xsl:text disable-output-escaping="yes"><![CDATA[
        <!DOCTYPE TEXT [
            <!ELEMENT TEXT (HEADER,S+)>
            <!ATTLIST TEXT xml:lang CDATA #IMPLIED>
            <!ELEMENT HEADER (TITLE?,SOUNDFILE?,SPEAKER*)>
            <!ELEMENT TITLE (#PCDATA)>
            <!ATTLIST TITLE xml:lang CDATA #IMPLIED>
            <!ELEMENT SOUNDFILE (#PCDATA)>
            <!ATTLIST SOUNDFILE href CDATA #IMPLIED>
            <!ELEMENT SPEAKER (#PCDATA)>
            <!ATTLIST SPEAKER personId ID #REQUIRED>
            <!ELEMENT S (FORM|TRANSL|AUDIO)*>
            <!ATTLIST S id ID #IMPLIED>
            <!ATTLIST S who IDREF #IMPLIED>
            <!ELEMENT FORM (#PCDATA)>
            <!ATTLIST FORM type CDATA #IMPLIED>
            <!ELEMENT TRANSL (#PCDATA)>
            <!ATTLIST TRANSL xml:lang CDATA #IMPLIED>
            <!ELEMENT AUDIO EMPTY>
            <!ATTLIST AUDIO start CDATA #REQUIRED>
            <!ATTLIST AUDIO end CDATA #REQUIRED>]>]]>
    </xsl:text>
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
