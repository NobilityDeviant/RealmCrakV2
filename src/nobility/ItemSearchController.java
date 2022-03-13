package nobility;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.SortType;
import javafx.stage.Stage;
import nobility.handler.modules.rotmg.Item;
import nobility.handler.modules.rotmg.ItemParser;

import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;


/**
 * @author Nobility
 *
 */

public class ItemSearchController implements Initializable {
    @FXML private TextField search_field;
    @FXML private TableView<Item> main_table;
    //don't make these final
    @FXML protected TableColumn<Item, Label> id_column = new TableColumn<>();
    @FXML protected TableColumn<Item, Label> name_column = new TableColumn<>();
    @FXML protected TableColumn<Item, CheckBox> on_column = new TableColumn<>();
    @FXML protected TableColumn<Item, TextField> amount_column = new TableColumn<>();
    private final ObservableList<Item> itemData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        main_table.setPlaceholder(new Label(""));
        main_table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        id_column.setMaxWidth(1f * Integer.MAX_VALUE * 15);
        name_column.setMaxWidth(1f * Integer.MAX_VALUE * 55);
        on_column.setMaxWidth(1f * Integer.MAX_VALUE * 10);
        amount_column.setMaxWidth(1f * Integer.MAX_VALUE * 20);
        on_column.setSortType(SortType.ASCENDING);
        main_table.getSortOrder().add(on_column);
        id_column.setCellValueFactory(item -> {
            Label label = new Label();
            label.setMaxWidth(Double.MAX_VALUE);
            label.setText(item.getValue().getIdString());
            label.setTooltip(new Tooltip(item.getValue().getDescription()));
            label.setStyle("-fx-text-fill: white;");
            return new SimpleObjectProperty<>(label);
        });
        name_column.setCellValueFactory(item -> {
            String name = item.getValue().getNameString();
            Label label = new Label();
            label.setMaxWidth(Double.MAX_VALUE);
            label.setText(name);
            label.setTooltip(new Tooltip(item.getValue().getDescription()));
            label.setStyle("-fx-text-fill: white;");
            return new SimpleObjectProperty<>(label);
        });
        on_column.setCellValueFactory(row -> {
            Item item = row.getValue();
            CheckBox checkBox = new CheckBox();
            checkBox.setSelected(item.getSelectedBool());
            checkBox.getStylesheets().add(String.valueOf(Main.class.getResource("/css/settings.css")));
            checkBox.selectedProperty().addListener((obv, oldVal, newVal) -> item.setSelected(newVal));
            return new SimpleObjectProperty<>(checkBox);
        });
        amount_column.setCellValueFactory(row -> {
            Item item = row.getValue();
            TextField field = new TextField();
            field.setPrefWidth(Double.MAX_VALUE);
            field.setMaxWidth(Double.MAX_VALUE);
            field.getStylesheets().add(String.valueOf(Main.class.getResource("/css/settings.css")));
            field.setText(String.valueOf(item.getAmountInt()));
            field.textProperty().addListener((obv, oldVal, newVal) -> {
                try {
                    item.setAmount(Integer.parseInt(newVal));
                } catch (Exception e) {
                    item.setAmount(0);
                    field.setText("");
                }
            });
            return new SimpleObjectProperty<>(field);
        });
        FilteredList<Item> filteredData = new FilteredList<>(itemData, item -> true);
        Comparator<CheckBox> enabledComparator = (a1, a2) -> Boolean.compare(a2.isSelected(), a1.isSelected());
        Comparator<Label> idComparator = (o1, o2) -> Integer.compare(Integer.parseInt(o2.getText()), Integer.parseInt(o1.getText()));
        id_column.setComparator(idComparator);
        on_column.setComparator(enabledComparator);
        search_field.textProperty().addListener((observable, oldValue, newValue) -> filteredData.setPredicate(item -> {
            if (newValue == null || newValue.isEmpty()) {
                return true;
            }
            String filter = newValue.toLowerCase();
            return item.getIdString().toLowerCase().contains(filter)
                    || item.getNameString().toLowerCase().contains(filter);
        }));
        SortedList<Item> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(main_table.comparatorProperty());
        main_table.setItems(sortedData);
    }

    public void setDefault(ItemParser items, Stage stage) {
        stage.widthProperty().addListener(((observable, oldValue, newValue) -> main_table.refresh()));
        itemData.addAll(items.getItemList());
    }

}
