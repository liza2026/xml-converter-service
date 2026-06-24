<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>
    <xsl:template match="*">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:for-each select="*[not(*)]">
                <xsl:attribute name="{local-name()}">
                    <xsl:value-of select="normalize-space(.)"/>
                </xsl:attribute>
            </xsl:for-each>
            <xsl:apply-templates select="*[*]"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>