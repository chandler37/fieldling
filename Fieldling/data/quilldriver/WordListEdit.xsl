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
				<xsl:when test="$qd.task='insertAfter'">
					<xsl:call-template name="copyTag"/>
					<WORD>
						<TIB><xsl:text> </xsl:text></TIB>
						<ENG><xsl:text> </xsl:text></ENG>
					</WORD>
				</xsl:when>
				<xsl:when test="$qd.task='insertBefore'">
					<WORD>
						<TIB><xsl:text> </xsl:text></TIB>
						<ENG><xsl:text> </xsl:text></ENG>
					</WORD>
					<xsl:call-template name="copyTag"/>
				</xsl:when>
				<xsl:when test="$qd.task='markStart'">
					<xsl:variable name="tag" select="name()"/>
					<xsl:element name="{$tag}">
						<xsl:attribute name="start"><xsl:value-of select="$qd.currentmediatime"/></xsl:attribute>
						<xsl:choose>
							<xsl:when test="@end">
								<xsl:attribute name="end"><xsl:value-of select="@end"/></xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="end"><xsl:value-of select="$qd.mediaduration"/></xsl:attribute>
							</xsl:otherwise>
						</xsl:choose>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='markStop'">
					<xsl:variable name="tag" select="name()"/>
					<xsl:element name="{$tag}">
						<xsl:attribute name="end"><xsl:value-of select="$qd.currentmediatime"/></xsl:attribute>
						<xsl:choose>
							<xsl:when test="@start">
								<xsl:attribute name="start"><xsl:value-of select="@start"/></xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="start"><xsl:value-of select="0"/></xsl:attribute>
							</xsl:otherwise>
						</xsl:choose>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='increaseStart'">
					<xsl:variable name="tag" select="name()"/>
					<xsl:element name="{$tag}">
						<xsl:attribute name="start"><xsl:value-of select="@start + $qd.slowincrease"/></xsl:attribute>
						<xsl:attribute name="end"><xsl:value-of select="@end"/></xsl:attribute>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='rapidIncreaseStart'">
					<xsl:variable name="tag" select="name()"/>
					<xsl:element name="{$tag}">
						<xsl:attribute name="start"><xsl:value-of select="@start + $qd.rapidincrease"/></xsl:attribute>
						<xsl:attribute name="end"><xsl:value-of select="@end"/></xsl:attribute>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='increaseStop'">
					<xsl:variable name="tag" select="name()"/>
					<xsl:element name="{$tag}">
						<xsl:attribute name="end"><xsl:value-of select="@end + $qd.slowincrease"/></xsl:attribute>
						<xsl:attribute name="start"><xsl:value-of select="@start"/></xsl:attribute>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='rapidIncreaseStop'">
					<xsl:variable name="tag" select="name()"/>
					<xsl:element name="{$tag}">
						<xsl:attribute name="end"><xsl:value-of select="@end + $qd.rapidincrease"/></xsl:attribute>
						<xsl:attribute name="start"><xsl:value-of select="@start"/></xsl:attribute>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:when>				
				<xsl:when test="$qd.task='decreaseStart'">
					<xsl:variable name="tag" select="name()"/>
					<xsl:element name="{$tag}">
						<xsl:attribute name="start"><xsl:value-of select="@start - $qd.slowincrease"/></xsl:attribute>
						<xsl:attribute name="end"><xsl:value-of select="@end"/></xsl:attribute>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='rapidDecreaseStart'">
					<xsl:variable name="tag" select="name()"/>
					<xsl:element name="{$tag}">
						<xsl:attribute name="start"><xsl:value-of select="@start - $qd.rapidincrease"/></xsl:attribute>
						<xsl:attribute name="end"><xsl:value-of select="@end"/></xsl:attribute>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='decreaseStop'">
					<xsl:variable name="tag" select="name()"/>
					<xsl:element name="{$tag}">
						<xsl:attribute name="end"><xsl:value-of select="@end - $qd.slowincrease"/></xsl:attribute>
						<xsl:attribute name="start"><xsl:value-of select="@start"/></xsl:attribute>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='rapidDecreaseStop'">
					<xsl:variable name="tag" select="name()"/>
					<xsl:element name="{$tag}">
						<xsl:attribute name="end"><xsl:value-of select="@end - $qd.rapidincrease"/></xsl:attribute>
						<xsl:attribute name="start"><xsl:value-of select="@start"/></xsl:attribute>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='qd.insertTimes'">
					<xsl:variable name="tag" select="name()"/>
					<xsl:element name="{$tag}">
						<xsl:attribute name="start"><xsl:value-of select="$qd.start"/></xsl:attribute>
						<xsl:attribute name="end"><xsl:value-of select="$qd.end"/></xsl:attribute>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='zapTimes'">
					<xsl:variable name="tag" select="name()"/>
					<xsl:element name="{$tag}">
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:when>

				<!-- All I really need to do here is replace SOUNDFILE; however, QD has a bug
				whereby it doesn't do transformations right on nodes that are not visually present. -->
				<xsl:when test="$qd.task='fixMedia'">
					<SOUNDFILE href="{$qd.mediaurlstring}"/>
				</xsl:when>
				<xsl:when test="$qd.task='removeNode'"/>
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
