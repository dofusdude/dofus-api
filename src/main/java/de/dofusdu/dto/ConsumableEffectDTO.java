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

package de.dofusdu.dto;

import de.dofusdu.entity.ConsumableEffect;

import javax.json.bind.annotation.JsonbProperty;
import java.util.Collection;

public class ConsumableEffectDTO {
    @JsonbProperty("type")
    public String name;
    public Collection<Integer> values;

    public ConsumableEffectDTO(String name, Collection<Integer> values) {
        this.name = name;
        this.values = values;
    }

    public ConsumableEffectDTO() {
    }

    /*
    public ConsumableEffect toConsumableEffect(String lang) {
        return new ConsumableEffect(name, lang, values);
    }*/

    public static ConsumableEffectDTO from(ConsumableEffect consumableEffect, String lang) {
        return new ConsumableEffectDTO(consumableEffect.getName().getName(lang), consumableEffect.getValues());
    }
}
