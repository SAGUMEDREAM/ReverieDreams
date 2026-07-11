package cc.thonly.reverie_dreams.api.client.midi;

import cc.thonly.reverie_dreams.networking.payload.PlayerMidiNotePacket;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import dev.architectury.networking.NetworkManager;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Midi2Sound {
    public static void register() {
        MidiListenerAPI midiListenerAPI = MidiListenerAPI.get();
        midiListenerAPI.register(new MidiListenerContextImpl());
    }

    private static String asNoteName(int code) {
        ArrayList<String> notes = new ArrayList<>(List.of("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"));
        return notes.get(code % 12) + (code / 12 - 1);
    }

    public static int keyToNote(int key) {
        int note = key - 54; // 对齐 F#4

        note %= 25;
        if (note < 0) {
            note += 25;
        }

        return note;
    }

    public static float noteToPitch(int note) {
        return (float) Math.pow(2.0, (note - 12) / 12.0);
    }

    public static class MidiListenerContextImpl implements MidiListenerContext {
        @Override
        public void press(int key, int velocity) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null) {
                return;
            }
            Screen screen = mc.screen;
            if (screen != null && !(screen instanceof ChatScreen)) {
                return;
            }
            EquipmentSlot slot;
            ItemStack main = player.getItemBySlot(EquipmentSlot.MAINHAND);
            ItemStack off = player.getItemBySlot(EquipmentSlot.OFFHAND);
            ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
            boolean stop = false;
            if (main.is(RDItemTags.MUSICAL_INSTRUMENTS) && main.has(RDDataComponents.NOTE_TYPE.value())) {
                slot = EquipmentSlot.MAINHAND;
            } else if (off.is(RDItemTags.MUSICAL_INSTRUMENTS) && off.has(RDDataComponents.NOTE_TYPE.value())) {
                slot = EquipmentSlot.OFFHAND;
            } else if (head.is(RDItemTags.MUSICAL_INSTRUMENTS) && head.has(RDDataComponents.NOTE_TYPE.value())) {
                slot = EquipmentSlot.HEAD;
            } else {
                slot = EquipmentSlot.MAINHAND;
                stop = true;
            }
            if (stop) {
                return;
            }
            int note = keyToNote(key);
            float volume = velocity / 127.0f;
//            log.info("key={} note={}", key, note);;
            mc.execute(() -> NetworkManager.sendToServer(new PlayerMidiNotePacket(slot, key, note, volume, true)));
        }

        @Override
        public void release(int key) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null) {
                return;
            }
            if (mc.screen != null) {
                return;
            }
            EquipmentSlot slot;
            ItemStack main = player.getItemBySlot(EquipmentSlot.MAINHAND);
            ItemStack off = player.getItemBySlot(EquipmentSlot.OFFHAND);
            ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
            boolean stop = false;
            if (main.is(RDItemTags.MUSICAL_INSTRUMENTS) && main.has(RDDataComponents.NOTE_TYPE.value())) {
                slot = EquipmentSlot.MAINHAND;
            } else if (off.is(RDItemTags.MUSICAL_INSTRUMENTS) && off.has(RDDataComponents.NOTE_TYPE.value())) {
                slot = EquipmentSlot.OFFHAND;
            } else if (head.is(RDItemTags.MUSICAL_INSTRUMENTS) && head.has(RDDataComponents.NOTE_TYPE.value())) {
                slot = EquipmentSlot.HEAD;
            } else {
                slot = EquipmentSlot.MAINHAND;
                stop = true;
            }
            if (stop) {
                return;
            }
            mc.execute(() -> NetworkManager.sendToServer(new PlayerMidiNotePacket(slot, key, 0, 0, false)));
        }
    }
}
