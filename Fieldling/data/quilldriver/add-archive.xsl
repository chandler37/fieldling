<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output method="xml" indent="yes"/>

<xsl:param name="date" select="''"/>
<xsl:param name="jnlp" select="''"/>
<xsl:param name="zip" select="''"/>
<xsl:param name="note" select="''"/>

<xsl:template match="qd-jws/version[position()=1]">
	<xsl:call-template name="copyTag"/>
	<version date="{$date}" jnlp="{$jnlp}" zip="{$zip}">
		<xsl:value-of select="$note"/>
	</version>
</xsl:template>

<xsl:template name="copyTag">
	<xsl:variable name="tag" select="local-name()"/>
	<xsl:variable name="elem" select="."/>
	<xsl:element name="{$tag}">
		<xsl:for-each select="@*">
			<xsl:if test="not(.='')"><xsl:copy-of select="."/></xsl:if>
		</xsl:for-each>
		<xsl:apply-templates/>
	</xsl:element>	
</xsl:template>

<xsl:template match="*">
	<xsl:call-template name="copyTag" />
</xsl:template>

</xsl:stylesheet>
