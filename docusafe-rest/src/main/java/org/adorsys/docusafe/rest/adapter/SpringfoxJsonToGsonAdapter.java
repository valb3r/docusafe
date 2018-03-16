package org.adorsys.docusafe.rest.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import springfox.documentation.spring.web.json.Json;

import java.lang.reflect.Type;

/** workaround, to run swagger2 with gson
 * THANX TO naXa
 * https://stackoverflow.com/questions/30219946/springfoxswagger2-does-not-work-with-gsonhttpmessageconverterconfig
 */

public class SpringfoxJsonToGsonAdapter implements JsonSerializer<Json> {

    @Override
    public JsonElement serialize(Json json, Type type, JsonSerializationContext context) {
        final JsonParser parser = new JsonParser();
        return parser.parse(json.value());
    }
}