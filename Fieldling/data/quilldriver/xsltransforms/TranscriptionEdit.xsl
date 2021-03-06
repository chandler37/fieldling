<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

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


<!-- <xsl:param name="dictURL" select="'http://iris.lib.virginia.edu/tibetan/servlet/org.thdl.tib.scanner.RemoteScannerFilter'"/> -->

<xsl:template name="make-speaker-id">
        <xsl:param name="speakers"/>
        <xsl:param name="count"/>
        <xsl:variable name="try" select="concat('s', $count)"/>
        <xsl:choose>
                <xsl:when test="not($speakers/SPEAKER[@personId=$try])">
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
                                <!-- title management tasks -->  
                                 <xsl:when test="$qd.task='insertEnglishTitle'">
					<HEADER>						
						<xsl:element name="TITLE">                                               
                                                        <xsl:text> </xsl:text>                                                                                                                                                	
						</xsl:element>
                                                <xsl:copy-of select="*" copy-namespaces="no"/>
					</HEADER>
				</xsl:when>
                                 <xsl:when test="$qd.task='insertTibetanTitle'">
					<HEADER>				
						<xsl:element name="TITLE_TIB">                                               
                                                        <xsl:text> </xsl:text>                                                                                                                                                	
						</xsl:element>
                                                <xsl:copy-of select="*" copy-namespaces="no"/>
					</HEADER>
				</xsl:when>
                                <xsl:when test="$qd.task='insertChineseTitle'">
					<HEADER>					
						<xsl:element name="TITLE_ZH">							                                              
                                                        <xsl:text> </xsl:text>                                                                                                                                                	
						</xsl:element>
                                                <xsl:copy-of select="*" copy-namespaces="no"/>
					</HEADER>
				</xsl:when>                         
                                <!-- speaker management tasks -->
				<xsl:when test="$qd.task='addSpeaker'">
                                        <xsl:variable name="speakers" select="ancestor-or-self::TEXT/HEADER"/>
					<xsl:variable name="n" select="count(SPEAKER)+1"/>
					<HEADER>
						<xsl:copy-of select="*" copy-namespaces="no"/>
						<xsl:element name="SPEAKER">
							<xsl:attribute name="personId">
                                                                <xsl:call-template name="make-speaker-id">
                                                                        <xsl:with-param name="speakers" select="$speakers"/>
                                                                        <xsl:with-param name="count" select="$n"/>
                                                                </xsl:call-template>
                                                        </xsl:attribute>
							<xsl:text> </xsl:text>
						</xsl:element>
					</HEADER>
				</xsl:when>
                                <xsl:when test="$qd.task='changeSpeaker'">
                                    <xsl:variable name="speakers" select="ancestor-or-self::TEXT/HEADER"/>
                                    <xsl:variable name="currentwho" select="@who"/>
                                    <xsl:variable name="speakercount" select="count($speakers/SPEAKER)"/>
                                    <xsl:variable name="currentwhonum" select="count($speakers/SPEAKER[@personId=$currentwho]/preceding-sibling::SPEAKER)"/>
                                    <xsl:choose>
                                        <xsl:when test="$currentwhonum+2 > $speakercount">
                                            <xsl:copy copy-namespaces="no">
                                                <xsl:attribute name="who">
                                                    <xsl:value-of select="$speakers/SPEAKER[position()=1]/@personId"/>
                                                </xsl:attribute>
                                                <xsl:copy-of select="child::*" copy-namespaces="no"/>
                                            </xsl:copy>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:copy copy-namespaces="no">
                                                <xsl:attribute name="who">
                                                    <xsl:value-of select="$speakers/SPEAKER[position()=$currentwhonum+2]/@personId"/>
                                                </xsl:attribute>
                                                <xsl:copy-of select="child::*" copy-namespaces="no"/>
                                            </xsl:copy>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:when>
                
                                <!-- id management: is this used? -->
				<xsl:when test="$qd.task='computeIDs'">
					<xsl:copy-of select="." copy-namespaces="no"/>
				</xsl:when>
                
                                <!-- insertions and deletions -->
				<xsl:when test="$qd.task='removeNode'"/> <!-- removes current node -->
				<xsl:when test="$qd.task='insertAfter'">
                                        <xsl:copy-of select="." copy-namespaces="no"/>
					<S who="{@who}">
						<FORM type="transliteration"><xsl:text> </xsl:text></FORM>
					</S>
				</xsl:when>
				<xsl:when test="$qd.task='insertBefore'">
					<S who="{@who}">
						<FORM type="transliteration"><xsl:text> </xsl:text></FORM>
					</S>
					<xsl:copy-of select="." copy-namespaces="no"/>
				</xsl:when>
				<xsl:when test="$qd.task='insertTranslation_EN'">
					<xsl:element name="S">
						<xsl:if test="@who">
							<xsl:attribute name="who"><xsl:value-of select="@who" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
						<xsl:copy-of select="*" copy-namespaces="no"/>
                    
                                                <xsl:if test="not(TRANSL)">
						    <TRANSL>
                                                 	    <xsl:text> </xsl:text>
                                                    </TRANSL>
						</xsl:if>
                                                
					</xsl:element>
				</xsl:when>
               
                               <xsl:when test="$qd.task='insertTranslation_ZH'">
					<xsl:element name="S">
						<xsl:if test="@who">
							<xsl:attribute name="who"><xsl:value-of select="@who" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
						<xsl:copy-of select="*" copy-namespaces="no"/>
						<xsl:if test="not(TRANSL_ZH)">
						    <TRANSL_ZH>
							    <xsl:text> </xsl:text>
                                                    </TRANSL_ZH>
						</xsl:if>
                                                
					</xsl:element>
				</xsl:when>
                              
                                <!-- time-coding tasks -->
				<xsl:when test="$qd.task='markStart'">
					<xsl:element name="S">
						<xsl:if test="@who">
							<xsl:attribute name="who"><xsl:value-of select="@who" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
						<xsl:copy-of select="*[not(local-name(.)='AUDIO')]" copy-namespaces="no"/>
						<xsl:choose>
							<xsl:when test="AUDIO">
								<AUDIO start="{$qd.currentmediatime}" end="{AUDIO/@end}" />
							</xsl:when>
							<xsl:otherwise>
								<AUDIO start="{$qd.currentmediatime}" end="{$qd.mediaduration}" />
							</xsl:otherwise>
						</xsl:choose>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='markStop'">
					<xsl:element name="S">
						<xsl:if test="@who">
							<xsl:attribute name="who"><xsl:value-of select="@who" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
						<xsl:copy-of select="*[not(local-name(.)='AUDIO')]" copy-namespaces="no"/>
						<xsl:choose>
							<xsl:when test="AUDIO">
								<AUDIO start="{AUDIO/@start}" end="{$qd.currentmediatime}" />
							</xsl:when>
							<xsl:otherwise>
								<AUDIO start="0" end="{$qd.currentmediatime}" />
							</xsl:otherwise>
						</xsl:choose>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='maximizeStop'">
					<xsl:element name="S">
						<xsl:if test="@who">
							<xsl:attribute name="who"><xsl:value-of select="@who" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
						<xsl:copy-of select="*[not(local-name(.)='AUDIO')]" copy-namespaces="no"/>
						<xsl:choose>
							<xsl:when test="AUDIO">
								<AUDIO start="{AUDIO/@start}" end="{$qd.mediaduration}" />
							</xsl:when>
						</xsl:choose>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='increaseStart'">
					<xsl:element name="S">
						<xsl:if test="@who">
							<xsl:attribute name="who"><xsl:value-of select="@who" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
						<xsl:copy-of select="*[not(local-name(.)='AUDIO')]" copy-namespaces="no"/>
						<xsl:if test="AUDIO">
							<AUDIO start="{AUDIO/@start + $qd.slowincrease}" end="{AUDIO/@end}" />
						</xsl:if>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='rapidIncreaseStart'">
					<xsl:element name="S">
						<xsl:if test="@who">
							<xsl:attribute name="who"><xsl:value-of select="@who" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
						<xsl:copy-of select="*[not(local-name(.)='AUDIO')]" copy-namespaces="no"/>
						<xsl:if test="AUDIO">
							<AUDIO start="{AUDIO/@start + $qd.rapidincrease}" end="{AUDIO/@end}" />
						</xsl:if>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='increaseStop'">
					<xsl:element name="S">
						<xsl:if test="@who">
							<xsl:attribute name="who"><xsl:value-of select="@who" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
						<xsl:copy-of select="*[not(local-name(.)='AUDIO')]" copy-namespaces="no"/>
						<xsl:if test="AUDIO">
							<AUDIO start="{AUDIO/@start}" end="{AUDIO/@end + $qd.slowincrease}" />
						</xsl:if>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='rapidIncreaseStop'">
					<xsl:element name="S">
						<xsl:if test="@who">
							<xsl:attribute name="who"><xsl:value-of select="@who" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
						<xsl:copy-of select="*[not(local-name(.)='AUDIO')]" copy-namespaces="no"/>
						<xsl:if test="AUDIO">
							<AUDIO start="{AUDIO/@start}" end="{AUDIO/@end + $qd.rapidincrease}" />
						</xsl:if>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='decreaseStart'">
					<xsl:element name="S">
						<xsl:if test="@who">
							<xsl:attribute name="who"><xsl:value-of select="@who" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
						<xsl:copy-of select="*[not(local-name(.)='AUDIO')]" copy-namespaces="no"/>
						<xsl:if test="AUDIO">
							<AUDIO start="{AUDIO/@start - $qd.slowincrease}" end="{AUDIO/@end}" />
						</xsl:if>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='rapidDecreaseStart'">
					<xsl:element name="S">
						<xsl:if test="@who">
							<xsl:attribute name="who"><xsl:value-of select="@who" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
						<xsl:copy-of select="*[not(local-name(.)='AUDIO')]" copy-namespaces="no"/>
						<xsl:if test="AUDIO">
							<AUDIO start="{AUDIO/@start - $qd.rapidincrease}" end="{AUDIO/@end}" />
						</xsl:if>
					</xsl:element>
				</xsl:when>				
				<xsl:when test="$qd.task='decreaseStop'">
					<xsl:element name="S">
						<xsl:if test="@who">
							<xsl:attribute name="who"><xsl:value-of select="@who" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
						<xsl:copy-of select="*[not(local-name(.)='AUDIO')]" copy-namespaces="no"/>
						<xsl:if test="AUDIO">
							<AUDIO start="{AUDIO/@start}" end="{AUDIO/@end - $qd.slowincrease}" />
						</xsl:if>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='rapidDecreaseStop'">
					<xsl:element name="S">
						<xsl:if test="@who">
							<xsl:attribute name="who"><xsl:value-of select="@who" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
						<xsl:copy-of select="*[not(local-name(.)='AUDIO')]" copy-namespaces="no"/>
						<xsl:if test="AUDIO">
							<AUDIO start="{AUDIO/@start}" end="{AUDIO/@end - $qd.rapidincrease}" />
						</xsl:if>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='qd.insertTimes'">
					<xsl:element name="S">
						<xsl:if test="@who">
							<xsl:attribute name="who"><xsl:value-of select="@who" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
						<xsl:copy-of select="*[not(local-name(.)='AUDIO')]" copy-namespaces="no"/>
						<AUDIO start="{$qd.start}" end="{$qd.end}" />
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='zapTimes'">
					<xsl:element name="S">
						<xsl:if test="@who">
							<xsl:attribute name="who"><xsl:value-of select="@who" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
						<xsl:copy-of select="*[not(local-name(.)='AUDIO')]" copy-namespaces="no"/>
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
                
                                <!-- annotation tasks -->
				<xsl:when test="$qd.task='addGeneral'">
					<xsl:element name="S">
						<xsl:if test="@who">
							<xsl:attribute name="who"><xsl:value-of select="@who" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
						<xsl:copy-of select="*" copy-namespaces="no"/>
						<NOTE type="general"><xsl:text> </xsl:text></NOTE>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='addLexical'">
					<xsl:element name="S">
						<xsl:if test="@who">
							<xsl:attribute name="who"><xsl:value-of select="@who" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
						<xsl:copy-of select="*" copy-namespaces="no"/>
						<NOTE type="lexical"><xsl:text> </xsl:text></NOTE>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='addGrammatical'">
					<xsl:element name="S">
						<xsl:if test="@who">
							<xsl:attribute name="who"><xsl:value-of select="@who" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
						<xsl:copy-of select="*" copy-namespaces="no"/>
						<NOTE type="grammatical"><xsl:text> </xsl:text></NOTE>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='addPronunciation'">
					<xsl:element name="S">
						<xsl:if test="@who">
							<xsl:attribute name="who"><xsl:value-of select="@who" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
						<xsl:copy-of select="*" copy-namespaces="no"/>
						<NOTE type="pronunciation"><xsl:text> </xsl:text></NOTE>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='addInterpretive'">
					<xsl:element name="S">
						<xsl:if test="@who">
							<xsl:attribute name="who"><xsl:value-of select="@who" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
						<xsl:copy-of select="*" copy-namespaces="no"/>
						<NOTE type="interpretive"><xsl:text> </xsl:text></NOTE>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='addSociolinguistic'">
					<xsl:element name="S">
						<xsl:if test="@who">
							<xsl:attribute name="who"><xsl:value-of select="@who" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
						<xsl:copy-of select="*" copy-namespaces="no"/>
						<NOTE type="sociolinguistic"><xsl:text> </xsl:text></NOTE>
					</xsl:element>
				</xsl:when>
				<xsl:when test="$qd.task='addCultural'">
					<xsl:element name="S">
						<xsl:if test="@who">
							<xsl:attribute name="who"><xsl:value-of select="@who" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@id">
							<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
						</xsl:if>
						<xsl:copy-of select="*" copy-namespaces="no"/>
						<NOTE type="cultural"><xsl:text> </xsl:text></NOTE>
					</xsl:element>
				</xsl:when>
                
                                <!-- media related tasks -->
				<!-- All I really need to do here is replace SOUNDFILE; however, QD has a bug
				whereby it doesn't do transformations right on nodes that are not visually present. -->
				<xsl:when test="$qd.task='fixMedia'">
					<SOUNDFILE href="{$qd.mediaurlstring}"/>
				</xsl:when>
                
                                <!-- default: just copy existing node -->
				<xsl:otherwise>
					<xsl:copy-of select="." copy-namespaces="no"/>
				</xsl:otherwise>
			</xsl:choose>
</xsl:template>

</xsl:stylesheet>
