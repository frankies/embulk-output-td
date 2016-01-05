package com.treasuredata.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

public class TDColumn
{
    private String name;
    private TDColumnType type;
    private byte[] key;

    public TDColumn(String name, TDColumnType type, byte[] key)
    {
        this.name = name;
        this.type = type;
        this.key = key;
    }

    public String getName()
    {
        return name;
    }

    public TDColumnType getType()
    {
        return type;
    }

    public byte[] getKey()
    {
        return key;
    }

    private static JSONArray castToArray(Object obj)
    {
        if (obj instanceof JSONArray) {
            return (JSONArray) obj;
        }
        else {
            throw new RuntimeJsonMappingException("Not an json array: " + obj);
        }
    }

    public static List<TDColumn> parseTuple(String jsonStr)
    {
        if (Strings.isNullOrEmpty(jsonStr)) {
            return new ArrayList<>(0);
        }

        // unescape json quotation
        try {
            String unescaped = jsonStr.replaceAll("\\\"", "\"");
            JSONArray arr = castToArray(new JSONParser().parse(unescaped));
            List<TDColumn> columnList = new ArrayList<>(arr.size());
            for (Object e : arr) {
                JSONArray columnNameAndType = castToArray(e);
                String[] s = new String[columnNameAndType.size()];
                for (int i = 0; i < columnNameAndType.size(); ++i) {
                    s[i] = columnNameAndType.get(i).toString();
                }
                columnList.add(parseTuple(s));
            }
            return columnList;
        }
        catch (ParseException e) {
            return new ArrayList<>(0);
        }
    }

    @JsonCreator
    public static TDColumn parseTuple(String[] tuple)
    {
        // TODO encode key in some ways
        if (tuple != null && tuple.length == 2) {
            return new TDColumn(
                    tuple[0],
                    TDColumnTypeDeserializer.parseColumnType(tuple[1]),
                    tuple[0].getBytes());

        }
        else if (tuple != null && tuple.length == 3) {
            return new TDColumn(
                    tuple[0],
                    TDColumnTypeDeserializer.parseColumnType(tuple[1]),
                    tuple[2].getBytes());

        }
        else {
            throw new RuntimeJsonMappingException("Unexpected string tuple to deserialize TDColumn");
        }
    }

    @JsonValue
    public String[] getTuple()
    {
        return new String[] { name, type.toString(), new String(key) };
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TDColumn other = (TDColumn) obj;
        return Objects.equal(this.name, other.name) &&
            Objects.equal(type, other.type) &&
            Objects.equal(key, other.key);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(name, type, key);
    }
}
