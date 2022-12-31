/*
   Proj:   Moose
   File:   NameService.java
   Desc:   Service class for autocomplete

   Copyright Samuel Sjoberg
   Copyright Pat Ripley 2018-2023
 */

// package
package com.mpfthprblmtq.moose.utilities.viewUtils;

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
     * @param data, the data to create the NameService with
     */
    public NameService(List<String> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        data.forEach((o) -> b.append(o).append("\n"));
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
