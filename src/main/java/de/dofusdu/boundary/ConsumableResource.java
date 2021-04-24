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

import de.dofusdu.dto.BaseItemAllApi;
import de.dofusdu.dto.ConsumableDTO;
import de.dofusdu.dto.CreateConsumableDTO;
import de.dofusdu.dto.CreateRecipePositionDTO;
import de.dofusdu.entity.*;
import de.dofusdu.gateway.BranchRepository;
import de.dofusdu.gateway.ConsumableRepository;
import de.dofusdu.gateway.EffectRepository;
import de.dofusdu.gateway.ItemFinder;
import de.dofusdu.util.LanguageHelper;
import io.quarkus.security.UnauthorizedException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Path("/{language}/consumables")
@RequestScoped
public class ConsumableResource {

    @Inject
    ConsumableRepository consumableRepository;

    @Inject
    EffectRepository effectRepository;

    @Inject
    BranchRepository branchRepository;

    @Inject
    ItemFinder itemFinder;

    @Context
    UriInfo uriInfo;

    @ConfigProperty(name = "admin.api.secret")
    String apiKey;

    @Inject
    BaseItemAllApi baseItemAllApi;

    @GET
    @Path("/{ankama_id}")
    @Tag(name = "Consumable")
    @Parameters({
            @Parameter(name = "language", in = ParameterIn.PATH, example = "en", description = "Language as code of length 2."),
            @Parameter(name = "ankama_id", in = ParameterIn.PATH, example = "7591")
    })
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "OK",
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = de.dofusdu.dto.ConsumableDTO.class)
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
    @Counted(name = "consumablesByIdCounter")
    @Timed(name = "consumablesByIdTime")
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public ConsumableDTO byId(
            @PathParam("language") String language,
            @PathParam("ankama_id") Long id
    ) {
        LanguageHelper.checkLanguage(language);

        Optional<Consumable> consumable = consumableRepository.byIdCached(id);
        if (consumable.isEmpty()) {
            throw new NotFoundException();
        }

        return ConsumableDTO.from(consumable.get(), language, uriInfo.getBaseUri());
    }

    @GET
    @Tag(name = "Consumable")
    @Parameters({
            @Parameter(name = "language", in = ParameterIn.PATH, example = "en", description = "Language as code of length 2."),
            @Parameter(name = "search[name]", in = ParameterIn.QUERY, example = ""),
            @Parameter(name = "page[number]", in = ParameterIn.QUERY, example = "1"),
            @Parameter(name = "page[size]", in = ParameterIn.QUERY, example = "96")
    })
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "OK",
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = de.dofusdu.dto.ItemListResponse.class)
                            )
                    }
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
    @Timed(name = "consumablesAllTime")
    @Counted(name = "consumablesAllCounter")
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Response all(
            @PathParam("language") String language,
            @QueryParam("search[name]") @DefaultValue("") String query,
            @DefaultValue("-1") @QueryParam("page[number]") int pageNumber,
            @DefaultValue("-1") @QueryParam("page[size]") int pageSize
    ) {
        return baseItemAllApi.all(language, query, pageNumber, pageSize, consumableRepository, uriInfo.getAbsolutePath());
    }

    private List<Integer> numbersFromString(String s) {
        Pattern p = Pattern.compile("-?\\d+");
        Matcher m = p.matcher(s);
        List<Integer> l = new ArrayList<>();
        while (m.find()) {
            l.add(Integer.valueOf(m.group()));
        }
        return l;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public ConsumableDTO create(
            @HeaderParam("Authorization") String apiKey,
            @PathParam("language") String language,
            CreateConsumableDTO consumable,
            @Context UriInfo uriInfo
    ) {
        LanguageHelper.checkLanguage(language);
        if (apiKey == null || !apiKey.equals("Bearer " + this.apiKey)) {
            throw new UnauthorizedException("Wrong secret.");
        }

        Optional<Consumable> consumable1 = consumableRepository.byId(consumable.ankamaId);
        if (consumable1.isPresent()) {
            throw new BadRequestException();
        }

        Collection<String> effects = consumable.effects;
        List<ConsumableEffect> conEffects = null;
        if (effects != null && !effects.isEmpty()) {
            // filter out all numbers, search in database for the rest
            conEffects = effects.stream().map(ef -> {
                List<Integer> l = numbersFromString(ef);
                for (Integer el : l) {
                    ef = ef.replace(" +" + el.toString() + " ", "");
                    ef = ef.replace("+" + el.toString() + " ", "");
                    ef = ef.replace("+" + el.toString(), "");
                    ef = ef.replace(" " + el.toString() + " ", "");
                    ef = ef.replace(el.toString(), "");
                }
                ef = ef.strip();
                // search for this effect

                Optional<Attribute> effect = effectRepository.closestIdByName(ef, language);
                if (effect.isEmpty()) {
                    throw new RuntimeException("Effect unknown.");
                }

                ConsumableEffect consumableEffect = new ConsumableEffect();
                consumableEffect.setName(effect.get());
                consumableEffect.setValues(l);

                return consumableEffect;
            })
                    .collect(Collectors.toList());
        }

        Collection<CreateRecipePositionDTO> recipe = consumable.recipe;
        List<RecipePosition> conRecipe = null;
        if (recipe != null && !recipe.isEmpty()) {
            conRecipe = recipe.stream().map(el -> {
                Optional<Item> item = itemFinder.findItem(el.itemId);
                if (item.isPresent()) {
                    return new RecipePosition(item.get(), el.quantity);
                }

                throw new NotFoundException("AnkaID from Recipe not found!");
            })
                    .collect(Collectors.toList());
        }

        Consumable r = consumable.toConsumable(language, conEffects, conRecipe, branchRepository.main());
        Consumable persist = consumableRepository.persist(r);

        return ConsumableDTO.from(persist, language, uriInfo.getBaseUri());
    }


    @PUT
    @Path("/{id}/lang")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public ConsumableDTO updateLanguage(
            @HeaderParam("Authorization") String apiKey,
            @PathParam("language") String language,
            @PathParam("id") Long id,
            CreateConsumableDTO equipmentDTO
    ) {
        LanguageHelper.checkLanguage(language);
        if (apiKey == null || !apiKey.equals("Bearer " + this.apiKey)) {
            throw new UnauthorizedException("Wrong secret.");
        }

        Optional<Consumable> consumable = consumableRepository.byId(equipmentDTO.ankamaId);
        if (consumable.isEmpty()) {
            throw new NotFoundException();
        }

        Consumable persisted = consumable.get();

        // base item
        persisted.setName(equipmentDTO.name, language);
        persisted.setType(equipmentDTO.type, language);
        persisted.setAnkamaUrl(equipmentDTO.ankamaUrl, language);
        persisted.setDescription(equipmentDTO.description, language);

        // update language specifics for equipment
        persisted.setConditions(equipmentDTO.conditions, language);
        persisted.setType(equipmentDTO.type, language);
        Consumable update = consumableRepository.update(persisted);

        return ConsumableDTO.from(update, language, uriInfo.getBaseUri());
    }


}
