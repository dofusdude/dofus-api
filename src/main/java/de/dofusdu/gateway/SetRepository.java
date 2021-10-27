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
import de.dofusdu.entity.Set;
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
public class SetRepository implements Repository {
    @Inject
    EntityManager em;

    @Transactional
    //@CacheResult(cacheName = "setId")
    public Optional<Set> byIdCached(Long ankamaId) {
        return byId(ankamaId);
    }

    @Transactional
    //@CacheResult(cacheName = "setList")
    public List<Set> allCached() {
        return all();
    }

    @Transactional
    public Optional<Set> byId(Long ankamaId) {
        TypedQuery<Set> query = em.createQuery("select r from Set r where r.ankamaId = :ankamaId", Set.class);
        query.setParameter("ankamaId", ankamaId);
        Set set;
        try {
            set = query.getSingleResult();
        } catch (NoResultException e) {
            return Optional.empty();
        }
        return Optional.of(set);
    }

    @Transactional
    public List<ItemBasicDTO> allBasic() {
        TypedQuery<ItemBasicDTO> query = em.createQuery("select new de.dofusdu.dto.ItemBasicDTO(r.ankamaId, 'sets') from Set r", ItemBasicDTO.class);
        List<ItemBasicDTO> weapons;
        try {
            weapons = query.getResultList();
        } catch (NoResultException e) {
            return List.of();
        }
        return weapons;
    }

    @Transactional
    public List<Set> all() {
        TypedQuery<Set> query = em.createQuery("select r from Set r", Set.class);
        List<Set> sets;
        try {
            sets = query.getResultList();
        } catch (NoResultException e) {
            return List.of();
        }
        return sets;
    }

    @Transactional
    //@CacheResult(cacheName = "setsNames")
    public List<ItemNameDTO> allBasicName(String language, URI absoluteUri) {
        TypedQuery<ItemNameDTO> query;
        switch (language) {
            case "de": {
                query = em.createQuery("select new de.dofusdu.dto.ItemNameDTO(r.ankamaId, 'sets', r.nameDe) from Set r ORDER BY r.level ASC", ItemNameDTO.class);
                break;
            }
            case "fr": {
                query = em.createQuery("select new de.dofusdu.dto.ItemNameDTO(r.ankamaId, 'sets', r.nameFr) from Set r ORDER BY r.level ASC", ItemNameDTO.class);
                break;
            }
            case "es": {
                query = em.createQuery("select new de.dofusdu.dto.ItemNameDTO(r.ankamaId, 'sets', r.nameEs) from Set r ORDER BY r.level ASC", ItemNameDTO.class);
                break;
            }
            case "it": {
                query = em.createQuery("select new de.dofusdu.dto.ItemNameDTO(r.ankamaId, 'sets', r.nameIt) from Set r ORDER BY r.level ASC", ItemNameDTO.class);
                break;
            }
            case "pt": {
                query = em.createQuery("select new de.dofusdu.dto.ItemNameDTO(r.ankamaId, 'sets', r.namePt) from Set r ORDER BY r.level ASC", ItemNameDTO.class);
                break;
            }
            default: {
                query = em.createQuery("select new de.dofusdu.dto.ItemNameDTO(r.ankamaId, 'sets', r.nameEn) from Set r ORDER BY r.level ASC", ItemNameDTO.class);
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
    public Set persist(Set set) {
        em.persist(set);
        return set;
    }

    @Transactional
    public Set update(Set set) {
        return em.merge(set);
    }
}
