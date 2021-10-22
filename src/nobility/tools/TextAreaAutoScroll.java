package nobility.tools;

import javafx.scene.control.TextArea;

public class TextAreaAutoScroll extends TextArea {

    public void appendText(String text, Boolean moveScrollBar) {
        if (moveScrollBar)
            this.appendText(text);
        else {
            double scrollTop = getScrollTop();
            setText(getText() + text);
            setScrollTop(scrollTop);
        }
    }
}
