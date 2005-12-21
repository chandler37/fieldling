<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
	<xsl:param name="qd.task" select="''"/>
	<xsl:param name="qd.mediaurlstring" select="''"/>

	<xsl:template match="*">
		<xsl:choose>
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
