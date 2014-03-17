require([
		"dojo/_base/connect",
		"dojo/dom-class",
		"dojo/ready",
		"dijit/registry",
		"dojo/dnd/Moveable",
		"dojo/dom",
		"dojo/dom-style",
		"dojo/dom-construct",
		"dojox/mobile/ProgressIndicator",
		"customWidgets/ValueWidget",
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
		"dijit/form/TextBox",
		"dojox/dgauges/components/black/CircularLinearGauge",
		"dojox/dgauges/components/black/SemiCircularLinearGauge",
		"dojox/dgauges/components/grey/SemiCircularLinearGauge",
		"dojo/window",
		"dojo/query",
		"dojox/geo/openlayers/Map",
		"dojo",
		"dojox/storage"
	], function(connect, domClass, ready, registry, Moveable, dom, domStyle, domConstruct, ProgressIndicator, ValueWidget){
	
	//global variables
	var displayGaugeAttribute="Size";
	
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
	show_progress_indicator = function(parameter,dlg,cont){
		dom.byId("titleDevicesPopup").innerHTML="Please wait ...";
		show(dlg);
		var container = dom.byId(cont);
		prog = ProgressIndicator.getInstance();
		container.appendChild(prog.domNode);
		
		var list = registry.byId(devices);
		dojo.empty(list);
		
		prog.start();
		setTimeout(function(){
			if(parameter == 'discover'){
				app.discoverDevices();
			}
			else if(parameter == 'listBoundedDevices'){
				app.listBoundDevices();
			}
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
             	app.connect(name, address);
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
		registry.byId(gaugeName+"_add").set("clickable", false);
		domStyle.set(dom.byId(gaugeName+"_add"), "background", "grey");
		
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
	
	//creating a new gauge
	makeGauge = function(id, gaugeType=0){
		var specs = getGaugeSpecs(id);
		
		var dataType=specs[0];
		var max=specs[1];
		var tick=specs[2];
		var label=specs[3];
		var unit=specs[4];
		
		if(gaugeType==0 && dataType!="string"){
			var newGauge = new dojox.dgauges.components.black.CircularLinearGauge({
			    minimum: 0,
			    maximum: max,
			    majorTickInterval: tick,
			    minorTickInterval: tick/2,
			    width: domStyle.get(id, "width"),
			    height: domStyle.get(id, "height"),
			    interactionMode: "none",
			    animationDuration: 200,
			}, dojo.byId(id));
			
			domConstruct.create("span", {
				id: id+"_label",
				innerHTML: label+" ["+unit+"]",
				style: { 
					position: "relative",
					bottom: "25%",
					color: "white"
				},
			}, 
			id);
		}else if(gaugeType==1 && dataType!="string"){
			var newGauge = new dojox.dgauges.components.grey.SemiCircularLinearGauge({
				borderColor: "#000000",
				fillColor: "#000000",
			    minimum: 0,
			    maximum: max,
			    majorTickInterval: tick,
			    minorTickInterval: tick/2,
			    width: domStyle.get(id, "width"),
			    height: domStyle.get(id, "height"),
			    interactionMode: "none",
			    animationDuration: 200,
			}, dojo.byId(id));
			
			domConstruct.create("span", {
				id: id+"_label",
				innerHTML: label+" ["+unit+"]",
				style: { 
					position: "relative",
					bottom: "70%",
					color: "black"
				},
			}, 
			id);
		}else if(gaugeType==2 && dataType!="string"){
			var newGauge = new dojox.dgauges.components.black.SemiCircularLinearGauge({
			    minimum: 0,
			    maximum: max,
			    majorTickInterval: tick,
			    minorTickInterval: tick/2,
			    width: domStyle.get(id, "width"),
			    height: domStyle.get(id, "height"),
			    interactionMode: "none",
			    animationDuration: 200,
			}, dojo.byId(id));
			
			domConstruct.create("span", {
				id: id+"_label",
				innerHTML: label+" ["+unit+"]",
				style: { 
					position: "relative",
					bottom: "50%",
					color: "white"
				},
			}, 
			id);
		}else{
			var newGauge = new ValueWidget({
				div: dom.byId(id),
				label: label,
				unit: unit
			}, dojo.byId(id));
		}
		registry.byId(id).set("dataType", dataType);
		registry.byId(id).set("gaugeType", gaugeType);
	};
	
	getGaugeSpecs = function(id){
		var dataType;
		var max;
		var tick;
		var label;
		var unit;
		if(id=="gauge_engineRPM"){
			dataType = "value";
			max = 7;
			tick = 1;
			label = "RPM";
			unit = "/s";
		}else if(id=="gauge_speed"){
			dataType = "value";
			max = 250;
			tick = 20;
			label = "Speed";
			unit = "km/h";
		}else if(id=="gauge_runtime"){
			dataType = "string";
			max = 1000;
			tick = 100;
			label = "Runtime";
			unit = "s";
		}else if(id=="gauge_oilTemperature"){
			dataType = "value";
			max = 200;
			tick = 20;
			label = "Oil Temp";
			unit = "°C";
		}else if(id=="gauge_fuelType"){
			dataType = "string";
			max = 100;
			tick = 10;
			label = "Fuel Type";
			unit = "";
		}else if(id=="gauge_fuelRate"){
			dataType = "value";
			max = 10;
			tick = 1;
			label = "Fuel Rate";
			unit = "l/100km";
		}else{
			alert("An Error getting the Gauge Specs occurred.");
			return;
		}
		return [dataType, max, tick, label, unit];
	}
	
	removeGauge = function(){
		selectedDivs=dojo.query(".selectedDiv");
		if(selectedDivs.length>0){
			registry.byId(selectedDivs[0].id).destroy();
			registry.byId(selectedDivs[0].id+"_add").set("clickable", true);
			domStyle.set(dom.byId(selectedDivs[0].id+"_add"), "background", "");
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
			
			//save gauges in storage
			saveGauges();
		}
	}
	
	saveGauges = function(){
		storageProvider.put("gaugesDiv", dom.byId("gauges").innerHTML);
		var gauges = new Array();
		registry.findWidgets(dom.byId("gauges")).forEach(function(gauge){
			gauges.push({"id":gauge.id, "gaugeType":gaugeType});
		});
		storageProvider.put("gauges", gauges);
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
					if(isNaN(parseFloat(item[key]))){
						registry.byId("gauge_"+key).set("value", item[key]);
					}else{
						registry.byId("gauge_"+key).set("value", parseFloat(item[key]));
					}
					
				}
			}
		});
	}
	
	renderMap=function(){
			var location = app.getLocation();
			var lat = location.lat;
			var lon = location.lon;
			
			var myLatlng = new google.maps.LatLng(lat, lon);
		    var myOptions = {
		        zoom: 13,
		        center: myLatlng,
		        mapTypeId: google.maps.MapTypeId.ROADMAP
		    }
		    var map = new google.maps.Map(document.getElementById("navigation_map"), myOptions);

		    var marker = new google.maps.Marker({
		        position: myLatlng, 
		        map: map,
		        title:"Hello World!"
		    });   
		}
	
	resizeGaugeIncrease=function(){
		selectedDivs=dojo.query(".selectedDiv");
		if(selectedDivs.length>0){
			if(displayGaugeAttribute=="Size"){
				domStyle.set(selectedDivs[0], "width", domStyle.get(selectedDivs[0], "width")+10+"px");
				domStyle.set(selectedDivs[0], "height", domStyle.get(selectedDivs[0], "height")+10+"px");
				rerenderGauge(selectedDivs[0].id);
			}else if(displayGaugeAttribute=="Type"){
				rerenderGauge(selectedDivs[0].id, 1);
			}
		}
	}
	
	resizeGaugeDecrease=function(){
		selectedDivs=dojo.query(".selectedDiv");
		if(selectedDivs.length>0){
			if(displayGaugeAttribute=="Size"){
				domStyle.set(selectedDivs[0], "width", domStyle.get(selectedDivs[0], "width")-10+"px");
				domStyle.set(selectedDivs[0], "height", domStyle.get(selectedDivs[0], "height")-10+"px");
				rerenderGauge(selectedDivs[0].id);
			}else if(displayGaugeAttribute=="Type"){
				rerenderGauge(selectedDivs[0].id, -1);
			}
		}
	}
	
	rerenderGauge = function(id, changeGaugeType=0){
		//store important data to rerender
		gaugediv=dom.byId(id);
		gaugeType=registry.byId(id).get("gaugeType");
		dataType=registry.byId(id).get("dataType");
		
		//destroy old gauge
		registry.byId(id).destroy();
		
		//create new gauge
		dom.byId("gauges").appendChild(gaugediv);
		if(dataType=="string"){
			gaugeType=4;
		}else{
			gaugeType=(gaugeType+changeGaugeType)%4;
		}
		makeGauge(id, gaugeType);
	}
	
	//this function is called when everything else is loaded
	ready(function(){
		//setting global variables
		btn1 = registry.byId("custButtonDisplay");
		list1 = registry.byId("displayList");
		list2 = registry.byId("properties");
		
		//initialize local storage
		dojox.storage.manager.initialize();
		storageProvider=dojox.storage.manager.getProvider();
		storageProvider.initialize();
		
		//load display list from local storage
		 if(storageProvider.get("displayListItems") != "" && storageProvider.get("displayListItems") != null){
			 //sort list items
			 var prevItem;
			 var itemCnt=0;
			 storageProvider.get("displayListItems").forEach(function(item){
				 if(prevItem == null){
					 dojo.place(dom.byId(item.id), dom.byId("displayList"), "first");
				 }else{
					 dojo.place(dom.byId(item.id), dom.byId(prevItem.id), "after");
				 }
				 prevItem=item;
				 itemCnt++;
			 });
			//delete items not in saved configuration
			 var allItems=registry.findWidgets(dom.byId("displayList"));
			 for(i=itemCnt; i<allItems.length; i++){
				 removeListItem(allItems[i]);
			 }
		}

		//load gauges page from local storage
		if(storageProvider.get("gaugesDiv") != "" && storageProvider.get("gaugesDiv") != null){
			dom.byId("gauges").innerHTML=storageProvider.get("gaugesDiv");
			
			//rerender gauges
			storageProvider.get("gauges").forEach(function(gauge){
				dom.byId(gauge.id).innerHTML="";
				makeGauge(gauge.id, gauge.gaugeType);
				registry.byId(gauge.id+"_add").set("clickable", false);
				domStyle.set(dom.byId(gauge.id+"_add"), "background", "grey");
			});
		}
		
		connect.subscribe("/dojox/mobile/afterTransitionIn",
			    function(view, moveTo, dir, transition, context, method){
				  if(moveTo=="navigation"){
					  renderMap();
				  }
				  else if(moveTo == 'displayGauges'){
				  		refreshGauges();
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
		
		//Hides loading overlay when dojo is fully initialized
		domStyle.set(dom.byId("loadingOverlay"),'display','none');
	});
	
	//customize functionality of "Display"
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
			removeListItem(item);
		}
		hideDeleteButton();
	}
	
	//removes an item from the display list
	function removeListItem(item){
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

	connect.subscribe("/dojox/mobile/deleteListItem", function(item){
		showDeleteButton(item);
	});

	makeMoveableDisplay = function(){
		var flag = btn1._flag = !btn1._flag; // true: editable
		if(flag){
			list1.startEdit();
			btn1.set("label", "Done!");
			registry.byId("propertieButtonDisplay").set("style", "visibility:visible");
		}else{
			hideDeleteButton();
			list1.endEdit();
			btn1.set("label", "Customize");
			registry.byId("propertieButtonDisplay").set("style", "visibility:hidden");
			
			//save display list in local storage
			saveDisplay();			
		}
	}
	
	saveDisplay = function(){
		var items = new Array();
		registry.findWidgets(dom.byId("displayList")).forEach(function(item){
			items.push({"id":item.id});
		});
		storageProvider.put("displayListItems", items);
	}
	
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
	
	changeAttribute=function(arg){
		dom.byId("displayGaugeAttribute").innerHTML=arg;
		displayGaugeAttribute=arg;
	}
});