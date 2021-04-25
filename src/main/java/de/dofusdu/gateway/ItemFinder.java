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

package de.dofusdu.gateway;

import de.dofusdu.entity.Item;
import de.dofusdu.util.NgnixUriReplacer;
import io.quarkus.cache.CacheResult;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Optional;

@RequestScoped
public class ItemFinder {

    @Inject
    ResourceRepository resourceRepository;

    @Inject
    ConsumableRepository consumableRepository;

    @Inject
    EquipmentRepository equipmentRepository;

    @Inject
    WeaponRepository weaponRepository;

    @Inject
    PetRepository petRepository;

    @Inject
    EntityManager em;

    public Optional<String> findResource(Long ankaId) {
        if (resourceRepository.byId(ankaId).isPresent()) {
            return Optional.of("resources");
        }

        if (consumableRepository.byId(ankaId).isPresent()) {
            return Optional.of("consumables");
        }

        if (weaponRepository.byId(ankaId).isPresent()) {
            return Optional.of("weapons");
        }

        if (petRepository.byId(ankaId).isPresent()) {
            return Optional.of("pets");
        }

        if (equipmentRepository.byId(ankaId).isPresent()) {
            return Optional.of("equipment");
        }

        return Optional.empty();
    }

    @Transactional
    public Optional<Item> findItem(Long ankamaId) {
        Optional<String> resource = findResource(ankamaId);
        if (resource.isEmpty()) {
            return Optional.empty();
        }

        String res = resource.get();
        switch (res) {
            case "resources":
                return resourceRepository.byId(ankamaId).map(e -> e);
            case "consumables":
                return consumableRepository.byId(ankamaId).map(e -> e);
            case "equipment":
                return equipmentRepository.byId(ankamaId).map(e -> e);
            case "weapons":
                return weaponRepository.byId(ankamaId).map(e -> e);
            case "pets":
                return petRepository.byId(ankamaId).map(e -> e);
            default:
                return Optional.empty();
        }
    }

    @CacheResult(cacheName = "findItemAnkaId")
    public Optional<Item> findItemCached(Long ankamaId) {
        return findItem(ankamaId);
    }

    public Optional<URI> linkForAnkaId(Long ankaId, URI baseUri, String language) {
        Optional<String> resourceType = findResource(ankaId);
        if (resourceType.isEmpty()) {
            return Optional.empty();
        }

        baseUri = NgnixUriReplacer.replace(baseUri);

        URI build = UriBuilder.fromUri(baseUri)
                .path("dofus")
                .path(language)
                .path(resourceType.get())
                .path(ankaId.toString())
                .build();
        return Optional.of(build);
    }

    @Transactional
    public Item update(Item item) {
        return em.merge(item);
    }
}
