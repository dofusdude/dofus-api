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

import de.dofusdu.entity.Effect;
import de.dofusdu.entity.MultilingualEntity;

import javax.json.bind.annotation.JsonbProperty;

public class EffectDTO {

    @JsonbProperty("type")
    public String name;
    public Integer min;
    public Integer max;
    public String additional;

    public EffectDTO(String name, Integer min, Integer max, String additional) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.additional = additional;
    }

    public EffectDTO() {
    }

    public static EffectDTO from(Effect effect, String lang) {
        return new EffectDTO(effect.getName().getName(lang), effect.getMin(), effect.getMax(), effect.getAdditional());
    }

    public Effect toEffect(MultilingualEntity name, String lang) {
        return new Effect(name, min, max, additional);
    }
}
