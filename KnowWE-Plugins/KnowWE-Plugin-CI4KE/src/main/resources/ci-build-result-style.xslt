<?xml version="1.0" encoding="UTF-8"?><!--
  ~ Copyright (C) 2016 denkbares GmbH. All rights reserved.
  -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
	xmlns:redirect="http://xml.apache.org/xalan/redirect" extension-element-prefixes="redirect"
	xmlns:xalan="http://xml.apache.org/xslt">
	<xsl:output method="xml" indent="yes" xalan:indent-amount="2" />
	<xsl:template match="/">
		<html>
			<head>
				<style>
					body {
					font: 80%/140% Helvetica Neue, Verdana, Arial, Helvetica, sans-serif;
					padding: 0 10px;
					}
					.depth1 {
					padding-left: 20px;
					}
					.depth2 {
					padding-left: 35px;
					}
					.indentMarker {
					position: absolute;
					margin-left: -15px;
					}
					h3 + br {
					display: none;
					}
					.success {
					color: green;
					}
					.error {
					color: grey;
					}
					.failure {
					color: red;
					}

				</style>
			</head>
			<body>
				<h2>Build Result</h2>
				<span>Build-Date:
					<xsl:value-of select="build/@date" />
				</span>

				<xsl:for-each select="build/test">
					<h3>
						<xsl:value-of select="@configuration" />
						(<xsl:value-of select="@name" />):
						<xsl:for-each select="message">
							<xsl:if test="@summary = 'true'">
								<xsl:if test="@type = 'SUCCESS'">
									<span class="success">SUCCESS</span>
								</xsl:if>
								<xsl:if test="@type = 'ERROR'">
									<span class="error">ERROR</span>
								</xsl:if>
								<xsl:if test="@type = 'FAILURE'">
									<span class="failure">FAILURE</span>
								</xsl:if>
							</xsl:if>
						</xsl:for-each>
					</h3>
					<xsl:if test="message/@type != 'SUCCESS'">
						<xsl:for-each select="message">
							<xsl:for-each select="tokenize(@text,'&#10;')">
								<xsl:choose>
									<xsl:when test="matches(., '^## |^\*\* ')">
										<xsl:choose>
											<xsl:when test="matches(., '^## ')">
												<div class="depth2">
													<span class="indentMarker">&#8226;</span>
													<xsl:value-of select="substring(., 3)" />
												</div>
											</xsl:when>
											<xsl:otherwise>
												<div class="depth2">
													<span class="indentMarker">&#8226;</span>
													<xsl:value-of select="substring(., 3)" />
												</div>
											</xsl:otherwise>
										</xsl:choose>
									</xsl:when>
									<xsl:otherwise>
										<xsl:choose>
											<xsl:when test="matches(., '^# |^\* ')">
												<xsl:choose>
													<xsl:when test="matches(., '^# ')">
														<div class="depth1">
															<span class="indentMarker">&#8226;</span>
															<xsl:value-of select="substring(., 2)" />
														</div>
													</xsl:when>
													<xsl:otherwise>
														<div class="depth1">
															<span class="indentMarker">&#8226;</span>
															<xsl:value-of select="substring(., 2)" />
														</div>
													</xsl:otherwise>
												</xsl:choose>
											</xsl:when>
											<xsl:otherwise>
												<br />
												<xsl:value-of select="." />
											</xsl:otherwise>
										</xsl:choose>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:for-each>
						</xsl:for-each>
					</xsl:if>
				</xsl:for-each>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>

