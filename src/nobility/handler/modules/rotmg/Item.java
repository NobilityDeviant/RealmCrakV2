package nobility.handler.modules.rotmg;

import javafx.beans.property.*;

import java.io.Serializable;

public class Item implements Serializable {

    private static final long serialVersionUID = 7872092398598330268L;

    private transient StringProperty name;
    private transient StringProperty id;
    private transient BooleanProperty selected;
    private transient IntegerProperty amount;
    private final String nameString;
    private final String idString;
    private String description;
    private boolean selectedBool;
    private int amountInt;

    public Item(String name, String id) {
        this.name = new SimpleStringProperty(name);
        this.id = new SimpleStringProperty(id);
        this.selected = new SimpleBooleanProperty(false);
        this.amount = new SimpleIntegerProperty(0);
        this.nameString = name;
        this.idString = id;
        this.selectedBool = false;
        this.amountInt = 0;
    }

    public StringProperty getName() {
        if (name == null)
            name = new SimpleStringProperty(nameString);
        return name;
    }

    public StringProperty getId() {
        if (id == null)
            id = new SimpleStringProperty(idString);
        return id;
    }

    public BooleanProperty getSelected() {
        if (selected == null)
            selected = new SimpleBooleanProperty(selectedBool);
        return selected;
    }

    public IntegerProperty getAmount() {
        if (amount == null)
            amount = new SimpleIntegerProperty(amountInt);
        return amount;
    }

    public void setSelected(boolean selected) {
        if (this.selected == null)
            this.selected = new SimpleBooleanProperty(selectedBool);

        this.selected.set(selected);
        selectedBool = selected;
    }

    public void setAmount(int i) {
        if (amount == null)
            amount = new SimpleIntegerProperty(amountInt);
        amount.set(i);
        amountInt = i;
    }

    public boolean getSelectedBool() {
        return selectedBool;
    }

    public String getNameString() {
        return nameString;
    }

    public String getIdString() {
        return idString;
    }

    public int getAmountInt() {
        return amountInt;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "ID: " + idString + " Name: " + nameString;
    }

    @Override
    public int hashCode() {
        return Integer.parseInt(idString);
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof Item){
            return ((Item) o).idString.equals(idString);
        }
        return false;
    }

}

