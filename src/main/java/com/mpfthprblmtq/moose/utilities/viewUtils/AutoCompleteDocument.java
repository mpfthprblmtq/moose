/*
 *  Proj:   Moose
 *  File:   AutoCompleteDocument.java
 *  Desc:   Service class for auto tagging
 *          NOTE: Not mine, but edited to fit my use case
 *
 *  Copyright Samuel Sjoberg
 *  Copyright Pat Ripley 2018-2023
 */

package com.mpfthprblmtq.moose.utilities.viewUtils;

// imports
import javax.swing.text.*;

/**
 * A {@link Document} performing auto-completion on the inserted text. This
 * document can be used on any {@link JTextComponent}.
 * <p>
 * The completion will only happen for inserts, that is, when characters are
 * typed. If characters are erased, no new completion is suggested until a new
 * character is typed.
 *
 * @see CompletionService
 *
 * @author Samuel Sjoberg, http://samuelsjoberg.com
 * @version 1.0.0
 */
// class AutoCompleteDocument
public class AutoCompleteDocument extends PlainDocument {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Completion service. */
    private final CompletionService<?> completionService;

    /** The document owner. */
    private final JTextComponent documentOwner;

    /**
     * Create a new <code>AutoCompletionDocument</code>.
     *
     * @param service
     *            the service to use when searching for completions
     * @param documentOwner
     *            the document owner
     */
    public AutoCompleteDocument(CompletionService<?> service,
                                JTextComponent documentOwner) {
        this.completionService = service;
        this.documentOwner = documentOwner;
    }

    /**
     * Look up the completion string.
     *
     * @param str
     *            the prefix string to complete
     * @return the completion or <code>null</code> if completion was found.
     */
    protected String complete(String str) {
        Object o = completionService.autoComplete(str);
        return o == null ? null : o.toString();
    }

    @Override
    public void insertString(int offs, String str, AttributeSet a)
            throws BadLocationException {
        if (str == null || str.length() == 0) {
            return;
        }

        String text = getText(0, offs); // Current text.
        String completion = complete(text + str);
        int length = offs + str.length();
        if (completion != null && text.length() > 0) {
            str = completion.substring(length - 1);
            super.insertString(offs, str, a);
            documentOwner.select(length, getLength());
        } else {
            super.insertString(offs, str, a);
        }
    }
}

// interface CompletionService
interface CompletionService<T> {

    /**
     * Autocomplete the passed string. The method will return the matching
     * object when one single object matches the search criteria. As long as
     * multiple objects stored in the service matches, the method will return
     * <code>null</code>.
     *
     * @param startsWith
     *            prefix string
     * @return the matching object or <code>null</code> if multiple matches are
     *         found.
     */
    T autoComplete(String startsWith);
}


