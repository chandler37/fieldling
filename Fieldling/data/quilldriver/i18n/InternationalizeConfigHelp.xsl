<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    version="2.0">

<xsl:param name="filename.minus.extension" select="''"/>

<xsl:template match="/">
    <xsl:variable name="root" select="."/>
    <xsl:for-each select="distinct-values(//text/@lang)">
        <xsl:variable name="i18n.filename" select="concat($filename.minus.extension, '_', ., '.html')"/>
        <xsl:result-document href="{$i18n.filename}" method="xhtml" encoding="utf8">
            <html>
                    <body>
                        <xsl:call-template name="doWork">
                                <xsl:with-param name="document" select="$root"/>
                                <xsl:with-param name="language" select="."/>
                        </xsl:call-template>
                    </body>
            </html>
        </xsl:result-document>
    </xsl:for-each>
</xsl:template>

<xsl:template name="doWork">
        <xsl:param name="document"/>
        <xsl:param name="language" select="''"/>
        <h3>Keyboard Shortcuts</h3>
        <dl>
                <xsl:for-each select="$document//action">
                    <dt>
                            <em><xsl:value-of select="id(@name)/text[@lang=$language]"/></em>
                            <xsl:if test="@keystroke">
                                <xsl:text> (</xsl:text>
                                <xsl:value-of select="keyutil:convertKeyDescriptionToReadableFormat(@keystroke)" xmlns:keyutil="java:fieldling.util.KeyStrokeUtils"/>
                                <xsl:text>)</xsl:text>
                            </xsl:if>
                    </dt>
                    <dd>
                            <xsl:value-of select="id(helptext/@ref)/text[@lang=$language]"/>
                    </dd>
                </xsl:for-each>
        </dl>
</xsl:template>

</xsl:stylesheet>
