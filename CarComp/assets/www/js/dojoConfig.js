require([
	"dojo/dnd/Moveable",
	"dojo/dom",
	"dojo/dom-style",
	"dijit/registry",
	"dojox/mobile/ProgressIndicator",
	"dojox/mobile/parser",
	"dojox/mobile",
	"dojox/mobile/compat",
	"dojox/mobile/SimpleDialog",
	"dojox/mobile/TextBox",
	"dojox/mobile/Button",
	"dojox/mobile/Slider", 
	"dojox/mobile/deviceTheme", 
	"dojo/domReady!",
	"dijit/form/Button",
	"dojox/gauges/GlossyCircularGauge"
	], function(Moveable, dom, domStyle, registry, ProgressIndicator){
		
		//displaying a dialog
		show = function(dlg){
			registry.byId(dlg).show();
		}
		
		//hiding a dialog
		hide = function(dlg){
			registry.byId(dlg).hide();
		}
		
		//displaying a progress indicator and its dialog
		var prog;
		show_progress_indicator = function(dlg,cont){
			dom.byId("titleDevicesPopup").innerHTML="Please wait for 5 seconds.";
			show(dlg);
			var container = dom.byId(cont);
			prog = ProgressIndicator.getInstance();
			container.appendChild(prog.domNode);
			prog.start();
			app.discoverDevices();
			setTimeout(function(){
				app.stopDiscoveringDevices();
				//just for testing:
				//appendListElement("moin", "mohoin", "devices");
				prog.stop();
			}, 5000);
		}
		
		//hiding a progress indicator and its dialog
		hide_progress_indicator = function(dlg){
			prog.stop();
			hide(dlg);
		}
		
		
		//function to add Elements to the List of found bluetooth devices
		appendListElement = function(name, address, list){
			 var list = registry.byId(list);
	         var childWidget = new dojox.mobile.ListItem({
	        	 label:name,
	        	 clickable : true,
	             onClick : function() {
	            	hide_progress_indicator("devicesPopup");
	            	dom.byId("btConnectionStatus").innerHTML="connecting to : "+name+" ...";
	             	if(app.createBound(address)){
	             		dom.byId("btConnectionStatus").innerHTML="Is connected to : "+name+" !";
	             	}else{
	             		dom.byId("btConnectionStatus").innerHTML="Connection Error.";
	             	}
	             }
	         });
	         list.addChild(childWidget);
		}
		
		//creating a circular gauge
		var glossyCircular;
		makeGauge = function(){
			glossyCircular = new dojox.gauges.GlossyCircularGauge({
			    background: [255, 255, 255, 0],
			    value: 0,
			    title: 'RPM',
			    id: "rpmGauge",
			    width: 300,
			    height: 300,
			    noChange: true
			}, dojo.byId("rpmGauge"));
			glossyCircular.startup();
		    	
			setInterval(function() {
			    var randomValue = Math.floor((Math.random() * 100) + 1);
		        glossyCircular.set("value", randomValue);
		    }, 3000);
		};
		
		//customize functionality of "display gauges"
		isCustomizeable=false;
		var dnd;
		makeMoveable= function(){
			if(!isCustomizeable){
				dnd = new Moveable(dom.byId("rpmGauge"));
				
				isCustomizeable=true;
				dom.byId("custButton").innerHTML="Done!";
			}else{
				dnd.destroy();
				
				isCustomizeable=false;
				dom.byId("custButton").innerHTML="Customize";
			}
		}
		
		//this function takes an array of 6 elements and uses it to update the labels of the display list
		setDisplayValues=function(values){
			registry.byId("display_rpm").set("rightText", values[0]);
			registry.byId("display_speed").set("rightText", values[1]);
			registry.byId("display_runTime").set("rightText", values[2]);
			registry.byId("display_oilTemp").set("rightText", values[3]);
			registry.byId("display_fuelType").set("rightText", values[4]);
			registry.byId("display_fuelRate").set("rightText", values[5]);
		}
		
		//this function is called when everything else is loaded
		dojo.ready(function(){
			//initialize circular gauge
			makeGauge();
			
			//configuration for bluetooth on/off-switch
			if(true){
				registry.byId("bluetoothSwitch").set("value", "on");
			}else{
				registry.byId("bluetoothSwitch").set("value", "off");
			}
			
			dojo.connect(registry.byId("bluetoothSwitch"), "onStateChanged", function(newState){
		 	   if(newState=="on"){
		 		  app.enable();
		 	   }else{
		 		  alert("bluetooth should be disabled here!");
		 	   }
			});
		});
		
		//Hides loading overlay when dojo is fully initialized
		domStyle.set(dom.byId("loadingOverlay"),'display','none');
});