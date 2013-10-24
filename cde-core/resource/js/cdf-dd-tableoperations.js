// Base model

var PropertyTypeUsage = Base.extend({
  constructor: function(pName, pAlias, isOwned, propType, isExtension) {
    this.alias = pAlias;
    this.name  = pName; // local type name, when owned
    this.owned = isOwned;
    this.type  = propType;
    this.isExtension = isExtension;
  },
          
  create: function() {
     return this.type.getPropertyObject({
       name: this.alias
     });
  }
}, { // static
  create: function(propSpec, Model) {
    var pName, pAlias, isOwned = false, isExtension = false;
    switch(typeof propSpec) {
      case 'string':
        pName = pAlias = propSpec;
        break;

      case 'object':
        // better be non-null...
        pAlias = propSpec.alias;
        pName  = propSpec.name;
             if(!pAlias && pName ) { pAlias = pName ; }
        else if(!pName  && pAlias) { pName  = pAlias; }
        isOwned = propSpec.owned === true;
        isExtension = !!propSpec.isExtension;
        break;
    }

    if(!pAlias) { return null; }
    
    var pFullName = isOwned ? (Model.MODEL + "_" + pName) : pName;
    var propType = PropertiesManager.getPropertyType(pFullName);
    if(!propType) { return null; }
    
    return new PropertyTypeUsage(pName, pAlias, isOwned, propType, isExtension);
  }
});

/**
 * @name BaseModel 
 * @class The design-time view of a component type.
 * <p>This is the base abstract class of all model classes.</p>
 * <p>A model class generally only has static properties/methods.</p>
 * @abstract
 * @static
 */
