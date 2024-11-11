/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cdf.dd.model.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.validation.DuplicateAttributeError;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.util.JsonUtils;

/**
 * A type of Property.
 */
public class PropertyType extends MetaObject {
  protected static Log _logger = LogFactory.getLog( PropertyType.class );

  public static enum ValueType {
    STRING, BOOLEAN, NUMBER, FUNCTION, ARRAY, QUERY, LITERAL, VOID
  }

  public static final String CAT_ADVANCED = "advanced";
  public static final String CAT_ADVANCED_DESC = "Advanced";
  public static final String DEF_BASE_TYPE = "BaseProperty";
  public static final ValueType DEF_VALUE_TYPE = ValueType.STRING;
  public static final String DEF_NONLIST_INPUTTYPE = "String";

  private final ComponentType _owner; // null for shared properties, otherwise, contains the owner component.
  private final String _base;
  private final ValueType _valueType; // also known as OutputType
  private final String _defaultValue;
  private final String _inputType;
  private final int _order;
  private final String _possibleValuesSource;
  private final Map<String, LabeledValue> _possibleValuesByValue;

  @SuppressWarnings( "OverridableMethodCallInConstructor" )
  protected PropertyType( Builder builder, ComponentType owner ) throws ValidationException {
    super( builder );

    this._owner = owner; // may be null
    this._base = StringUtils.isEmpty( builder._base ) ? DEF_BASE_TYPE : builder._base;
    this._valueType = builder._valueType == null ? DEF_VALUE_TYPE : builder._valueType;
    this._defaultValue = processDefaultValue( this._valueType, builder._defaultValue );
    this._order = builder._order;

    if ( StringUtils.isNotEmpty( builder._possibleValuesSource ) ) {
      if ( builder.getPossibleValueCount() > 0 ) {
        _logger.warn( String.format(
          "PropertyType '%s' has a possible values source specified. Ignoring provided static values list.",
          this.getName() ) );
      }

      this._possibleValuesSource = builder._possibleValuesSource;
    } else {
      this._possibleValuesSource = null;
    }

    if ( this._possibleValuesSource == null && builder.getPossibleValueCount() > 0 ) {
      this._possibleValuesByValue = new LinkedHashMap<String, LabeledValue>();

      for ( LabeledValue.Builder labeledValueBuilder : builder._possibleValues ) {
        LabeledValue labeledValue = labeledValueBuilder.build();
        if ( this._possibleValuesByValue.containsKey( labeledValue.getValue() ) ) {
          // Ignore LabeledValue, log warning and continue.
          // TODO: error class should not be this one?!
          _logger.warn( new DuplicateAttributeError( labeledValue.getValue() ) );
          continue;
        }

        this._possibleValuesByValue.put( labeledValue.getValue(), labeledValue );
      }
    } else {
      this._possibleValuesByValue = null;
    }

    String inputType = builder._inputType;
    if ( StringUtils.isEmpty( inputType ) ) {
      if ( this._possibleValuesByValue == null && this._possibleValuesSource == null ) {
        inputType = DEF_NONLIST_INPUTTYPE;
      } else {
        inputType = this.getName() + "Custom"; // Cannot have spaces or special chars or JS renderer stuff wont work?
      }
    }

    this._inputType = inputType;
  }

  private String processDefaultValue( ValueType valueType, String defaultValue ) {
    if ( StringUtils.isEmpty( defaultValue ) ) {
      switch ( valueType ) {
        case ARRAY:
          defaultValue = "\"[]\""; // TODO: What the heck? "[]" Why the "??
          break;

        case NUMBER:
        case BOOLEAN:
        case FUNCTION:
        case STRING:
          defaultValue = "\"\"";  // ""
          break;

        default: defaultValue = ""; // undefined?
      }
    } else {
      switch ( valueType ) {
        // TODO: normalize other default values?
        // Like: "true" -> true, ...

        // TODO: Should "foo" remain "foo", instead of being converted to "\"foo\""?
        case STRING:
          defaultValue = JsonUtils.toJsString( defaultValue );
          break;
      }
    }

    return defaultValue;
  }

  @Override
  public String getKind() {
    return KnownThingKind.PropertyType;
  }

