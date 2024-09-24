/**
 @param: je (dictionary) - json element description
  key (string) - html tag name
  value - one of:
   * String: text content
   * Array: array of child elements as json
   * JQuery: html content
   * Dictionary: jquery methods.
         key: method name.
         value: parameters (single value or array)
*/
function json2dom(je){
    // get tag name and content
    var tag,content;
    for(var key in je){
	if(tag)
	    throw "dictionary must have a single item";
	tag = key;
	content = je[key];
    }

    // create tag element
    var element = $(document.createElement(tag));

    // add content
    // string (text content)
    if(typeof content == "string"){
	element.text(content);
    }

    // array (child elements)
    else if(content instanceof Array){
	for(var i=0,max=content.length;i<max;i++){
	    element.append(json2dom(content[i]));
	}
    }

    // dom element or jQuery object (html content)
    else if(content instanceof jQuery){
	element.html(content);
    }

    // dictionary
    else{
	for(var method in content){
	    if(content[method] instanceof Array)
		element[method].apply(element,content[method]);
	    else
		element[method].call(element,content[method]);
	}
    }

    return element;
}

jQuery.extend({json2dom:json2dom});