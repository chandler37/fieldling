<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:ucuchi="http://altiplano.emich.edu/ucuchi"
    xmlns:qd="http://altiplano.emich.edu/quilldriver"
    version="1.0">

<!-- <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:scanner="xalan://org.thdl.tib.scanner.RemoteTibetanScanner"
	exclude-result-prefixes="scanner" version="1.0"> -->

<xsl:output indent="yes"/>

<xsl:param name="qd.task" select="''"/>
<xsl:param name="qd.currentmediatime" select="''"/>
<xsl:param name="qd.start" select="''"/>
<xsl:param name="qd.end" select="''"/>
<xsl:param name="qd.mediaduration" select="''"/>
<xsl:param name="qd.slowincrease" select=".025"/>
<xsl:param name="qd.rapidincrease" select=".250"/>
<xsl:param name="qd.mediaurlstring" select="''"/>

<xsl:param name="speaker1" select="''"/>
<xsl:param name="speaker2" select="''"/>
<xsl:param name="speaker3" select="''"/>
<xsl:param name="speaker4" select="''"/>
<xsl:param name="speaker5" select="''"/>
<xsl:param name="speaker6" select="''"/>
<xsl:param name="speaker7" select="''"/>
<xsl:param name="speaker8" select="''"/>

<!-- <xsl:param name="dictURL" select="'http://iris.lib.virginia.edu/tibetan/servlet/org.thdl.tib.scanner.RemoteScannerFilter'"/> -->

