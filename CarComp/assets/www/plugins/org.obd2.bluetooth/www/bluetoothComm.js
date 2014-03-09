cordova.define("org.obd2.bluetooth.BluetoothConnection", function(require, exports, module) {var bConnection = {
	
	enableBT : function(success, failure) {cordova.exec(success, failure, "BluetoothConnection","enableBT", []); },
	
	disableBT: function(success, failure){cordova.exec(success, failure, "BluetoothConnection", "disableBT", []);},
	
	isBTEnabled: function(success, failure){
		return cordova.exec(success, failure, "BluetoothConnection", "isBTEnabled", []);},
	
	discoverDevices : function(success, failure){
		return cordova.exec(success, failure, "BluetoothConnection", "discoverDevices", []);},

	stopDiscover: function(success,failure) {cordova.exec(success, failure, "BluetoothConnection", "stopDiscover", []);},
	
	pair : function (macAddress, success, failure) {cordova.exec(success, failure, "BluetoothConnection", "pair", [macAddress]);},
	
	unpair : function (macAddress, success, failure) {cordova.exec(success, failure, "BluetoothConnection", "pair", [macAddress]);},
	
	isBound : function (macAddress, success, failure) {
		return cordova.exec(success, failure, "BluetoothConnection", "isBound", [macAddress]);},
	
	listBoundDevices : function (success, failure) {
		return cordova.exec(success, failure, "BluetoothConnection", "listBoundDevices", []);},

	connect: function(address, success, failure){cordova.exec(success, failure, "BluetoothConnection", "connect", [address]);},
	
	writeMessage : function (message, success, failure) {cordova.exec(success, failure, "BluetoothConnection", "writeMessage", [message]);},
	
	getOBD2ConnectionStatus : function (success, failure) {
		return cordova.exec(success, failure, "BluetoothConnection", "getOBD2ConnectionStatus", []);},
		
	getOBD2Values : function (values, success, failure) {
		return cordova.exec(success, failure, "BluetoothConnection", "getOBD2Values", values);},

	getLocationStatus : function (success, failure) {
		return cordova.exec(success, failure, "BluetoothConnection", "getLocationStatus", []);}, 
	
	getLocation : function (success, failure) {
		return cordova.exec(success, failure, "BluetoothConnection", "getLocation", []);}	
	}
module.exports = bConnection;});
