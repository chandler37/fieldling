<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:qq="http://altiplano.emich.edu/ucuchi"
    xmlns:qd="http://altiplano.emich.edu/quilldriver"
    exclude-result-prefixes="qd qq"
    version="2.0">
    
    <xsl:output method="text"/>
    
    <xsl:variable name="delimiter" select="'^'"/>
    <xsl:variable name="speakers" select="/qq:TEXT/qq:META"/>
    
    <xsl:template match="/">
        <xsl:apply-templates select="qq:TEXT"/>
    </xsl:template>
    
    <xsl:template match="qq:TEXT">
        <xsl:apply-templates select="qq:C"/>
    </xsl:template>
    
    <xsl:template match="qq:C">
        <xsl:variable name="sp" select="@spid"/>
        <xsl:choose>
                <xsl:when test="$sp = preceding-sibling::qq:C[1]/@spid">
                        <xsl:value-of select="$delimiter"/>
                </xsl:when>
                <xsl:otherwise>
                        <xsl:value-of select="$speakers/qq:SPEAKER[@id=$sp]"/><xsl:value-of select="$delimiter"/>
                </xsl:otherwise>
        </xsl:choose>
        <xsl:value-of select="qq:F"/><xsl:value-of select="$delimiter"/>
        <xsl:value-of select="qq:TRANSL"/><xsl:value-of select="$delimiter"/>
        <xsl:value-of select="@qd:t1"/><xsl:value-of select="$delimiter"/>
        <xsl:value-of select="@qd:t2"/><xsl:value-of select="$delimiter"/>
        <xsl:value-of select="$speakers/qq:SPEAKER[@id=$sp]"/><xsl:text>
</xsl:text>
    </xsl:template>
    
</xsl:stylesheet>
