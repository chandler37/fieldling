<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:ucuchi="http://altiplano.emich.edu/ucuchi"
    xmlns:qd="http://altiplano.emich.edu/quilldriver"
    exclude-result-prefixes="qd ucuchi"
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

<xsl:param name="speakers" select="''"/>

<!-- <xsl:param name="dictURL" select="'http://iris.lib.virginia.edu/tibetan/servlet/org.thdl.tib.scanner.RemoteScannerFilter'"/> -->

<xsl:template match="*">
    <xsl:choose> 
            <!-- speaker management tasks -->
            <xsl:when test="$qd.task='changeSpeaker'">
                    <xsl:variable name="currentwho" select="@spid"/>
                    <xsl:variable name="speakercount" select="count($speakers/ucuchi:SPEAKER)"/>
                    <xsl:variable name="currentwhonum" select="count($speakers/ucuchi:SPEAKER[@id=$currentwho]/preceding-sibling::ucuchi:SPEAKER)"/>
                    <xsl:choose>
                        <xsl:when test="$currentwhonum+2 > $speakercount">
                            <xsl:copy>
                                <xsl:attribute name="spid">
                                    <xsl:value-of select="$speakers/ucuchi:SPEAKER[position()=1]/@id"/>
                                </xsl:attribute>
                                <xsl:attribute name="id">
                                    <xsl:value-of select="@id"/>
                                </xsl:attribute>
                                <xsl:copy-of select="@qd:*"/>
                                <xsl:copy-of select="child::*"/>
                            </xsl:copy>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:copy>
                                <xsl:attribute name="spid">
                                    <xsl:value-of select="$speakers/ucuchi:SPEAKER[position()=$currentwhonum+2]/@id"/>
                                </xsl:attribute>
                                <xsl:attribute name="id">
                                    <xsl:value-of select="@id"/>
                                </xsl:attribute>
                                <xsl:copy-of select="@qd:*"/>
                                <xsl:copy-of select="child::*"/>
                            </xsl:copy>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
				<xsl:when test="$qd.task='addSpeaker'">
					<xsl:variable name="n" select="count(ucuchi:SPEAKER)"/>
					<ucuchi:META>
						<xsl:copy-of select="*" />
						<xsl:element name="ucuchi:SPEAKER">
							<xsl:attribute name="id">
                                                                <xsl:text>s</xsl:text><xsl:value-of select="$n"/>
							</xsl:attribute>
							<xsl:text> </xsl:text>
						</xsl:element>
					</ucuchi:META>
				</xsl:when>
                
                <!-- id management: is this used?
				<xsl:when test="$qd.task='computeIDs'">
					<xsl:copy-of select="."/>
				</xsl:when>
                -->
		
                <!-- insertions and deletions -->
				<xsl:when test="$qd.task='removeNode'"/> <!-- delete current node -->
				<xsl:when test="$qd.task='newClause'">
                                        <xsl:copy-of select="."/>
					<ucuchi:C id="{generate-id()}" spid="{@spid}" qd:t1="{@qd:t2}" qd:t2="{$qd.mediaduration}">
						<ucuchi:S-F><xsl:text> </xsl:text></ucuchi:S-F>
					</ucuchi:C>
				</xsl:when>
                <xsl:when test="$qd.task='newSentence'">
                    <xsl:copy-of select="."/>
                    <xsl:variable name="lastclause" select="ucuchi:C[position()=last()]"/>
					<ucuchi:S>
                        <ucuchi:C id="{generate-id()}" spid="{$lastclause/@spid}" qd:t1="{$lastclause/@qd:t2}" qd:t2="{$qd.mediaduration}">
                            <ucuchi:S-F><xsl:text> </xsl:text></ucuchi:S-F>
                        </ucuchi:C>
                    </ucuchi:S>
                </xsl:when>
                
                <!-- time-coding tasks -->
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
                        <xsl:copy-of select="*"/>
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
                        <xsl:copy-of select="*"/>
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
                        <xsl:copy-of select="*"/>
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
                        <xsl:copy-of select="*"/>
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
                        <xsl:copy-of select="*"/>
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
                        <xsl:copy-of select="*"/>
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
                        <xsl:copy-of select="*"/>
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
                        <xsl:copy-of select="*"/>
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
                        <xsl:copy-of select="*"/>
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
                        <xsl:copy-of select="*"/>
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
                        <xsl:copy-of select="*"/>
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
                        <xsl:copy-of select="*"/>
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
                        <xsl:copy-of select="*"/>
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
                
				<!-- default: just copy existing node -->
				<xsl:otherwise>
					<xsl:copy-of select="."/>
				</xsl:otherwise>
     </xsl:choose>
</xsl:template>
	
</xsl:stylesheet>
