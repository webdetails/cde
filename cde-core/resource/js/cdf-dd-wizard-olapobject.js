/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

var WizardOlapObjectManager = Base.extend({},{
	
	MEASURE: "Measure",
	DIMENSION: "Dimension",
	FILTER: "Filter",

	getOlapObject: function(type,object){
		if(type == WizardOlapObjectManager.MEASURE)
			return new WizardMeasureObject(object);
		if(type == WizardOlapObjectManager.DIMENSION)
			return new WizardDimensionObject(object);
		if(type == WizardOlapObjectManager.FILTER)
			return new WizardFilterObject(object);
	}
	
});

var WizardOlapObject = Base.extend({
 
	constructor: function(object){
		this.logger = new Logger("OlapObject");
		this.olapObject = object;
	},
	
	getValue: function(preview){
		this.logger.error("getValue not implemented");
	},
	
	getFilterValue: function(preview){
		this.logger.error("getFilterValue not implemented");
	},
	
	render: function(container){
		this.logger.error("render not implemented");
	},
	
	setProcessChange: function(processChange){
		this.processChange = processChange;
	}

},{
});

var WizardMeasureObject = WizardOlapObject.extend({
	
	getValue: function(preview){
		return this.olapObject.qualifiedName;
	},
	
	getFilterValue: function(preview){
		return this.olapObject.qualifiedName;
	},
	
	render: function(container){
		this.htmlObject = $('' +
			'<div class="cdfdd-olap-measures">' +
			'  <div class="cdfdd-olap-measures-label">' + this.olapObject.caption + '</div>' +
			'</div>');
		container.append(this.htmlObject);
		return this.htmlObject;
	},
	
	clear: function(){
		this.htmlObject.remove();
	}
});

var WizardDimensionObject = WizardOlapObject.extend({

	constructor: function(olapObject){
		this.base(olapObject);
		
		this.memberDepth = 0;
		this.member = olapObject.hierarchy.defaultMemberQualifiedName;
		this.catalog = olapObject.catalog;
		this.cube = olapObject.cube;
		this.membersArray = [{name:olapObject.hierarchy.defaultMember,qualifiedName:olapObject.hierarchy.defaultMemberQualifiedName}];
		
		this.initialMembersArray = Util.clone(this.membersArray);
		this.initialMember = olapObject.hierarchy.defaultMemberQualifiedName;
	},
	
	getValue: function(preview){
		return "Descendants("+ this.member + " ," + this.olapObject.qualifiedName + ")";
	},
	
	getFilterValue: function(preview){
		var set = "set [" + this.olapObject.name + "_set] as Descendants("+ this.member + " ," + this.olapObject.qualifiedName + ")"; 
		var member = "member " + this.member + ".[Filter] as Aggregate([" + this.olapObject.name + "_set])";
		return {set: set, member: member, condition: this.member + ".[Filter]"};
	},
	

	render: function(container){
		
		if(this.htmlObject == undefined){
			this.htmlObject = $('<div class="cdfdd-olap-dimensions"></div>');
			container.append(this.htmlObject);
		}
		
		this.htmlObject.empty();
		this.htmlObject.append('<div class="cdfdd-olap-dimensions-label">' + this.olapObject.caption + '</div>');
		
		var selector = this.buildSelector();
		this.renderLeftButton(selector);
		this.htmlObject.append(selector);
		this.renderRightButton(selector);
	
		return this.htmlObject;
	},
	
	renderLeftButton: function(selector,container){
		var myself = this;
		if(this.memberDepth > 0){
			this.htmlObject.append($('<a><img class="image-left" src="' + wd.cde.endpoints.getStaticUrl() + '/images/arrow-left.png"></a>').bind('click',
				function(){myself.getMembersArray('up',selector.val(),container);}));
		}
		else
			this.htmlObject.append('<div class="image-left"></div>');
	},
	
	renderRightButton: function(selector,container){
		var myself = this;
		if(this.memberDepth < this.olapObject.depth -1)
			this.htmlObject.append( $('<a><img class="image-right" src="' + wd.cde.endpoints.getStaticUrl() + '/images/arrow-right.png"></a>').bind('click',
				function(){myself.getMembersArray('down',selector.val(),container);}));
	},
	
	buildSelector: function(){
		var myself = this;
		var selector = $('<select class="cdfdd-olap-dimensions-input" ></select>');
		for(var m in this.membersArray){
			if(this.membersArray.hasOwnProperty(m)){
				if(m != undefined && m != null){
					selector.append($('<option value="'+ this.membersArray[m].qualifiedName +'">'+ this.membersArray[m].name + '</option>'));
				}
			}
		}
		selector.change(function () {
			myself.member = $(this).val();
			myself.processChange();
		});

		return selector;
	},
	
	clear: function(){
		this.htmlObject.remove();
	},
	
	getMembersArray: function(direction,memberQualifiedName,container){
	
		var myself = this;
		var params = {
			catalog: myself.catalog,
			cube: myself.cube,
			member: memberQualifiedName,
			direction: direction
		};
			
		OlapWizardRequests.olapObject(params, container, myself, direction);
	}
});

var WizardFilterObject = WizardOlapObject.extend({

	render: function(container){
		this.htmlObject = $('' +
		'<div class="cdfdd-olap-measures">' +
		'  <div class="cdfdd-olap-measures-label">' + this.getId() + '</div>' +
		'</div>');
		container.append(this.htmlObject);
		return this.htmlObject;
	},
	
	getId: function(){
		return this.olapObject.properties[0].value;
	},
	
	getValue: function(preview){
		return preview ? this.olapObject.dimension : "${" + this.getId() + "}";
	},
	
	getFilterValue: function(preview){
		if(preview) return this.olapObject.dimension;
		var set = "set [" + this.getId() + "_set] as {${" + this.getId() + "}}"; 
		var member = "member " + this.olapObject.dimension + ".[Filter] as Aggregate([" + this.getId() + "_set])";
		return {set: set, member: member, condition: this.olapObject.dimension  + ".[Filter]"};
	},
	
	clear: function(){
		this.htmlObject.remove();
	}
});
