<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:ucuchi="http://altiplano.emich.edu/ucuchi"
    xmlns:qd="http://altiplano.emich.edu/quilldriver"
    exclude-result-prefixes="qd ucuchi"
    version="2.0">
    
<xsl:output indent="yes"/>

<xsl:param name="qd.task" select="''"/>
<xsl:param name="qd.currentmediatime" select="''"/>
<xsl:param name="qd.start" select="''"/>
<xsl:param name="qd.end" select="''"/>
<xsl:param name="qd.mediaduration" select="''"/>
<xsl:param name="qd.slowincrease" select=".025"/>
<xsl:param name="qd.rapidincrease" select=".250"/>
<xsl:param name="qd.mediaurlstring" select="''"/>

<xsl:template name="make-speaker-id">
        <xsl:param name="speakers"/>
        <xsl:param name="count"/>
        <xsl:variable name="try" select="concat('s', $count)"/>
        <xsl:choose>
                <xsl:when test="not($speakers/ucuchi:SPEAKER[@id=$try])">
                        <xsl:value-of select="$try"/>
                </xsl:when>
                <xsl:otherwise>
                        <xsl:call-template name="make-speaker-id">
                                <xsl:with-param name="count" select="$count + 1"/>
                        </xsl:call-template>
                </xsl:otherwise>
       </xsl:choose>
</xsl:template>

