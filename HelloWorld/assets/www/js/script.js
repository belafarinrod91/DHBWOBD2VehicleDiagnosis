var bluetoothSerial = cordova.require('bluetoothSerial');


function init() {
	document.addEventListener("deviceready", function() {
	}, false)
}


function findBTDevices(){
	$.mobile.loading( 'show', {
		text: 'Discovering Devices...',
		textVisible: true,
		theme: 'a',
		html: ""
	});
	
	discoverBTDevices();
	
	setTimeout(function() {
		stopDiscoverBTDevices();
		$( "#devicesPopup" ).popup("open");
		$.mobile.loading('hide');
	}, 5000); 
}

function checkIfBluetoothIsEnabled(){
	var booleanIsEnabled;
	bluetoothSerial.isEnabled(
		    function() { 
		        booleanIsEnabled = true;
		    },
		    function() { 
		        booleanIsEnabled = false;
		    }
		);
	
	if(booleanIsEnabled == true){
		alert("Bluetooth was deactivated now.")
		bluetoothSerial.deactivateBluetooth(
				function(){},
				function(){});
	}
	
	if(booleanIsEnabled == false){
		alert("Bluetooth was activated now.")
		bluetoothSerial.activateBluetooth(
				function(){},
				function(){});
	}
	
	
}

function discoverBTDevices(){
	
	//TODO : If Bluetooth is deactivated, device should not search !
	var booleanIsEnabled;
	bluetoothSerial.isEnabled(
		    function() { 
		        booleanIsEnabled = true;
		    },
		    function() { 
		        booleanIsEnabled = false;
		    }
		);
	
	if(booleanIsEnabled == true){
	
	console.log("DISCOVERING STARTED");

	bluetoothSerial.discoverDevices(
	   function() {},
	    function() {}
	); }
	else {
		alert("Bluetooth is not enabled.");
		return;
	}
}

function stopDiscoverBTDevices(){
	var devices = "";
	console.log("DISCOVERING ENDED");
	bluetoothSerial.stopDiscovering(
	   function(r) { 
		   console.log(JSON.stringify(r));
		   var devices = r;
		   
		   
		   var listIsEmpty = jQuery.isEmptyObject(devices); 
		   if(listIsEmpty == false){
			   $("#titleDevicesPopup").text("Please select a device to connect to.");
		   }
		   else {
			   $("#titleDevicesPopup").text("No Device was found !");
		   }
		   
		   
		   for(d in devices) {
			   $("#devices").append("<li><a onclick=\"pair('"+devices[d].name+"', '"+devices[d].adress+"')\">"+devices[d].name+"</a></li>");
		   }
		},
	    function(e) { 
	        console.log("failure");
	    }
	);
	$("#devices").listview("refresh");
}

function pair(name, adress){
	$('#devicesPopup').popup('close');
	$("#btConnectionStatus").text("connecting to : "+name+" ...");
	
	var isConnected;
	var upComErr;
	bluetoothSerial.isConnected(
		    function() { 
		    	isConnected = true;
		    },
		    function() { 
		        isConnected = false;
		    }
		); 
	
	if(!isConnected){
		bluetoothSerial.connect(
                adress, 
                function(success) { 
                	console.log("connected !");
                	}, 
                function (error) { 
                	alert(error);	
                	upComErr = true;
                	$("#btConnectionStatus").text("Connection Error.");
                }
            );        
		
	}
	
	if(!upComErr){
		$("#btConnectionStatus").text("Is connected to : "+name+" !");
	}

	var i = 100;
	while(i> 0){
	bluetoothSerial.read(function (data) {
	    console.log(data);
	}, 
	function(error){
		console.log(error);
	});
	i = i-1;
	}
	
}


function disconnectDevice(){
	alert("Disconnecting");
}

function moin() {
	if($(".draggable").draggable("option","disabled") != false){
		$("#displayConf").html($("#displayConf").html().replace("Configure", "Done"));
		$(".draggable").draggable();
		$(".draggable").resizable({
		    stop: function(event, ui) {
		    	
		    	var canvasNode = this.getElementsByTagName("canvas").item(0);
			    canvasNode.width = $(this).width();
			    canvasNode.height = $(this).height();
		        canvasNode.__object__.Draw();
		        
		    }, aspectRatio: 1 / 1
		});
	}else{
		$("#displayConf").html($("#displayConf").html().replace("Done", "Configure"));
		$(".draggable").draggable("destroy");
		$(".draggable").resizable("destroy");
	    localStorage.setItem("display", $("#display").html());
	}
}

function drawGauge(canvasID){
	var canvasNode = document.getElementById(canvasID);
    canvasNode.width = $("#"+canvasNode.parentNode.id).width();
    canvasNode.height = $("#"+canvasNode.parentNode.id).width();
	
    switch(canvasID){
    case "speedCanvas":
		new RGraph.Gauge(canvasID, 0, 300, 120)
		.Set('title', 'Speed')
	    .Set('title.bottom', 'km/h')
	    .Set('title.bottom.color', '#aaa')
	    .Draw();
		break;
    case "RPMCanvas":
    	new RGraph.Gauge(canvasID, 0, 7, 0.8)
		.Set('title', 'RPM')
	    .Set('title.bottom', '*1000/s')
	    .Set('title.bottom.color', '#aaa')
	    .Draw();
      	break;
    default:
    	console.log("No gauge defined for "+canvasID+".")
    }
}

function updateGauges(){
	var gauges=document.getElementById("displayContent").getElementsByTagName("canvas");
	for (var i = 0; i < gauges.length; i++) {
		
		switch(gauges.item(i).id){
	    case "speedCanvas":
	    	gauges.item(i).__object__.value = Math.floor(Math.random()*300);
	    	RGraph.Effects.Gauge.Grow(gauges.item(i).__object__);
			break;
	    case "RPMCanvas":
	    	gauges.item(i).__object__.value = Math.floor(Math.random()*7);
	    	RGraph.Effects.Gauge.Grow(gauges.item(i).__object__);
	      	break;
	    default:
	    	console.log("No data defined for "+canvasID+".")
	    
		}
		gauges.item(i).__object__.Draw();
	}
}

