<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output indent="yes"/>

<xsl:param name="qd.task" select="''"/>
<xsl:param name="qd.start" select="''"/>
<xsl:param name="qd.end" select="''"/>

<xsl:template match="CloneOwner">
	<CloneOwner>
		<xsl:for-each select="*[position()=1]">
			<xsl:choose>
				<xsl:when test="$qd.task='insertTimes'">
					<DIV>
						<xsl:apply-templates select="DESC" />
						<AUDIO start="{$qd.start}" end="{$qd.end}" />
						<xsl:apply-templates select="*[not(local-name(.)='AUDIO') and not(local-name(.)='DESC')]"/>
					</DIV>
				</xsl:when>
				<xsl:when test="$qd.task='insertTextAnchor'">
					<DIV>
						<xsl:apply-templates select="DESC" />
						<xsl:apply-templates select="AUDIO" />
						<TEXT start="Page" end="Page" />
						<xsl:apply-templates select="*[not(local-name(.)='DESC') and not(local-name(.)='AUDIO') and not(local-name(.)='TEXT')]"/>
					</DIV>
				</xsl:when>
				<xsl:when test="$qd.task='appendDivision'">
					<DIV>
						<xsl:apply-templates select="*" />
						<DIV>
							<DESC><xsl:text> </xsl:text></DESC>
						</DIV>
					</DIV>
				</xsl:when>
				<xsl:when test="$qd.task='insertBefore'">
					<DIV>
						<DESC><xsl:text> </xsl:text></DESC>
					</DIV>
					<DIV>
						<xsl:apply-templates select="*" />
					</DIV>
				</xsl:when>
				<xsl:when test="$qd.task='insertAfter'">
					<DIV>
						<xsl:apply-templates select="*" />
					</DIV>
					<DIV>
						<DESC><xsl:text> </xsl:text></DESC>
					</DIV>
				</xsl:when>
				<xsl:when test="$qd.task='addLexical'">
					<DIV>
						<xsl:apply-templates select="*[not(local-name(.)='DIV')]" />
						<NOTE type="lexical"><xsl:text> </xsl:text></NOTE>
						<xsl:apply-templates select="*[local-name(.)='DIV']" />
					</DIV>
				</xsl:when>
				<xsl:when test="$qd.task='addGrammatical'">
					<DIV>
						<xsl:apply-templates select="*[not(local-name(.)='DIV')]" />
						<NOTE type="grammatical"><xsl:text> </xsl:text></NOTE>
						<xsl:apply-templates select="*[local-name(.)='DIV']" />
					</DIV>
				</xsl:when>
				<xsl:when test="$qd.task='addPronunciation'">
					<DIV>
						<xsl:apply-templates select="*[not(local-name(.)='DIV')]" />
						<NOTE type="pronunciation"><xsl:text> </xsl:text></NOTE>
						<xsl:apply-templates select="*[local-name(.)='DIV']" />
					</DIV>
				</xsl:when>
				<xsl:when test="$qd.task='addInterpretive'">
					<DIV>
						<xsl:apply-templates select="*[not(local-name(.)='DIV')]" />
						<NOTE type="interpretive"><xsl:text> </xsl:text></NOTE>
						<xsl:apply-templates select="*[local-name(.)='DIV']" />
					</DIV>
				</xsl:when>
				<xsl:when test="$qd.task='addSociolinguistic'">
					<DIV>
						<xsl:apply-templates select="*[not(local-name(.)='DIV')]" />
						<NOTE type="sociolinguistic"><xsl:text> </xsl:text></NOTE>
						<xsl:apply-templates select="*[local-name(.)='DIV']" />
					</DIV>
				</xsl:when>
				<xsl:when test="$qd.task='addCultural'">
					<DIV>
						<xsl:apply-templates select="*[not(local-name(.)='DIV')]" />
						<NOTE type="cultural"><xsl:text> </xsl:text></NOTE>
						<xsl:apply-templates select="*[local-name(.)='DIV']" />
					</DIV>
				</xsl:when>
				<xsl:when test="$qd.task='removeNode'" />
			</xsl:choose>
		</xsl:for-each>
	</CloneOwner>
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

<xsl:template match="text()">
	<xsl:copy-of select="."/>
</xsl:template>

<xsl:template match="processing-instruction()">
</xsl:template>

<xsl:template match="comment()">
	<xsl:copy-of select="."/>
</xsl:template>
	
</xsl:stylesheet>
