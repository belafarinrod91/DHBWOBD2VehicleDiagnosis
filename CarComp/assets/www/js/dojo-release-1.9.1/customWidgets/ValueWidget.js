define(["dojo/_base/declare",
        "dijit/_WidgetBase", 
        "dijit/_TemplatedMixin",
        "dojo/dom-construct",
        "dojox/dgauges/GaugeBase",
        "dojo/dom-style"],
    function(declare, _WidgetBase, _TemplatedMixin, domConstruct, domStyle){
        return declare([_WidgetBase], {
        	div: domConstruct.create("div", {innerHTML: "No div Specified!"}),
        	label: "",
        	unit: "",
        	value: 0,
        	valueSpan:null,
        	
        	buildRendering: function(){
                var fontsize= Math.round(parseInt(this.div.style.width)/10);
                
                this.div.appendChild(domConstruct.create("div", {
					style:"height:30%;"
				}));
                
        		var labelNode=domConstruct.create("div", {
					innerHTML: this.label,
					style:"font-size:"+fontsize+"pt;"
				});
        		this.div.appendChild(labelNode);
        		
        		var valueNode=domConstruct.create("div", {
				});
        		this.valueSpan=domConstruct.create("span", {
					innerHTML: this.value,
					style:"font-size:"+fontsize+"pt;"
				});
        		var unitSpan=domConstruct.create("span", {
					innerHTML: this.unit,
					style:"font-size:"+fontsize+"pt;"
				});
        		valueNode.appendChild(this.valueSpan);
        		valueNode.appendChild(unitSpan);
        		this.div.appendChild(valueNode);
        		
                this.domNode = this.div;
            },
            _setValueAttr: function(value){	
    			this.valueSpan.innerHTML=value;
    		}
        });
});