package org.sugarcanemc.sugarcane.util.yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.sugarcanemc.sugarcane.config.SugarcaneConfig;
import org.sugarcanemc.sugarcane.util.Util;

public class YamlCommenter {
    private final HashMap<String, String> comments = new HashMap<>();
    private String Header = "";

    /**
     * Add comment to a config option.<br>
     * Supports multiline comments!
     *
     * @param path    Config path to add comment to
     * @param comment Comment to add
     */
    public void addComment(String path, String comment) {
        comments.put(path, comment);
    }

    /**
     * Set the header for this config file
     *
     * @param header Header to add
     */
    public void setHeader(String header) {
        Header = header;
    }

    /**
     * Saves comments to config file
     *
     * @param file File to save to
     * @throws IOException
     */
    public void saveComments(File file) throws IOException {
        ld("Saving comments...");
        ArrayList<String> lines = (ArrayList<String>) Files.readAllLines(file.toPath());
        lines.removeIf(s -> s.trim().startsWith("#") || s.trim().length() <= 3);
        lines.add(0, "# " + Header.replace("\n", "\n# ") + "\n");
        for (Map.Entry<String, String> _comment : comments.entrySet()) {
            ld(_comment.getKey());
            int line = YamlUtils.findKey(lines, _comment.getKey());
            if(line == -1) {
                System.out.printf("Couldn't find key %s in sugarcane.yml! Are you sure this key exists?\n", _comment.getKey());
                //System.exit(1);
                continue;
            }
            String prefix = " ".repeat(Util.getIndentation(lines.get(line))) + "# ";
            boolean noNewline = Util.getIndentation(lines.get(line)) > Util.getIndentation(lines.get(line-1));
            if (line >= 0)
                lines.add(line, (noNewline ?"":"\n") + prefix + _comment.getValue().replace("\n", "\n" + prefix));
            else System.out.printf("Failed to find key %s in %s!", _comment.getKey(), file);
            //String text = String.join("\n", lines);
            //ld(text);
        }
        String text = String.join("\n", lines);
        //Util.logDebug(text);
        FileWriter fw = new FileWriter(file);
        fw.write(text);
        fw.close();
    }
    private static void ld(String s) {
        //if(SugarcaneConfig.debug) System.out.println(s);
    }
}