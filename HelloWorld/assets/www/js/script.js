var bluetoothSerial = cordova.require('bluetoothSerial');


function init() {
	document.addEventListener("deviceready", function() {
	    alert('You are the winner!')
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
	alert("called");
	bluetoothSerial.stopDiscovering(
	   function(r) { 
		   console.log(JSON.stringify(r));
		   var devices = r;
		   for(d in devices) {
			   $(".devices").append("<b>Appended text</b></br>");
		   }
		   
		},
	    function(e) { 
	        console.log("failure");
	    }
	);  
	location.reload();
	console.log("was called");

}



