<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output indent="yes"/>

<xsl:param name="qd.task" select="''"/>
<xsl:param name="qd.currentmediatime" select="''"/>
<xsl:param name="qd.start" select="''"/>
<xsl:param name="qd.end" select="''"/>
<xsl:param name="qd.mediaduration" select="''"/>
<xsl:param name="qd.slowincrease" select=".025"/>
<xsl:param name="qd.rapidincrease" select=".250"/>
<xsl:param name="qd.mediaurlstring" select="''"/>

<xsl:template match="CloneOwner">
	<CloneOwner>
		<xsl:for-each select="*[position()=1]">
			<xsl:choose>
				<xsl:when test="$qd.task='fixMedia'">
					<SOUNDFILE href="{$qd.mediaurlstring}"/>
				</xsl:when>
				<xsl:when test="$qd.task='markStart'">
					<DIV>
						<xsl:apply-templates select="*[not(local-name(.)='AUDIO')]"/>
						<xsl:choose>
							<xsl:when test="AUDIO">
								<AUDIO start="{$qd.currentmediatime}" end="{AUDIO/@end}" />
							</xsl:when>
							<xsl:otherwise>
								<AUDIO start="{$qd.currentmediatime}" end="{$qd.mediaduration}" />
							</xsl:otherwise>
						</xsl:choose>
					</DIV>
				</xsl:when>
				<xsl:when test="$qd.task='markStop'">
					<DIV>
						<xsl:apply-templates select="*[not(local-name(.)='AUDIO')]"/>
						<xsl:choose>
							<xsl:when test="AUDIO">
								<AUDIO start="{AUDIO/@start}" end="{$qd.currentmediatime}" />
							</xsl:when>
							<xsl:otherwise>
								<AUDIO start="0" end="{$qd.currentmediatime}" />
							</xsl:otherwise>
						</xsl:choose>
					</DIV>
				</xsl:when>
				<xsl:when test="$qd.task='increaseStart'">
					<DIV>
						<xsl:apply-templates select="*[not(local-name(.)='AUDIO')]"/>
						<xsl:if test="AUDIO">
							<AUDIO start="{AUDIO/@start + $qd.slowincrease}" end="{AUDIO/@end}" />
						</xsl:if>
					</DIV>
				</xsl:when>
				<xsl:when test="$qd.task='rapidIncreaseStart'">
					<DIV>
						<xsl:apply-templates select="*[not(local-name(.)='AUDIO')]"/>
						<xsl:if test="AUDIO">
							<AUDIO start="{AUDIO/@start + $qd.rapidincrease}" end="{AUDIO/@end}" />
						</xsl:if>
					</DIV>
				</xsl:when>
				<xsl:when test="$qd.task='increaseStop'">
					<DIV>
						<xsl:apply-templates select="*[not(local-name(.)='AUDIO')]"/>
						<xsl:if test="AUDIO">
							<AUDIO start="{AUDIO/@start}" end="{AUDIO/@end + $qd.slowincrease}" />
						</xsl:if>
					</DIV>
				</xsl:when>
				<xsl:when test="$qd.task='rapidIncreaseStop'">
					<DIV>
						<xsl:apply-templates select="*[not(local-name(.)='AUDIO')]"/>
						<xsl:if test="AUDIO">
							<AUDIO start="{AUDIO/@start}" end="{AUDIO/@end + $qd.rapidincrease}" />
						</xsl:if>
					</DIV>
				</xsl:when>				
				<xsl:when test="$qd.task='decreaseStart'">
					<DIV>
						<xsl:apply-templates select="*[not(local-name(.)='AUDIO')]"/>
						<xsl:if test="AUDIO">
							<AUDIO start="{AUDIO/@start - $qd.slowincrease}" end="{AUDIO/@end}" />
						</xsl:if>
					</DIV>
				</xsl:when>
				<xsl:when test="$qd.task='rapidDecreaseStart'">
					<DIV>
						<xsl:apply-templates select="*[not(local-name(.)='AUDIO')]"/>
						<xsl:if test="AUDIO">
							<AUDIO start="{AUDIO/@start - $qd.rapidincrease}" end="{AUDIO/@end}" />
						</xsl:if>
					</DIV>
				</xsl:when>				
				<xsl:when test="$qd.task='decreaseStop'">
					<DIV>
						<xsl:apply-templates select="*[not(local-name(.)='AUDIO')]"/>
						<xsl:if test="AUDIO">
							<AUDIO start="{AUDIO/@start}" end="{AUDIO/@end - $qd.slowincrease}" />
						</xsl:if>
					</DIV>
				</xsl:when>
				<xsl:when test="$qd.task='rapidDecreaseStop'">
					<DIV>
						<xsl:apply-templates select="*[not(local-name(.)='AUDIO')]"/>
						<xsl:if test="AUDIO">
							<AUDIO start="{AUDIO/@start}" end="{AUDIO/@end - $qd.rapidincrease}" />
						</xsl:if>
					</DIV>
				</xsl:when>
				<xsl:when test="$qd.task='qd.insertTimes'">
					<DIV>
						<xsl:apply-templates select="*[not(local-name(.)='AUDIO')]"/>
						<AUDIO start="{$qd.start}" end="{$qd.end}" />
					</DIV>
				</xsl:when>
			<!--	<xsl:when test="$qd.task='insertTextAnchor'">
					<DIV>
						<xsl:apply-templates select="DESC" />
						<TEXT start="Page" end="Page" />
						<xsl:apply-templates select="*[not(local-name(.)='DESC') and not(local-name(.)='TEXT')]"/>
					</DIV>
				</xsl:when> -->
				<xsl:when test="$qd.task='surroundWithBreaks'">
					<PG id=" "/>
					<xsl:call-template name="copyTag"/>
					<PG id=" "/>
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
				<xsl:otherwise>
					<xsl:call-template name="copyTag"/>
				</xsl:otherwise>
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