var BaseModel = (function() {
  
  /** 
   * Make all BaseModel descendant classes inheritc static methods
   * defined in the base class at extension time.
   */
  var baseExtend = Base.extend;
  var staticExtend = function(instanceProps, staticProps) {
    var extStaticProps = Object.create(this);
    $.extend(extStaticProps, staticProps);

    var SubClass = baseExtend.call(this, instanceProps, extStaticProps);
    SubClass.extend = staticExtend;
    return SubClass;
  };

  // ---------

  // The BaseModel class.
  var BM = Base.extend({}, /** @lends BaseModel */{
    /**
     * The unique name of the corresponding component type.
     * <p>This is the name that component instances will 
     *     have in their <tt>type</tt> property.</p>
     * @type string
     */
    MODEL: 'BaseModel',
    
    /**
     * Legacy names by which this component type is known.
     * @type Array<string>
     */
    legacyNames: undefined,
    
    /**
     * Obtains a JSON stub for a component instance 
     * of the model's corresponding component type.
     * <p>This method must be overriden in concrete sub-classes.</p>
     * @return object the component instance stub.
     */
    getStub: function() { return {}; },

    /**
     * Obtains a property usage for a given property type.
     * <p>
     * The base implementation satisfies legacy model classes -- 
     * the ones created directly in JavaScript, 
     * generally the case of CDF only component types.
     * </p>
     * @param alias the alias of the property type usage.
     * @return PropertyTypeUsage a property type usage, 
     * when there is one defined, or <tt>null</tt>, otherwise.
     */
    getPropertyUsage: function(alias) {
      return PropertyTypeUsage.create(alias, this); // may be null
    },
    
    getPropertyStubAndUsage: function(alias) {
      var usage = this.getPropertyUsage(alias);
      var type  = usage && usage.type;
      var isUndefinedProp;
      var isSpecialProp = (alias === 'Group');
      var stub;
      if(type) {
        stub = type.stub;
      } else {
        isUndefinedProp = !isSpecialProp;
        stub = {
          description: alias,
          tooltip:     alias
        };
      }

      // Add a ? or ?! prefix to the description, 
      // so that the user can realize that something is wrong.
      if(!isSpecialProp) {
        if(this.isUndefined) {
          if(type) { stub = $.extend({}, stub); }
          stub.description = "?! " + stub.description;
          stub.tooltip     = "The component type of property '" + alias + "' is not defined.";
          stub.classType = 'advanced';
        } else if(isUndefinedProp) {
          stub.description = "? " + stub.description;
          stub.tooltip     = "Property '" + alias + "' is not defined.";
          stub.classType = 'advanced';
        }
      }
      return [stub, usage];
    },

    /**
     * Creates a property instance for a given property type usage,
     * given its alias.
     * @param alias the alias of the property type usage.
     * @return object a property instance, 
     * when the property type usage is defined, or <tt>null</tt>, otherwise.
     */
    createProperty: function(alias) {
      var propUsage = this.getPropertyUsage(alias);
      return propUsage && propUsage.create();
    },

    /**
     * A dictionary of the registered model classes, 
     * indexed by their {@link MODEL} property.
     * @type object
     */
    models: {},
    
    /**
     * A dictionary of the registered model classes, 
     * indexed by their legacy names.
     * @type object
     */        
    modelsByLegacyName: {},
    
    /**
     * Registers a {@link BaseModel} sub-class that represents a model.
     * <p>The <tt>MODEL</tt> property has the model name.</p>
     * <p>The <tt>getStub</tt> method returns a stub 
     * for a new component of the corresponding type.
     * </p>
     * @param {function} modelClass the model class constructor function.
     * @return function the model class.
     */
    registerModel: function(modelClass) {
      this.models[modelClass.MODEL] = modelClass;
      
      // Index also by legacy names
      var legacyNames = modelClass.legacyNames;
      if(legacyNames) {
        for(var i = 0, L = legacyNames.length; i < L; i++) {
          this.modelsByLegacyName[legacyNames[i]] = modelClass;
        }
      }
      
      if(!modelClass.getPropertyUsage) {
        $.extend(modelClass, LegacyModel);
      }
      
      return modelClass;
    },

    /**
     * Obtains a model class given the value of its {@link MODEL} property.
     * @param string modelId the model id.
     * @param boolean [createIfUndefined=false] indicates if 
     * an undefined model class should be created when a model with the given name exists.
     * @return function the model class, if one exists, or <tt>undefined</tt>, otherwise.
     */
    getModel: function(modelId, createIfUndefined) {
      var ModelClass;
      if(modelId) {
        ModelClass = this.models[modelId];
        
        // Legacy naming
        if(!ModelClass) {
          // Remove any "Model" Suffix.
          var m = /^(.+?)Model$/.exec(modelId);
          if(m) { modelId = m[1]; }
          
          ModelClass = this.models[modelId];
          if(!ModelClass) {
            ModelClass = this.modelsByLegacyName[modelId];
          }
        }

        if(!ModelClass && createIfUndefined) {
          ModelClass = this.createUndefined(modelId);
        }
      }
      return ModelClass;
    },
      
    /**
     * Creates and registers a {@link BaseModel} sub-class, 
     * with appropriate static properties and methods,
     * given a model specification.
     * <p>
     * This is a helper method that supports 
     * CDE generated code for component definitions, 
     * allow for mostly declarative and concise 
     * descriptions of component types.
     * </p>
     * @param object spec the component type specification. 
     * <p>It has the following structure:</p>
     * <ul>
     *    <li>name - the name of the component type. 
     *        The value of the {@link BaseModel.MODEL} property of the generated sub-class.
     *        The name that component instances have in their <tt>type</tt> property.
     *    </li>
     *    <li>description - the description is the user-visible name of the component type.</li>
     *    <li>parent - where the component type is shown in 
     *        a dashboard's component tree (components' view).
     *        By default, the value of {@link IndexManager.ROOTID}.
     *    </li>
     *    <li>legacyNames - an array of legacy name strings.</li>
     *    <li>metas - an object with meta properties and their values. 
     *        These are properties that are named with the prefix "meta_", or, 
     *        possibly, named just "meta".
     *    </li>
     *    <li>properties - an array of property usage specifications. 
     *        See {@link PropertyTypeUsage.create} for the structure of this specification.
     *        Optional.
     *    </li>
     *    <li>baseModelClass - the base model class 
     *        from which to derive the new model class. Optional. Internal use.
     *    </li>
     * </ul>
     * @return function the custom component type model class.
     */
    create: function(spec) {
      var modelName   = spec.name;
      var modelDesc   = spec.description;
      var modelParent = spec.parent != null ? spec.parent : IndexManager.ROOTID;
      var modelMetas  = spec.metas;
      var modelLegacyNames = spec.legacyNames;
      var BaseModelClass   = spec.baseModelClass || CdeComponentModel;

      // Extend the base BaseModelClass class.
      var ModelClass = BaseModelClass.extend({}, {
        MODEL:       modelName,
        legacyNames: modelLegacyNames,
        description: modelDesc,
        parent:      modelParent,
        
        // Store the property specs for later lazy compilation by CdeComponentModel._getPropertyUsages.
        _propertySpecs: spec.properties || [],
        
        getStub: function() {
          return $.extend({
              id:         TableManager.generateGUID(),
              type:       modelName,
              typeDesc:   modelDesc,
              parent:     modelParent,
              properties: this._getPropertyUsages().map(function(pu) { return pu.create(); })
            }, modelMetas);
        }
      });
  
      // Register the just created model class and return it.
      return this.registerModel(ModelClass);
    },

    /**
     * Creates and registers a model class for 
     * an undefined component type, given its name.
     * <p>Having a dynamically generated model class helps
     *    the editor preserve information of unknown component types,
     *    and work with defined properties as much as possible.
     *    This can be dangerous, however, if a property alias used
     *    in the component definition is also the name of an existing global property type.
     * </p>
     * <p>An undefined component type can result from a coding bug, or, 
     *    from a custom component not being read by CDE, generally
     *    due to a CDE configuration problem or CDE plugin installation.
     * </p>
     * @return function the undefined component type model class.
     */
    createUndefined: function(name) {
      var description;
      switch(name) {
        case 'Label':
        case 'Group': description = "<i>Group</i>"; break;
        default: 
          // Give a clue that the component is not defined.
          description = "? " + name;
      }

      return this.create({
        name: name,
        description:    description,
        baseModelClass: UndefinedCdeComponentModel
      });
    }
  }); // End BaseModel
  
  BM.extend = staticExtend;

  /**
   * The base model class for CDE generated model classes.
   * <p>This class is used internally, by {@link BaseModel.create}.</p>
   * @class CdeComponentModel
   * @extends BaseModel
   * @see BaseModel.create
   */
  var CdeComponentModel = BM.extend({}, /** @lends CdeComponentModel */{
    /** @private */
    _addPropertyUsage: function(propUsage) {
      this._properties.push(propUsage);
      this._propertiesByAlias[propUsage.alias] = propUsage;
      this._propertiesByName [propUsage.name ] = propUsage;
      return propUsage;
    },
    
    /** @private */
    _addPropSpec: function(propSpec) {
      var propUsage = PropertyTypeUsage.create(propSpec, this);
      return propUsage && this._addPropertyUsage(propUsage);
    },
    
    /** @private */
    _getPropertyUsages: function() {
      if(this._propertySpecs) {
        this._propertiesByAlias = {};
        this._propertiesByName  = {};
        this._properties = [];
        
        this._propertySpecs.forEach(this._addPropSpec, this);
        
        delete this._propertySpecs;
      }

      return this._properties;
    },

    /** @override */
    getPropertyUsage: function(name) {
      // Lazy init
      if(this._propertySpecs) { this._getPropertyUsages(); }
      
      return this._propertiesByAlias[name] || 
             this._propertiesByName[name]  ||
             // Some component properties are extension properties --
             // are not defined in the component type, 
             // because they are deprecated or this is an undefined component model --
             // try to find a global definition for them, anyway.
             // If found, the property is dynamically registered in 
             // the component type's properties.
             this._addPropSpec({name: name, isExtension: true});
      }
  }); // End CdeComponentModel
      
  var UndefinedCdeComponentModel = CdeComponentModel.extend({}, {
    isUndefined: true
  });
      
  return BM;
}());


