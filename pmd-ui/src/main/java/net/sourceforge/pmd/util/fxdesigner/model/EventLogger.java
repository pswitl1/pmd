/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.util.fxdesigner.model;

import static net.sourceforge.pmd.util.fxdesigner.model.LogEntry.Category.PARSE_EXCEPTION;
import static net.sourceforge.pmd.util.fxdesigner.model.LogEntry.Category.PARSE_OK;
import static net.sourceforge.pmd.util.fxdesigner.model.LogEntry.Category.SELECTION_EVENT_TRACING;
import static net.sourceforge.pmd.util.fxdesigner.model.LogEntry.Category.XPATH_EVALUATION_EXCEPTION;
import static net.sourceforge.pmd.util.fxdesigner.model.LogEntry.Category.XPATH_OK;
import static net.sourceforge.pmd.util.fxdesigner.util.DesignerUtil.countNotMatching;
import static net.sourceforge.pmd.util.fxdesigner.util.DesignerUtil.reduceIfPossible;

import java.time.Duration;
import java.util.EnumSet;
import java.util.Objects;

import org.reactfx.EventSource;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;
import org.reactfx.collection.LiveArrayList;
import org.reactfx.collection.LiveList;
import org.reactfx.value.Val;

import net.sourceforge.pmd.util.fxdesigner.model.LogEntry.Category;
import net.sourceforge.pmd.util.fxdesigner.model.LogEntry.LogEntryWithData;
import net.sourceforge.pmd.util.fxdesigner.util.DesignerUtil;
import net.sourceforge.pmd.util.fxdesigner.util.NodeSelectionSource.NodeSelectionEvent;


/**
 * Logs events. Stores the whole log in case no view was open.
 *
 * @author Clément Fournier
 * @since 6.0.0
 */
public class EventLogger {

    /**
     * Exceptions from XPath evaluation or parsing are never emitted
     * within less than that time interval to keep them from flooding the tableview.
     */
    private static final Duration PARSE_EXCEPTION_REDUCTION_DELAY = Duration.ofMillis(3000);
    private static final Duration EVENT_TRACING_REDUCTION_DELAY = Duration.ofMillis(200);
    private final EventSource<LogEntry> latestEvent = new EventSource<>();
    private final LiveList<LogEntry> fullLog = new LiveArrayList<>();


    public EventLogger() {

        EventStream<LogEntryWithData<NodeSelectionEvent>> eventTraces =
            reduceIfPossible(filterOnCategory(latestEvent, false, SELECTION_EVENT_TRACING).map(t -> (LogEntryWithData<NodeSelectionEvent>) t),
                             // the user data for those is the event
                             // if they're the same event we reduce them together
                             (lastEv, newEv) -> Objects.equals(lastEv.getUserData(), newEv.getUserData()),
                             LogEntryWithData::reduceEventTrace,
                             EVENT_TRACING_REDUCTION_DELAY);

        EventStream<LogEntry> onlyParseException = deleteOnSignal(latestEvent, PARSE_EXCEPTION, PARSE_OK);
        EventStream<LogEntry> onlyXPathException = deleteOnSignal(latestEvent, XPATH_EVALUATION_EXCEPTION, XPATH_OK);

        EventStream<LogEntry> otherExceptions =
            filterOnCategory(latestEvent, true, PARSE_EXCEPTION, XPATH_EVALUATION_EXCEPTION, SELECTION_EVENT_TRACING)
                .filter(it -> !it.getCategory().isFlag());

        EventStreams.merge(eventTraces, onlyParseException, otherExceptions, onlyXPathException)
                    .subscribe(fullLog::add);
    }


    /** Number of log entries that were not yet examined by the user. */
    public Val<Integer> numNewLogEntriesProperty() {
        return countNotMatching(fullLog.map(LogEntry::wasExaminedProperty));
    }


    private static EventStream<LogEntry> deleteOnSignal(EventStream<LogEntry> input, Category normal, Category deleteSignal) {
        return DesignerUtil.deleteOnSignal(filterOnCategory(input, false, normal, deleteSignal),
                                           x -> x.getCategory() == deleteSignal,
                                           PARSE_EXCEPTION_REDUCTION_DELAY);
    }


    private static EventStream<LogEntry> filterOnCategory(EventStream<LogEntry> input, boolean complement, Category first, Category... selection) {
        EnumSet<Category> considered = EnumSet.of(first, selection);
        EnumSet<Category> complemented = complement ? EnumSet.complementOf(considered) : considered;

        return input.filter(e -> complemented.contains(e.getCategory()));
    }


    /** Total number of log entries. */
    public Val<Integer> numLogEntriesProperty() {
        return fullLog.sizeProperty();
    }


    public void logEvent(LogEntry event) {
        if (event != null) {
            latestEvent.push(event);
        }
    }

    /**
     * Returns the full log.
     */
    public LiveList<LogEntry> getLog() {
        return fullLog;
    }
}
