package edu.asu.snac.manager;

import org.json.JSONArray;
import org.json.JSONObject;

public class Marshalling {
  public static final String SERVICE_NAME = "service";
  public static final String METHOD_NAME = "method";
  public static final String PARAM_LIST = "params";

  JSONObject obj = new JSONObject();

  public String build() {
    return obj.toString();
  }

  public Marshalling setService(String s) {
    obj.put(SERVICE_NAME, s);
    return this;
  }

  public Marshalling setMethod(String m) {
    obj.put(METHOD_NAME, m);
    return this;
  }

  public Marshalling addParams(Object... objects) {
    for (Object o : objects) {
      addParam(o);
    }
    return this;
  }

  public Marshalling addParam(Object o) {
    if (!obj.has(PARAM_LIST)) {
      obj.put(PARAM_LIST, new JSONArray());
    }
    ((JSONArray) obj.get(PARAM_LIST)).put(o);
    return this;
  }
}
