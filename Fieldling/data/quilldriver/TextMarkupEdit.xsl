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
				<!-- Timecoding functions -->
				<xsl:when test="$qd.task='markStart'">
					<xsl:variable name="tag" select="name()"/>
					<xsl:element name="{$tag}">
						<xsl:for-each select="@*[not(name()='t1' or name()='t2')]">
							<xsl:if test="not(.='')"><xsl:copy-of select="."/></xsl:if>
						</xsl:for-each>
						<xsl:attribute name="t1"><xsl:value-of select="$qd.currentmediatime"/></xsl:attribute>
						<!-- seg tags can only get a single time point -->
						<xsl:if test="not($tag='seg')">
							<xsl:choose>
								<xsl:when test="@t2">
									<xsl:attribute name="t2"><xsl:value-of select="@t2"/></xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute name="t2"><xsl:value-of select="$qd.mediaduration"/></xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:if>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='markStop'">
					<xsl:variable name="tag" select="name()"/>
					<xsl:choose>
						<xsl:when test="$tag='seg'">
							<xsl:call-template name="copyTag"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:element name="{$tag}">
								<xsl:for-each select="@*[not(name()='t1' or name()='t2')]">
									<xsl:if test="not(.='')"><xsl:copy-of select="."/></xsl:if>
								</xsl:for-each>
								<xsl:attribute name="t2"><xsl:value-of select="$qd.currentmediatime"/></xsl:attribute>
								<xsl:choose>
									<xsl:when test="@t1">
										<xsl:attribute name="t1"><xsl:value-of select="@t1"/></xsl:attribute>
									</xsl:when>
									<xsl:otherwise>
										<xsl:attribute name="t1"><xsl:value-of select="0"/></xsl:attribute>
									</xsl:otherwise>
								</xsl:choose>
								<xsl:apply-templates/>
							</xsl:element>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:when test="$qd.task='qd.insertTimes'">
					<xsl:variable name="tag" select="name()"/>
					<xsl:element name="{$tag}">
						<xsl:for-each select="@*[not(name()='t1' or name()='t2')]">
							<xsl:if test="not(.='')"><xsl:copy-of select="."/></xsl:if>
						</xsl:for-each>
						<xsl:if test="not($qd.start=-1)">
							<xsl:attribute name="t1"><xsl:value-of select="$qd.start"/></xsl:attribute>
						</xsl:if>
						<xsl:if test="not($qd.end=-1)">
							<xsl:attribute name="t2"><xsl:value-of select="$qd.end"/></xsl:attribute>
						</xsl:if>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='increaseStart'">
					<xsl:variable name="tag" select="name()"/>
					<xsl:element name="{$tag}">
						<xsl:for-each select="@*[not(name()='t1')]">
							<xsl:if test="not(.='')"><xsl:copy-of select="."/></xsl:if>
						</xsl:for-each>
						<xsl:attribute name="t1"><xsl:value-of select="@t1 + $qd.slowincrease"/></xsl:attribute>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='rapidIncreaseStart'">
					<xsl:variable name="tag" select="name()"/>
					<xsl:element name="{$tag}">
						<xsl:for-each select="@*[not(name()='t1')]">
							<xsl:if test="not(.='')"><xsl:copy-of select="."/></xsl:if>
						</xsl:for-each>
						<xsl:attribute name="t1"><xsl:value-of select="@t1 + $qd.rapidincrease"/></xsl:attribute>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='increaseStop'">
					<xsl:variable name="tag" select="name()"/>
					<xsl:element name="{$tag}">
						<xsl:for-each select="@*[not(name()='t2')]">
							<xsl:if test="not(.='')"><xsl:copy-of select="."/></xsl:if>
						</xsl:for-each>
						<xsl:attribute name="t2"><xsl:value-of select="@t2 + $qd.slowincrease"/></xsl:attribute>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='rapidIncreaseStop'">
					<xsl:variable name="tag" select="name()"/>
					<xsl:element name="{$tag}">
						<xsl:for-each select="@*[not(name()='t2')]">
							<xsl:if test="not(.='')"><xsl:copy-of select="."/></xsl:if>
						</xsl:for-each>
						<xsl:attribute name="t2"><xsl:value-of select="@t2 + $qd.rapidincrease"/></xsl:attribute>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:when>				
				<xsl:when test="$qd.task='decreaseStart'">
					<xsl:variable name="tag" select="name()"/>
					<xsl:element name="{$tag}">
						<xsl:for-each select="@*[not(name()='t1')]">
							<xsl:if test="not(.='')"><xsl:copy-of select="."/></xsl:if>
						</xsl:for-each>
						<xsl:attribute name="t1"><xsl:value-of select="@t1 - $qd.slowincrease"/></xsl:attribute>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='rapidDecreaseStart'">
					<xsl:variable name="tag" select="name()"/>
					<xsl:element name="{$tag}">
						<xsl:for-each select="@*[not(name()='t1')]">
							<xsl:if test="not(.='')"><xsl:copy-of select="."/></xsl:if>
						</xsl:for-each>
						<xsl:attribute name="t1"><xsl:value-of select="@t1 - $qd.rapidincrease"/></xsl:attribute>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='decreaseStop'">
					<xsl:variable name="tag" select="name()"/>
					<xsl:element name="{$tag}">
						<xsl:for-each select="@*[not(name()='t2')]">
							<xsl:if test="not(.='')"><xsl:copy-of select="."/></xsl:if>
						</xsl:for-each>
						<xsl:attribute name="t2"><xsl:value-of select="@t2 - $qd.slowincrease"/></xsl:attribute>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='rapidDecreaseStop'">
					<xsl:variable name="tag" select="name()"/>
					<xsl:element name="{$tag}">
						<xsl:for-each select="@*[not(name()='t2')]">
							<xsl:if test="not(.='')"><xsl:copy-of select="."/></xsl:if>
						</xsl:for-each>
						<xsl:attribute name="t2"><xsl:value-of select="@t2 - $qd.rapidincrease"/></xsl:attribute>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='zapTimes'">
					<xsl:variable name="tag" select="name()"/>
					<xsl:element name="{$tag}">
						<xsl:for-each select="@*[not(name()='t1' or name()='t2')]">
							<xsl:if test="not(.='')"><xsl:copy-of select="."/></xsl:if>
						</xsl:for-each>
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				
				<!-- Inserting Notes -->
				<xsl:when test="$qd.task='insertGeneral'">
					<xsl:call-template name="copyTag"/>
					<NOTE type="general"><xsl:text> </xsl:text></NOTE>
				</xsl:when>
				<xsl:when test="$qd.task='insertLexical'">
					<xsl:call-template name="copyTag"/>
					<NOTE type="lexical"><xsl:text> </xsl:text></NOTE>
				</xsl:when>
				<xsl:when test="$qd.task='insertGrammatical'">
					<xsl:call-template name="copyTag"/>
					<NOTE type="grammatical"><xsl:text> </xsl:text></NOTE>
				</xsl:when>
				<xsl:when test="$qd.task='insertPronunciation'">
					<xsl:call-template name="copyTag"/>
					<NOTE type="pronunciation"><xsl:text> </xsl:text></NOTE>
				</xsl:when>
				<xsl:when test="$qd.task='insertInterpretive'">
					<xsl:call-template name="copyTag"/>
					<NOTE type="interpretive"><xsl:text> </xsl:text></NOTE>
				</xsl:when>
				<xsl:when test="$qd.task='insertSociolinguistic'">
					<xsl:call-template name="copyTag"/>
					<NOTE type="sociolinguistic"><xsl:text> </xsl:text></NOTE>
				</xsl:when>
				<xsl:when test="$qd.task='insertCultural'">
					<xsl:call-template name="copyTag"/>
					<NOTE type="cultural"><xsl:text> </xsl:text></NOTE>
				</xsl:when>
				
				<!-- Removing and copying nodes -->
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
