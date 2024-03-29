/*
 * Copyright 2013 ZhangZhenli <Taveloper@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.taveloper.http.test.pojo.parse;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.taveloper.http.json.JsonReaderable;
import com.taveloper.http.test.pojo.ActivityObject;

/**
 *
 * @author ZhangZhenli <Taveloper@gmail.com>
 */
public class ActivityObjectParse implements JsonReaderable<ActivityObject> {

    public ActivityObject readJson(JsonParser in) throws JsonParseException, IOException {
//        System.out.println("ActivityObjectParse.readJson");
        JsonToken curToken = in.nextToken();
        ActivityObject object = new ActivityObject();
        while (curToken == JsonToken.FIELD_NAME) {
            String curName = in.getText();
            JsonToken nextToken = in.nextToken();
            if ("content".equals(curName)) {
                object.setContent(in.getText());
            } else if ("plusoners".equals(curName)) {
                PlusOnersParse plusOnersParse = new PlusOnersParse();
                object.setPlusOners(plusOnersParse.readJson(in));
            }
            curToken = in.nextToken();
        }
        return object;
    }
}
