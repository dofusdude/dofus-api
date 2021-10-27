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

import de.dofusdu.entity.Attribute;
import de.dofusdu.exception.LanguageNotFoundException;
import de.dofusdu.util.LanguageHelper;
import io.quarkus.cache.CacheResult;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class EffectRepository {

    @Inject
    EntityManager em;

    @Transactional
    //@CacheResult(cacheName = "effectClosestName")
    public Optional<Attribute> closestIdByNameCached(String nameSearch, String language) {
        return closestIdByName(nameSearch, language);
    }

    @Transactional
    public Optional<Attribute> closestIdByName(String nameSearch, String language) {
        // try direct
        LanguageHelper.Language lang = LanguageHelper.getLanguage(language);

        TypedQuery<Attribute> query;
        switch (lang) {
            case GERMAN: query = em.createQuery("select e from Attribute e where e.nameDe = :name", Attribute.class);break;
            case FRENCH: query = em.createQuery("select e from Attribute e where e.nameFr = :name", Attribute.class);break;
            case ITALIAN: query = em.createQuery("select e from Attribute e where e.nameIt = :name", Attribute.class);break;
            case ENGLISH: query = em.createQuery("select e from Attribute e where e.nameEn = :name", Attribute.class);break;
            case SPANISH: query = em.createQuery("select e from Attribute e where e.nameEs = :name", Attribute.class);break;
            case PORTUGUESE: query = em.createQuery("select e from Attribute e where e.namePt = :name", Attribute.class);break;
            default: {
                throw new LanguageNotFoundException(language);
            }
        }

        query.setParameter("name", nameSearch);
        Attribute a;
        try {
            a = query.getSingleResult();
        } catch (NoResultException e) {
            // if not exact, try fuzzy
            TypedQuery<Attribute> allQuery = em.createQuery("select e from Attribute e", Attribute.class);
            List<Attribute> entitys;

            entitys = allQuery.getResultList();

            List<String> collect = entitys.stream().map(el -> el.getName(language)).collect(Collectors.toList());

            ExtractedResult extractedResult = FuzzySearch.extractOne(nameSearch, collect);
            return Optional.of(entitys.get(extractedResult.getIndex()));
        }

        return Optional.ofNullable(a);
    }
}
