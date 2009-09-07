function semantikrequest(rqst) {

sem_xmlhttp_obj=window.XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP");
 sem_xmlhttp_obj.onreadystatechange = semlok;
sem_xmlhttp_obj.open("GET",rqst+"&KWikiUser=TWikiGuest&KWikiWeb=Fitness");

sem_xmlhttp_obj.send(null);

}




 function semlok() {
 if ((sem_xmlhttp_obj.readyState == 4) && (sem_xmlhttp_obj.status == 200)) {
 SolutionState.update();
 }
 }

function readform(prefix,questid,counter) {
 var data=document.getElementsByName('f'+counter+'id'+questid);
 var values="";
 var kommas=0;
 for (var j=0; j<data.length;j++) {
 if (data[j].checked) {
 if (kommas>0) {values+=",";}
 kommas++;
 values+=data[j].value;
 }
 }
 semantikrequest(prefix+"&ValueIDS="+values);
}

function readformnum(prefix,questid,counter) {
 var data=document.getElementsByName(questid+counter);
 var values="";
 for (var j=0; j<data.length;j++) {
 values+=data[j].value;
 }
 semantikrequest(prefix+"&ValueNum="+values);
}

function readoc(prefix,questid,counter) {
 var data=document.getElementsByName('f'+counter+'id'+questid);
 var values="";
 var kommas=0;
 for (var j=0; j<data.length;j++) {
 if (data[j].checked) {
 if (kommas>0) {values+=",";}
 kommas++;
 values+=data[j].value;
 }
 }
 semantikrequest(prefix+"&ValueID="+values);
}