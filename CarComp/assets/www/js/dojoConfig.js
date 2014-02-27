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
		"dojox/geo/openlayers/Map"
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
				hide_progress_indicator_only();
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
		
		var gaugeSize=Math.min((dojo.window.getBox().h-dom.byId("displayGaugesHeading").offsetHeight)/3, dojo.window.getBox().w/2);
		
		addGauge=function(gaugeName){
			dom.byId("gauges").innerHTML+="<div id='"+gaugeName+"' align='center' style='width:"+gaugeSize+"px; height:"+gaugeSize+"px; background:grey;' onClick='customizeDiv("+gaugeName+")'>"
			+gaugeName
			+"</div>";
		};
		
		var dnd;
		customizeDiv = function(div){
			if(registry.byId(div).className != "selectedDiv"){
				selectedDivs=dojo.query(".selectedDiv");
				if(selectedDivs.length>0){
					selectedDivs[0].className = "";
					dnd.destroy();
				}
				registry.byId(div).className = "selectedDiv";
				dnd = new Moveable(dom.byId(div));
			}
		}
		
		//creating a circular gauge
		makeGauge = function(gauge, title, id, max, tick){
			
			gauge = new dojox.gauges.GlossyCircularGauge({
			    background: [255, 255, 255, 0],
			    value: 0,
			    min: 0,
			    max: max,
			    majorTicksInterval: tick,
			    minorTicksInterval: tick/2,
			    title: title,
			    id: id,
			    width: gaugeSize,
			    height: gaugeSize,
			    noChange: true
			}, dojo.byId(id));
			gauge.startup();
		    	
			setInterval(function() {
			    var randomValue = Math.floor((Math.random() * 100) + 1);
		        gauge.set("value", randomValue);
		    }, 3000);
		};
		
		//customize functionality of "display gauges"
		isCustomizeable=false;
		//var dnd;
		makeMoveable= function(){
			if(!isCustomizeable){
				/*dnd = new Moveable(dom.byId("rpmGauge"));
				dnd = new Moveable(dom.byId("speedGauge"));
				dnd = new Moveable(dom.byId("runTimeGauge"));
				dnd = new Moveable(dom.byId("oilTempGauge"));
				dnd = new Moveable(dom.byId("fuelTypeGauge"));
				dnd = new Moveable(dom.byId("fualRateGauge"));*/
				
				isCustomizeable=true;
				registry.byId("addGaugeButton").set("style", "visibility:visible");
				dom.byId("custButton").innerHTML="Done!";
			}else{
				//dnd.destroy();
				
				isCustomizeable=false;
				registry.byId("addGaugeButton").set("style", "visibility:hidden");
				dom.byId("custButton").innerHTML="Customize";
			}
		}
		
		//this function takes an array of 6 elements and uses it to update the labels of the display list
		setDisplayValues=function(values){
			var response = values;
			response.forEach(function(item) {
				for (key in item){
					registry.byId("display_"+key).set("rightText", item[key]);
				}
			});
		}
		
		renderMap=function(){
			map = new dojox.geo.openlayers.Map("navigation_map");
		    map.fitTo([ -160, 70, 160, -70 ]);
		}
		
		//this function is called when everything else is loaded
		dojo.ready(function(){
			
			connect.subscribe("/dojox/mobile/afterTransitionIn",
				    function(view, moveTo, dir, transition, context, method){
					  if(moveTo=="navigation"){
						  renderMap();
					  }
			});
			
			//initialize circular gauges
			/*var rpmGauge;
			makeGauge(rpmGauge, 'RPM', "rpmGauge", 7000, 500);
			var speedGauge;
			makeGauge(speedGauge, 'Speed', "speedGauge", 250, 20);
			var runTimeGauge;
			makeGauge(runTimeGauge, 'Run Time since Engine Start', "runTimeGauge", 1000, 100);
			var oilTempGauge;
			makeGauge(oilTempGauge, 'Oil Temperature', "oilTempGauge", 200, 10);
			var fuelTypeGauge;
			makeGauge(fuelTypeGauge, 'Fuel Type', "fuelTypeGauge", 100, 10);
			var fualRateGauge;
			makeGauge(fualRateGauge, 'Fuel Rate', "fualRateGauge", 10, 0.5);*/
			
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