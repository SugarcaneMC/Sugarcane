package org.sugarcanemc.sugarcane.util.yaml;

import java.util.ArrayList;
import java.util.List;
import org.sugarcanemc.sugarcane.util.Util;

public class YamlUtils {
    public static int findKey(List<String> lines, String key) {
        String[] parts = key.split("\\.");
        //Util.logDebug("Starting to look for " + String.join(".", parts) + "...");
        int _line = 0;
        int indent = 0;
        List<String> _cpath = new ArrayList<>();
        for (String part : parts) {
            _cpath.add(part);
            //Util.logDebug("Looking for " + String.join(".", _cpath) + "...");
            for (int i = _line; i < lines.size(); i++) {
                if (lines.get(i).trim().startsWith(part)) {
                    _line = i;
                    //Util.logDebug(String.format("Found key %s at line %d with %d indents (%s)%n", String.join(".", _cpath), i, indent, lines.get(i)));
                    if (String.join(".", _cpath).equals(key)) {
                        return _line;
                    }
                }
            }
        }
        return -1;
    }
}
