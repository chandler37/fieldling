<?xml version="1.0" encoding="iso-8859-1"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" indent="yes" encoding="utf-8"/>

<!-- 
 ***                                                   ***
 *** Feuille de styles pour afficher les concordances  ***
 ***                                                   *** 
-->

<xsl:template match="W">
	<xsl:apply-templates select="M"/>
</xsl:template>
<xsl:template match="M">
	<xsl:apply-templates select="@form"/>
</xsl:template>

<!-- 
*** On separe les mots par des espaces blancs          ***
-->
<xsl:template match="W/@form">
	<xsl:value-of select="."/><xsl:text> </xsl:text>
</xsl:template>

<!-- 
*** On ecrit un '.' apres le dernier mot d'une phrase  ***
-->
<xsl:template match="S/W[last()]/@form">
	<xsl:value-of select="."/><xsl:text>. </xsl:text>
</xsl:template>



<!-- 
*** On ecrit un '-' entre les morphemes d'un meme mot  ***
-->
<xsl:template match="W/M[position()!=last()]/@form">
	<xsl:value-of select="."/><xsl:text>-</xsl:text>
</xsl:template>
<xsl:template match="W/M[position()=last()]/@form">
	<xsl:value-of select="."/><xsl:text> </xsl:text>
</xsl:template>

<!-- 
 ***                                                   ***
 *** Les trois templates a implementer obligatoirement ***
 ***                                                   *** 
-->
<xsl:template name="rightContext">
	<xsl:param name="scope"         select="''"/>
	<xsl:param name="nbWords"       select="'1'"/>
	<xsl:variable name="level"     select="local-name()"/>
	<xsl:choose>
		<xsl:when test="($scope='sentence') and ($level='W')">
			<xsl:apply-templates select="following-sibling::W[position()&lt;=$nbWords]/@form"/>
		</xsl:when>
		<xsl:when test="($scope='text') and ($level='W')">
			<xsl:apply-templates select="following::W[position()&lt;=$nbWords]/@form"/>
		</xsl:when>
		<xsl:when test="($scope='sentence') and ($level='M')">
			<xsl:apply-templates select="following-sibling::M"/>
			<xsl:apply-templates select="following::W[position()&lt;=$nbWords]"/>
		</xsl:when>
		<xsl:when test="($scope='text') and ($level='M')">
			<xsl:apply-templates select="following-sibling::M"/>
			<xsl:apply-templates select="following::W[position()&lt;=$nbWords]"/>
		</xsl:when>
		<xsl:otherwise>error</xsl:otherwise>
	</xsl:choose>
</xsl:template>
<xsl:template name="leftContext">
	<xsl:param name="scope"         select="''"/>
	<xsl:param name="nbWords"       select="'1'"/>
	<xsl:variable name="level"     select="local-name()"/>
	<xsl:choose>
		<xsl:when test="($scope='sentence') and ($level='W')">
			<xsl:apply-templates select="preceding-sibling::W[position()&lt;=$nbWords]/@form"/>
		</xsl:when>
		<xsl:when test="($scope='text') and ($level='W')">
			<xsl:apply-templates select="preceding::W[position()&lt;=$nbWords]/@form"/>
		</xsl:when>
		<xsl:when test="($scope='sentence') and ($level='M')">
			<xsl:apply-templates select="preceding::W[position()&lt;=$nbWords]"/>
			<xsl:apply-templates select="preceding-sibling::M"/>
		</xsl:when>
		<xsl:when test="($scope='text') and ($level='M')">
			<xsl:apply-templates select="preceding::W[position()&lt;=$nbWords]"/>
			<xsl:apply-templates select="preceding-sibling::M"/>
		</xsl:when>
		<xsl:otherwise>error</xsl:otherwise>
	</xsl:choose>
</xsl:template>
<xsl:template name="itemForm">
	<xsl:choose>
		<xsl:when test="local-name()='W'"><xsl:apply-templates select="@form"/></xsl:when>
		<xsl:when test="local-name()='M'"><xsl:apply-templates select="@form"/></xsl:when>
		<xsl:otherwise>error</xsl:otherwise>
	</xsl:choose>
</xsl:template>

</xsl:stylesheet>