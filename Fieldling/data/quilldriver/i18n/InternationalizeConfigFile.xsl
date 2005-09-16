<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
        
        <xsl:template match="/">
                <xsl:text disable-output-escaping="yes"><![CDATA[
<!DOCTYPE qd-configuration [
<!ELEMENT qd-configuration (all-messages, parameters, menus?, actions, rendering-instructions?)>
<!ELEMENT all-messages (message*)>
<!ELEMENT message (text*)>
<!ATTLIST message id ID #REQUIRED>
<!ELEMENT text (#PCDATA)>
<!ATTLIST text lang CDATA #REQUIRED>
<!ELEMENT parameters (namespaces?, xmlschema?, newtemplate?, xsltransform?, parameter+)>
<!ELEMENT namespaces EMPTY>
<!ATTLIST namespaces val CDATA #REQUIRED>
<!ELEMENT xmlschema EMPTY>
<!ATTLIST xmlschema val CDATA #REQUIRED>
<!ELEMENT newtemplate EMPTY>
<!ATTLIST newtemplate val CDATA #REQUIRED>
<!ELEMENT parameter EMPTY>
<!ATTLIST parameter name CDATA #REQUIRED
                    val CDATA #REQUIRED
		    type CDATA #IMPLIED
		    >
<!ELEMENT menus (menu+)>
<!ELEMENT menu EMPTY>
<!ATTLIST menu name IDREF #REQUIRED
                        contains IDREFS #REQUIRED>
<!ELEMENT actions (action+)>
<!ELEMENT action (helptext?)>
<!ATTLIST action name IDREF #REQUIRED
                 keystroke CDATA #REQUIRED
		 node CDATA #IMPLIED
                 move CDATA "true"
		 qd-command CDATA #IMPLIED
		 xsl-task CDATA #IMPLIED>
<!ELEMENT helptext EMPTY>
<!ATTLIST helptext ref IDREF #REQUIRED>
<!ELEMENT rendering-instructions (tag | tagview )*>
<!ELEMENT tagview (tag+)>
<!ATTLIST tagview name CDATA #REQUIRED
                  keystroke CDATA #REQUIRED
		  >
<!ELEMENT tag (attribute*, desc?)>
<!ATTLIST tag name CDATA #REQUIRED
              visible CDATA "true"
	      visiblecontents CDATA "true"
	      displayas CDATA #IMPLIED
	      editable CDATA "true"
	      icon CDATA #IMPLIED
	      tibetan CDATA "false"
              tibetancontents CDATA "false"
	      >
<!ELEMENT attribute EMPTY>
<!ATTLIST attribute name CDATA #REQUIRED
                    visible CDATA "true"
		    icon CDATA #IMPLIED
                    tibetan CDATA "false"
		    >
<!ELEMENT desc (#PCDATA)>
]>
]]></xsl:text>
                <xsl:apply-templates select="qd-configuration"/>
        </xsl:template>
        <xsl:template match="qd-configuration">
                <qd-configuration>
                        <xsl:copy-of select="document('i18n.xml')/all-messages"/>
                        <xsl:copy-of select="*"/>
                </qd-configuration>
        </xsl:template>
</xsl:stylesheet>