<xsl:template match="*">
    <xsl:choose> 
                            <!-- speaker management tasks -->
                            <xsl:when test="$qd.task='changeSpeaker'">
                                    <xsl:variable name="speakers" select="ancestor-or-self::ucuchi:TEXT/ucuchi:META"/>
                                    <xsl:variable name="currentwho" select="@spid"/>
                                    <xsl:variable name="speakercount" select="count($speakers/ucuchi:SPEAKER)"/>
                                    <xsl:variable name="currentwhonum" select="count($speakers/ucuchi:SPEAKER[@id=$currentwho]/preceding-sibling::ucuchi:SPEAKER)"/>
                                    <xsl:choose>
                                        <xsl:when test="$currentwhonum+2 > $speakercount">
                                            <xsl:copy>
                                                <xsl:attribute name="spid">
                                                    <xsl:value-of select="$speakers/ucuchi:SPEAKER[position()=1]/@id"/>
                                                </xsl:attribute>
                                                <xsl:copy-of select="@qd:*" copy-namespaces="no"/>
                                                <xsl:copy-of select="child::*" copy-namespaces="no"/>
                                            </xsl:copy>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:copy>
                                                <xsl:attribute name="spid">
                                                    <xsl:value-of select="$speakers/ucuchi:SPEAKER[position()=$currentwhonum+2]/@id"/>
                                                </xsl:attribute>
                                                <xsl:copy-of select="@qd:*" copy-namespaces="no"/>
                                                <xsl:copy-of select="child::*" copy-namespaces="no"/>
                                            </xsl:copy>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:when>
				<xsl:when test="$qd.task='addSpeaker'">
                                        <xsl:variable name="speakers" select="ancestor-or-self::ucuchi:TEXT/ucuchi:META"/>
					<xsl:variable name="n" select="count(ucuchi:SPEAKER)+1"/>
					<ucuchi:META>
						<xsl:copy-of select="*" copy-namespaces="no"/>
						<xsl:element name="ucuchi:SPEAKER">
							<xsl:attribute name="id">
                                                                <xsl:call-template name="make-speaker-id">
                                                                        <xsl:with-param name="speakers" select="$speakers"/>
                                                                        <xsl:with-param name="count" select="$n"/>
                                                                </xsl:call-template>
							</xsl:attribute>
							<xsl:text> </xsl:text>
						</xsl:element>
					</ucuchi:META>
				</xsl:when>
		
                                <!-- insertions and deletions -->
				<xsl:when test="$qd.task='removeNode'"/> <!-- delete current node -->
				<xsl:when test="$qd.task='newClause'">
                                        <xsl:copy-of select="." copy-namespaces="no"/>
                                        <xsl:choose>
                                                <xsl:when test="@qd:t2">
                                                    <ucuchi:C spid="{@spid}" qd:t1="{@qd:t2}" qd:t2="{$qd.mediaduration}">
                                                            <ucuchi:F><xsl:text> </xsl:text></ucuchi:F>
                                                    </ucuchi:C>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <ucuchi:C spid="{@spid}">
                                                            <ucuchi:F><xsl:text> </xsl:text></ucuchi:F>
                                                    </ucuchi:C>
                                                </xsl:otherwise>
                                        </xsl:choose>
				</xsl:when>
                
                                <!-- time-coding tasks -->
				<xsl:when test="$qd.task='markStart'">
					<xsl:element name="ucuchi:C">
						<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
                                                <xsl:attribute name="qd:t1"><xsl:value-of select="$qd.currentmediatime"/></xsl:attribute>
						<xsl:choose>
							<xsl:when test="@qd:t2">
								<xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2"/></xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="qd:t2"><xsl:value-of select="$qd.mediaduration"/></xsl:attribute>
							</xsl:otherwise>
						</xsl:choose>
                                                <xsl:copy-of select="*" copy-namespaces="no"/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='markStop'">
					<xsl:element name="ucuchi:C">
						<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
                                                <xsl:attribute name="qd:t2"><xsl:value-of select="$qd.currentmediatime"/></xsl:attribute>
						<xsl:choose>
							<xsl:when test="@qd:t1">
								<xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1"/></xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="qd:t1"><xsl:value-of select="0"/></xsl:attribute>
							</xsl:otherwise>
						</xsl:choose>
                                                <xsl:copy-of select="*" copy-namespaces="no"/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='maximizeStop'">
					<xsl:element name="ucuchi:C">
						<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
                                                <xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1"/></xsl:attribute>
                                                <xsl:attribute name="qd:t2"><xsl:value-of select="$qd.mediaduration"/></xsl:attribute>
                                                <xsl:copy-of select="*" copy-namespaces="no"/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='decreaseStart'">
					<xsl:element name="ucuchi:C">
						<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
                                                <xsl:if test="@qd:t2">
                                                    <xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2"/></xsl:attribute>
                                                </xsl:if>
                                                <xsl:if test="@qd:t1">
                                                    <xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1 - $qd.slowincrease"/></xsl:attribute>
                                                </xsl:if>
                                                <xsl:copy-of select="*" copy-namespaces="no"/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='rapidDecreaseStart'">
					<xsl:element name="ucuchi:C">
						<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
                                                <xsl:if test="@qd:t2">
                                                    <xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2"/></xsl:attribute>
                                                </xsl:if>
                                                <xsl:if test="@qd:t1">
                                                    <xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1 - $qd.rapidincrease"/></xsl:attribute>
                                                </xsl:if>
                                                <xsl:copy-of select="*" copy-namespaces="no"/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='decreaseStop'">
					<xsl:element name="ucuchi:C">
						<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
                                                <xsl:if test="@qd:t1">
                                                    <xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1"/></xsl:attribute>
                                                </xsl:if>
                                                <xsl:if test="@qd:t2">
                                                    <xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2 - $qd.slowincrease"/></xsl:attribute>
                                                </xsl:if>
                                                <xsl:copy-of select="*" copy-namespaces="no"/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='rapidDecreaseStop'">
					<xsl:element name="ucuchi:C">
						<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
                                                <xsl:if test="@qd:t1">
                                                    <xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1"/></xsl:attribute>
                                                </xsl:if>
                                                <xsl:if test="@qd:t2">
                                                    <xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2 - $qd.rapidincrease"/></xsl:attribute>
                                                </xsl:if>
                                                <xsl:copy-of select="*" copy-namespaces="no"/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='increaseStart'">
					<xsl:element name="ucuchi:C">
						<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
                                                <xsl:if test="@qd:t2">
                                                    <xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2"/></xsl:attribute>
                                                </xsl:if>
                                                <xsl:if test="@qd:t1">
                                                    <xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1 + $qd.slowincrease"/></xsl:attribute>
                                                </xsl:if>
                                                <xsl:copy-of select="*" copy-namespaces="no"/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='rapidIncreaseStart'">
					<xsl:element name="ucuchi:C">
						<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
                                                <xsl:if test="@qd:t2">
                                                    <xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2"/></xsl:attribute>
                                                </xsl:if>
                                                <xsl:if test="@qd:t1">
                                                    <xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1 + $qd.rapidincrease"/></xsl:attribute>
                                                </xsl:if>
                                                <xsl:copy-of select="*" copy-namespaces="no"/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='increaseStop'">
					<xsl:element name="ucuchi:C">
						<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
                                                <xsl:if test="@qd:t1">
                                                    <xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1"/></xsl:attribute>
                                                </xsl:if>
                                                <xsl:if test="@qd:t2">
                                                    <xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2 + $qd.slowincrease"/></xsl:attribute>
                                                </xsl:if>
                                                <xsl:copy-of select="*" copy-namespaces="no"/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='rapidIncreaseStop'">
					<xsl:element name="ucuchi:C">
						<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
                                                <xsl:if test="@qd:t1">
                                                    <xsl:attribute name="qd:t1"><xsl:value-of select="@qd:t1"/></xsl:attribute>
                                                </xsl:if>
                                                <xsl:if test="@qd:t2">
                                                    <xsl:attribute name="qd:t2"><xsl:value-of select="@qd:t2 + $qd.rapidincrease"/></xsl:attribute>
                                                </xsl:if>
                                                <xsl:copy-of select="*" copy-namespaces="no"/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='qd.insertTimes'">
					<xsl:element name="ucuchi:C">
						<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
                                                <xsl:attribute name="qd:t1"><xsl:value-of select="$qd.start"/></xsl:attribute>
                                                <xsl:attribute name="qd:t2"><xsl:value-of select="$qd.end"/></xsl:attribute>
                                                <xsl:copy-of select="*" copy-namespaces="no"/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='zapTimes'">
					<xsl:element name="ucuchi:C">
						<xsl:attribute name="spid"><xsl:value-of select="@spid" /></xsl:attribute>
                                                <xsl:copy-of select="*" copy-namespaces="no"/>
					</xsl:element>
				</xsl:when>

				<!-- default: just copy existing node -->
				<xsl:otherwise>
					<xsl:copy-of select="." copy-namespaces="no"/>
				</xsl:otherwise>
     </xsl:choose>
</xsl:template>
	
</xsl:stylesheet>