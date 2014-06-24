<%--
 * Created on June 23, 2005
 * author Brent Hamby (brenthamby@gmail.com)
 * Copyright (C) 2005 Telcontar Inc.
--%>  
<%@ page import="org.apache.struts.action.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.*" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ page language="java" contentType="text/html;charset=UTF-8"%> 

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <script language="JavaScript" src="js/deCarta.js"></script>
	<script language="JavaScript" src="js/CONFIG.js"></script>
	<script language="JavaScript" src="js/script.js"></script>
	<script language="JavaScript" src="js/Calendar3.js"></script>
	<script src="modal-form_files/jquery-1.6.2.js"></script>
	<script src="modal-form_files/external/jquery.bgiframe-2.1.2.js"></script>
	<script src="modal-form_files/ui/jquery.ui.core.js"></script>
	<script src="modal-form_files/ui/jquery.ui.widget.js"></script>
	<script src="modal-form_files/ui/jquery.ui.mouse.js"></script>
	<script src="modal-form_files/ui/jquery.ui.button.js"></script>
	<script src="modal-form_files/ui/jquery.ui.draggable.js"></script>
	<script src="modal-form_files/ui/jquery.ui.position.js"></script>
	<script src="modal-form_files/ui/jquery.ui.resizable.js"></script>
	<script src="modal-form_files/ui/jquery.ui.dialog.js"></script>
	<script src="modal-form_files/ui/jquery.effects.core.js"></script>
	<link rel="stylesheet" href="css/deCarta.css" type="text/css" />
	<link rel="stylesheet" href="css/telcontar.css" type="text/css">
	<link rel="stylesheet" href="modal-form_files/themes/base/jquery.ui.all.css">
	<link rel="stylesheet" href="modal-form_files/demos.css">
    
    <script type="text/javascript">
	   	$(document).ready(function(){
	   		
	   	  $("#regis").click(function(){
	   	      var result = CheckForm();
	   		  if (result) {
		   		  var account = $("#account").val();
		   		  var password = $("#password").val();
		   		  var email = $("#email").val();
		   		  
		   		  $.post("register.do",
		   			  {
		   			    "account":account,
		   			    "password":password,
		   			 	"email":email
		   			  },
		   			  function(data){
		   				var dataObj=eval("("+data+")");
		   				if (dataObj.register == "success") {
		   					alert("注册成功！");
		   				}
		   				else if (dataObj.register == "exist") {
		   					alert("此用户名已存在，请重新注册！");
		   				}
		   				else {
		   					alert("注册失败，请重新注册！");
		   				}
		   				
		   				top.location='map.jsp';
		   			  });
	   		  	}
		   	});
	   	});
        
		function CheckForm()
		{
		    if ( document.getElementById("account").value.length < 6
		       ||document.getElementById("account").value.length > 20) {
		      alert("请输入6～20个字符作为帐号");
		      document.getElementById("account").focus(); 
		      return false;
		   	}
			if ( document.getElementById("password").value.length < 6
			   ||document.getElementById("password").value.length > 20) {
		      alert("请输入6～20个字符作为密码");     
		      document.getElementById("password").focus(); 
		      return false;
		   	}
			if ( document.getElementById("pswconfirm").value != document.getElementById("password").value) {
		      alert("两次输入的密码不一致");     
		      document.getElementById("pswconfirm").focus(); 
		      return false;
		   	}
			
			var email = document.getElementById("email").value;
			var isemail = new RegExp(/^\w+((-\w+)|(\.\w+))*\@[A-Za-z0-9]+((\.|-)[A-Za-z0-9]+)*\.[A-Za-z0-9]+$/).test(email);
			if ( document.getElementById("email").value.length == 0 || !isemail) {
		      alert("请正确输入邮箱");     
		      document.getElementById("email").focus(); 
		      return false;
		   	}
			
		   return true;
		}
	</script>
  </head>
  
  <title>满天星定位系统</title>
  
  <BODY>
    <IMG SRC=img/telcontar_home.gif>
		
	    <TABLE BORDER="1" CELLPADDING="10" CELLSPACING="10" WIDTH="800">
	        <tr>
				<td WIDTH=60 align="right" valign="bottom" style="font-family: Verdana, Arial;font-weight:bold;FONT-VARIANT: SMALL-CAPS;color:808080; HEIGHT: 38px;font-size:9pt;" >
     				*用户名：
     			</td>
				<td align="left" valign="middle" WIDTH=200>
					<input name="account" id="account" type="text" size="35" />
				</td>
				<td class=INTRO WIDTH=280 align="left" valign="bottom">请输入6～20个字符</td>
			</tr>
			<tr>
				<td WIDTH=60 align="right" valign="bottom" style="font-family: Verdana, Arial;font-weight:bold;FONT-VARIANT: SMALL-CAPS;color:808080; HEIGHT: 38px;font-size:9pt;" >
	     				*密码：
	     			</td>
				<td align="left" valign="middle" WIDTH=200>
					<input name="password" id="password" type="password" size="35" />
				</td>
				<td class=INTRO WIDTH=280 align="left" valign="bottom">请输入6～20个字符</td>
			</tr>
	        <tr>
				<td WIDTH=60 align="right" valign="bottom" style="font-family: Verdana, Arial;font-weight:bold;FONT-VARIANT: SMALL-CAPS;color:808080; HEIGHT: 38px;font-size:9pt;" >
	     				*再次输入密码：
	     			</td>
				<td align="left" valign="middle" WIDTH=200>
					<input name="pswconfirm" id="pswconfirm" type="password" size="35" />
				</td>
				<td class=INTRO WIDTH=280 align="left" valign="bottom"></td>
			</tr>
	        <tr>
				<td WIDTH=60 align="right" valign="bottom" style="font-family: Verdana, Arial;font-weight:bold;FONT-VARIANT: SMALL-CAPS;color:808080; HEIGHT: 38px;font-size:9pt;" >
	     				*email地址：
	     			</td>
				<td align="left" valign="middle" WIDTH=200>
					<input name="email" id="email" type="text" size="35" />
				</td>
				<td class=INTRO WIDTH=280 align="left" valign="bottom"></td>
			</tr>
	        <tr>
	        	<td>
				</td>
				<td WIDTH=120 >
					<input type="button" id="regis" value="立即注册"/>
				</td>
	        </tr>
	        
	     </table>
    
    </body>
</html>
