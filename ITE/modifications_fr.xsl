<?xml version="1.0" encoding="iso-8859-1"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:xalan="http://xml.apache.org/xalan"
	xmlns:java="http://xml.apache.org/xslt/java"
	exclude-result-prefixes="xalan java"
	version="1.0">

<xsl:output method="xml" indent="yes" encoding="utf-8"/>

<xsl:param name="what"       select="''"/>
<xsl:param name="doc"        select="java:org.w3c.Document.new"/>
<xsl:param name="level"      select="''"/>
<xsl:param name="annotation" select="''"/>

<!-- 
 ***                                                   ***
 *** Incorporer un elt dans un niveau                  ***
 ***                                                   *** 
-->

<xsl:template match="/">
	<xsl:choose>
		<xsl:when test="$what='createLevel'">
			<xsl:call-template name="createLevel"/>
		</xsl:when>
		<xsl:when test="$what='createAnnotation'">
			<xsl:call-template name="createAnnotation"/>
		</xsl:when>
		<xsl:when test="$what='embedLevel'">
			<xsl:call-template name="embedLevel"/>
		</xsl:when>
		<xsl:when test="$what='embedAnnotation'">
			<xsl:call-template name="embedAnnotation"/>
		</xsl:when>
	</xsl:choose>
</xsl:template>

<xsl:template name="createLevel">
	<xsl:choose>
		<xsl:when test="$level='doc'"><TEXT><S/></TEXT></xsl:when>
		<xsl:when test="$level='text'"><TEXT/></xsl:when>
		<xsl:when test="$level='sentence'"><S/></xsl:when>
		<xsl:when test="$level='word'"><W/></xsl:when>
		<xsl:when test="$level='morpheme'"><M/></xsl:when>
	</xsl:choose>
</xsl:template>

<xsl:template name="createAnnotation">
	<xsl:choose>
		<xsl:when test="$level='text'">
			<xsl:if test="$annotation='translation'"><TRANSL xml:lang="fr"/></xsl:if>
			<xsl:if test="$annotation='transcription'"><FORM/></xsl:if>
		</xsl:when>
		<xsl:when test="$level='sentence'">
			<xsl:if test="$annotation='translation'"><TRANSL xml:lang="fr"/></xsl:if>
			<xsl:if test="$annotation='transcription'"><FORM/></xsl:if>
		</xsl:when>
		<xsl:when test="$level='word'">
			<xsl:if test="$annotation='translation'"><TRANSL xml:lang="fr"/></xsl:if>
			<xsl:if test="$annotation='transcription'"><FORM/></xsl:if>
		</xsl:when>
		<xsl:when test="$level='morpheme'">
			<xsl:if test="$annotation='translation'"><TRANSL xml:lang="fr"/></xsl:if>
			<xsl:if test="$annotation='transcription'"><FORM/></xsl:if>
		</xsl:when>
	</xsl:choose>
</xsl:template>

<xsl:template name="embedLevel">
	<xsl:choose>
		<xsl:when test="child::TEXT">
			<TEXT>
				<xsl:copy-of select="TEXT/@*"/>
				<xsl:copy-of select="TEXT/*"/>
				<xsl:copy-of select="$doc"/>
			</TEXT>
		</xsl:when>
		<xsl:when test="child::S">
			<S>
				<xsl:copy-of select="S/@*"/>
				<xsl:copy-of select="S/*"/>
				<xsl:copy-of select="$doc"/>
			</S>
		</xsl:when>
		<xsl:when test="child::W">
			<W>
				<xsl:copy-of select="W/@*"/>
				<xsl:copy-of select="W/*"/>
				<xsl:copy-of select="$doc"/>
			</W>
		</xsl:when>
		<xsl:when test="child::M">
			<M>
				<xsl:copy-of select="M/@*"/>
				<xsl:copy-of select="M/*"/>
				<xsl:copy-of select="$doc"/>
			</M>
		</xsl:when>
	</xsl:choose>
</xsl:template>

<xsl:template name="embedAnnotation">
	<xsl:choose>
		<xsl:when test="child::TEXT">
			<TEXT>
				<xsl:copy-of select="TEXT/@*"/>
				<xsl:copy-of select="TEXT/*"/>
				<xsl:copy-of select="$doc"/>
			</TEXT>
		</xsl:when>
		<xsl:when test="child::S">
			<S>
				<xsl:copy-of select="S/@*"/>
				<xsl:copy-of select="S/*"/>
				<xsl:copy-of select="$doc"/>
			</S>
		</xsl:when>
		<xsl:when test="child::W">
			<W>
				<xsl:copy-of select="W/@*"/>
				<xsl:copy-of select="W/*"/>
				<xsl:copy-of select="$doc"/>
			</W>
		</xsl:when>
		<xsl:when test="child::M">
			<M>
				<xsl:copy-of select="M/@*"/>
				<xsl:copy-of select="M/*"/>
				<xsl:copy-of select="$doc"/>
			</M>
		</xsl:when>
	</xsl:choose>
</xsl:template>

</xsl:stylesheet>