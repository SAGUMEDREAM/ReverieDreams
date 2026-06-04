package cc.thonly.reverie_dreams.server.dialog;

import net.minecraft.network.chat.Component;
import net.minecraft.server.dialog.Input;
import net.minecraft.server.dialog.input.*;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("ALL")
public class InputFactory {
    static InputFactory INSTANCE = new InputFactory();

    public Input input(String key, InputControl control) {
        return new Input(key, control);
    }

    public Input textInput(String key,
                           int width,
                           Component label,
                           boolean labelVisible,
                           String initial,
                           int maxLength,
                           TextInput.MultilineOptions multiline
    ) {
        return new Input(key, new TextInput(width, label, labelVisible, initial, maxLength, Optional.ofNullable(multiline)));
    }

    public Input textInput(String key,
                           int width,
                           Component label,
                           boolean labelVisible,
                           String initial,
                           int maxLength
    ) {
        return new Input(key, new TextInput(width, label, labelVisible, initial, maxLength, Optional.empty()));
    }

    public Input numberRangeInput(String key,
                                  int width, Component label, String labelFormat, NumberRangeInput.RangeInfo rangeInfo
    ) {
        return new Input(key, new NumberRangeInput(width, label, labelFormat, rangeInfo));
    }

    public Input singleOptionInput(String key,
                                   int width,
                                   List<SingleOptionInput.Entry> entries,
                                   Component label,
                                   boolean labelVisible
    ) {
        return new Input(key, new SingleOptionInput(width, entries, label, labelVisible));
    }

    public Input booleanInput(String key,
                              Component label,
                              boolean initial,
                              String onTrue,
                              String onFalse
    ) {
        return new Input(key, new BooleanInput(label, initial, onTrue, onFalse));
    }

}
