package com.example.social.app.enums.converters;

import com.example.social.app.enums.MediaType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class MediaTypeConverter implements AttributeConverter<MediaType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(MediaType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public MediaType convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return MediaType.fromCode(dbData);
    }
}
