package com.tonyp.onlinechess.model;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class ColorConverter implements AttributeConverter<Color, String> {
    @Override
    public String convertToDatabaseColumn(Color color) {
        return color.name().toLowerCase();
    }

    @Override
    public Color convertToEntityAttribute(String s) {
        return Color.valueOf(s.toUpperCase());
    }
}
