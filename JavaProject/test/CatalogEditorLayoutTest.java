import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.nio.file.*;
import javax.swing.*;
import model.*;
import ui.panels.posPanel.CatalogEditor;
import util.config.UIStyle;

/**
 * Exercises the real add/edit dialogs without displaying or saving store data.
 */
public class CatalogEditorLayoutTest {
    private static int checks;

    private static void check(boolean value, String message) {
        checks++;
        if (!value)
            throw new AssertionError(message);
    }

    private static Component named(Container parent, String name) {
        for (Component child : parent.getComponents()) {
            if (name.equals(child.getName()))
                return child;
            if (child instanceof Container container) {
                Component found = named(container, name);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    private static void layout(Container parent) {
        parent.doLayout();
        for (Component child : parent.getComponents()) {
            if (child instanceof Container container)
                layout(container);
        }
    }

    private static void verify(CatalogStore store, CatalogItem item) {
        CatalogEditor editor = new CatalogEditor(null, store, item, () -> {
        });
        try {
            editor.pack();
            JTextField name = (JTextField) named(editor, "catalogName");
            JTextField price = (JTextField) named(editor, "catalogPrice");
            JComponent unit = (JComponent) named(editor, "catalogUnit");
            JScrollPane scroll = (JScrollPane) named(editor, "catalogFormScroll");
            JComponent actions = (JComponent) named(editor, "catalogActions");
            JPanel body = (JPanel) named(editor, "catalogFormBody");
            name.setText("Unsaved product name");
            price.setText("123.45");
            for (Dimension size : new Dimension[] { new Dimension(820, 640), new Dimension(420, 400),
                    new Dimension(380, 360), new Dimension(820, 640) }) {
                editor.setSize(size);
                for (int i = 0; i < 8; i++)
                    layout(editor);
                check(body.getWidth() == scroll.getViewport().getExtentSize().width,
                        "Form tracks viewport width at " + size);
                check(price.getWidth() >= 130 && name.getWidth() >= 200, "Fields keep usable widths at " + size);
                Rectangle footer = SwingUtilities.convertRectangle(actions.getParent(), actions.getBounds(),
                        editor.getContentPane());
                check(footer.y >= 0 && footer.y + footer.height <= editor.getContentPane().getHeight(),
                        "Save and Cancel remain inside the dialog at " + size);
                check("Unsaved product name".equals(name.getText()) && "123.45".equals(price.getText()),
                        "Resizing preserves entered values");
                if (size.width < 620) {
                    JScrollBar bar = scroll.getVerticalScrollBar();
                    check(bar.isVisible() && bar.getMaximum() > bar.getVisibleAmount(),
                            "Small dialogs expose vertical overflow");
                    bar.setValue(0);
                    scroll.dispatchEvent(new MouseWheelEvent(scroll, MouseEvent.MOUSE_WHEEL, System.currentTimeMillis(),
                            0, 30, 30, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, 3, 3));
                    check(bar.getValue() > 0, "Mouse wheel moves the actual form");
                    bar.setValue(bar.getMaximum());
                    Rectangle lastField = SwingUtilities.convertRectangle(unit.getParent(), unit.getBounds(), body);
                    Rectangle visible = new Rectangle(scroll.getViewport().getViewPosition(),
                            scroll.getViewport().getExtentSize());
                    check(visible.contains(lastField), "Last field is reachable at the bottom of the form");
                }
            }
            check(editor.getRootPane().getDefaultButton() == named(editor, "catalogSave"), "Enter still invokes Save");
            editor.setSize(380, 360);
            price.setText("invalid-price");
            editor.getRootPane().getDefaultButton().doClick();
            for (int i = 0; i < 8; i++)
                layout(editor);
            check(editor.getRootPane().getDefaultButton().isEnabled(),
                    "Invalid input keeps the form open for correction");
            Rectangle footer = SwingUtilities.convertRectangle(actions.getParent(), actions.getBounds(),
                    editor.getContentPane());
            check(footer.y + footer.height <= editor.getContentPane().getHeight(),
                    "Wrapped validation messages do not push Save offscreen");
        } finally {
            editor.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("laundry.dataDir", Files.createTempDirectory("catalog-layout-test-").toString());
        UIStyle.installDefaults();
        CatalogStore store = new CatalogStore();
        SwingUtilities.invokeAndWait(() -> {
            verify(store, null);
            verify(store, store.items().get(0));
        });
        System.out.println("PASS: " + checks + " add/edit sizing, scrolling and draft-preservation checks.");
    }
}
