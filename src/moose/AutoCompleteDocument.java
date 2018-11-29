package moose;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import java.util.Arrays;
import java.util.List;

/**
 * A {@link Document} performing auto completion on the inserted text. This
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
public class AutoCompleteDocument extends PlainDocument {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Completion service. */
    private CompletionService<?> completionService;

    /** The document owner. */
    private JTextComponent documentOwner;

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

class NameService implements CompletionService<String> {

    /** Our name data. */
    private List<String> data;

    /**
     * Create a new <code>NameService</code> and populate it.
     */
    public NameService(List<String> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (String o : data) {
            b.append(o).append("\n");
        }
        return b.toString();
    }

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
