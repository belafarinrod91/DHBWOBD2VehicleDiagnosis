cordova.define("org.obd2.bluetooth.BluetoothConnection", function(require, exports, module) {var bConnection = {
	enableBluetooth : function(success, failure) {
					cordova.exec(success, failure, "BluetoothConnection",
							"enableBluetooth", []); 
							},
	disableBluetooth: function(success, failure){
					cordova.exec(success, failure, "BluetoothConnection", "disableBluetooth", []);
							},
	discoverDevices : function(success, failure){
					cordova.exec(success, failure, "BluetoothConnection", "discoverDevices", []);
							},
	stopDiscovering: function(success,failure) {
					return cordova.exec(success, failure, "BluetoothConnection", "stopDiscoverDevices", []);
							},
	createBound : function (macAddress, success, failure) {
        cordova.exec(success, failure, "BluetoothConnection", "createBond", [macAddress]);
    						},
    writeMessage : function (message, success, failure) {
    	cordova.exec(success, failure, "BluetoothConnection", "writeMessage", [message]);
    						}


					}
module.exports = bConnection;});
