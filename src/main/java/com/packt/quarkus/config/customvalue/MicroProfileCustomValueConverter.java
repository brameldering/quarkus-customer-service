package com.packt.quarkus.config.customvalue;

import org.eclipse.microprofile.config.spi.Converter;

public class MicroProfileCustomValueConverter implements Converter<CustomConfigValue> {

    public MicroProfileCustomValueConverter() {
    }

    @Override
    public CustomConfigValue convert(String value) {
        return new CustomConfigValue(value);
    }

}
