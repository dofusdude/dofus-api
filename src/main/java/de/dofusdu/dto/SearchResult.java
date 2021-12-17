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


public class SearchResult {
    public Long ankama_id;
    public String type;
    public String url;
    public int score;

    public SearchResult(Long ankama_id, String type, String url, int score) {
        this.ankama_id = ankama_id;
        this.type = type;
        this.url = url;
        this.score = score;
    }
}
