package com.arckenver.towny.cmdelement;

import java.util.stream.Collectors;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.PatternMatchingCommandElement;
import org.spongepowered.api.text.Text;

import com.arckenver.towny.DataHandler;

public class NationNameElement extends PatternMatchingCommandElement {
    public NationNameElement(Text key) {
        super(key);
    }

    @Override
    protected Iterable<String> getChoices(CommandSource src) {
        return DataHandler
                .getNations()
                .stream()
                .map(nation -> nation.getRealName())
                .collect(Collectors.toList());
    }

    @Override
    protected Object getValue(String choice) throws IllegalArgumentException {
        return choice;
    }

    @Override
    public Text getUsage(CommandSource src) {
        return Text.EMPTY;
    }
}
