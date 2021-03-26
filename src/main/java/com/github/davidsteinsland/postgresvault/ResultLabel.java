package com.github.davidsteinsland.postgresvault;

import javax.swing.*;
import java.awt.*;

public class ResultLabel {
    private final Boolean isSuccess;

    public ResultLabel(final Boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public JLabel getLabel() {
        if (this.isSuccess == null) {
            return new JLabel();
        } else if (this.isSuccess) {
            final JLabel label = new JLabel("Success");
            label.setForeground(new Color(76, 175, 80));
            return label;
        } else {
            final JLabel label = new JLabel("Failure");
            label.setForeground(new Color(244, 67, 54));
            return label;
        }
    }
}
