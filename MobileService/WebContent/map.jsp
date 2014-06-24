<%--
 * Created on June 27, 2005
 * author Brent Hamby (brenthamby@gmail.com)
 * Copyright (C) 2005 Telcontar Inc.
--%>
<%@ page import="org.apache.struts.action.*"%>
<%@ page import="java.util.*"%>
<%@ page language="java" contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ page language="java"%>

<%
if (session.getAttribute("client")==null || session.getAttribute("client")==null )
    response.sendRedirect("index.jsp");
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>满天星定位系统</title>

<script src="js/deCarta.js"></script>
<script src="js/CONFIG.js"></script>
<script src="js/script.js"></script>
<script src="js/Calendar3.js"></script>
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
<link rel="stylesheet" href="css/telcontar.css" type="text/css" />
<link rel="stylesheet" href="modal-form_files/themes/base/jquery.ui.all.css" />
<link rel="stylesheet" href="modal-form_files/demos.css" />

<script type="text/javascript">
	
	var map = null;
	var vPins = null;

	function init(){
		
	  map = new Map(document.getElementById("map"));
	  JSRequest.setDynamicScriptTagMode();
	  map.setShapeRendering("client");
	  map.setURL("http://wsdds1.dz.cn.decartahws.com/openls/openls");
	  map.authenticate(CONFIG.clientName, CONFIG.clientPassword);
	  
	  // add zoom control (1 through 17)
	  map.addZoomController(new ZoomController(8));
	
	  // add scale bar
	  map.addScaleBar(new ScaleBar());
	
	  // center map on position and register callback
	  // to receive the asynchronous response
	  var pos = new Position("31.25667748 121.53147932");
	  map.centerOnPosition(pos);
	  
	  EventRegistry.addListener(map,"moveend", renderCallBack);
	  EventRegistry.addListener(map,"zoomend", renderCallBack);
	  
	}
	
	$(document).ready(function(){
	  $("#getpoints").click(function(){
		  
		  var deviceId = $("#deviceid").val();
		  var date = $("#control_date").val();
		  
		  $.post("queryBack.do",
			  {
			    "deviceid":deviceId,
			    "date":date
			  },
			  function(data){
				var num = 0;
				var dataObj=eval("("+data+")");
				var pointNum = dataObj.length;
				
				vPins = new Array(pointNum);
				
				//jQuery提供的each方法是对参数1对应的对象中所有的子元素逐一进行方法调用
				$.each(dataObj.points, function(i, item) {
					var currPos = new Position(item.latitude, item.longitude);
					var pinInfo = item.date;
					var icon = new Icon("img/blue-dot.gif", 12, 12, 24, 24);
					vPins[num] = new Pin(currPos, pinInfo, "mouseover", icon);
					num++;
			      });
				
			    if (num>0) {
					map.centerOnPosition(vPins[0].getPosition(), renderCallBack);
				}
			    else {
			    	alert("抱歉，此手机当天无定位信息！");
			    }
			  });
		});
	});

	function renderCallBack() {
	  try {
		map.removeAllPins();
		var bounding = map.getBoundingBoxViewable();
		var lastDrawPoint = null;
		var lastGeoPos = null;
		var currDate = null;
		
		//1. 当只有一个点时只执行else if (i == vPins.length -1)直接存储
		//2. 当有多个点时，
		//		1）第一个点只执行lastPoint = currDrawPoint;lastGeoPos = vPins[i];两个赋值语句，不进行存储
		//		2）之后所有重叠的点都被continue，只更新最新的Date值；
		//		3）找到第一个不重叠的点后，就执行if (lastGeoPos != null)将之前的所有点存储，然后以此不重叠点为新的起点，开始下一轮处理
		for (var i=0; i < vPins.length; i++) {
			
		  if (vPins[i] && bounding.contains(vPins[i].getPosition())) {
			  var currDrawPoint = map.positionToMapLayerPixel(vPins[i].getPosition());
			  if (lastDrawPoint != null) {
			     if (Math.abs(currDrawPoint.x-lastDrawPoint.x)<12 && Math.abs(currDrawPoint.y-lastDrawPoint.y)<12){
			    	 if (i == vPins.length - 1) {//此时所有的点都重合成一个点
			    		 var pin = new Pin(lastGeoPos.getPosition(), lastGeoPos.getMessage()+" ~ "+vPins[i].getMessage(), "mouseover", vPins[0].getIcon());
						 map.addPin(pin);
			    	 }
			    	 else {
			    		 currDate = vPins[i].getMessage();
				    	 continue;
			    	 }
			    	 
			     }
			  }

			  if (lastGeoPos != null) {//此时找到一个不重叠的点，就把之前重叠的点存成一个点，然后进入下一轮
				  if (lastGeoPos.getMessage() == currDate){//之前一轮中只有一个点，没有重叠
					  var pin = new Pin(lastGeoPos.getPosition(), lastGeoPos.getMessage(), "mouseover", vPins[i].getIcon());
					  map.addPin(pin);
				  }
				  else {
					  var pin = new Pin(lastGeoPos.getPosition(), lastGeoPos.getMessage()+" ~ "+currDate, "mouseover", vPins[i].getIcon());
					  map.addPin(pin);
				  }
				  
				  
				  if (i == vPins.length - 1){//下一轮只有最后一个点的情况，直接加入
					  var pin = new Pin(vPins[i].getPosition(), vPins[i].getMessage(), "mouseover", vPins[i].getIcon());
					  map.addPin(pin);
				  }
			  }
			  else if (i == vPins.length - 1){//总共只有一个点的情况
				  var pin = new Pin(vPins[i].getPosition(), vPins[i].getMessage(), "mouseover", vPins[i].getIcon());
				  map.addPin(pin);
			  }
			  
			  lastDrawPoint = currDrawPoint;
			  currDate = vPins[i].getMessage();
			  lastGeoPos = vPins[i];
		  }
		}
	  } 
	  catch (e) {
	    //alert("render pins error: \n" + e);
	  }
	}

	
	
	function updateMap()
	{
	 
	 
	}
	
	</script>

