package com.multicriteriasdkdemo;




import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;

public class JsonUtils {

    /**
     * 默认的序列化工具，如果double值是负值则排序此字段
     */
    public static Gson gson = new GsonBuilder().registerTypeAdapter(Double.class, new JsonSerializer<Double>() {
        @Override
        public JsonElement serialize(final Double src, final Type typeOfSrc, final JsonSerializationContext context) {
            BigDecimal value = BigDecimal.valueOf(src);
            return new JsonPrimitive(value);
        }
    }).create() ;


    public static Gson gsonNoEscape = new GsonBuilder().registerTypeAdapter(Double.class, new JsonSerializer<Double>() {
        @Override
        public JsonElement serialize(final Double src, final Type typeOfSrc, final JsonSerializationContext context) {
            BigDecimal value = BigDecimal.valueOf(src);
            return new JsonPrimitive(value);
        }
    }).disableHtmlEscaping().create();

    public static JsonParser jsonParser = new JsonParser();

    public static String toJson(Object src) {
        return gson.toJson(src);
    }
    public static String toJsonL(Object src) {
        return gsonNoEscape.toJson(src);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        try {
            return gson.fromJson(json, classOfT);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T fromJson(JsonElement json, Class<T> classOfT) {
        try {
            return gson.fromJson(json, classOfT);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T fromJson(JsonElement json, Type type) {
        try {
            return gson.fromJson(json, type);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static  <T>  ArrayList<T> fromJsonList(String json,Class<T> tClass) {
        try {
            ArrayList<T> list = new ArrayList<T>();
            JsonArray array = new JsonParser().parse(json).getAsJsonArray();
            for (final  JsonElement element : array){
                list.add(gson.fromJson(element,tClass));
            }
            return list;
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static <T> T fromJson(String json, Type type) {
        try {
            return gson.fromJson(json, type);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JsonElement jsonToElement(String json) {
        JsonElement jsonElement = null;
        try {
            jsonElement = jsonParser.parse(json);
        }catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return jsonElement;

    }

    public static String toJson(InputStream inputStream) {
        return toString(inputStream, "utf-8");
    }

    public static String toString(InputStream is, String charset) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                } else {
                    sb.append(line).append("\n");
                }
            }
            reader.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
