/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2017 The ZAP Development Team
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.openapi.generators;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

public class BodyGenerator {

    private ModelGenerator modelGenerator;
    private DataGenerator dataGenerator;

    public BodyGenerator(ModelGenerator modelGenerator, DataGenerator dataGenerator) {
        this.modelGenerator = modelGenerator;
        this.dataGenerator = dataGenerator;
    }

    private enum Element {
        OBJECT_BEGIN,
        OBJECT_END,
        ARRAY_BEGIN,
        ARRAY_END,
        PROPERTY_CONTAINER,
        INNER_SEPARATOR,
        OUTER_SEPARATOR
    }

    @SuppressWarnings("serial")
    private final Map<Element, String> SYNTAX = Collections.unmodifiableMap(new HashMap<Element, String>() {

        {
            put(Element.OBJECT_BEGIN, "{");
            put(Element.OBJECT_END, "}");
            put(Element.ARRAY_BEGIN, "[");
            put(Element.ARRAY_END, "]");
            put(Element.PROPERTY_CONTAINER, "\"");
            put(Element.INNER_SEPARATOR, ":");
            put(Element.OUTER_SEPARATOR, ",");
        }
    });

    public String generate(String name, boolean isArray) {
        StringBuilder json = new StringBuilder();
        json.append(SYNTAX.get(Element.OBJECT_BEGIN));
        boolean isFirst = true;
        for (Map.Entry<String, Property> property : modelGenerator.getProperty(name).entrySet()) {
            if (isFirst) {
                isFirst = false;
            } else {
                json.append(SYNTAX.get(Element.OUTER_SEPARATOR));
            }
            json.append(SYNTAX.get(Element.PROPERTY_CONTAINER));
            json.append(property.getKey());
            json.append(SYNTAX.get(Element.PROPERTY_CONTAINER));
            json.append(SYNTAX.get(Element.INNER_SEPARATOR));
            if (dataGenerator.isSupported(property.getValue().getType())) {
                json.append(dataGenerator.generateBodyValue(property.getValue()));
            } else {
                if (property.getValue() instanceof RefProperty) {
                    json.append(generate(((RefProperty) property.getValue()).getSimpleRef(), false));
                } else {
                    // Best we can do
                    json.append(generate(property.getValue().getName(), false));
                }
            }
        }
        json.append(SYNTAX.get(Element.OBJECT_END));
        String jsonStr = json.toString();
        if (isArray) {
            jsonStr = SYNTAX.get(Element.ARRAY_BEGIN) + jsonStr + SYNTAX.get(Element.OUTER_SEPARATOR) + jsonStr
                    + SYNTAX.get(Element.ARRAY_END);
        }
        return jsonStr;
    }

}