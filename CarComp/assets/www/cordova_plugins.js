cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
    {
        "file": "plugins/org.obd2.bluetooth/www/bluetoothComm.js",
        "id": "org.obd2.bluetooth.BluetoothConnection",
        "clobbers": [
            "window.bConnection"
        ]
    }
]
});