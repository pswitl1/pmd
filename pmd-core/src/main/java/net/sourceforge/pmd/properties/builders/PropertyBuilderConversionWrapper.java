/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.properties.builders;


import static net.sourceforge.pmd.properties.PropertyDescriptorField.DELIMITER;
import static net.sourceforge.pmd.properties.PropertyDescriptorField.LEGAL_PACKAGES;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import net.sourceforge.pmd.properties.MultiValuePropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyDescriptorField;
import net.sourceforge.pmd.properties.ValueParser;
import net.sourceforge.pmd.properties.ValueParserConstants;


/**
 * Wraps a property builder and maps its inputs from strings to the target types of the descriptor.
 *
 * @param <E> Value type of the descriptor
 * @param <T> Concrete type of the underlying builder
 * @author Clément Fournier
 * @since 6.0.0
 */
public abstract class PropertyBuilderConversionWrapper<E, T extends PropertyDescriptorBuilder<E, T>> {

    private final Class<?> valueType;


    protected PropertyBuilderConversionWrapper(Class<?> valueType) {
        this.valueType = valueType;
    }


    /** Populates the builder with extracted fields. To be overridden. */
    protected void populate(T builder, Map<PropertyDescriptorField, String> fields) {
        builder.desc(fields.get(PropertyDescriptorField.DESCRIPTION));
        builder.uiOrder(Float.parseFloat(fields.get(PropertyDescriptorField.UI_ORDER)));
    }


    public abstract boolean isMultiValue();


    public Class<?> valueType() {
        return valueType;
    }


    protected abstract T newBuilder(); // FUTURE 1.8: use a Supplier constructor parameter


    /**
     * Gets a property builder populated with the given fields.
     *
     * @param fields Key value pairs
     * @return A builder
     */
    public T getBuilder(Map<PropertyDescriptorField, String> fields) {
        T builder = newBuilder();
        populate(builder, fields);
        builder.isDefinedInXML = true;
        return builder;
    }


    protected static String[] legalPackageNamesIn(Map<PropertyDescriptorField, String> valuesById, char delimiter) {
        String names = valuesById.get(LEGAL_PACKAGES);
        if (StringUtils.isBlank(names)) {
            return null;
        }
        return StringUtils.split(names, delimiter);
    }


    private static char delimiterIn(Map<PropertyDescriptorField, String> valuesById, char defaultDelimiter) {
        String characterStr = "";
        if (valuesById.containsKey(DELIMITER)) {
            characterStr = valuesById.get(DELIMITER).trim();
        }

        if (StringUtils.isBlank(characterStr)) {
            return defaultDelimiter;
        }

        if (characterStr.length() != 1) {
            throw new RuntimeException("Ambiguous delimiter character, must have length 1: \"" + characterStr + "\"");
        }
        return characterStr.charAt(0);
    }


    /**
     * For multi-value properties.
     *
     * @param <V> Element type of the list
     * @param <T> Concrete type of the underlying builder
     */
    public abstract static class MultiValue<V, T extends MultiValuePropertyBuilder<V, T>>
            extends PropertyBuilderConversionWrapper<List<V>, T> {

        protected final ValueParser<V> parser;


        protected MultiValue(Class<V> valueType, ValueParser<V> parser) {
            super(valueType);
            this.parser = parser;
        }


        @Override
        protected void populate(T builder, Map<PropertyDescriptorField, String> fields) {
            super.populate(builder, fields);
            char delim = delimiterIn(fields, builder.multiValueDelimiter);
            builder.delim(delim);
            builder.deft(ValueParserConstants.multi(parser, delim)
                                             .valueOf(fields.get(PropertyDescriptorField.DEFAULT_VALUE)));
        }


        @Override
        public boolean isMultiValue() {
            return true;
        }


        /**
         * For multi-value numeric properties.
         *
         * @param <V> Element type of the list
         * @param <T> Concrete type of the underlying builder
         */
        public abstract static class Numeric<V, T extends MultiNumericPropertyBuilder<V, T>>
                extends MultiValue<V, T> {

            protected Numeric(Class<V> valueType, ValueParser<V> parser) {
                super(valueType, parser);
            }


            @Override
            protected void populate(T builder, Map<PropertyDescriptorField, String> fields) {
                super.populate(builder, fields);
                builder.min(parser.valueOf(fields.get(PropertyDescriptorField.MIN)));
                builder.max(parser.valueOf(fields.get(PropertyDescriptorField.MAX)));
            }
        }


        /**
         * For single-value packaged properties.
         *
         * @param <V> Element type of the list
         * @param <T> Concrete type of the underlying builder
         */
        public abstract static class Packaged<V, T extends MultiPackagedPropertyBuilder<V, T>>
                extends MultiValue<V, T> {

            protected Packaged(Class<V> valueType, ValueParser<V> parser) {
                super(valueType, parser);
            }


            @Override
            protected void populate(T builder, Map<PropertyDescriptorField, String> fields) {
                super.populate(builder, fields);
                builder.legalPackageNames(legalPackageNamesIn(fields, PropertyBuilderConversionWrapper.delimiterIn(fields,
                        MultiValuePropertyDescriptor.DEFAULT_DELIMITER)));
            }
        }

    }


    /**
     * For single-value properties.
     *
     * @param <E> Value type of the property
     * @param <T> Concrete type of the underlying builder
     */
    public abstract static class SingleValue<E, T extends SingleValuePropertyBuilder<E, T>>
            extends PropertyBuilderConversionWrapper<E, T> {

        protected final ValueParser<E> parser;


        protected SingleValue(Class<E> valueType, ValueParser<E> parser) {
            super(valueType);
            this.parser = parser;
        }


        @Override
        protected void populate(T builder, Map<PropertyDescriptorField, String> fields) {
            super.populate(builder, fields);
            builder.deft(parser.valueOf(fields.get(PropertyDescriptorField.DEFAULT_VALUE)));
        }


        @Override
        public boolean isMultiValue() {
            return false;
        }


        /**
         * For single-value numeric properties.
         *
         * @param <E> Element type of the list
         * @param <T> Concrete type of the underlying builder
         */
        public abstract static class Numeric<E, T extends SingleNumericPropertyBuilder<E, T>>
                extends SingleValue<E, T> {

            protected Numeric(Class<E> valueType, ValueParser<E> parser) {
                super(valueType, parser);
            }


            @Override
            protected void populate(T builder, Map<PropertyDescriptorField, String> fields) {
                super.populate(builder, fields);
                builder.min(parser.valueOf(fields.get(PropertyDescriptorField.MIN)));
                builder.max(parser.valueOf(fields.get(PropertyDescriptorField.MAX)));
            }
        }


        /**
         * For single-value packaged properties.
         *
         * @param <E> Element type of the list
         * @param <T> Concrete type of the underlying builder
         */
        public abstract static class Packaged<E, T extends SinglePackagedPropertyBuilder<E, T>>
                extends SingleValue<E, T> {

            protected Packaged(Class<E> valueType, ValueParser<E> parser) {
                super(valueType, parser);
            }


            @Override
            protected void populate(T builder, Map<PropertyDescriptorField, String> fields) {
                super.populate(builder, fields);
                builder.legalPackageNames(legalPackageNamesIn(fields, PropertyBuilderConversionWrapper.delimiterIn(fields,
                        MultiValuePropertyDescriptor.DEFAULT_DELIMITER)));
            }
        }


    }

}
