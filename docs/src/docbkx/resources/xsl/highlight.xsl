<?xml version='1.0'?>
<!-- 
    Simple highlighter for HTML output. Follows the Eclipse color scheme.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"

                xmlns:d="http://docbook.org/ns/docbook"
                xmlns:s6hl="http://net.sf.xslthl/ConnectorSaxon6"
                xmlns:sbhl="http://net.sf.xslthl/ConnectorSaxonB"
                xmlns:xhl="http://net.sf.xslthl/ConnectorXalan"
                xmlns:saxon6="http://icl.com/saxon"
                xmlns:saxonb="http://saxon.sf.net/"
                xmlns:xalan="http://xml.apache.org/xalan"

                xmlns:exsl="http://exslt.org/common"
                xmlns:xslthl="http://xslthl.sf.net"
                exclude-result-prefixes="exsl xslthl s6hl sbhl xhl d"
                version='1.0'>
    <!-- this construction is needed to have the saxon and xalan connectors working alongside each other -->
    <xalan:component prefix="xhl" functions="highlight">
        <xalan:script lang="javaclass" src="xalan://net.sf.xslthl.ConnectorXalan"/>
    </xalan:component>

    <!-- for saxon 6 -->
    <saxon6:script implements-prefix="s6hl" language="java" src="java:net.sf.xslthl.ConnectorSaxon6"/>

    <!-- for saxon 8.5 and later -->
    <saxonb:script implements-prefix="sbhl" language="java" src="java:net.sf.xslthl.ConnectorSaxonB"/>


    <!-- You can override this template to do more complex mapping of
         language attribute to highlighter language ID (see xslthl-config.xml) -->
    <xsl:template name="language.to.xslthl">
        <xsl:param name="context"/>

        <xsl:choose>
            <xsl:when test="$context/@language != ''">
                <xsl:value-of select="$context/@language"/>
            </xsl:when>
            <xsl:when test="$highlight.default.language != ''">
                <xsl:value-of select="$highlight.default.language"/>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="apply-highlighting">
        <xsl:choose>
            <!-- Do we want syntax highlighting -->
            <xsl:when test="$highlight.source != 0">
                <xsl:variable name="language">
                    <xsl:call-template name="language.to.xslthl">
                        <xsl:with-param name="context" select="."/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="$language != ''">
                        <xsl:variable name="content">
                            <xsl:apply-templates/>
                        </xsl:variable>
                        <xsl:choose>
                            <xsl:when test="function-available('s6hl:highlight')">
                                <xsl:apply-templates
                                        select="s6hl:highlight($language, exsl:node-set($content), $highlight.xslthl.config)"
                                        mode="xslthl"/>
                            </xsl:when>
                            <xsl:when test="function-available('sbhl:highlight')">
                                <xsl:apply-templates
                                        select="sbhl:highlight($language, exsl:node-set($content), $highlight.xslthl.config)"
                                        mode="xslthl"/>
                            </xsl:when>
                            <xsl:when test="function-available('xhl:highlight')">
                                <xsl:apply-templates
                                        select="xhl:highlight($language, exsl:node-set($content), $highlight.xslthl.config)"
                                        mode="xslthl"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:copy-of select="$content"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <!-- No syntax highlighting -->
            <xsl:otherwise>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- A fallback when the specific style isn't recognized -->
    <xsl:template match="xslthl:*" mode="xslthl">
        <xsl:message>
            <xsl:text>unprocessed xslthl style: </xsl:text>
            <xsl:value-of select="local-name(.)"/>
        </xsl:message>
        <xsl:apply-templates mode="xslthl"/>
    </xsl:template>

    <!-- Copy over already produced markup (FO/HTML) -->
    <xsl:template match="node()" mode="xslthl" priority="-1">
        <xsl:copy>
            <xsl:apply-templates select="node()" mode="xslthl"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="*" mode="xslthl">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="node()" mode="xslthl"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="xslthl:keyword" mode="xslthl">
        <strong class="hl-keyword">
            <xsl:apply-templates mode="xslthl"/>
        </strong>
    </xsl:template>
    <xsl:template match="xslthl:string" mode="xslthl">
        <strong class="hl-string">
            <em>
                <xsl:apply-templates mode="xslthl"/>
            </em>
        </strong>
    </xsl:template>
    <xsl:template match="xslthl:comment" mode="xslthl">
        <em class="hl-comment">
            <xsl:apply-templates mode="xslthl"/>
        </em>
    </xsl:template>
    <xsl:template match="xslthl:directive" mode="xslthl">
        <span class="hl-directive">
            <xsl:apply-templates mode="xslthl"/>
        </span>
    </xsl:template>
    <xsl:template match="xslthl:tag" mode="xslthl">
        <strong class="hl-tag">
            <xsl:apply-templates mode="xslthl"/>
        </strong>
    </xsl:template>
    <xsl:template match="xslthl:attribute" mode="xslthl">
        <span class="hl-attribute">
            <xsl:apply-templates mode="xslthl"/>
        </span>
    </xsl:template>
    <xsl:template match="xslthl:value" mode="xslthl">
        <span class="hl-value">
            <xsl:apply-templates mode="xslthl"/>
        </span>
    </xsl:template>
    <xsl:template match='xslthl:html' mode="xslthl">
        <span class="hl-html">
            <xsl:apply-templates mode="xslthl"/>
        </span>
    </xsl:template>
    <xsl:template match="xslthl:xslt" mode="xslthl">
        <strong>
            <xsl:apply-templates mode="xslthl"/>
        </strong>
    </xsl:template>
    <!-- Not emitted since XSLTHL 2.0 -->
    <xsl:template match="xslthl:section" mode="xslthl">
        <strong>
            <xsl:apply-templates mode="xslthl"/>
        </strong>
    </xsl:template>
    <xsl:template match="xslthl:number" mode="xslthl">
        <span class="hl-number">
            <xsl:apply-templates mode="xslthl"/>
        </span>
    </xsl:template>
    <xsl:template match="xslthl:annotation" mode="xslthl">
        <em>
            <span class="hl-annotation">
                <xsl:apply-templates mode="xslthl"/>
            </span>
        </em>
    </xsl:template>
    <!-- Not sure which element will be in final XSLTHL 2.0 -->
    <xsl:template match="xslthl:doccomment|xslthl:doctype" mode="xslthl">
        <strong class="hl-tag">
            <xsl:apply-templates mode="xslthl"/>
        </strong>
    </xsl:template>
</xsl:stylesheet>