/*
 * Copyright 2021 Christopher Sieh (stelzo@steado.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.dofusdu.entity;

import javax.persistence.*;
import java.util.Collection;
import java.util.List;

@Entity
public class ConsumableEffect {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private MultilingualEntity name;

    private String values;

    public ConsumableEffect() {
        super();
    }

    public List<String> getValues() {
        String[] split = values.split(";");
        return List.of(split);
    }

    public void setValues(Collection<String> values) {
        String delimiter = ";";

        String result = "", prefix = "";
        for (String s: values)
        {
            result += prefix + s;
            prefix = delimiter;
        }

        this.values = result;
    }

    public void setName(MultilingualEntity name) {
        this.name = name;
    }

    public MultilingualEntity getName() {
        return name;
    }

    public ConsumableEffect(String name, String lang, Collection<String> values) {
        this.setValues(values);
        this.name = new MultilingualEntity(name, lang);
    }
}