var CellOperations = Base.extend({

    logger: {},

    constructor: function(){
      this.logger = new Logger("BaseType");
    }
},{

    operations: [],


    // After defining an operation. we need to register it
    registerOperation: function(operation){
      this.operations.push(operation);
    },

    getOperationsByType: function(type){
    
      var _operations = [];

      $.each(CellOperations.operations,function(i,value){
      
          for(var i in value.types){
            if(value.types.hasOwnProperty(i)){
              if(type.match("^"+value.types[i])){
                _operations.push(value);
              }
            }
          }
        });
      return _operations;
    }

});

var BaseOperation = Base.extend({

    id: "BASE_OPERATION",
    types: ["TYPE"],
    name: "Base operation",
    description: "Base Operation description",
    order: 20,
    logger: {},
    hoverIcon: null, //icon to display on hover
    clickIcon: null, //icon while clicking
    showInactiveIcon: false, //show icon when !canExecute

    execute: function(tableManager){
      this.logger.error("Method not implemented; " + tableManager.getTableId() + "; " + tableManager.getSelectedCell());
    },

    canExecute: function(tableModel){
    
      return true;
    },

    constructor: function(){
      this.logger = new Logger("BaseOperation");
    },

    getHtml: function(tableManager,idx){

      var tableManagerId = tableManager.getTableId();
      var code ='';

      if (this.canExecute(tableManager)){
        code = '<a class="tooltip '+this.getId().toLowerCase()+' tableOperation" title="' + this.getName() + '"  href="javascript:TableManager.executeOperation(\'' + tableManagerId + '\','+ idx+');">\n</a>';
      }

      return code;
    
    },

    getId: function(){return this.id},
    setId: function(id){this.id = id},
    getName: function(){return this.name},
    setName: function(name){this.name = name},
    getDescription: function(){return this.description},
    setDescription: function(description){this.description = description}

});


