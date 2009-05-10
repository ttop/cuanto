package feedsplugin

import com.sun.syndication.feed.module.itunes.*
import com.sun.syndication.feed.module.itunes.types.*

class EnhancediTunesFeedInformationImpl extends FeedInformationImpl {
    void setCategories(List cats) {
        def newcats = []
        cats.each { entry ->
            def c
            if (entry instanceof List) {
                c = new Category(entry[0])
                if (entry.size() > 1) {
                    c.subcategory = new Subcategory(entry[1])
                }
            } else if (entry instanceof String) {
                c = new Category(entry)
            } else if (entry instanceof Category) {
                c = entry
            }
            newcats << c
        }
        super.setCategories(newcats)
    }
}
