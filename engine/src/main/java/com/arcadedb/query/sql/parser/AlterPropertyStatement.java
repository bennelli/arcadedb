/*
 * Copyright 2021 Arcade Data Ltd
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/* Generated By:JJTree: Do not edit this line. OAlterPropertyStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import com.arcadedb.database.Database;
import com.arcadedb.exception.CommandSQLParsingException;
import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.ResultSet;

import java.util.Map;

public class AlterPropertyStatement extends ODDLStatement {

  Identifier typeName;

  Identifier propertyName;
  Identifier customPropertyName;
  Expression customPropertyValue;

  Identifier settingName;
  public Expression settingValue;

  public AlterPropertyStatement(int id) {
    super(id);
  }

  public AlterPropertyStatement(SqlParser p, int id) {
    super(p, id);
  }

  @Override
  public ResultSet executeDDL(CommandContext ctx) {
    Database db = ctx.getDatabase();

    throw new UnsupportedOperationException();
//    OClass typez = db.getMetadata().getSchema().getClass(className.getStringValue());
//
//    if (typez == null) {
//      throw new PCommandExecutionException("Invalid class name or class not found: " + typez);
//    }
//
//    OProperty property = typez.getProperty(propertyName.getStringValue());
//    if (property == null) {
//      throw new PCommandExecutionException("Property " + property + " not found on class " + typez);
//    }
//
//    OResultInternal result = new OResultInternal();
//    result.setProperty("class", className.getStringValue());
//    result.setProperty("property", propertyName.getStringValue());
//
//    if (customPropertyName != null) {
//      String customName = customPropertyName.getStringValue();
//      Object oldValue = property.getCustom(customName);
//      Object finalValue = customPropertyValue.execute((PIdentifiable) null, ctx);
//      property.setCustom(customName, finalValue == null ? null : "" + finalValue);
//
//      result.setProperty("operation", "alter property custom");
//      result.setProperty("customAttribute", customPropertyName.getStringValue());
//      result.setProperty("oldValue", oldValue != null ? oldValue.toString() : null);
//      result.setProperty("newValue", finalValue != null ? finalValue.toString() : null);
//    } else {
//      String setting = settingName.getStringValue();
//      Object finalValue = settingValue.execute((PIdentifiable) null, ctx);
//
//      OProperty.ATTRIBUTES attribute;
//      try {
//        attribute = OProperty.ATTRIBUTES.valueOf(setting.toUpperCase(Locale.ENGLISH));
//      } catch (IllegalArgumentException e) {
//        throw OException.wrapException(new PCommandExecutionException(
//            "Unknown property attribute '" + setting + "'. Supported attributes are: " + Arrays
//                .toString(OProperty.ATTRIBUTES.values())), e);
//      }
//      Object oldValue = property.get(attribute);
//      property.set(attribute, finalValue);
//      finalValue = property.get(attribute);//it makes some conversions...
//
//      result.setProperty("operation", "alter property");
//      result.setProperty("attribute", setting);
//      result.setProperty("oldValue", oldValue != null ? oldValue.toString() : null);
//      result.setProperty("newValue", finalValue != null ? finalValue.toString() : null);
//    }
//    OInternalResultSet rs = new OInternalResultSet();
//    rs.add(result);
//    return rs;
  }

  @Override
  public void validate() throws CommandSQLParsingException {
    super.validate();//TODO
  }

  @Override
  public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append("ALTER PROPERTY ");
    typeName.toString(params, builder);
    builder.append(".");
    propertyName.toString(params, builder);
    if (customPropertyName != null) {
      builder.append(" CUSTOM ");
      customPropertyName.toString(params, builder);
      builder.append(" = ");
      customPropertyValue.toString(params, builder);
    } else {
      builder.append(" ");
      settingName.toString(params, builder);
      builder.append(" ");
      settingValue.toString(params, builder);
    }
  }

  @Override
  public AlterPropertyStatement copy() {
    AlterPropertyStatement result = new AlterPropertyStatement(-1);
    result.typeName = typeName == null ? null : typeName.copy();
    result.propertyName = propertyName == null ? null : propertyName.copy();
    result.customPropertyName = customPropertyName == null ? null : customPropertyName.copy();
    result.customPropertyValue = customPropertyValue == null ? null : customPropertyValue.copy();
    result.settingName = settingName == null ? null : settingName.copy();
    result.settingValue = settingValue == null ? null : settingValue.copy();
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    AlterPropertyStatement that = (AlterPropertyStatement) o;

    if (typeName != null ? !typeName.equals(that.typeName) : that.typeName != null)
      return false;
    if (propertyName != null ? !propertyName.equals(that.propertyName) : that.propertyName != null)
      return false;
    if (customPropertyName != null ? !customPropertyName.equals(that.customPropertyName) : that.customPropertyName != null)
      return false;
    if (customPropertyValue != null ? !customPropertyValue.equals(that.customPropertyValue) : that.customPropertyValue != null)
      return false;
    if (settingName != null ? !settingName.equals(that.settingName) : that.settingName != null)
      return false;
    return settingValue != null ? settingValue.equals(that.settingValue) : that.settingValue == null;
  }

  @Override
  public int hashCode() {
    int result = typeName != null ? typeName.hashCode() : 0;
    result = 31 * result + (propertyName != null ? propertyName.hashCode() : 0);
    result = 31 * result + (customPropertyName != null ? customPropertyName.hashCode() : 0);
    result = 31 * result + (customPropertyValue != null ? customPropertyValue.hashCode() : 0);
    result = 31 * result + (settingName != null ? settingName.hashCode() : 0);
    result = 31 * result + (settingValue != null ? settingValue.hashCode() : 0);
    return result;
  }
}
/* JavaCC - OriginalChecksum=2421f6ad3b5f1f8e18149650ff80f1e7 (do not edit this line) */