var AddRowOperation = BaseOperation.extend({

    id: "ADD_ROW",
    types: ["GenericRow"],
    name: "New Row",
    description: "Adds a new row to the layout on the specific position",

    constructor: function(){
      this.logger = new Logger("AddRowOperation");
    }

});

CellOperations.registerOperation(new AddRowOperation());

var MoveUpOperation = BaseOperation.extend({

    id: "MOVE_UP",
    types: ["GenericMoveUp"],
    name: "Move Up",
    description: "Move up",

    constructor: function(){
      this.logger = new Logger("MoveUpOperation");
    },

    canExecute: function(tableManager){
    
      var rowIdx = tableManager.getSelectedCell()[0];
      var rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);

      return !tableManager.getTableModel().getIndexManager().isFirstChild(rowId);

    },

    execute: function(tableManager){

      // Move up: move the selected node and all children
      // up to the previous item

      var rowIdx = tableManager.getSelectedCell()[0];
      var colIdx = tableManager.getSelectedCell()[1];
      var rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);

      var fromIdx = rowIdx;
      var toIdx = -1;

      var nextSibling = tableManager.getTableModel().getIndexManager().getNextSibling(rowId);
      if (typeof nextSibling == 'undefined'){
        toIdx = tableManager.getTableModel().getIndexManager().getLastChild(rowId).index;
      }
      else{
        toIdx = nextSibling.index - 1;
      }
      var targetIdx = tableManager.getTableModel().getIndexManager().getPreviousSibling(rowId).index;

      this.logger.debug("Moving nodes from " + fromIdx + " to " + toIdx + " to the place of " + targetIdx);

      // Build a new data array
      var _data = tableManager.getTableModel().getData();
      var _tmp = _data.splice(fromIdx, toIdx-fromIdx + 1);

      _data.splice(targetIdx,0)
      Array().splice.apply(_data,[targetIdx,0].concat(_tmp));
      //_data = _data.slice(0,targetIdx).concat(_tmp).concat(_data.slice(targetIdx));
      tableManager.getTableModel().setData(_data);

      // Now do the same on the UI

      // move rows id: fromIdx -> toIdx to targetIdx
      for(var i = 0; i<= toIdx-fromIdx; i++){
        $('#'+tableManager.getTableId() + " > tbody > tr:eq("+ (targetIdx + i) +")").before(
          $('#'+tableManager.getTableId() + " > tbody > tr:eq("+ (fromIdx + i) +")")
        );
      }

      tableManager.setSelectedCell([targetIdx,colIdx]);
      tableManager.updateOperations();
      
      
      /*
      tableManager.cleanSelections();
      tableManager.init();
      tableManager.selectCell(targetIdx,colIdx);
      */

      var a = [];
      $.each(_data,function(i,row){
        a.push(row.id);
      })
      this.logger.debug("Result: " + a.join(', '));
    
    }

});

CellOperations.registerOperation(new MoveUpOperation());

var MoveDownOperation = BaseOperation.extend({

    id: "MOVE_DOWN",
    types: ["GenericMoveDown"],
    name: "Move Down",
    description: "Move down",

    constructor: function(){
      this.logger = new Logger("MoveDownOperation");
    },

    canExecute: function(tableManager){
    
      var rowIdx = tableManager.getSelectedCell()[0];
      var rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);

      return !tableManager.getTableModel().getIndexManager().isLastChild(rowId);

    },


    execute: function(tableManager){

      // Move up: move the selected node and all children
      // up to the previous item

      var rowIdx = tableManager.getSelectedCell()[0];
      var colIdx = tableManager.getSelectedCell()[1];
      var rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);

      var fromIdx = rowIdx;
      var toIdx = -1;

      var indexManager = tableManager.getTableModel().getIndexManager();
      var nextSibling = indexManager.getNextSibling(rowId);
      if (typeof nextSibling == 'undefined'){
        toIdx = indexManager.getLastChild(rowId).index;
      }
      else{
        toIdx = nextSibling.index - 1;
      }
      var targetIdx = parseFloat(indexManager.getLastChild(indexManager.getNextSibling(rowId).id).index);

      this.logger.debug("Moving nodes from " + fromIdx + " to " + toIdx + " to the place of " + targetIdx);

      // Build a new data array
      var _data = tableManager.getTableModel().getData();
      var _tmp = _data.splice(fromIdx, toIdx-fromIdx + 1);

      Array().splice.apply(_data,[targetIdx-toIdx+fromIdx,0].concat(_tmp));
      //_data = _data.slice(0,targetIdx-toIdx+fromIdx).concat(_tmp).concat(_data.slice(targetIdx-toIdx+fromIdx));
      tableManager.getTableModel().setData(_data);

      // Now do the same on the UI

      // move rows id: fromIdx -> toIdx to targetIdx
      for(var i = 0; i<= toIdx-fromIdx; i++){
        $('#'+tableManager.getTableId() + " > tbody > tr:eq("+ (targetIdx) +")").after(
          $('#'+tableManager.getTableId() + " > tbody > tr:eq("+ (fromIdx) +")")
        );
      }

      tableManager.setSelectedCell([targetIdx-toIdx+fromIdx,colIdx]);
      tableManager.updateOperations();
      
      /*
      tableManager.cleanSelections();
      tableManager.init();
      tableManager.selectCell(targetIdx-toIdx+fromIdx,colIdx);
      */

      var a = [];
      $.each(_data,function(i,row){
        a.push(row.id);
      })
      this.logger.debug("Result: " + a.join(', '));
    
    }

});

