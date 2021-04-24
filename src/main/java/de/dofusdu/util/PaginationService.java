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

package de.dofusdu.util;

import de.dofusdu.dto.PaginationLinkDTO;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequestScoped
public class PaginationService {

    private long pageNumber;
    private long pageSize;

    @ConfigProperty(name = "pagination.page-size.max")
    long biggestPageSize;

    private void setPageNumber(long pageNumber) {
        this.pageNumber = pageNumber;
    }

    private void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public void validatePagination(List<?> list) {
        if(pageSize > biggestPageSize) {
            throw new BadRequestException("page[size] must be smaller than " + biggestPageSize + ".");
        }
        if (biggestPageSize * pageNumber > list.size() + biggestPageSize) {
            throw new BadRequestException("page[size] and page[number] combination is out of bounds.");
        }
    }

    public <T> Set<T> getPaginatedList(List<T> list) {
        return list.stream()
                .skip((pageNumber * pageSize) - pageSize)
                .limit(pageSize)
                .collect(Collectors.toSet());
    }

    public void setPageState(int pageNumber, int pageSize) {
        setPageNumber(pageNumber);
        setPageSize(pageSize);
        if ((pageNumber == -1 && pageSize != -1) || (pageNumber != -1 && pageSize == -1)) {
            throw new BadRequestException("Parameter page[size] must be combined with page[number].");
        }
        setPage1IfNeeded();
    }

    boolean isPagination() {
        return pageNumber != -1 || pageSize != -1;
    }

    void setPage1() {
        pageNumber = 1;
        pageSize = biggestPageSize;
    }

    public void setPage1IfNeeded() {
        if (!isPagination()) setPage1();
    }

    public PaginationLinkDTO build(URI absolutePath, long listSize) {
        PaginationLinkDTO paginationLinkDTO = new PaginationLinkDTO();
        long firstPage = 1;

        long lastPageSize = listSize % pageSize;
        long lastPage = lastPageSize == 0 ? listSize / pageSize : (listSize / pageSize) + 1;

        URI firstUri = UriBuilder.fromUri(absolutePath)
                .queryParam("page[number]", firstPage)
                .queryParam("page[size]", pageSize)
                .build();
        URI prevUri = UriBuilder.fromUri(absolutePath)
                .queryParam("page[number]", pageNumber - 1)
                .queryParam("page[size]", pageSize)
                .build();
        URI nextUri = UriBuilder.fromUri(absolutePath)
                .queryParam("page[number]", pageNumber + 1)
                .queryParam("page[size]", pageSize)
                .build();
        URI lastUri = UriBuilder.fromUri(absolutePath)
                .queryParam("page[number]",  lastPage)
                .queryParam("page[size]", pageSize)
                .build();


        paginationLinkDTO.first = URLDecoder.decode(firstUri.toString(), StandardCharsets.UTF_8);

        if (pageNumber != firstPage) {
            paginationLinkDTO.prev = URLDecoder.decode(prevUri.toString(), StandardCharsets.UTF_8);
        }

        if (pageNumber != lastPage) {
            paginationLinkDTO.next = URLDecoder.decode(nextUri.toString(), StandardCharsets.UTF_8);
        }

        paginationLinkDTO.last = URLDecoder.decode(lastUri.toString(), StandardCharsets.UTF_8);

        return paginationLinkDTO.last.equals(paginationLinkDTO.first) ? null : paginationLinkDTO;
    }
}