  // ----------
  // Simple Properties
  //  public final String getFullName()
  //  {
  //    String fullName = this.getName();
  //
  //    ComponentType owner = this.getOwner();
  //    if(owner != null)
  //    {
  //      fullName = owner.getCamelName() + "_" + fullName;
  //    }
  //
  //    return fullName;
  //  }

  public final ComponentType getOwner() {
    return this._owner;
  }

  public final String getBase() {
    return this._base;
  }

  public final String getDefaultValue() {
    return this._defaultValue;
  }

  public final String getInputType() {
    return this._inputType;
  }

  public final ValueType getValueType() {
    return this._valueType;
  }

  public int getOrder() {
    return this._order;
  }

  public final boolean isAdvanced() {
    return CAT_ADVANCED.equals( this.getCategory() );
  }


  public String getPossibleValuesSource() {
    return this._possibleValuesSource;
  }

  // ---------------
  // Possible Values
  public LabeledValue getPossibleValue( String value ) {
    if ( StringUtils.isEmpty( value ) ) {
      throw new IllegalArgumentException( "value" );
    }

    LabeledValue possibleValue = this._possibleValuesByValue != null ? this._possibleValuesByValue.get( value ) : null;
    if ( possibleValue == null ) {
      throw new IllegalArgumentException( "There is no possible value with a value of '" + value + "'." );
    }

    return possibleValue;
  }

  public Iterable<LabeledValue> getPossibleValues() {
    return this._possibleValuesByValue != null ? this._possibleValuesByValue.values() : Collections.<LabeledValue>emptyList();
  }

  public int getPossibleValueCount() {
    return this._possibleValuesByValue != null ? this._possibleValuesByValue.size() : 0;
  }

  /**
   * Class to create and modify PropertyType instances.
   */
  public static class Builder extends MetaObject.Builder {
    private String _base;
    private ValueType _valueType;
    private String _defaultValue;
    private String _inputType;
    private int _order;

    private String _possibleValuesSource;
    private List<LabeledValue.Builder> _possibleValues;

    public Builder() {
      super();
    }

    // ----------
    // Simple Properties
    public ValueType getValueType() {
      return this._valueType;
    }

    public Builder setValueType( ValueType valueType ) {
      this._valueType = valueType;
      return this;
    }

    public String getBase() {
      return this._base;
    }

    public Builder setBase( String base ) {
      this._base = base;
      return this;
    }

    public String getDefaultValue() {
      return this._defaultValue;
    }

    public Builder setDefaultValue( String defaultValue ) {
      this._defaultValue = defaultValue;
      return this;
    }

    public String getInputType() {
      return this._inputType;
    }

    public Builder setInputType( String inputType ) {
      this._inputType = inputType;
      return this;
    }

    public int getOrder() {
      return this._order;
    }

    public Builder setOrder( int order ) {
      this._order = order;
      return this;
    }

    public String getPossibleValuesSource() {
      return this._possibleValuesSource;
    }

    public Builder setPossibleValuesSource( String source ) {
      this._possibleValuesSource = StringUtils.isEmpty( source ) ? null : source;
      return this;
    }

    public MetaObject.Builder addPossibleValue( LabeledValue.Builder possibleValue ) {
      if ( possibleValue == null ) {
        throw new IllegalArgumentException( "possibleValue" );
      }

      if ( this._possibleValues == null ) {
        this._possibleValues = new ArrayList<LabeledValue.Builder>();
      }

      this._possibleValues.add( possibleValue );

      return this;
    }

    public MetaObject.Builder addPossibleValue( String value, String label ) {
      return this.addPossibleValue( new LabeledValue.Builder()
        .setValue( value )
        .setLabel( label ) );
    }

    public Iterable<LabeledValue.Builder> getPossibleValues() {
      return this._possibleValues != null ? this._possibleValues : Collections.<LabeledValue.Builder>emptyList();
    }

    public int getPossibleValueCount() {
      return this._possibleValues != null ? this._possibleValues.size() : 0;
    }

    // ---------

    public PropertyType build( ComponentType owner ) throws ValidationException {
      return new PropertyType( this, owner );
    }

    public PropertyType build() throws ValidationException {
      return new PropertyType( this, null );
    }
  }
}
