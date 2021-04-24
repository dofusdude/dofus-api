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

import de.dofusdu.dto.ItemBasicDTO;
import de.dofusdu.dto.ItemNameDTO;
import de.dofusdu.entity.Consumable;
import io.quarkus.cache.CacheResult;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@RequestScoped
public class ConsumableRepository implements Repository {

    @Inject
    EntityManager em;

    @Transactional
    @CacheResult(cacheName = "consumableId")
    public Optional<Consumable> byIdCached(Long ankamaId) {
        return byId(ankamaId);
    }

    @Transactional
    @CacheResult(cacheName = "consumableList")
    public List<Consumable> allCached() {
        return all();
    }

    @Transactional
    public List<ItemBasicDTO> allBasic() {
        TypedQuery<ItemBasicDTO> query = em.createQuery("select new de.dofusdu.dto.ItemBasicDTO(r.ankamaId, 'consumables') from Consumable r", ItemBasicDTO.class);
        List<ItemBasicDTO> weapons;
        try {
            weapons = query.getResultList();
        } catch (NoResultException e) {
            return List.of();
        }
        return weapons;
    }

    @Transactional
    @CacheResult(cacheName = "consumablesNames")
    public List<ItemNameDTO> allBasicName(String language, URI absoluteUri) {
        TypedQuery<ItemNameDTO> query;
        switch (language) {
            case "de": {
                query = em.createQuery("select new de.dofusdu.dto.ItemNameDTO(r.ankamaId, 'consumables', r.nameDe) from Consumable r ORDER BY r.level ASC", ItemNameDTO.class);
                break;
            }
            case "fr": {
                query = em.createQuery("select new de.dofusdu.dto.ItemNameDTO(r.ankamaId, 'consumables', r.nameFr) from Consumable r ORDER BY r.level ASC", ItemNameDTO.class);
                break;
            }
            case "es": {
                query = em.createQuery("select new de.dofusdu.dto.ItemNameDTO(r.ankamaId, 'consumables', r.nameEs) from Consumable r ORDER BY r.level ASC", ItemNameDTO.class);
                break;
            }
            case "it": {
                query = em.createQuery("select new de.dofusdu.dto.ItemNameDTO(r.ankamaId, 'consumables', r.nameIt) from Consumable r ORDER BY r.level ASC", ItemNameDTO.class);
                break;
            }
            case "pt": {
                query = em.createQuery("select new de.dofusdu.dto.ItemNameDTO(r.ankamaId, 'consumables', r.namePt) from Consumable r ORDER BY r.level ASC", ItemNameDTO.class);
                break;
            }
            default: {
                query = em.createQuery("select new de.dofusdu.dto.ItemNameDTO(r.ankamaId, 'consumables', r.nameEn) from Consumable r ORDER BY r.level ASC", ItemNameDTO.class);
                break;
            }
        }

        List<ItemNameDTO> weapons;
        try {
            weapons = query.getResultList();
        } catch (NoResultException e) {
            return List.of();
        }
        weapons.forEach(el -> el.url =
                URLDecoder.decode(
                        UriBuilder.fromUri(absoluteUri)
                                .path(el.ankamaId.toString())
                                .build()
                                .toString(),
                        StandardCharsets.UTF_8));

        return weapons;
    }

    @Transactional
    public Optional<Consumable> byId(Long ankamaId) {
        TypedQuery<Consumable> query = em.createQuery("select r from Consumable r where r.ankamaId = :ankamaId", Consumable.class);
        query.setParameter("ankamaId", ankamaId);
        Consumable consumable;
        try {
            consumable = query.getSingleResult();
        } catch (NoResultException e) {
            return Optional.empty();
        }
        return Optional.of(consumable);
    }

    @Transactional
    public List<Consumable> all() {
        TypedQuery<Consumable> query = em.createQuery("select r from Consumable r", Consumable.class);
        List<Consumable> consumables;
        try {
            consumables = query.getResultList();
        } catch (NoResultException e) {
            return List.of();
        }
        return consumables;
    }

    @Transactional
    public Consumable persist(Consumable consumable) {
        em.persist(consumable);
        return consumable;
    }

    @Transactional
    public Consumable update(Consumable consumable) {
        return em.merge(consumable);
    }
}
