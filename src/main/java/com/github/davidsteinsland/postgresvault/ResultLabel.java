package com.github.davidsteinsland.postgresvault;

import javax.swing.JLabel;
import java.awt.Color;

public class ResultLabel {
    private final Boolean isSuccess;

    public ResultLabel(final Boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public JLabel getLabel() {
        if (this.isSuccess == null) {
            return new JLabel();
        }
        else if (this.isSuccess) {
            final JLabel label = new JLabel("Success");
            label.setForeground(Color.GREEN);
            return  label;
        }
        else {
            final JLabel label = new JLabel("Failure");
            label.setForeground(Color.RED);
            return  label;
        }
    }
}
