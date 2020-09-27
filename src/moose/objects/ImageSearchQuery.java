package moose.objects;

import java.io.File;
import java.util.List;

public class ImageSearchQuery {
    private String query;
    private File dir;
    private List<Integer> rows;

    public ImageSearchQuery() {

    }

    public ImageSearchQuery(String query, File dir, List<Integer> rows) {
        this.setQuery(query);
        this.setDir(dir);
        this.setRows(rows);
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public File getDir() {
        return dir;
    }

    public void setDir(File dir) {
        this.dir = dir;
    }

    public List<Integer> getRows() {
        return rows;
    }

    public void setRows(List<Integer> rows) {
        this.rows = rows;
    }

    public static boolean contains(List<ImageSearchQuery> queries, String query) {
        for (int i = 0; i < queries.size(); i++) {
            if (queries.get(i).getQuery().equals(query)) {
                return true;
            }
        }
        return false;
    }

    public static int getIndex(List<ImageSearchQuery> queries, String query) {
        for (int i = 0; i < queries.size(); i++) {
            if (queries.get(i).getQuery().equals(query)) {
                return i;
            }
        }
        return -1;
    }
}
