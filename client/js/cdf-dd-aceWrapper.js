//ACE wrapper
var CodeEditor = Base.extend({
	
	MODES : {
    JAVASCRIPT: 'javascript',
    CSS: 'css',
    XML: 'xml'
  },
  MODE_BASE : 'ace/mode/',
	DEFAULT_MODE: 'text',
	
	modeMap :
	{ //highlight modes
		'css' : 'css',
		'javascript' : 'javascript',
		'js' : 'javascript',
		'xml' : 'xml',
		'cda' : 'xml',
		'cdv' : 'javascript',
		'html': 'html',
		'sql' : 'text',
		'mdx' : 'text'
	},
  
  mode: 'javascript',
  theme: 'ace/theme/twilight',
  editor: null,
	
	initEditor: function(editorId){
		this.editor = ace.edit(editorId);
    this.editor.setTheme(this.theme);
    
		if(this.mode != null){
			this.setMode(this.mode);
		}
    this.editor.setShowPrintMargin(false);
	},
	
	loadFile: function(fileName){
		var myself = this;
		//check edit permission
		$.get("canEdit", {path: fileName},
			function(result){
				var readonly = result != 'true';
				myself.setReadOnly(readonly);
				//TODO: can read?..get permissions?...

				//load file contents
				$.get("getFile",{path:fileName},
					function(fileContents) {
						myself.setContents(fileContents);
					}
				);
				
			}
		);
	},
	
	setContents: function(contents){
		this.editor.getSession().setValue(contents);
		this.editor.navigateFileStart();
	},
	
	saveFile: function(fileName, contents, callback){
		$.post("writeFile", { path: fileName, data: contents },
			 function(data){
				if(typeof callback == 'function'){
					callback(data);
				}
			 }
		);
	},
	
	getContents: function(){
		return this.editor.getSession().getValue();
	},
	
	setMode: function(mode)
	{
		this.mode = this.modeMap[mode];

    if(this.mode == null){
      this.mode = this.DEFAULT_MODE;
    }
    
		if(this.editor != null)
		{
			if(this.mode != null){
				var HLMode = require(this.MODE_BASE + this.mode).Mode;
				this.editor.getSession().setMode(new HLMode());
			}
		}
		
	},
	
	setTheme: function(themePath){
		this.theme = themePath;
	},
	
	setReadOnly: function(readOnly){
		if(readOnly == this.editor.getReadOnly()){ return; }
		else{ this.editor.setReadOnly(readOnly); }
	},
	
	isReadOnly: function(){
		return this.editor.getReadOnly();
	},
	
	insert: function(text){
		this.editor.insert(text);
	},
	
	getEditor: function(){
		return this.editor;
	},
	
	onChange: function(callback){
		this.editor.getSession().on('change', callback);
	}
	
});
