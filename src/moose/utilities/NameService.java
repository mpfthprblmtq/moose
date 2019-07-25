/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moose.utilities;

import java.util.List;

/**
 * Code taken from the AutoCompleteDocument class
 * @author Samuel Sjoberg, http://samuelsjoberg.com
 * @version 1.0.0
 */
public class NameService implements CompletionService<String> {

    /** Our name data. */
    private final List<String> data;

    /**
     * Create a new <code>NameService</code> and populate it.
     * @param data
     */
    public NameService(List<String> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        data.forEach((o) -> {
            b.append(o).append("\n");
        });
        return b.toString();
    }

    @Override
    public String autoComplete(String startsWith) {
        // Naive implementation, but good enough for the sample
        String hit = null;
        for (String o : data) {
            if (o.startsWith(startsWith)) {
                // CompletionService contract states that we only
                // should return completion for unique hits.
                if (hit == null) {
                    hit = o;
                } else {
                    hit = null;
                    break;
                }
            }
        }
        return hit;
    }

}
