require([
		"dojo/dnd/Moveable",
		"dojo/dom",
		"dojo/dom-style",
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
	], function(Moveable, dom, domStyle, registry, ProgressIndicator, connect){
		
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
		
		addGauge=function(gaugeName){
			dom.byId("gauges").innerHTML+="<div id='"+gaugeName+"' align='center' style='width:100px; height:100px;' onClick='customizeDiv("+gaugeName+")'></div>";
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
			max=getGaugeMax(id);
			tick=getGaugeTick(id);
			
			gauge = new dojox.gauges.GlossyCircularGauge({
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
			gauge.startup();
		};
		
		getGaugeMax = function(id){
			if(id=="rpmGauge"){
				return 7;
			}else if(id=="speedGauge"){
				return 250;
			}else if(id=="runTimeGauge"){
				return 1000;
			}else if(id=="oilTempGauge"){
				return 200;
			}else if(id=="fuelTypeGauge"){
				return 100;
			}else if(id=="fualRateGauge"){
				return 10;
			}else{
				return 0;
			}
		}
		
		getGaugeTick = function(id){
			if(id=="rpmGauge"){
				return 1;
			}else if(id=="speedGauge"){
				return 20;
			}else if(id=="runTimeGauge"){
				return 100;
			}else if(id=="oilTempGauge"){
				return 10;
			}else if(id=="fuelTypeGauge"){
				return 10;
			}else if(id=="fualRateGauge"){
				return 1;
			}else{
				return 0;
			}
		}
		
		removeGauges = function(){
			if (registry.byId("rpmGauge") != null){
				gauge = registry.byNode(dom.byId("rpmGauge"));
				alert(gauge.value);
				dojo.empty("rpmGauge");
				gauge.destroy();
			}
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
		
		//this function takes an array of 6 elements and uses it to update the labels of the display list
		setDisplayValues = function(values){
			var response = values;
			response.forEach(function(item) {
				for (key in item){
					registry.byId("display_"+key).set("rightText", item[key]);
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
				domStyle.set(selectedDivs[0], "width", domStyle.get(selectedDivs[0], "width")+5+"px");
				domStyle.set(selectedDivs[0], "height", domStyle.get(selectedDivs[0], "height")+5+"px");
				rerenderGauge(selectedDivs[0].id);
			}
		}
		
		resizeGaugeDecrease=function(){
			selectedDivs=dojo.query(".selectedDiv");
			if(selectedDivs.length>0){
				domStyle.set(selectedDivs[0], "width", domStyle.get(selectedDivs[0], "width")-5+"px");
				domStyle.set(selectedDivs[0], "height", domStyle.get(selectedDivs[0], "height")-5+"px");
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
			
			setInterval(function() {
				var gauges = registry.findWidgets(dom.byId("gauges"));
				for (i=0; i<gauges.length; i++){
					randomValue = Math.floor((Math.random() * 100) + 1);
				    gauges[i].set("value", randomValue);
				}
		    }, 5000);
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