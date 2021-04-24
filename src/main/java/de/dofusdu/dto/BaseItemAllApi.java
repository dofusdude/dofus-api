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

import de.dofusdu.gateway.Repository;
import de.dofusdu.util.LanguageHelper;
import de.dofusdu.util.NgnixUriReplacer;
import de.dofusdu.util.PaginationService;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RequestScoped
public class BaseItemAllApi {

    @ConfigProperty(name = "search.cutoff")
    Integer percentageCutoff;

    @Inject
    PaginationService paginationService;

    public Response all(String language, String query, int pageNumber, int pageSize, Repository repository, URI absolutePath) {
        LanguageHelper.checkLanguage(language);

        absolutePath = NgnixUriReplacer.replace(absolutePath);

        List<ItemNameDTO> itemBasicDTOS = repository.allBasicName(language, absolutePath);

        paginationService.setPageState(pageNumber, pageSize);

        if (query.length() != 0) {
            List<ExtractedResult> extractedResults = FuzzySearch.extractAll(query, itemBasicDTOS.stream().map(res -> res.name).collect(Collectors.toList()), percentageCutoff);
            List<ItemNameDTO> finalItemBasicDTOS = itemBasicDTOS;
            itemBasicDTOS = extractedResults.stream().map(el -> finalItemBasicDTOS.get(el.getIndex())).collect(Collectors.toList());
        }

        paginationService.validatePagination(itemBasicDTOS);

        // response
        ItemListResponse itemListResponse = new ItemListResponse();
        itemListResponse.links = paginationService.build(absolutePath, itemBasicDTOS.size());
        itemListResponse.items = paginationService.getPaginatedList(itemBasicDTOS);

        return Response.ok(itemListResponse).build();
    }
}
