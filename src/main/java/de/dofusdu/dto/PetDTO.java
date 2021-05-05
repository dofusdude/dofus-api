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

import de.dofusdu.entity.Pet;

import javax.transaction.Transactional;
import java.net.URI;
import java.util.Collection;
import java.util.stream.Collectors;

public class PetDTO extends EquipmentDTO {
    public Collection<CharacteristicDTO> characteristics;

    public PetDTO() {
        super();
    }

    public PetDTO(Long ankamaId, String name, String description, String imageUrl, String ankamaUrl, String type, Integer level, String conditions, String itemType) {
        super(ankamaId, name, description, imageUrl, ankamaUrl, type, level, null, conditions, null, itemType);
    }

    @Transactional
    public static PetDTO from(Pet pet, String language, URI baseUri) {
        EquipmentDTO equipmentDTO = EquipmentDTO.from(pet, language, baseUri);

        PetDTO petDTO = new PetDTO(equipmentDTO.ankamaId, equipmentDTO.name, equipmentDTO.description, equipmentDTO.imageUrl, equipmentDTO.ankamaUrl, equipmentDTO.type, equipmentDTO.level, equipmentDTO.conditions, pet.getItemType());
        petDTO.characteristics = pet.getCharacteristics() == null || pet.getCharacteristics().isEmpty() ? null : pet.getCharacteristics().stream().map(el -> CharacteristicDTO.from(el, language)).collect(Collectors.toList());
        return petDTO;
    }
}
