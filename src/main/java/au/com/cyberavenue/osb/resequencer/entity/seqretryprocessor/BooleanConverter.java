package au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.lang3.StringUtils;

@Converter
public class BooleanConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        return attribute != null && attribute.equals(Boolean.TRUE) ? "Y" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        return StringUtils.equals("Y", dbData);
    }

}
