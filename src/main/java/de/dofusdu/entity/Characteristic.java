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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Characteristic {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Attribute name; // e.g. AP
    private String value; // e.g. 5

    public MultilingualEntity getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public Characteristic(Attribute name, String language, String value) {
        this.name = name;
        this.value = value;
    }

    public Characteristic() {

    }
}
