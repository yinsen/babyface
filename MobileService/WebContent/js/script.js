
/*
you can modify this script to allow the persistence 
of the display state to be relative to the page.
Right now the persistence will hold between submits
to different forms.  Just uncomment the areas marked
// RELATIVE PATH
*/


//Enable saving state of content structure using session cookies? (on/off) 
var enablepersist="on" 
//Collapse previously open content when opening present? (yes/no) 
var collapseprevious="yes" 
//HTML for contract symbol. For image, use: <img src="whatever.gif">
var contractsymbol='<IMG SRC=images/minus.gif>'
//HTML for expand symbol. 
var expandsymbol='<IMG SRC=images/plus.gif>'


if (document.getElementById){
	document.write('<style type="text/css">')
	document.write('.switchcontent{display:none;}')
	document.write('</style>')
}

function getElementbyClass(rootobj, classname){
    var temparray=new Array()
	var inc=0
	var rootlength=rootobj.length
	for (i=0; i<rootlength; i++){
	    if (rootobj[i].className==classname)
	    temparray[inc++]=rootobj[i]
	}
    return temparray
}

function sweeptoggle(ec){
    var thestate=(ec=="expand")? "block" : "none"
	var inc=0
	while (ccollect[inc]){
	    ccollect[inc].style.display=thestate
	    inc++
	}
    revivestatus()
}


function contractcontent(omit){
    var inc=0
	while (ccollect[inc]){
	    if (ccollect[inc].id!=omit)
	    ccollect[inc].style.display="none"
	    inc++
	}
}

function expandcontent(curobj, cid){
    var spantags=curobj.getElementsByTagName("SPAN")
	var showstateobj=getElementbyClass(spantags, "showstate")
	if (ccollect.length>0){
	    if (collapseprevious=="yes")
	      contractcontent(cid)
	    document.getElementById(cid).style.display=(document.getElementById(cid).style.display!="block")? "block" : "none"
	    if (showstateobj.length>0){ //if "showstate" span exists in header
		 if (collapseprevious=="no")
		  showstateobj[0].innerHTML=(document.getElementById(cid).style.display=="block")? contractsymbol : expandsymbol
		 else
		  revivestatus()
	    }
	}
}

function revivecontent(){
    contractcontent("omitnothing")
	selectedItem=getselectedItem()
	selectedComponents=selectedItem.split("|")
	for (i=0; i<selectedComponents.length-1; i++)
	    document.getElementById(selectedComponents[i]).style.display="block"
}

function revivestatus(){
    var inc=0
	while (statecollect[inc]){
	    if (ccollect[inc].style.display=="block")
	      statecollect[inc].innerHTML=contractsymbol
	    else
	      statecollect[inc].innerHTML=expandsymbol
	    inc++
	}
}

function get_cookie(Name) {
    var search = Name + "="
	var returnvalue = "";
    if (document.cookie.length > 0) {
	offset = document.cookie.indexOf(search)
	    if (offset != -1) {
		offset += search.length
		end = document.cookie.indexOf(";", offset);
		if (end == -1) end = document.cookie.length;
		returnvalue=unescape(document.cookie.substring(offset, end))
	    }
    }
    return returnvalue;
}

function getselectedItem(){
      // RELATIVE PATH
      //if (get_cookie(window.location.pathname) != ""){
      if (get_cookie("SELEXTED") != ""){
        // RELATIVE PATH
  		//selectedItem=get_cookie(window.location.pathname)
    	selectedItem=get_cookie("SELEXTED")
        return selectedItem
    }
    else
	return ""
}

function saveswitchstate(){
    var inc=0, selectedItem=""
	while (ccollect[inc]){
	    if (ccollect[inc].style.display=="block")
	    selectedItem+=ccollect[inc].id+"|"
	    inc++
	}
    // RELATIVE PATH
    // document.cookie=window.location.pathname+"="+selectedItem
	document.cookie="SELEXTED="+selectedItem
}

function do_onload(){
    // RELATIVE PATH
	//uniqueidn=window.location.pathname+"firsttimeload"
	uniqueidn="SELEXTEDfirsttimeload"
	var alltags=document.all? document.all : document.getElementsByTagName("*")
	ccollect=getElementbyClass(alltags, "switchcontent")
	statecollect=getElementbyClass(alltags, "showstate")
	if (enablepersist=="on" && ccollect.length>0){
	    document.cookie=(get_cookie(uniqueidn)=="")? uniqueidn+"=1" : uniqueidn+"=0"
	    firsttimeload=(get_cookie(uniqueidn)==1)? 1 : 0 //check if this is 1st page load
	    if (!firsttimeload)
	    revivecontent()
	}
    if (ccollect.length>0 && statecollect.length>0)
	revivestatus()
}

if (window.addEventListener)
    window.addEventListener("load", do_onload, false)
    else if (window.attachEvent)
	window.attachEvent("onload", do_onload)
	else if (document.getElementById)
	    window.onload=do_onload

if (enablepersist=="on" && document.getElementById)
    window.onunload=saveswitchstate

function podParse(str){


	//alert(str);
    var days    = str.substring(str.indexOf('P')+1,str.indexOf('D'));
    var hours   = str.substring(str.indexOf('T')+1,str.indexOf('H'));
    var minutes = str.substring(str.indexOf('H')+1,str.indexOf('M'));
    var seconds = str.substring(str.indexOf('M')+1,str.indexOf('S'));
    if (days > 0) {
		return ((days*24)+hours) + ' hours ' + minutes + ' minutes ';
    } else if (hours > 0) {
		return hours + ' hours ' + minutes + ' minutes ';
    } else if (minutes > 0) { 
		return  minutes + ' minutes ' + seconds + ' seconds ';
    } else {
		return  seconds + ' seconds';
    }
}
