<html>
<head>


	<meta charset="ISO-8859-1">
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">	
	<script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
	<link rel="stylesheet" href="http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css">		
    <script src="http://code.jquery.com/ui/1.10.3/jquery-ui.js"></script>
   
 
	<link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.0.2/css/bootstrap.min.css">
	<link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.0.2/css/bootstrap-theme.min.css">           
	
	<script src="//netdna.bootstrapcdn.com/bootstrap/3.0.2/js/bootstrap.min.js"></script>
 	<title>MyFiles</title>
 	
           

<script>  
</script>
</head>

<style type="text/css">
  .ui-autocomplete-loading {
    background: white url('/assets/busy.gif') right center no-repeat;
  }
  .modal-content{
    height:470px;
  }
  .modal-body{
    height:300px;
  }
  .searchWindow{
	height:100%;
	width:100%;
	border:none;
  }
</style>

<script>
  $(document).ready(function(){init();})
  function init(){
    debugger;    
    window.searchedUsers = "";   
    window.userID = "";
    window.fileID = ""; 
   ('#ui-id-1').className += ' modal-content';  
   $('#shareBtn').click(function(){	    
	    onShare();
   });
  }
  function iframeRef( frameRef ) {
    return frameRef.contentWindow ? frameRef.contentWindow.document : frameRef.contentDocument
   }
  function onShare(){
    var searchFrame = iframeRef( document.getElementById('searchFrame') );
    window.searchedUsers = searchFrame.getElementById('users').value;
    window.shareFileURL = "/dropbox/v1/users/" + window.userID + "/files/" +  window.fileID +  "?sharedWith=" + window.searchedUsers;
    $.ajax({
	type : "PUT",
	url : window.shareFileURL,
	dataType : "json",
	contentType : "application/json",
	success:function(data){
	 //$( ".close" )[0].trigger( "click" );
	},
	error:function(xhr){
	var errorStr = 'HTTP Error: ' + xhr.status + ' ' +  xhr.statusText + ' Login Failed!'
             $("#alertSpan").text(errorStr);
	}
	});    
       
  }
$(function() {
    function split( val ) {
      return val.split( /,\s*/ );
    }
    function extractLast( term ) {
      return split( term ).pop();
    }
 
    $( "#users" )
      // don't navigate away from the field on tab when selecting an item
      .bind( "keydown", function( event ) {
        if ( event.keyCode === $.ui.keyCode.TAB &&
            $( this ).data( "ui-autocomplete" ).menu.active ) {
          event.preventDefault();
        }
      }).autocomplete({
        source: function( request, response ) {
          $.getJSON( "http://localhost:8080/dropbox/v1/users/1/myfiles/share1", {
              term: extractLast( request.term )
            }, 
          response );
        },
        search: function() {
          // custom minLength
          var term = extractLast( this.value );
          if ( term.length < 2 ) {
            return false;
          }
        },
        focus: function() {
          // prevent value inserted on focus
          return false;
        },
        select: function( event, ui ) {
          var terms = split( this.value );
          // remove the current input
          terms.pop();
          // add the selected item
          terms.push( ui.item.value );
          // add placeholder to get the comma-and-space at the end
          terms.push( "" );
          this.value = terms.join( ", " );
          return false;
        }
      });
  });
</script>
<body style="width:100%;height:100%"> 
<div>
 </div>
 <div class="container">
                <div class="jumbotron">
                    <!-- calls getBooks() from HomeResource -->
                    <table class="table table-hover" border="1">
                    <#list myfiles as file>
                    <tr> <td> <button id="home" type="button" name="home" value="${file.owner}" >Home</button>
                       <button id="logout" type="button" name="logout" value="${file.owner}" >Log Out</button> </td> </tr>
                        <tr>
                            <td>${file.name}</td>
                           <td>${file.fileID}</td> 
                           
                           <td><button id="V${file.fileID}" type="button" name="View" value="${file.owner}" >VIEW</button></td>
                            <td><button id="D${file.fileID}" type="button" name="Delete" value="${file.owner}" >DELETE</button></td>
                        <td><button id="S${file.fileID}" value="${file.owner}" class="btn btn-primary btn-lg" data-toggle="modal" data-target="#myModal" >Share File</button></td>
                        </tr>
                    </#list>
                    </table>
                </div>
            </div> <!-- end of container -->
             
	 </center>
	 
	 <!-- Button trigger modal -->


<!-- Modal -->
<div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">Share File</h4>
      </div>
      <div class="modal-body">      
		  <iframe id="searchFrame" class="searchWindow" src="/assets/search.html">
		  </iframe>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
        <button type="button" class="btn btn-primary" id="shareBtn" >Share</button>
      </div>
    </div><!-- /.modal-content -->
  </div><!-- /.modal-dialog -->
</div><!-- /.modal -->
         
         
         <script>   
           
       $(":button").click(function() {
        var str = this.id;
        
        if(str!='shareBtn'){
            debugger;
	        window.fileID = str.substring(1);
		    window.userID = this.value;
        }		
        debugger;
        if(this.name == 'View'){
        $.ajax({
                  type: "GET",
                  url: "/dropbox/v1/users/"+window.userID+"/mydoc/" + window.fileID ,
        
                  dataType: "json",
                  contentType: "application/json",
                 
                  success: function(data) {
                          //  alert('please refresh the page to see current status of books');
                          
                          }
                });
                }
                else if(this.name == "Delete")
		{
		
		$.ajax({
		  type: "DELETE",
		  url: "/dropbox/v1/users/"+ window.userID +"/files/" + window.fileID ,
	
		  dataType: "json",
		  contentType: "application/json",
		 
		  success: function(data) {
			  
			  }
		});}
		
        else  if(this.name == 'logout')
	{
	 window.location = "/dropbox/v1/users/login";
	}
	 else  if(this.name == 'home')
	{
	 window.location = "/dropbox/v1/users/" + window.userID + "/home";
	} 
	});
    </script>
           
</body>
</html>