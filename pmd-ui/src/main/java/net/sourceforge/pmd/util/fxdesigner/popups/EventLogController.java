/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.util.fxdesigner.popups;

import static net.sourceforge.pmd.util.fxdesigner.model.LogEntry.Category.PARSE_EXCEPTION;
import static net.sourceforge.pmd.util.fxdesigner.model.LogEntry.Category.PARSE_OK;
import static net.sourceforge.pmd.util.fxdesigner.model.LogEntry.Category.XPATH_EVALUATION_EXCEPTION;
import static net.sourceforge.pmd.util.fxdesigner.model.LogEntry.Category.XPATH_OK;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import org.reactfx.EventStream;
import org.reactfx.EventStreams;
import org.reactfx.value.Val;
import org.reactfx.value.Var;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.util.fxdesigner.DesignerRoot;
import net.sourceforge.pmd.util.fxdesigner.MainDesignerController;
import net.sourceforge.pmd.util.fxdesigner.model.LogEntry;
import net.sourceforge.pmd.util.fxdesigner.model.LogEntry.Category;
import net.sourceforge.pmd.util.fxdesigner.util.AbstractController;
import net.sourceforge.pmd.util.fxdesigner.util.DesignerUtil;
import net.sourceforge.pmd.util.fxdesigner.util.SoftReferenceCache;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;


/**
 * @author Clément Fournier
 * @since 6.0.0
 */
public class EventLogController extends AbstractController {

    /**
     * Exceptions from XPath evaluation or parsing are never emitted
     * within less than that time interval to keep them from flooding the tableview.
     */
    private static final Duration PARSE_EXCEPTION_DELAY = Duration.ofMillis(3000);

    private final DesignerRoot designerRoot;
    private final MainDesignerController mediator;

    @FXML
    private TableView<LogEntry> eventLogTableView;
    @FXML
    private TableColumn<LogEntry, Date> logDateColumn;
    @FXML
    private TableColumn<LogEntry, Category> logCategoryColumn;
    @FXML
    private TableColumn<LogEntry, String> logMessageColumn;
    @FXML
    private TextArea logDetailsTextArea;

    private Var<List<Node>> selectedErrorNodes = Var.newSimpleVar(Collections.emptyList());


    private SoftReferenceCache<Stage> popupStageCache = new SoftReferenceCache<>(this::createStage);

    private Var<Integer> numLogEntries = Var.newSimpleVar(0);

    public EventLogController(DesignerRoot owner, MainDesignerController mediator) {
        this.designerRoot = owner;
        this.mediator = mediator;
    }


    @Override
    protected void beforeParentInit() {

        logCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        logMessageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        logDateColumn.setCellValueFactory(entry -> new SimpleObjectProperty<>(entry.getValue().getTimestamp()));
        logDateColumn.setCellFactory(column -> new TableCell<LogEntry, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(dateFormat.format(item));
                }
            }
        });

        EventStream<LogEntry> onlyParseException = designerRoot.getLogger().getLog()
                                                               .filter(x -> x.getCategory() == PARSE_EXCEPTION || x.getCategory() == PARSE_OK)
                                                               .successionEnds(PARSE_EXCEPTION_DELAY)
                                                               // don't output anything when the last state recorded was OK
                                                               .filter(x -> x.getCategory() != PARSE_OK);

        EventStream<LogEntry> onlyXPathException = designerRoot.getLogger().getLog()
                                                               .filter(x -> x.getCategory() == XPATH_EVALUATION_EXCEPTION || x.getCategory() == XPATH_OK)
                                                               .successionEnds(PARSE_EXCEPTION_DELAY)
                                                               // don't output anything when the last state recorded was OK
                                                               .filter(x -> x.getCategory() != XPATH_OK);

        EnumSet<Category> otherExceptionSet = EnumSet.complementOf(EnumSet.of(PARSE_EXCEPTION, XPATH_EVALUATION_EXCEPTION, PARSE_OK, XPATH_OK));

        EventStream<LogEntry> otherExceptions = designerRoot.getLogger().getLog()
                                                            .filter(x -> otherExceptionSet.contains(x.getCategory()));

        EventStreams.merge(onlyParseException, otherExceptions, onlyXPathException)
                    .hook(e -> numLogEntries.setValue(numLogEntries.getValue() + 1))
                    .subscribe(t -> eventLogTableView.getItems().add(t));

        eventLogTableView.getSelectionModel()
                         .selectedItemProperty()
                         .addListener((obs, oldVal, newVal) -> onExceptionSelectionChanges(oldVal, newVal));

        EventStreams.combine(EventStreams.changesOf(eventLogTableView.focusedProperty()),
                             EventStreams.changesOf(selectedErrorNodes));

        EventStreams.valuesOf(eventLogTableView.focusedProperty())
                    .successionEnds(Duration.ofMillis(100))
                    .subscribe(b -> {
                        if (b) {
                            mediator.handleSelectedNodeInError(selectedErrorNodes.getValue());
                        } else {
                            mediator.resetSelectedErrorNodes();
                        }
                    });

        selectedErrorNodes.values().subscribe(mediator::handleSelectedNodeInError);

        eventLogTableView.resizeColumn(logMessageColumn, -1);

        logMessageColumn.prefWidthProperty()
                        .bind(eventLogTableView.widthProperty()
                                               .subtract(logCategoryColumn.getPrefWidth())
                                               .subtract(logDateColumn.getPrefWidth())
                                               .subtract(2)); // makes it work
        logDateColumn.setSortType(SortType.DESCENDING);

    }


    private void handleSelectedEntry(LogEntry entry) {
        if (entry == null) {
            selectedErrorNodes.setValue(Collections.emptyList());
            return;
        }
        switch (entry.getCategory()) {
        case OTHER:
            break;
        case PARSE_EXCEPTION:
            // TODO
            break;
        case TYPERESOLUTION_EXCEPTION:
        case SYMBOL_FACADE_EXCEPTION:
            DesignerUtil.stackTraceToXPath(entry.getThrown()).map(mediator::runXPathQuery).ifPresent(selectedErrorNodes::setValue);
            break;
        default:
            break;
        }
    }


    public void showPopup() {
        popupStageCache.getValue().show();
    }


    public void hidePopup() {
        popupStageCache.getValue().hide();
    }


    private void onExceptionSelectionChanges(LogEntry oldVal, LogEntry newVal) {
        logDetailsTextArea.setText(newVal == null ? "" : newVal.getStackTrace());

        if (!Objects.equals(newVal, oldVal)) {
            handleSelectedEntry(newVal);
        }
    }


    public Val<Integer> numLogEntriesProperty() {
        return numLogEntries;
    }


    private Stage createStage() {
        FXMLLoader loader = new FXMLLoader(DesignerUtil.getFxml("event-log.fxml"));
        loader.setController(this);

        final Stage dialog = new Stage();
        dialog.initOwner(designerRoot.getMainStage().getScene().getWindow());
        dialog.initModality(Modality.WINDOW_MODAL);
        // dialog.initStyle(StageStyle.UNDECORATED);

        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        Scene scene = new Scene(root);
        dialog.setTitle("Event log");
        dialog.setScene(scene);
        return dialog;
    }

}
