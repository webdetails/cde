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

/****************************************************************************************************************************/
/**										 		WIZARDS Functions MANAGER									 ***/
/****************************************************************************************************************************/

var WizardFunctionsManager = Base.extend({
	},{
		FunctionTypes: {},
		WizardFunctionsArray: {},
		
		register: function(FunctionType){
			WizardFunctionsManager.FunctionTypes[FunctionType.getType()] = FunctionType;
		},
		
		registerWizardFunctions: function(wizardFunctions){
			WizardFunctionsManager.WizardFunctionsArray[wizardFunctions.type] = wizardFunctions;
		},

		getFunction: function(FunctionType){
			return WizardFunctionsManager.FunctionTypes[FunctionType];
		},
		
		getWizardFunctions: function(wizardFunctions){
			return WizardFunctionsManager.WizardFunctionsArray[wizardFunctions];
		}
	}
);

/****************************************************************************************************************************/
/**										 		WIZARDS Functions 											 ***/
/****************************************************************************************************************************/

var PromptFunction = Base.extend({

		type: "",
		parent: "",
		name: "",
		value: "",
		
		setType: function(type){this.type = type},
		getType: function(){return this.type},
		
		setParent: function(parent){this.parent = parent},
		getParent: function(){return this.parent},
		
		setName: function(type){this.name = name},
		getName: function(){return this.name},
		
		setValue: function(value){this.value = value},
		getValue: function(){return typeof this.value == 'function' ? this.value() : this.value }

	});
	
//var JavascriptEvalFunction = PromptFunction.extend({
//
//	type: "eval",
//	parent: "Functions",
//	name: "eval",
//	value: "eval(stringValue)"
//	
//});
//WizardFunctionsManager.register(new JavascriptEvalFunction());

var JavascriptDecodeURIFunction = PromptFunction.extend({

	type: "decodeURI",
	parent: "Functions",
	name: "decodeURI",
	value: "decodeURI(stringValue)"
	
});
WizardFunctionsManager.register(new JavascriptDecodeURIFunction());

var JavascriptToStringFunction = PromptFunction.extend({

	type: "toString",
	parent: "String Functions",
	name: "ToString",
	value: ".toString()"
	
});
WizardFunctionsManager.register(new JavascriptToStringFunction());


var JavascriptSubStringFunction = PromptFunction.extend({

	type: "subString",
	parent: "String Functions",
	name: "SubString",
	value: ".substring(start,stop)"
	
});
WizardFunctionsManager.register(new JavascriptSubStringFunction());

var JavascriptClickFunction = PromptFunction.extend({

	type: "click",
	parent: "Event Handlers",
	name: "Click",
	value: ".click(function() { /* INSERT CODE HERE */ })"
	
});
WizardFunctionsManager.register(new JavascriptClickFunction());

var MdxAndOperator = PromptFunction.extend({

	type: "and",
	parent: "Operators",
	name: "AND",
	value: "Expression1 AND Expression2"
	
});
WizardFunctionsManager.register(new MdxAndOperator());

var MdxDescendantsFunction = PromptFunction.extend({

	type: "descendants",
	parent: "Functions",
	name: "Descendants",
	value: "Descendants(Member_Expression [ , Level_Expression [ ,Desc_Flag ] ] )"
	
});
WizardFunctionsManager.register(new MdxDescendantsFunction());

var SqlFormatFunction = PromptFunction.extend({

	type: "format",
	parent: "Scalar",
	name: "FORMAT",
	value: "FORMAT(column_name,format)"
	
});
WizardFunctionsManager.register(new SqlFormatFunction());

/****************************************************************************************************************************/
/**										 		WIZARDS Functions											***/
/****************************************************************************************************************************/

var JavascriptWizardFunctions = Base.extend({

	type: "JavascriptWizard",
	
	getFunctions: function(){ 
		var Functions = [];

		Functions.push(WizardFunctionsManager.getFunction("toString"));
		Functions.push(WizardFunctionsManager.getFunction("subString"));
		Functions.push(WizardFunctionsManager.getFunction("click"));
		Functions.push(WizardFunctionsManager.getFunction("eval"));
		Functions.push(WizardFunctionsManager.getFunction("decodeURI"));
		
		return Functions;
	}
});
WizardFunctionsManager.registerWizardFunctions(new JavascriptWizardFunctions());

var MdxWizardFunctions = Base.extend({

	type: "MdxWizard",
	
	getFunctions: function(){ 
		var Functions = [];
		
		Functions.push(WizardFunctionsManager.getFunction("and"));
		Functions.push(WizardFunctionsManager.getFunction("descendants"));
		
		return Functions;
	}
});
WizardFunctionsManager.registerWizardFunctions(new MdxWizardFunctions());

var SqlWizardFunctions = Base.extend({

	type: "SqlWizard",
	
	getFunctions: function(){ 
		var Functions = [];

		Functions.push(WizardFunctionsManager.getFunction("format"));
		
		return Functions;
	}
});
WizardFunctionsManager.registerWizardFunctions(new SqlWizardFunctions());


var CdaWizardFunctions = Base.extend({

	type: "CdaWizard",
	
	getFunctions: function(){ 
		var Functions = [];

		return Functions;
	}
});
WizardFunctionsManager.registerWizardFunctions(new CdaWizardFunctions());

