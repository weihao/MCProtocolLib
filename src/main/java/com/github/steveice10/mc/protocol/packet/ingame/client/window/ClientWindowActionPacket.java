package com.github.steveice10.mc.protocol.packet.ingame.client.window;

import com.github.steveice10.mc.protocol.data.MagicValues;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.ItemStack;
import com.github.steveice10.mc.protocol.data.game.window.ClickItemParam;
import com.github.steveice10.mc.protocol.data.game.window.CreativeGrabParam;
import com.github.steveice10.mc.protocol.data.game.window.DropItemParam;
import com.github.steveice10.mc.protocol.data.game.window.FillStackParam;
import com.github.steveice10.mc.protocol.data.game.window.MoveToHotbarParam;
import com.github.steveice10.mc.protocol.data.game.window.ShiftClickItemParam;
import com.github.steveice10.mc.protocol.data.game.window.SpreadItemParam;
import com.github.steveice10.mc.protocol.data.game.window.WindowAction;
import com.github.steveice10.mc.protocol.data.game.window.WindowActionParam;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.packet.Packet;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

import java.io.IOException;

@Data
@With
@Setter(AccessLevel.NONE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientWindowActionPacket implements Packet {
    public static final int CLICK_OUTSIDE_NOT_HOLDING_SLOT = -999;

    private int windowId;
    private int actionId;
    private int slot;
    private ItemStack clickedItem;
    private WindowAction action;
    private WindowActionParam param;

    public ClientWindowActionPacket(int windowId, int actionId, int slot, ItemStack clickedItem, WindowAction action, WindowActionParam param) {
        if((param == DropItemParam.LEFT_CLICK_OUTSIDE_NOT_HOLDING || param == DropItemParam.RIGHT_CLICK_OUTSIDE_NOT_HOLDING)
                && slot != -CLICK_OUTSIDE_NOT_HOLDING_SLOT) {
            throw new IllegalArgumentException("Slot must be " + CLICK_OUTSIDE_NOT_HOLDING_SLOT
                    + " with param LEFT_CLICK_OUTSIDE_NOT_HOLDING or RIGHT_CLICK_OUTSIDE_NOT_HOLDING");
        }

        this.windowId = windowId;
        this.actionId = actionId;
        this.slot = slot;
        this.clickedItem = clickedItem;
        this.action = action;
        this.param = param;
    }

    @Override
    public void read(NetInput in) throws IOException {
        this.windowId = in.readByte();
        this.slot = in.readShort();
        byte param = in.readByte();
        this.actionId = in.readShort();
        this.action = MagicValues.key(WindowAction.class, in.readByte());
        this.clickedItem = ItemStack.read(in);
        if(this.action == WindowAction.CLICK_ITEM) {
            this.param = MagicValues.key(ClickItemParam.class, param);
        } else if(this.action == WindowAction.SHIFT_CLICK_ITEM) {
            this.param = MagicValues.key(ShiftClickItemParam.class, param);
        } else if(this.action == WindowAction.MOVE_TO_HOTBAR_SLOT) {
            this.param = MagicValues.key(MoveToHotbarParam.class, param);
        } else if(this.action == WindowAction.CREATIVE_GRAB_MAX_STACK) {
            this.param = MagicValues.key(CreativeGrabParam.class, param);
        } else if(this.action == WindowAction.DROP_ITEM) {
            this.param = MagicValues.key(DropItemParam.class, param + (this.slot != -999 ? 2 : 0));
        } else if(this.action == WindowAction.SPREAD_ITEM) {
            this.param = MagicValues.key(SpreadItemParam.class, param);
        } else if(this.action == WindowAction.FILL_STACK) {
            this.param = MagicValues.key(FillStackParam.class, param);
        }
    }

    @Override
    public void write(NetOutput out) throws IOException {
        out.writeByte(this.windowId);
        out.writeShort(this.slot);

        int param = MagicValues.value(Integer.class, this.param);
        if(this.action == WindowAction.DROP_ITEM) {
            param %= 2;
        }

        out.writeByte(param);
        out.writeShort(this.actionId);
        out.writeByte(MagicValues.value(Integer.class, this.action));
        ItemStack.write(out, this.clickedItem);
    }

    @Override
    public boolean isPriority() {
        return false;
    }
}