</head>

<body leftmargin=20 topmargin=20 onload="init();">
	<IMG SRC=img/telcontar_home.gif>

	<div id="select" style="position: absolute; top: 80px; left: 10px; width: 400px; height: 600px;">
		<table CELLPADDING="2" CELLSPACING="2" WIDTH="300">
			<tr>
				<td WIDTH=100 align="right" valign="middle"
					style="font-family: Verdana, Arial; font-weight: bold; FONT-VARIANT: SMALL-CAPS; color: 808080; HEIGHT: 18px; font-size: 10pt;">
					选择设备号：</td>

				<td valign="bottom">
					<select name="deviceid" id="deviceid" style="text-align: center; width: 160px">
						<% Vector<String> deviceIds = (Vector<String>)request.getAttribute("device_list");
						if (deviceIds != null && !deviceIds.isEmpty()) {
							for (int i=0;i<deviceIds.size();i++){
						%>
								<option value=<%=deviceIds.get(i) %>><%=deviceIds.get(i) %></option>
						<% 
							}
						}
						else {
						%>
						  <option value="您还没有绑定手机">您还没有绑定手机</option>
						<%
						}
						%>
					</select>
				</td>
			</tr>

			<tr>
				<td align="right" valign="middle"
					style="font-family: Verdana, Arial; font-weight: bold; FONT-VARIANT: SMALL-CAPS; color: 808080; HEIGHT: 18px; font-size: 10pt;">
					设定日期：</td>
				<td valign="bottom">
					<input name="control_date" type="text" id="control_date" size="20" maxlength="10"
					onclick="new Calendar().show(this);" readonly="readonly" /> 
					<script type="text/javascript"> 
					    var today = new Date();
					    var year = today.getFullYear();
					    var month = today.getMonth()+1;
					    var date = today.getDate();
					    control_date.value=year + "-" + (month>9 ? month:'0'+month) + "-" + (date>9 ? date:'0'+date);
					</script>
				</td>
			</tr>

			<tr>
				<td></td>
				<td>
					<input type="button" id="getpoints" value="查看定位轨迹" />
				</td>
			</tr>
		</table>
	</div>

	<div id="map" style="position: absolute; top: 80px; left: 310px; width: 1000px; height: 600px;"></div>
</body>
</html>

