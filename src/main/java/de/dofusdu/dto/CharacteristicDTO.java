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

import de.dofusdu.entity.Attribute;
import de.dofusdu.entity.Characteristic;
import de.dofusdu.gateway.EffectRepository;

import javax.ws.rs.NotFoundException;
import java.util.Optional;

public class CharacteristicDTO {
    public String name;
    public String value;

    public CharacteristicDTO() {
    }

    public CharacteristicDTO(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public static CharacteristicDTO from(Characteristic characteristic, String language) {
        return new CharacteristicDTO(characteristic.getName().getName(language), characteristic.getValue());
    }

    public Characteristic toCharacteristic(String language, EffectRepository effectRepository) {
        // check name
        Optional<Attribute> attribute = effectRepository.closestIdByName(name, language);
        if (attribute.isEmpty()) {
            throw new NotFoundException("effect with name " + name + " does not exists in database.");
        }
        return new Characteristic(attribute.get(), language, value);
    }
}
