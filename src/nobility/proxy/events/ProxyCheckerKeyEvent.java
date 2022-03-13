package nobility.proxy.events;

import javafx.event.EventHandler;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import nobility.proxy.components.entities.Proxy;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class ProxyCheckerKeyEvent implements EventHandler<KeyEvent> {
    @Override
    public void handle(KeyEvent event) {
        if (event.getSource() instanceof TableView) {
            if (event.getCode().equals(KeyCode.C)) { // copy from TableView
                TableView<Proxy> tableView = (TableView) event.getSource();
                Proxy proxy = tableView.getSelectionModel().getSelectedItem();
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                        new StringSelection(proxy.getIp() + ":" + proxy.getPort()), null
                );
            }
        }
    }
}
