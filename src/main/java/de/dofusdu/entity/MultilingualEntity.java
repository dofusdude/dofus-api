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

import de.dofusdu.exception.LanguageNotFoundException;
import de.dofusdu.util.LanguageHelper;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(indexes = {
        @Index(columnList = "name_en", name = "idx_name_en", unique = false)
})
@Inheritance(strategy = InheritanceType.JOINED) // use in hibernate like all subclasses have the attributes in protected.
public class MultilingualEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    protected String id;

    @Column(name = "name_en", length = 2048)
    protected String nameEn;

    @Column(name = "name_de", length = 2048)
    protected String nameDe;

    @Column(name = "name_fr", length = 2048)
    protected String nameFr;

    @Column(name = "name_it", length = 2048)
    protected String nameIt;

    @Column(name = "name_es", length = 2048)
    protected String nameEs;

    @Column(name = "name_pt", length = 2048)
    protected String namePt;

    public MultilingualEntity() {
    }

    public String getId() {
        return id;
    }

    public MultilingualEntity(String name, String lang) {
        setName(name, lang);
    }

    public void setName(String name, String lang) {
        LanguageHelper.Language language = LanguageHelper.getLanguage(lang);

        switch (language) {
            case GERMAN: nameDe = name; break;
            case FRENCH: nameFr = name; break;
            case ITALIAN: nameIt = name; break;
            case ENGLISH: nameEn = name; break;
            case SPANISH: nameEs = name; break;
            case PORTUGUESE: namePt = name; break;
            default: {
                throw new LanguageNotFoundException(language);
            }
        }
    }

    public String getName(String lang) {
        LanguageHelper.Language language = LanguageHelper.getLanguage(lang);
        switch (language) {
            case GERMAN: return nameDe;
            case FRENCH: return nameFr;
            case ITALIAN: return nameIt;
            case ENGLISH: return nameEn;
            case SPANISH: return nameEs;
            case PORTUGUESE: return namePt;
            default: {
                throw new LanguageNotFoundException(language);
            }
        }
    }

}
