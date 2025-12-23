package com.example.transactions.domain.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OperationTypeConverter implements AttributeConverter<OperationType, Integer> {

  @Override
  public Integer convertToDatabaseColumn(OperationType attribute) {
    return attribute == null ? null : attribute.id();
  }

  @Override
  public OperationType convertToEntityAttribute(Integer dbData) {
    return dbData == null ? null : OperationType.fromId(dbData);
  }
}