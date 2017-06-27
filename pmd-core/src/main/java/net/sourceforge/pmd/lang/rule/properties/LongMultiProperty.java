/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule.properties;

import static net.sourceforge.pmd.lang.rule.properties.AbstractNumericProperty.NUMBER_FIELD_TYPES_BY_KEY;
import static net.sourceforge.pmd.lang.rule.properties.factories.ValueParser.LONG_PARSER;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.sourceforge.pmd.PropertyDescriptorFactory;
import net.sourceforge.pmd.PropertyDescriptorField;
import net.sourceforge.pmd.lang.rule.properties.factories.BasicPropertyDescriptorFactory;

/**
 * Multi-valued long property.
 *
 * @author Brian Remedios
 * @version Refactored June 2017 (6.0.0)
 */
public final class LongMultiProperty extends AbstractMultiNumericProperty<Long> {

    /** Factory. */
    public static final PropertyDescriptorFactory FACTORY // @formatter:off
        = new BasicPropertyDescriptorFactory<List<Long>>(Long.class, NUMBER_FIELD_TYPES_BY_KEY) {
            @Override
            public LongMultiProperty createWith(Map<PropertyDescriptorField, String> valuesById) {
                String[] minMax = minMaxFrom(valuesById);
                char delimiter = delimiterIn(valuesById, DEFAULT_NUMERIC_DELIMITER);
                List<Long> defaultValues = parsePrimitives(defaultValueIn(valuesById), delimiter, LONG_PARSER);
                return new LongMultiProperty(nameIn(valuesById),
                                             descriptionIn(valuesById),
                                             LONG_PARSER.valueOf(minMax[0]),
                                             LONG_PARSER.valueOf(minMax[1]),
                                             defaultValues, 0f);
            }
        }; // @formatter:on


    /**
     * Constructor using an array of defaults.
     *
     * @param theName        Name
     * @param theDescription Description
     * @param min            Minimum value of the property
     * @param max            Maximum value of the property
     * @param defaultValues  Array of defaults
     * @param theUIOrder     UI order
     *
     * @throws IllegalArgumentException if min > max or one of the defaults is not between the bounds
     */
    public LongMultiProperty(String theName, String theDescription, Long min, Long max,
                             Long[] defaultValues, float theUIOrder) {
        super(theName, theDescription, min, max, Arrays.asList(defaultValues), theUIOrder);
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
     *
     * @throws IllegalArgumentException if min > max or one of the defaults is not between the bounds
     */
    public LongMultiProperty(String theName, String theDescription, Long min, Long max,
                             List<Long> defaultValues, float theUIOrder) {
        super(theName, theDescription, min, max, defaultValues, theUIOrder);
    }


    @Override
    public Class<Long> type() {
        return Long.class;
    }


    @Override
    protected Long createFrom(String value) {
        return Long.valueOf(value);
    }

}
