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
	console.log("DISCOVERING STARTED");
	// give UI-HINT while device is searching for devices !
	bluetoothSerial.discoverDevices(
	   function() { 
	        alert("Bluetooth is enabled");
	    },
	    function() { 
	        alert("Bluetooth is *not* enabled");
	    }
	); 
}

function stopDiscoverBTDevices(){
	console.log("DISCOVERING ENDED");
	bluetoothSerial.stopDiscovering(
	   function(r) { 
		   console.log(JSON.stringify(r));
		   var devices = r;
		   for(d in devices) {
			   $("#devices").append("<li><a onclick=\"pair('"+devices[d].name+"', '"+devices[d].adress+"')\">"+devices[d].name+"</a></li>");
		   }
		},
	    function(e) { 
	        console.log("failure");
	    }
	);
	$("#devices").listview("refresh");
	console.log("was called");

}

function pair(name, adress){
	alert(name);
}



