package feedsplugin

import com.sun.syndication.feed.module.itunes.*
import com.sun.syndication.feed.module.itunes.types.Duration

class EnhancediTunesEntryInformationImpl extends EntryInformationImpl {
    void setDurationText(String s) {
        super.setDuration(new Duration(s))
    }
}
