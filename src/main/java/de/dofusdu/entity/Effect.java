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
public class Effect {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private MultilingualEntity name; // e.g. AP

    private Integer min;
    private Integer max; // null if only one effect (e.g. 1 AP)

    private String additional; // legendary stuff

    public Long getId() {
        return id;
    }

    public MultilingualEntity getName() {
        return name;
    }

    public Effect() {
    }

    public Effect(MultilingualEntity name, Integer min, Integer max, String additional) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.additional = additional;
    }

    public Integer getMin() {
        return min;
    }

    public Integer getMax() {
        return max;
    }

    public String getAdditional() {
        return additional;
    }
}
