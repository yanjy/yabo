package navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bare Menu
 *
 * Menu as they come from the navigation.yml file
 */
public class Menu {
    public Menu parent;
    public String name;
    public String text;
    public String action;
    public String url;

    public List<Menu> children = new ArrayList<Menu>();
    public Set<String> labels = new HashSet<String>();
    public Map<String, String> params = new HashMap<String, String>();
    public Map<String, Object> properties = new HashMap<String, Object>();
    public boolean hasLink() {
        return url != null || action != null;
    }
}