<xsl:template match="CloneOwner">
	<CloneOwner>
		<xsl:for-each select="*[position()=1]">
			<xsl:choose>
				<xsl:when test="$qd.task='setSpeaker1'">
                    <xsl:element name="ucuchi:C">
                        <xsl:attribute name="spid"><xsl:value-of select="$speaker1" /></xsl:attribute>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t1">
							<xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t2">
							<xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2" /></xsl:attribute>
						</xsl:if>
						<xsl:apply-templates />
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='setSpeaker2'">
					<xsl:element name="ucuchi:C">
                        <xsl:attribute name="spid"><xsl:value-of select="$speaker2" /></xsl:attribute>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t1">
							<xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t2">
							<xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2" /></xsl:attribute>
						</xsl:if>
						<xsl:apply-templates />
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='setSpeaker3'">
					<xsl:element name="ucuchi:C">
                        <xsl:attribute name="spid"><xsl:value-of select="$speaker3" /></xsl:attribute>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t1">
							<xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t2">
							<xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2" /></xsl:attribute>
						</xsl:if>
						<xsl:apply-templates />
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='setSpeaker4'">
					<xsl:element name="ucuchi:C">
                        <xsl:attribute name="spid"><xsl:value-of select="$speaker4" /></xsl:attribute>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t1">
							<xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t2">
							<xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2" /></xsl:attribute>
						</xsl:if>
						<xsl:apply-templates />
					</xsl:element>
				</xsl:when>
                <xsl:when test="$qd.task='setSpeaker5'">
					<xsl:element name="ucuchi:C">
                        <xsl:attribute name="spid"><xsl:value-of select="$speaker5" /></xsl:attribute>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t1">
							<xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t2">
							<xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2" /></xsl:attribute>
						</xsl:if>
						<xsl:apply-templates />
					</xsl:element>
				</xsl:when>
                <xsl:when test="$qd.task='setSpeaker6'">
					<xsl:element name="ucuchi:C">
                        <xsl:attribute name="spid"><xsl:value-of select="$speaker6" /></xsl:attribute>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t1">
							<xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t2">
							<xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2" /></xsl:attribute>
						</xsl:if>
						<xsl:apply-templates />
					</xsl:element>
				</xsl:when>
                <xsl:when test="$qd.task='setSpeaker7'">
					<xsl:element name="ucuchi:C">
                        <xsl:attribute name="spid"><xsl:value-of select="$speaker7" /></xsl:attribute>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t1">
							<xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t2">
							<xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2" /></xsl:attribute>
						</xsl:if>
						<xsl:apply-templates />
					</xsl:element>
				</xsl:when>
                <xsl:when test="$qd.task='setSpeaker8'">
					<xsl:element name="ucuchi:C">
                        <xsl:attribute name="spid"><xsl:value-of select="$speaker8" /></xsl:attribute>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t1">
							<xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t2">
							<xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2" /></xsl:attribute>
						</xsl:if>
						<xsl:apply-templates />
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='addSpeaker'">
					<xsl:variable name="n" select="count(ucuchi:SPEAKER)"/>
					<ucuchi:META>
						<xsl:apply-templates select="*" />
						<xsl:element name="ucuchi:SPEAKER">
							<xsl:attribute name="id">
								<xsl:choose>
									<xsl:when test="$n=0">s1</xsl:when>
									<xsl:when test="$n=1">s2</xsl:when>
									<xsl:when test="$n=2">s3</xsl:when>
									<xsl:when test="$n=3">s4</xsl:when>
                                    <xsl:when test="$n=4">s5</xsl:when>
									<xsl:when test="$n=5">s6</xsl:when>
									<xsl:when test="$n=6">s7</xsl:when>
									<xsl:when test="$n=7">s8</xsl:when>
									<xsl:otherwise>X</xsl:otherwise>
								</xsl:choose>
							</xsl:attribute>
							<xsl:text> </xsl:text>
						</xsl:element>
					</ucuchi:META>
				</xsl:when>
				<xsl:when test="$qd.task='computeIDs'">
					<xsl:call-template name="copyTag" />
				</xsl:when>
				<xsl:when test="$qd.task='newClause'">
					<xsl:element name="ucuchi:C">
						<xsl:if test="@spid">
							<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t1">
							<xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t2">
							<xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2" /></xsl:attribute>
						</xsl:if>
						<xsl:apply-templates />
					</xsl:element>
					<ucuchi:C spid="{@spid}" qd:t1="{@qd:t2}" qd:t2="{$qd.mediaduration}">
						<ucuchi:S-F><xsl:text> </xsl:text></ucuchi:S-F>
					</ucuchi:C>
				</xsl:when>
                <xsl:when test="$qd.task='newSentence'">
                    <ucuchi:S>
					    <xsl:apply-templates/>
                    </ucuchi:S>
                    <xsl:variable name="lastclause" select="ucuchi:C[position()=last()]"/>
					<ucuchi:S>
                        <ucuchi:C spid="{$lastclause/@spid}" qd:t1="{$lastclause/@qd:t2}" qd:t2="{$qd.mediaduration}">
                            <ucuchi:S-F><xsl:text> </xsl:text></ucuchi:S-F>
                        </ucuchi:C>
                    </ucuchi:S>
                </xsl:when>
				<xsl:when test="$qd.task='markStart'">
					<xsl:element name="ucuchi:C">
						<xsl:if test="@spid">
							<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
                        <xsl:attribute name="qd:t1"><xsl:value-of select="$qd.currentmediatime"/></xsl:attribute>
						<xsl:choose>
							<xsl:when test="@qd:t2">
								<xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2"/></xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="qd:t2"><xsl:value-of select="$qd.mediaduration"/></xsl:attribute>
							</xsl:otherwise>
						</xsl:choose>
                        <xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='markStop'">
					<xsl:element name="ucuchi:C">
						<xsl:if test="@spid">
							<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
                        <xsl:attribute name="qd:t2"><xsl:value-of select="$qd.currentmediatime"/></xsl:attribute>
						<xsl:choose>
							<xsl:when test="@qd:t1">
								<xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1"/></xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="qd:t1"><xsl:value-of select="0"/></xsl:attribute>
							</xsl:otherwise>
						</xsl:choose>
                        <xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='maximizeStop'">
					<xsl:element name="ucuchi:C">
						<xsl:if test="@spid">
							<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
                        <xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1"/></xsl:attribute>
                        <xsl:attribute name="qd:t2"><xsl:value-of select="$qd.mediaduration"/></xsl:attribute>
                        <xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='decreaseStart'">
					<xsl:element name="ucuchi:C">
						<xsl:if test="@spid">
							<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t2">
                            <xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2"/></xsl:attribute>
                        </xsl:if>
                        <xsl:if test="@qd:t1">
                            <xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1 - $qd.slowincrease"/></xsl:attribute>
                        </xsl:if>
                        <xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='rapidDecreaseStart'">
					<xsl:element name="ucuchi:C">
						<xsl:if test="@spid">
							<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t2">
                            <xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2"/></xsl:attribute>
                        </xsl:if>
                        <xsl:if test="@qd:t1">
                            <xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1 - $qd.rapidincrease"/></xsl:attribute>
                        </xsl:if>
                        <xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='decreaseStop'">
					<xsl:element name="ucuchi:C">
						<xsl:if test="@spid">
							<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t1">
                            <xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1"/></xsl:attribute>
                        </xsl:if>
                        <xsl:if test="@qd:t2">
                            <xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2 - $qd.slowincrease"/></xsl:attribute>
                        </xsl:if>
                        <xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='rapidDecreaseStop'">
					<xsl:element name="ucuchi:C">
						<xsl:if test="@spid">
							<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t1">
                            <xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1"/></xsl:attribute>
                        </xsl:if>
                        <xsl:if test="@qd:t2">
                            <xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2 - $qd.rapidincrease"/></xsl:attribute>
                        </xsl:if>
                        <xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='increaseStart'">
					<xsl:element name="ucuchi:C">
						<xsl:if test="@spid">
							<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t2">
                            <xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2"/></xsl:attribute>
                        </xsl:if>
                        <xsl:if test="@qd:t1">
                            <xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1 + $qd.slowincrease"/></xsl:attribute>
                        </xsl:if>
                        <xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='rapidIncreaseStart'">
					<xsl:element name="ucuchi:C">
						<xsl:if test="@spid">
							<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t2">
                            <xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2"/></xsl:attribute>
                        </xsl:if>
                        <xsl:if test="@qd:t1">
                            <xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1 + $qd.rapidincrease"/></xsl:attribute>
                        </xsl:if>
                        <xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='increaseStop'">
					<xsl:element name="ucuchi:C">
						<xsl:if test="@spid">
							<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t1">
                            <xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1"/></xsl:attribute>
                        </xsl:if>
                        <xsl:if test="@qd:t2">
                            <xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2 + $qd.slowincrease"/></xsl:attribute>
                        </xsl:if>
                        <xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='rapidIncreaseStop'">
					<xsl:element name="ucuchi:C">
						<xsl:if test="@spid">
							<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
                        <xsl:if test="@qd:t1">
                            <xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1"/></xsl:attribute>
                        </xsl:if>
                        <xsl:if test="@qd:t2">
                            <xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2 + $qd.rapidincrease"/></xsl:attribute>
                        </xsl:if>
                        <xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='qd.insertTimes'">
					<xsl:element name="ucuchi:C">
						<xsl:if test="@spid">
							<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
                        <xsl:attribute name="qd:t1"><xsl:value-of select="$qd.start"/></xsl:attribute>
                        <xsl:attribute name="qd:t2"><xsl:value-of select="$qd.end"/></xsl:attribute>
                        <xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='zapTimes'">
					<xsl:element name="ucuchi:C">
						<xsl:if test="@spid">
							<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
                        <xsl:apply-templates/>
					</xsl:element>
				</xsl:when>
				<!--
				<xsl:when test="$qd.task='parseSentence'">
					<xsl:element name="S">
						<xsl:if test="@who">
							<xsl:attribute name="who"><xsl:value-of select="@who" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
						<xsl:apply-templates select="*[not(name(.)='W')]" />
						<xsl:variable name="scan" select="scanner:new($dictURL)" />
						<xsl:value-of select="scanner:scanBody($scan,FORM)" />
						<xsl:apply-templates select="scanner:getAsXML($scan,false)" />
					</xsl:element>
				</xsl:when>

				In order to do 'joinNextWord' and 'joinPreviousWord', we must
				(a) transform the containing sentence; and (b) have a way to
				identify the pivot word. Without word ids, the latter is not
				possible until we can pass XML nodes as paramters, as well as
				String values.
				
				<xsl:when test="$qd.task='joinNextWord'">
					<W>
						
					</W>
				</xsl:when>
				-->
				<!-- All I really need to do here is replace SOUNDFILE; however, QD has a bug
				whereby it doesn't do transformations right on nodes that are not visually present. -->
				<!--
                <xsl:when test="$qd.task='fixMedia'">
					<SOUNDFILE href="{$qd.mediaurlstring}"/>
				</xsl:when>
                -->
				<xsl:when test="$qd.task='removeNode'"/>
				<xsl:otherwise>
					<xsl:call-template name="copyTag"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</CloneOwner>
</xsl:template>

<xsl:template name="copyTag">
    <xsl:variable name="tag" select="name()"/>
	<xsl:variable name="namespace" select="namespace-uri()"/>
    <xsl:element name="{$tag}" namespace="{$namespace}">
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
