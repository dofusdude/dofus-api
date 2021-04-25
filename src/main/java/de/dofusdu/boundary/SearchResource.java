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

package de.dofusdu.boundary;

import de.dofusdu.dto.ItemNameDTO;
import de.dofusdu.dto.SearchResult;
import de.dofusdu.gateway.*;
import de.dofusdu.util.LanguageHelper;
import de.dofusdu.util.NgnixUriReplacer;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Path("/dofus/{language}/search")
public class SearchResource {

    @Inject
    ItemFinder itemFinder;

    @Context
    UriInfo uriInfo;

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
    SetRepository setRepository;

    private List<String> searchItemTypes = List.of("resources", "consumables", "equipment", "weapons", "pets", "sets");

    @GET
    @Tag(name = "Search")
    @Parameters({
            @Parameter(name = "language", in = ParameterIn.PATH, example = "en", description = "Language as code of length 2."),
            @Parameter(name = "query", in = ParameterIn.PATH, example = "meow", description = "Fuzzy name")
    })
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "OK",
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = SearchResult.class)
                            )
                    }
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Not Found"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Bad Request - unknown language"
            )
    })
    @Produces(MediaType.APPLICATION_JSON)
    @Timeout
    @CircuitBreaker
    @Retry
    @Counted(name = "searchNameCount")
    @Timed(name = "searchNameTime")
    @Path("/{query}")
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public SearchResult findItemByName(@PathParam("language") String language,
                                       @PathParam("query") String query) {

        LanguageHelper.checkLanguage(language);

        URI absolutePath = NgnixUriReplacer.replace(uriInfo.getAbsolutePath());

        List<List<ItemNameDTO>> lists = List.of(
                resourceRepository.allBasicName(language, absolutePath),
                consumableRepository.allBasicName(language, absolutePath),
                equipmentRepository.allBasicName(language, absolutePath),
                weaponRepository.allBasicName(language, absolutePath),
                petRepository.allBasicName(language, absolutePath),
                setRepository.allBasicName(language, absolutePath));

        // convert to list of results for best hit in every category
        List<ExtractedResult> collect = lists.stream().map(l -> l.stream()
                .map(it -> it.name)
                .collect(Collectors.toList()))
                .map(nameList -> FuzzySearch.extractOne(query, nameList))
                .collect(Collectors.toList());

        int highestScore = -1;
        int highestScoreIndex = -1;
        for (int i = 0; i < collect.size(); i++) {
            if (collect.get(i).getScore() > highestScore) {
                highestScore = collect.get(i).getScore();
                highestScoreIndex = i;
            }
        }

        ItemNameDTO winner = lists.get(highestScoreIndex) // list with highest score in name comparison
                .get(collect.get(highestScoreIndex).getIndex()); // get in that list the element at the highest score index
        return new SearchResult(winner.ankamaId, searchItemTypes.get(highestScoreIndex), itemFinder.linkForAnkaId(winner.ankamaId, uriInfo.getBaseUri(), language).get().toString());
    }
}
