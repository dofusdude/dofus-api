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

import de.dofusdu.util.IntegerArray;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Collection;

@Entity
public class ConsumableEffect {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private MultilingualEntity name;

    @Lob
    private IntegerArray values;

    public ConsumableEffect() {
        super();
    }

    public Collection<Integer> getValues() {
        return Arrays.asList(values.getValues());
    }

    public void setValues(Collection<Integer> values) {
        this.values = new IntegerArray(values.toArray(Integer[]::new));
    }

    public void setName(MultilingualEntity name) {
        this.name = name;
    }

    public MultilingualEntity getName() {
        return name;
    }

    public ConsumableEffect(String name, String lang, Collection<Integer> values) {
        IntegerArray integerArray = new IntegerArray(values.toArray(Integer[]::new));
        this.values = integerArray;
        this.name = new MultilingualEntity(name, lang);
    }
}