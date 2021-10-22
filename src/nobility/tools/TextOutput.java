package nobility.tools;

import javafx.application.Platform;
import nobility.model.Model;
import nobility.save.Defaults;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

public class TextOutput extends OutputStream {

    private final TextAreaAutoScroll textArea;
    private StringBuilder sb = new StringBuilder();
    private int size;
    private final Model model;

    public TextOutput(final TextAreaAutoScroll textArea, Model model) {
        this.textArea = textArea;
        this.model = model;
        sb.append(time()).append(" ");
    }

    private String time() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR);
        int minute = c.get(Calendar.MINUTE);
        int am = c.get(Calendar.AM_PM);
        return "[" + hour + ":" + (String.valueOf(minute).length() == 1 ? "0" : "")
                + minute + (am == 0 ? "AM" : "PM") + "]";
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }

    @Override
    public void write(int b) throws IOException {

        if (b == '\r')
            return;

        if (b == '\n') {
            final String text = sb.toString() + "\n";
            SwingUtilities.invokeLater(() -> {
                Platform.runLater(() -> textArea.appendText(text,
                        model.save().getBoolean(Defaults.AUTOSCROLL)));
                size++;
                if (model.save().getBoolean(Defaults.EMPTYCONSOLECYCLE)) {
                    if (size >= 200) {
                        clear();
                    }
                }
            });
            sb.setLength(0);
            sb.append(time()).append(" ");
            return;
        }
        if (sb.length() == 0) {
            sb.append(time()).append(" ");
        }
        sb.append((char) b);
    }
    public void clear() {
        sb = new StringBuilder();
        size = 0;
        Platform.runLater(() -> model.getFxModel().getOut().clear());
    }
    public int getSize() {
        return size;
    }
}