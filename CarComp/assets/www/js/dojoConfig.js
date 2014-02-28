require([
		"dojo/dnd/Moveable",
		"dojo/dom",
		"dojo/dom-style",
		"dojo/dom-construct",
		"dijit/registry",
		"dojox/mobile/ProgressIndicator",
		"dojo/_base/connect",
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
		"dojox/gauges/GlossyCircularGauge",
		"dojo/window",
		"dojo/query",
		"dojox/geo/openlayers/Map",
		"dojo"
	], function(Moveable, dom, domStyle, domConstruct, registry, ProgressIndicator, connect){
		
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
			dom.byId("titleDevicesPopup").innerHTML="Please wait ...";
			show(dlg);
			var container = dom.byId(cont);
			prog = ProgressIndicator.getInstance();
			container.appendChild(prog.domNode);
			prog.start();
			setTimeout(function(){
				app.discoverDevices();
			}, 1000);
		}
		
		//hiding a progress indicator and its dialog
		hide_progress_indicator = function(dlg){
			prog.stop();
			hide(dlg);
		}
		
		hide_progress_indicator_only = function(){
			prog.stop();
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
	             	app.connect(address);
	            }
	         });
	         list.addChild(childWidget);
		}
		
		//creating a new Gauge and append it to the "gauges" div
		addGauge = function(gaugeName){
			domConstruct.create("div", {
				id: gaugeName,
				align: "center",
				style: { 
					width: "200px",
					height: "200px"
				},
				onClick: "customizeDiv("+gaugeName+")"
			}, 
			"gauges");
			makeGauge(gaugeName);
		};
		
		var dnd;
		customizeDiv = function(div){
			if(registry.byId(div).className != "selectedDiv" && isCustomizeable){
				deselectGauge();
				registry.byId(div).className = "selectedDiv";
				dnd = new Moveable(dom.byId(div));
			}
		}
		
		deselectGauge = function(){
			selectedDivs=dojo.query(".selectedDiv");
			if(selectedDivs.length>0){
				selectedDivs[0].className = "";
				dnd.destroy();
			}
		}
		
		//creating a circular gauge
		makeGauge = function(id){
			var specs = getGaugeSpecs(id);
			
			var type=specs[0];
			var max=specs[1];
			var tick=specs[2];
			
			var newGauge = new dojox.gauges.GlossyCircularGauge({
			    background: [255, 255, 255, 0],
			    value: 0,
			    min: 0,
			    max: max,
			    majorTicksInterval: tick,
			    minorTicksInterval: tick/2,
			    width: domStyle.get(id, "width"),
			    height: domStyle.get(id, "height"),
			    noChange: true
			}, dojo.byId(id));
			newGauge.startup();
		};
		
		getGaugeSpecs = function(id){
			var type;
			var max;
			var tick;
			if(id=="gauge_engineRPM"){
				type = "circular";
				max = 7;
				tick = 1;
			}else if(id=="gauge_speed"){
				type = "circular";
				max = 250;
				tick = 20;
			}else if(id=="gauge_runtime"){
				type = "circular";
				max = 1000;
				tick = 100;
			}else if(id=="gauge_oilTemperature"){
				type = "circular";
				max = 200;
				tick = 20;
			}else if(id=="gauge_fuelType"){
				type = "circular";
				max = 100;
				tick = 10;
			}else if(id=="gauge_fuelRate"){
				type = "circular";
				max = 10;
				tick = 1;
			}else{
				alert("An Error getting the Gauge Specs occurred.");
				return;
			}
			return [type, max, tick];
		}
		
		removeGauge = function(){
			//TODO: add code to delete gauge
		}
		
		//customize functionality of "display gauges"
		isCustomizeable=false;
		//var dnd;
		makeMoveable = function(){
			if(!isCustomizeable){		
				isCustomizeable=true;
				
				registry.byId("addGaugeButton").set("style", "visibility:visible");
				registry.byId("displayGaugesFooter").set("style", "visibility:visible");
				
				dom.byId("custButton").innerHTML="Done!";
			}else{
				isCustomizeable=false;
				
				deselectGauge();
				registry.byId("addGaugeButton").set("style", "visibility:hidden");
				registry.byId("displayGaugesFooter").set("style", "visibility:hidden");
				
				dom.byId("custButton").innerHTML="Customize";
			}
		}
		
		//this function takes an array of elements and uses it to update the labels of the display list
		setDisplayValues = function(values){
			values.forEach(function(item) {
				for (key in item){
					registry.byId("display_"+key).set("rightText", item[key]);
				}
			});
		}

		//this function takes an array of elements and uses it to update the labels of the gauges
		setGaugeValues = function(values){
			values.forEach(function(item) {
				for (key in item){
					if(registry.byId("gauge_"+key) != null){
						registry.byId("gauge_"+key).set("value", item[key]);
					}
				}
			});
		}
		
		renderMap = function(){
			map = new dojox.geo.openlayers.Map("navigation_map");
		    map.fitTo([ -160, 70, 160, -70 ]);
		}
		
		resizeGaugeIncrease=function(){
			selectedDivs=dojo.query(".selectedDiv");
			if(selectedDivs.length>0){
				domStyle.set(selectedDivs[0], "width", domStyle.get(selectedDivs[0], "width")+10+"px");
				domStyle.set(selectedDivs[0], "height", domStyle.get(selectedDivs[0], "height")+10+"px");
				rerenderGauge(selectedDivs[0].id);
			}
		}
		
		resizeGaugeDecrease=function(){
			selectedDivs=dojo.query(".selectedDiv");
			if(selectedDivs.length>0){
				domStyle.set(selectedDivs[0], "width", domStyle.get(selectedDivs[0], "width")-10+"px");
				domStyle.set(selectedDivs[0], "height", domStyle.get(selectedDivs[0], "height")-10+"px");
				rerenderGauge(selectedDivs[0].id);
			}
		}
		
		rerenderGauge = function(id){
			gaugediv=dom.byId(id);
			registry.byId(id).destroy();
			dom.byId("gauges").appendChild(gaugediv);
			makeGauge(id);
		}
		
		//this function is called when everything else is loaded
		dojo.ready(function(){
			
			connect.subscribe("/dojox/mobile/afterTransitionIn",
				    function(view, moveTo, dir, transition, context, method){
					  if(moveTo=="navigation"){
						  renderMap();
					  }
			});
			
			//configuration for bluetooth on/off-switch
			if(app.isBTEnabled()){
				registry.byId("bluetoothSwitch").set("value", "on");
			}else{
				registry.byId("bluetoothSwitch").set("value", "off");
			}
			
			dojo.connect(registry.byId("bluetoothSwitch"), "onStateChanged", function(newState){
		 	   if(newState=="on"){
		 		  app.enableBT();
		 	   }else if (newState == "off"){
		 		  app.disableBT();
		 	   }
		 	   else {
		 		  alert("There went something wrong, calling the new state of BluetoothIndicator");
		 	   }
			});
		});
		
		//Hides loading overlay when dojo is fully initialized
		domStyle.set(dom.byId("loadingOverlay"),'display','none');
});

