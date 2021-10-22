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
import nobility.moduleshandler.modules.rotmg.Item;
import nobility.moduleshandler.modules.rotmg.ItemParser;

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
    @FXML private TableColumn<Item, Label> main_column1 = new TableColumn<>();
    @FXML private TableColumn<Item, Label> main_column2 = new TableColumn<>();
    @FXML private TableColumn<Item, CheckBox> main_column3 = new TableColumn<>();
    @FXML private TableColumn<Item, TextField> main_column4 = new TableColumn<>();
    private final ObservableList<Item> itemData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
    }

    public void setDefault(ItemParser items) {
        itemData.addAll(items.getItemList());
        main_column3.setSortType(SortType.ASCENDING);
        main_table.getSortOrder().add(main_column3);
        main_column1.setCellValueFactory(item -> {
            Label label = new Label();
            label.setText(item.getValue().getIdString());
            label.setTooltip(new Tooltip(item.getValue().getDescription()));
            label.setStyle("-fx-text-fill: white;");
            return new SimpleObjectProperty<>(label);
        });
        main_column2.setCellValueFactory(item -> {
            String name = item.getValue().getNameString();
            Label label = new Label();
            label.setText(name);
            label.setTooltip(new Tooltip(item.getValue().getDescription()));
            label.setStyle("-fx-text-fill: white;");
            return new SimpleObjectProperty<>(label);
        });
        main_column3.setCellValueFactory(row -> {
            Item item = row.getValue();
            CheckBox checkBox = new CheckBox();
            checkBox.setSelected(item.getSelectedBool());
            checkBox.selectedProperty().addListener((obv, oldVal, newVal) -> item.setSelected(newVal));
            return new SimpleObjectProperty<>(checkBox);
        });
        main_column4.setCellValueFactory(row -> {
            Item item = row.getValue();
            TextField field = new TextField();
            int width = 60;
            field.setPrefWidth(width);
            field.setMaxWidth(width);
            field.setMinWidth(width);
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
        main_column1.setComparator(idComparator);
        main_column3.setComparator(enabledComparator);
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

}
