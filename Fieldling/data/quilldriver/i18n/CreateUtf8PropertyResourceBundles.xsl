<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    version="2.0">

<xsl:output method="text" 
     encoding="utf-8"/>
     
<xsl:param name="output.directory" select="''"/>

<xsl:template match="/">
    <xsl:variable name="root" select="."/>
    <xsl:for-each select="distinct-values(//text/@lang)">
        <xsl:variable name="resource.bundle.filename" select="concat($output.directory, '/QdResources', '_', ., '.properties')"/>
        <xsl:result-document href="{$resource.bundle.filename}" method="text">
            <html>
                    <body>
                        <xsl:call-template name="doWork">
                                <xsl:with-param name="root.element" select="$root/all-messages"/>
                                <xsl:with-param name="language" select="."/>
                        </xsl:call-template>
                    </body>
            </html>
        </xsl:result-document>
    </xsl:for-each>
</xsl:template>

<xsl:template name="doWork">
        <xsl:param name="root.element"/>
        <xsl:param name="language" select="''"/>
        <xsl:for-each select="$root.element/message">
                <xsl:value-of select="@id"/>
                <xsl:text>=</xsl:text>
                <xsl:value-of select="text[@lang=$language]"/>
<xsl:text>
</xsl:text>
        </xsl:for-each>
</xsl:template>

</xsl:stylesheet>
