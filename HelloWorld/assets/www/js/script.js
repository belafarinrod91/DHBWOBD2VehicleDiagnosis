var bluetoothSerial = cordova.require('bluetoothSerial');


function init() {
	document.addEventListener("deviceready", function() {
	}, false)
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
	bluetoothSerial.stopDiscovering(
	   function(r) { 
		   console.log(JSON.stringify(r));
		   var devices = r;
		   for(d in devices) {
			   $("#devices").append("<li><a onclick=\"pair('"+devices[d].name+"', '"+devices[d].adress+"')\">"+devices[d].name+"-"+devices[d].adress+"</a></li>");
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



