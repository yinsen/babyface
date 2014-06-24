<%--
 * Created on June 23, 2005
 * author Brent Hamby (brenthamby@gmail.com)
 * Copyright (C) 2005 Telcontar Inc.
--%>
<%@ page import="org.apache.struts.action.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.text.*"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ page language="java" contentType="text/html;charset=UTF-8"%>
<html>
<head>
<link rel="stylesheet" type="text/css" href="css/telcontar.css">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>

<title>满天星定位系统</title>
<BODY>

	<IMG SRC=img/telcontar_home.gif>
	<table WIDTH="600" CELLPADDING="2">
		<tr>
			<td colspan=3 align="right" valign="bottom" bgcolor="808080">
				<a href="register.jsp" target="_self" style="font-family: Verdana, Arial;font-weight:bold;FONT-VARIANT: SMALL-CAPS;color:808080; HEIGHT: 38px;font-size:12pt;"> 注 册 </a>
			</td>
		</tr>
	</table>
	
	<table BORDER="1" CELLPADDING="5" CELLSPACING="10" WIDTH="600">
		<html:form action="/login">
			<tr>
				<td colspan=3>
					<H1>满天星定位系统</H1>
				</td>
				<td colspan=3>
					<%if (request.getParameter("failure")!=null && (request.getParameter("failure")).equals("true") ) {%> 
						There was a problem authenticating this account... 
					<%}%>
				</td>
			</tr>

			<tr>
				<td WIDTH=70 align="right" valign="bottom" style="font-family: Verdana, Arial;font-weight:bold;FONT-VARIANT: SMALL-CAPS;color:808080; HEIGHT: 38px;font-size:9pt;" >
      				用户名：
      			</td>
				<td align="left" valign="middle" WIDTH=200>
					<input name="account" type="text" size="25" style="font-family: Verdana, Arial;font-weight:bold;FONT-VARIANT: SMALL-CAPS;color:808080; HEIGHT: 38px;font-size:9pt;"/>
				</td>
				<td class=ERROR WIDTH=290 align="left" valign="bottom"><html:errors property='account_err' /></td>
			</tr>
			<tr>
				<td WIDTH=70 align="right" valign="bottom" style="font-family: Verdana, Arial;font-weight:bold;FONT-VARIANT: SMALL-CAPS;color:808080; HEIGHT: 38px;font-size:9pt;" >
      				密码：
      			</td>
				<td align="left" valign="middle" WIDTH=200>
					<input name="password" type="password" size="25" style="font-family: Verdana, Arial;font-weight:bold;FONT-VARIANT: SMALL-CAPS;color:808080; HEIGHT: 38px;font-size:9pt;"/>
				</td>
				<td class=ERROR WIDTH=290 align="left" valign="bottom"><html:errors property='password_err' /></td>
			</tr>
			
			<tr>
				<td>
				</td>
				<td WIDTH=120 cellpadding="20">
					<html:submit property="submit" styleClass="BUTTON" >登 录</html:submit>
				</td>

			</tr>
		</html:form>
	</table>

	
	
	<%--  for Internationalization
        <DIV ALIGN=LEFT>
        <bean:message key="label.copyright" />
        <a href=locale.do?language=en&country=US><bean:message key="label.en" /></A> | 
        <a href=locale.do?language=zh&country=ZH><bean:message key="label.zh" /></A>
        </DIV>
        --%>

</body>
</html>
