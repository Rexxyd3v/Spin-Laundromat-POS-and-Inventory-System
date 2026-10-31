package util.components;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import util.config.UIStyle;
import static util.components.ModernUI.*;

/**
 * A keyboard-editable quantity field with distinct, touch-sized step actions.
 */
public final class QuantityControl extends JPanel {
    private final JSpinner spinner;

    public QuantityControl(double value, double min, double max, double step, boolean fractional, String name) {
        super(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createLineBorder(UIStyle.INPUT_BORDER));
        spinner = new JSpinner(new SpinnerNumberModel(value, min, max, step));
        spinner.setUI(new javax.swing.plaf.basic.BasicSpinnerUI() {
            protected Component createNextButton() {
                return null;
            }

            protected Component createPreviousButton() {
                return null;
            }
        });
        spinner.setEditor(new JSpinner.NumberEditor(spinner, fractional ? "0.###" : "0"));
        spinner.setBorder(null);
        JFormattedTextField input = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        input.setHorizontalAlignment(SwingConstants.CENTER);
        input.setFont(UIStyle.BOLD);
        input.setBorder(new EmptyBorder(3, 2, 3, 2));
        input.setBackground(Color.WHITE);
        spinner.getAccessibleContext().setAccessibleName("Quantity of " + name);
        JButton minus = iconButton("Decrease " + name, AppIcon.Name.MINUS),
                plus = iconButton("Increase " + name, AppIcon.Name.PLUS);
        minus.setPreferredSize(new Dimension(32, 34));
        plus.setPreferredSize(new Dimension(32, 34));
        add(minus, BorderLayout.WEST);
        add(spinner, BorderLayout.CENTER);
        add(plus, BorderLayout.EAST);
        setPreferredSize(new Dimension(136, 36));
        minus.addActionListener(e -> {
            if (commit()) {
                Object previous = spinner.getPreviousValue();
                if (previous != null)
                    spinner.setValue(previous);
            }
        });
        plus.addActionListener(e -> {
            if (commit()) {
                Object next = spinner.getNextValue();
                if (next != null)
                    spinner.setValue(next);
            }
        });
        spinner.addChangeListener(e -> {
            minus.setEnabled(((Number) spinner.getValue()).doubleValue() > min);
            plus.setEnabled(((Number) spinner.getValue()).doubleValue() < max);
        });
        minus.setEnabled(value > min);
    }

    private boolean commit() {
        try {
            spinner.commitEdit();
            return true;
        } catch (java.text.ParseException ex) {
            spinner.requestFocusInWindow();
            Toolkit.getDefaultToolkit().beep();
            return false;
        }
    }

    public JSpinner spinner() {
        return spinner;
    }
}
