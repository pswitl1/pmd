/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.properties;

import static net.sourceforge.pmd.properties.ValueParserConstants.DOUBLE_PARSER;

import net.sourceforge.pmd.properties.builders.PropertyDescriptorBuilderConversionWrapper;
import net.sourceforge.pmd.properties.builders.SingleNumericPropertyBuilder;


/**
 * Defines a property type that support single double-type property values within an upper and lower boundary.
 *
 * @author Brian Remedios
 * @version Refactored June 2017 (6.0.0)
 */
public final class DoubleProperty extends AbstractNumericProperty<Double> {


    /**
     * Constructor for DoubleProperty that limits itself to a single value within the specified limits. Converts string
     * arguments into the Double values.
     *
     * @param theName        Name
     * @param theDescription Description
     * @param minStr         Minimum value of the property
     * @param maxStr         Maximum value of the property
     * @param defaultStr     Default value
     * @param theUIOrder     UI order
     *
     * @throws IllegalArgumentException if min > max or one of the defaults is not between the bounds
     * @deprecated will be removed in 7.0.0
     */
    public DoubleProperty(String theName, String theDescription, String minStr, String maxStr, String defaultStr,
                          float theUIOrder) {
        this(theName, theDescription, doubleFrom(minStr), doubleFrom(maxStr), doubleFrom(defaultStr), theUIOrder, false);
    }


    /** Master constructor. */
    private DoubleProperty(String theName, String theDescription, Double min, Double max, Double theDefault,
                           float theUIOrder, boolean isDefinedExternally) {
        super(theName, theDescription, min, max, theDefault, theUIOrder, isDefinedExternally);
    }


    /**
     * Constructor that limits itself to a single value within the specified limits.
     *
     * @param theName        Name
     * @param theDescription Description
     * @param min            Minimum value of the property
     * @param max            Maximum value of the property
     * @param theDefault     Default value
     * @param theUIOrder     UI order
     *
     * @throws IllegalArgumentException if min > max or one of the defaults is not between the bounds
     */
    public DoubleProperty(String theName, String theDescription, Double min, Double max, Double theDefault,
                          float theUIOrder) {
        this(theName, theDescription, min, max, theDefault, theUIOrder, false);
    }


    @Override
    public Class<Double> type() {
        return Double.class;
    }


    @Override
    protected Double createFrom(String value) {
        return doubleFrom(value);
    }


    /**
     * Parses a String into a Double.
     *
     * @param numberString String to parse
     *
     * @return Parsed Double
     */
    private static Double doubleFrom(String numberString) {
        return DOUBLE_PARSER.valueOf(numberString);
    }


    static PropertyDescriptorBuilderConversionWrapper.SingleValue.Numeric<Double, DoublePBuilder> extractor() {
        return new PropertyDescriptorBuilderConversionWrapper.SingleValue.Numeric<Double, DoublePBuilder>(Double.class, ValueParserConstants.DOUBLE_PARSER) {
            @Override
            protected DoublePBuilder newBuilder(String name) {
                return new DoublePBuilder(name);
            }
        };
    }


    public static DoublePBuilder builder(String name) {
        return new DoublePBuilder(name);
    }


    public static final class DoublePBuilder extends SingleNumericPropertyBuilder<Double, DoublePBuilder> {
        private DoublePBuilder(String name) {
            super(name);
        }


        @Override
        public DoubleProperty build() {
            return new DoubleProperty(name, description, lowerLimit, upperLimit, defaultValue, uiOrder, isDefinedInXML);
        }
    }

}
