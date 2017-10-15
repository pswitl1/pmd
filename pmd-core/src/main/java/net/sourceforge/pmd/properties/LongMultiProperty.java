/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.properties;

import java.util.Arrays;
import java.util.List;

import net.sourceforge.pmd.properties.builders.MultiNumericPropertyBuilder;
import net.sourceforge.pmd.properties.builders.PropertyBuilderConversionWrapper;


/**
 * Multi-valued long property.
 *
 * @author Brian Remedios
 * @version Refactored June 2017 (6.0.0)
 */
public final class LongMultiProperty extends AbstractMultiNumericProperty<Long> {


    /**
     * Constructor using an array of defaults.
     *
     * @param theName        Name
     * @param theDescription Description
     * @param min            Minimum value of the property
     * @param max            Maximum value of the property
     * @param defaultValues  Array of defaults
     * @param theUIOrder     UI order
     * @throws IllegalArgumentException if min > max or one of the defaults is not between the bounds
     */
    public LongMultiProperty(String theName, String theDescription, Long min, Long max,
                             Long[] defaultValues, float theUIOrder) {
        this(theName, theDescription, min, max, Arrays.asList(defaultValues), theUIOrder, false);
    }


    /** Master constructor. */
    private LongMultiProperty(String theName, String theDescription, Long min, Long max,
                              List<Long> defaultValues, float theUIOrder, boolean isDefinedExternally) {
        super(theName, theDescription, min, max, defaultValues, theUIOrder, isDefinedExternally);
    }


    /**
     * Constructor using a list of defaults.
     *
     * @param theName        Name
     * @param theDescription Description
     * @param min            Minimum value of the property
     * @param max            Maximum value of the property
     * @param defaultValues  List of defaults
     * @param theUIOrder     UI order
     * @throws IllegalArgumentException if min > max or one of the defaults is not between the bounds
     */
    public LongMultiProperty(String theName, String theDescription, Long min, Long max,
                             List<Long> defaultValues, float theUIOrder) {
        this(theName, theDescription, min, max, defaultValues, theUIOrder, false);
    }


    @Override
    public Class<Long> type() {
        return Long.class;
    }


    @Override
    protected Long createFrom(String value) {
        return Long.valueOf(value);
    }


    static PropertyBuilderConversionWrapper.MultiValue.Numeric<Long, LongMultiPBuilder> extractor() {
        return new PropertyBuilderConversionWrapper.MultiValue.Numeric<Long, LongMultiPBuilder>(Long.class, ValueParserConstants.LONG_PARSER) {
            @Override
            protected LongMultiPBuilder newBuilder() {
                return new LongMultiPBuilder();
            }
        };
    }


    public static LongMultiPBuilder builder(String name) {
        return new LongMultiPBuilder().name(name);
    }


    private static final class LongMultiPBuilder
            extends MultiNumericPropertyBuilder<Long, LongMultiPBuilder> {

        @Override
        protected LongMultiProperty createInstance() {
            return new LongMultiProperty(name, description, lowerLimit, upperLimit,
                    defaultValues, uiOrder, isDefinedInXML);
        }
    }


}
