<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">

<xsl:param name="qd.mediaURL" select="''"/>

<xsl:output indent="yes"/>

<xsl:template match="/">
	<ucuchi:TEXT xmlns:ucuchi="http://altiplano.emich.edu/ucuchi" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:qd="http://altiplano.emich.edu/quilldriver" xsi:schemaLocation="http://altiplano.emich.edu/ucuchi QuechuaTranscript.xsd">
		<ucuchi:META>
            <ucuchi:SPEAKER id="s1">PUT SPEAKER NAME HERE</ucuchi:SPEAKER>
        </ucuchi:META>
        <ucuchi:S>
            <ucuchi:C spid="s1" id="i0">
                <ucuchi:S-F><xsl:text> </xsl:text></ucuchi:S-F>
            </ucuchi:C>
        </ucuchi:S>
    </ucuchi:TEXT>
</xsl:template>
	
</xsl:stylesheet>