//customize functionality of "Display"
require([
	"dojo/_base/connect",
	"dojo/dom-class",
	"dojo/ready",
	"dijit/registry",
	"dojox/mobile/parser",
	"dojox/mobile",
	"dojox/mobile/compat"
], function(connect, domClass, ready, registry){
	var delItem, handler, btn1, list1;

	function showDeleteButton(item){
		hideDeleteButton();
		delItem = item;
		item.rightIconNode.style.display = "none";
		if(!item.rightIcon2Node){
			item.set("rightIcon2", "mblDomButtonMyRedButton_0");
			item.rightIcon2Node.firstChild.innerHTML = "Delete";
		}
		item.rightIcon2Node.style.display = "";
		handler = connect.connect(list1.domNode, "onclick", onClick);
	}

	function hideDeleteButton(){
		if(delItem){
			delItem.rightIconNode.style.display = "";
			delItem.rightIcon2Node.style.display = "none";
			delItem = null;
		}
		connect.disconnect(handler);
	}

	function onClick(e){
		var item = registry.getEnclosingWidget(e.target);
		if(domClass.contains(e.target, "mblDomButtonMyRedButton_0")){
			oldId=item.id;
			oldLabel=item.get("label");
			oldRightText=item.get("rightText");
			item.destroy();
			list2.addChild(new dojox.mobile.ListItem({
				 id:oldId,
	        	 label:oldLabel,
	        	 rightText:oldRightText
	        }));
		}
		hideDeleteButton();
	}

	connect.subscribe("/dojox/mobile/deleteListItem", function(item){
		showDeleteButton(item);
	});

	makeMoveableDisplay = function(){
		var flag = btn1._flag = !btn1._flag; // true: editable
		if(flag){
			list1.startEdit();
			btn1.set("label", "Done");
			registry.byId("propertieButtonDisplay").set("style", "visibility:visible");
		}else{
			hideDeleteButton();
			list1.endEdit();
			btn1.set("label", "Customize");
			registry.byId("propertieButtonDisplay").set("style", "visibility:hidden");
		}
	}

	ready(function(){
		btn1 = registry.byId("custButtonDisplay");
		list1 = registry.byId("displayList");
		list2 = registry.byId("properties");
	});
	
	addProperties=function(){
		selected=registry.byId("properties").getChildren();
		for(var i=0; i<selected.length; i++){
			item=selected[i];
			if(selected[i].checked){
				oldId=item.id;
				oldLabel=item.get("label");
				oldRightText=item.get("rightText");
				item.destroy();
				list1.addChild(new dojox.mobile.ListItem({
					 id:oldId,
		        	 label:oldLabel,
		        	 rightText:oldRightText
		        }));
				list1.startEdit();
			}
		}
	}
});