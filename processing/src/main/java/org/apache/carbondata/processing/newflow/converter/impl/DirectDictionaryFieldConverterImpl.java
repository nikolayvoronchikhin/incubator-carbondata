/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.carbondata.processing.newflow.converter.impl;

import java.util.List;

import org.apache.carbondata.core.keygenerator.directdictionary.DirectDictionaryGenerator;
import org.apache.carbondata.core.keygenerator.directdictionary.DirectDictionaryKeyGeneratorFactory;
import org.apache.carbondata.core.metadata.schema.table.column.CarbonColumn;
import org.apache.carbondata.processing.newflow.DataField;
import org.apache.carbondata.processing.newflow.converter.BadRecordLogHolder;
import org.apache.carbondata.processing.newflow.row.CarbonRow;

public class DirectDictionaryFieldConverterImpl extends AbstractDictionaryFieldConverterImpl {

  private DirectDictionaryGenerator directDictionaryGenerator;

  private int index;

  private String nullFormat;

  private CarbonColumn column;

  public DirectDictionaryFieldConverterImpl(DataField dataField, String nullFormat, int index) {
    this.nullFormat = nullFormat;
    this.column = dataField.getColumn();
    if (dataField.getDateFormat() != null && !dataField.getDateFormat().isEmpty()) {
      this.directDictionaryGenerator = DirectDictionaryKeyGeneratorFactory
          .getDirectDictionaryGenerator(dataField.getColumn().getDataType(),
              dataField.getDateFormat());

    } else {
      this.directDictionaryGenerator = DirectDictionaryKeyGeneratorFactory
          .getDirectDictionaryGenerator(dataField.getColumn().getDataType());
    }
    this.index = index;
  }

  @Override
  public void convert(CarbonRow row, BadRecordLogHolder logHolder) {
    String value = row.getString(index);
    if (value == null) {
      logHolder.setReason(
          "The value " + " \"" + row.getString(index) + "\"" + " with column name " + column
              .getColName() + " and column data type " + column.getDataType() + " is not a valid "
              + column.getDataType() + " type.");
      row.update(1, index);
    } else if (value.equals(nullFormat)) {
      row.update(1, index);
    } else {
      int key = directDictionaryGenerator.generateDirectSurrogateKey(value);
      if (key == 1) {
        logHolder.setReason(
            "The value " + " \"" + row.getString(index) + "\"" + " with column name " + column
                .getColName() + " and column data type " + column.getDataType() + " is not a valid "
                + column.getDataType() + " type.");
      }
      row.update(key, index);
    }
  }

  @Override
  public void fillColumnCardinality(List<Integer> cardinality) {
    cardinality.add(Integer.MAX_VALUE);
  }
}
