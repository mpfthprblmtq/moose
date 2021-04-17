package moose.services;

import moose.Main;

import javax.swing.*;
import java.awt.*;

public class DialogService {

//    public static int showOptionDialog(
//            Component component,
//            String message,
//            String title,
//            int optionType,
//            int messageType,
//            Icon icon,
//            Object[] options,
//            Object initialValue) {
//        return JOptionPane.showOptionDialog(
//                component, message, title, optionType, messageType, icon, options, initialValue);
//    }

    public static void showMessageDialog(Component component, String message, String title, int messageType) {
        JOptionPane.showMessageDialog(component, message, title, messageType);
    }

    public static int showExistingAuditDialog() {
        Object[] options = new Object[]{"Cancel", "Start New", "Continue"};
        return JOptionPane.showOptionDialog(
                Main.auditFrame,
                "An existing audit is in process, do you want to continue?",
                "Existing audit found",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                null);
    }
}