CellOperations.registerOperation(new MoveDownOperation());

var DeleteOperation = BaseOperation.extend({

    id: "Delete",
    types: ["GenericDelete"],
    name: "Delete",
    description: "Delete",

    constructor: function(){
      this.logger = new Logger("DeleteOperation");
    },


    execute: function(tableManager){

      // Move up: move the selected node and all children
      // up to the previous item

      var rowIdx = tableManager.getSelectedCell()[0];
      var colIdx = tableManager.getSelectedCell()[1];
      var rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);

      var fromIdx = rowIdx;
      var toIdx = -1;

      var indexManager = tableManager.getTableModel().getIndexManager();
      var nextSibling = indexManager.getNextSibling(rowId);
      if (typeof nextSibling == 'undefined'){
        toIdx = indexManager.getLastChild(rowId).index;
      }
      else{
        toIdx = nextSibling.index - 1;
      }
      
      // Store the parent to update the table
      var _parentId = indexManager.getIndex()[rowId].parent;
      
      //check if last in group, except in layout
      var deleteParent = tableManager.id != LayoutPanel.TREE &&
                         _parentId != IndexManager.ROOTID && 
                         indexManager.isFirstChild(rowId) && 
                         indexManager.isLastChild(rowId);
      if(deleteParent){
        //start deleting in parent
        fromIdx = indexManager.getIndex()[_parentId].index;
        //update grandpa
        _parentId = indexManager.getIndex()[_parentId].parent;
      }
      
      this.logger.debug("Deleting nodes from " + fromIdx + " to " + toIdx );
      
      // Build a new data array
      tableManager.getTableModel().getData().splice(fromIdx, toIdx-fromIdx + 1);
      indexManager.updateIndex();


      // Now do the same on the UI

      // move rows id: fromIdx -> toIdx to targetIdx
      for(var i = 0; i<= toIdx-fromIdx; i++){
        $('#'+tableManager.getTableId() + " > tbody > tr:eq("+ (fromIdx) +")").remove();
      }

      tableManager.cellUnselected();
      
      /*
      tableManager.cleanSelections();
      tableManager.init();
      tableManager.selectCell(targetIdx-toIdx+fromIdx,colIdx);
      */

      var a = [];
      $.each(tableManager.getTableModel().getData(),function(i,row){
        a.push(row.id);
      })
      this.logger.debug("Result: " + a.join(', '));

      // Update treeTable:
      tableManager.updateTreeTable(_parentId);
    
    }

});

CellOperations.registerOperation(new DeleteOperation());


var ApplyTemplateOperation = BaseOperation.extend({

    id: "APPLY_TEMPLATE",
    types: ["GenericApplyTemplate"],
    name: "Apply Template",
    description: "Applies a template.",

    constructor: function(){
      this.logger = new Logger("ApplyTemplateOperation");
    }

});

CellOperations.registerOperation(new ApplyTemplateOperation());

var SaveAsTemplateOperation = BaseOperation.extend({

    id: "SAVEAS_TEMPLATE",
    types: ["GenericSaveAsTemplate"],
    name: "Save as Template",
    description: "Save as template.",

    constructor: function(){
      this.logger = new Logger("SaveAsTemplateOperation");
    }

});

CellOperations.registerOperation(new SaveAsTemplateOperation());
