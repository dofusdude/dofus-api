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

import de.dofusdu.entity.SetBonusPosition;
import de.dofusdu.gateway.EffectRepository;

import java.util.List;
import java.util.stream.Collectors;

public class SetBonusPositionDTO {
    public Integer quantity; // e.g. set bonus with 2 items
    public List<CharacteristicDTO> bonus;

    public SetBonusPositionDTO() {
    }

    public SetBonusPositionDTO(Integer quantity, List<CharacteristicDTO> bonus) {
        this.quantity = quantity;
        this.bonus = bonus;
    }

    public static SetBonusPositionDTO from(SetBonusPosition setBonusPosition, String language) {
        return new SetBonusPositionDTO(setBonusPosition.getQuantity(),
                setBonusPosition.getBonus().stream().map(el -> CharacteristicDTO.from(el, language)).collect(Collectors.toList())
        );
    }

    public SetBonusPosition toBonusPosition(String language, EffectRepository effectRepository) {
        return new SetBonusPosition(quantity,
                bonus.stream().map(el -> el.toCharacteristic(language, effectRepository)).collect(Collectors.toList())
        );
    }
}
