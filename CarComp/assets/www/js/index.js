/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    // Application Constructor
    initialize: function() {
        this.bindEvents();
    },
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicity call 'app.receivedEvent(...);'
    onDeviceReady: function() {
        app.receivedEvent('deviceready');
    },
    // Update DOM on a Received Event
    receivedEvent: function(id) {
        var parentElement = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');

        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');

        console.log('Received Event: ' + id);
    },
    
    enable : function() {
        bConnection.enableBluetooth();
    },
    
    discoverDevices : function(){
    	bConnection.discoverDevices();
    },
    
    stopDiscoveringDevices : function(){
    	var devices = "";
    	console.log("DISCOVERING ENDED");
    	bConnection.stopDiscovering(
    	   function(r) { 
    		   console.log(JSON.stringify(r));
    		   var devices = r;
    		   
    		   //clearing device list
    		   document.getElementById("devices").innerHTML = '';
    		   
    		   //adding new elements to device list
    		   for(d in devices) {
    			   appendListElement(devices[d].name, devices[d].adress, "devices");
    		   }
    		   
    		   //setting message displayed to the user
    		   if(document.getElementById("devices").getElementsByTagName("li").length){
    			   document.getElementById("titleDevicesPopup").innerHTML="Please select a device to connect to.";
    		   }
    		   else {
    			   document.getElementById("titleDevicesPopup").innerHTML="No Device was found !";
    		   }
    		},
    	    function(e) { 
    	        console.log("failure");
    	    }
    	    
    	);
    },
    
    createBound : function(macAdress){
    	bConnection.createBound(macAdress);
    },
    
    writeMessage : function(message){
    	bConnection.writeMessage(message);
    }
    
    
};

//customized android backbutton
document.addEventListener("backbutton", function(){
	window.location.reload();
}, false);


//just generates random values atm... Here a function should be called receiving the actual values from the bt-adapter.
function refreshValues(){
	var randomValues = new Array();
	randomValues[0] = Math.floor((Math.random() * 100) + 1);
	randomValues[1] = Math.floor((Math.random() * 100) + 1);
	randomValues[2] = Math.floor((Math.random() * 100) + 1);
	randomValues[3] = Math.floor((Math.random() * 100) + 1);
	randomValues[4] = "gasoline E"+Math.floor((Math.random() * 100) + 1);
	randomValues[5] = Math.floor((Math.random() * 100) + 1);
	
	setDisplayValues(randomValues);
}



