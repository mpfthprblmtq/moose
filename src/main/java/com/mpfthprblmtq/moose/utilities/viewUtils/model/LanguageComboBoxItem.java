package com.mpfthprblmtq.moose.utilities.viewUtils.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LanguageComboBoxItem {
    private String key;
    private String value;

    @Override
    public String toString() {
        return value;
    }
}
