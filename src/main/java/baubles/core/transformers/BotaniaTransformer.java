package baubles.core.transformers;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.common.Baubles;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.equipment.bauble.ItemTravelBelt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Botania has some items that gets item from specific bauble slots.
 * Makes items work properly with Bubbles. Typical Vazkii mess.
 **/
public class BotaniaTransformer extends BaseTransformer {

    public static byte[] transformItemDivaCharm(byte[] basicClass) {
        ClassNode cls = read(basicClass);
        for (MethodNode method : cls.methods) {
            if (method.name.equals("lambda$onEntityDamaged$0")) {
                Iterator<AbstractInsnNode> iterator = method.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode node = iterator.next();
                    if (node.getOpcode() == BIPUSH) {
                        InsnList list = new InsnList();
                        list.add(new VarInsnNode(ALOAD, 3));
                        list.add(new VarInsnNode(ALOAD, 0));
                        list.add(new MethodInsnNode(INVOKESTATIC, "baubles/api/BaublesApi", "isBaubleEquipped", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/Item;)I", false));
                        method.instructions.insertBefore(node, list);
                        method.instructions.remove(node);
                        break;
                    }
                }
                break;
            }
        }
        return write(cls);
    }

    public static byte[] transformItemTiara(byte[] basicClass) {
        ClassNode cls = read(basicClass);
        for (MethodNode method : cls.methods) {
            if (method.name.equals("updatePlayerFlyStatus")) {
                Iterator<AbstractInsnNode> iterator = method.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode node = iterator.next();
                    if (node.getOpcode() == ICONST_4) {
                        InsnList list = new InsnList();
                        list.add(new VarInsnNode(ALOAD, 2));
                        list.add(new VarInsnNode(ALOAD, 0));
                        list.add(new MethodInsnNode(INVOKESTATIC, "baubles/api/BaublesApi", "isBaubleEquipped", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/Item;)I", false));
                        method.instructions.insertBefore(node, list);
                        node = node.getNext();
                        method.instructions.remove(node.getPrevious());
                        node = node.getNext();
                        list.add(new VarInsnNode(ALOAD, 3));
                        list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/item/ItemStack", "isEmpty", "()Z", false));
                        LabelNode l_con = new LabelNode();
                        list.add(new JumpInsnNode(IFEQ, l_con));
                        list.add(new LabelNode());
                        list.add(new InsnNode(RETURN));
                        list.add(l_con);
                        list.add(new FrameNode(F_SAME, 0, null, 0, null));
                        method.instructions.insert(node, list);
                        break;
                    }
                }
            }
            else if (method.name.equals("shouldPlayerHaveFlight")) {
                Iterator<AbstractInsnNode> iterator = method.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode node = iterator.next();
                    if (node.getOpcode() == ICONST_4) {
                        InsnList list = new InsnList();
                        list.add(new VarInsnNode(ALOAD, 1));
                        list.add(new VarInsnNode(ALOAD, 0));
                        list.add(new MethodInsnNode(INVOKESTATIC, "baubles/api/BaublesApi", "isBaubleEquipped", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/Item;)I", false));
                        method.instructions.insertBefore(node, list);
                        method.instructions.remove(node);
                        break;
                    }
                }
                break;
            }
        }
        return write(cls);
    }

    public static byte[] transformItemGoddessCharm(byte[] basicClass) {
        ClassNode cls = read(basicClass);
        for (MethodNode method : cls.methods) {
            if (method.name.equals("onExplosion")) {
                Iterator<AbstractInsnNode> iterator = method.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode node = iterator.next();
                    if (node.getOpcode() == BIPUSH) {
                        InsnList list = new InsnList();
                        list.add(new VarInsnNode(ALOAD, 5));
                        list.add(new FieldInsnNode(GETSTATIC, "vazkii/botania/common/item/ModItems", "goddessCharm", "Lnet/minecraft/item/Item;"));
                        list.add(new MethodInsnNode(INVOKESTATIC, "baubles/api/BaublesApi", "isBaubleEquipped", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/Item;)I", false));
                        method.instructions.insertBefore(node, list);
                        method.instructions.remove(node);
                        break;
                    }
                }
                break;
            }
        }
        return write(cls);
    }

    public static byte[] transformItemHolyCloak(byte[] basicClass) {
        ClassNode cls = read(basicClass);
        for (MethodNode method : cls.methods) {
            if (method.name.equals("getBaubleUUID")) {
                Iterator<AbstractInsnNode> iterator = method.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode node = iterator.next();
                    if (node.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode) node).name.equals("getBaubleUUID")) {

                        InsnList list = new InsnList();
                        list.add(new VarInsnNode(ALOAD, 5));
                        list.add(new FieldInsnNode(GETSTATIC, "vazkii/botania/common/item/ModItems", "goddessCharm", "Lnet/minecraft/item/Item;"));
                        list.add(new MethodInsnNode(INVOKESTATIC, "baubles/api/BaublesApi", "isBaubleEquipped", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/Item;)I", false));
                        method.instructions.insertBefore(node, list);
                        method.instructions.remove(node);
                        break;
                    }
                }
            }
        }
        return write(cls);
    }

    public static byte[] transformItemMonocle(byte[] basicClass) {
        ClassNode cls = read(basicClass);
        for (MethodNode method : cls.methods) {
            if (method.name.equals("hasMonocle")) {
                Iterator<AbstractInsnNode> iterator = method.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode node = iterator.next();
                    if (node.getOpcode() == BIPUSH) {
                        InsnList list = new InsnList();
                        list.add(new VarInsnNode(ALOAD, 0));
                        list.add(new MethodInsnNode(INVOKESTATIC, "baubles/api/BaublesApi", "getBaublesHandler", "(Lnet/minecraft/entity/player/EntityPlayer;)Lbaubles/api/cap/IBaublesItemHandler;", false));
                        list.add(new MethodInsnNode(INVOKEINTERFACE, "baubles/api/cap/IBaublesItemHandler", "getSlots", "()I", true));
                        method.instructions.insertBefore(node, list);
                        method.instructions.remove(node);
                        break;
                    }
                }
                break;
            }
        }
        return write(cls);
    }

    public static byte[] transformItemTravelBelt(byte[] basicClass) {
        ClassNode cls = read(basicClass);
        for (MethodNode method : cls.methods) {

            if (method.name.equals("onPlayerJump")) {

                method.instructions.clear();
                method.tryCatchBlocks.clear();
                method.localVariables.clear();

                InsnList insn = new InsnList();

                insn.add(new VarInsnNode(ALOAD, 0));
                insn.add(new VarInsnNode(ALOAD, 1));
                insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "baubles/core/transformers/BotaniaTransformer",
                        "onPlayerJump",
                        "(Lnet/minecraftforge/event/entity/living/LivingEvent$LivingJumpEvent;)V",
                        false));
                insn.add(new InsnNode(RETURN));

                method.instructions = insn;

                method.maxStack = 0;
                method.maxLocals = 0;
            }
            else if (method.name.equals("shouldPlayerHaveStepup")) {
                Iterator<AbstractInsnNode> iterator = method.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode node = iterator.next();
                    if (node.getOpcode() == INVOKEINTERFACE) {
                        MethodInsnNode methodInsn = (MethodInsnNode) node;

                        if (methodInsn.name.equals("getBaublesHandler")) {
                            InsnList newInstructions = new InsnList();

                            for (int i = 0; i <= 1; i++) {
                                iterator.remove();
                                node = iterator.next();
                            }

                            newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                            newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            newInstructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                    "baubles/core/transformers/BotaniaTransformer",
                                    "getTrueBelt",
                                    "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
                                    false));

                            method.instructions.insertBefore(node, newInstructions);
//                            method.instructions.remove(node);

                        }
                    }
                }
            } else if (method.name.equals("updatePlayerStepStatus")) {
                Iterator<AbstractInsnNode> iterator = method.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode node = iterator.next();
                    if (node.getOpcode() == INVOKEINTERFACE) {
                        MethodInsnNode methodInsn = (MethodInsnNode) node;

                        if (methodInsn.name.equals("getBaublesHandler")) {
                            InsnList newInstructions = new InsnList();

                            for (int i = 0; i <= 1; i++) {
                                iterator.remove();
                                node = iterator.next();
                            }

                            newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                            newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            newInstructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                    "baubles/core/transformers/BotaniaTransformer",
                                    "getTrueBelt",
                                    "Lbaubles/core/transformers/BotaniaTransformer;onPlayerJump(Lnet/minecraftforge/event/entity/living/LivingEvent$LivingJumpEvent;)V",
                                    false));

                            method.instructions.insertBefore(node, newInstructions);
//                            method.instructions.remove(node);

                        }
                    }
                }
            }
        }
        return write(cls);
    }

    public static byte[] transformItemWaterRing(byte[] basicClass) {
        ClassNode cls = read(basicClass);
        for (MethodNode method : cls.methods) {
            if (method.name.equals("onWornTick")) {
                Iterator<AbstractInsnNode> iterator = method.instructions.iterator();
                boolean remove = false;
                while (iterator.hasNext()) {
                    AbstractInsnNode node = iterator.next();
                    if (node.getOpcode() == INSTANCEOF) {
                        remove = true;
                    }
                    if (remove) {
                        method.instructions.remove(node.getPrevious());
                        if (node.getOpcode() == RETURN) {
                            method.instructions.remove(node.getNext().getNext().getNext());
                            method.instructions.remove(node.getNext().getNext());
                            method.instructions.remove(node.getNext());
                            method.instructions.remove(node);
                            break;
                        }
                    }
                }
                break;
            }
        }
        return write(cls);
    }

    public static byte[] transformItemFlightTiara(byte[] basicClass) {
        ClassNode cls = read(basicClass);
        for (MethodNode method : cls.methods) {

            if (method.name.equals("updatePlayerFlyStatus")) {

                Iterator<AbstractInsnNode> iterator = method.instructions.iterator();

                while (iterator.hasNext()) {
                    AbstractInsnNode node = iterator.next();

                    if (node.getOpcode() == Opcodes.ICONST_4) {

                        InsnList newInstructions = new InsnList();

                        newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
                        newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        newInstructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                "baubles/api/BaublesApi",
                                "isBaubleEquipped",
                                "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/Item;)I",
                                false));

                        method.instructions.insertBefore(node, newInstructions);
                        method.instructions.remove(node);
                        break;
                    }
                }
            }
            if (method.name.equals("shouldPlayerHaveFlight")) {

                Iterator<AbstractInsnNode> iterator = method.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode node = iterator.next();

                    if (node.getOpcode() == Opcodes.ICONST_4) {

                        InsnList newInstructions = new InsnList();

                        newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        newInstructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                "baubles/api/BaublesApi",
                                "isBaubleEquipped",
                                "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/Item;)I",
                                false));

                        method.instructions.insertBefore(node, newInstructions);
                        method.instructions.remove(node);

                        break;
                    }
                }

                break;
            }
        }
        return write(cls);
    }

    public static byte[] transformHUDHandler(byte[] basicClass) {
        ClassNode cls = read(basicClass);
        for (MethodNode method : cls.methods) {

            if (method.name.equals("onDrawScreenPre")) {

                Iterator<AbstractInsnNode> iterator = method.instructions.iterator();

                while (iterator.hasNext()) {
                    AbstractInsnNode node = iterator.next();

                    if (node.getOpcode() == Opcodes.ICONST_4) {

                        InsnList newInstructions = new InsnList();
                        newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        newInstructions.add(new FieldInsnNode(Opcodes.GETFIELD,
                                "net/minecraft/client/Minecraft",
                                getMcPlayerStr(),
                                "Lnet/minecraft/client/entity/EntityPlayerSP;"));
                        newInstructions.add(new FieldInsnNode(Opcodes.GETSTATIC,
                                "vazkii/botania/common/item/ModItems",
                                "flightTiara",
                                "Lnet/minecraft/item/Item;"));
                        newInstructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                "baubles/api/BaublesApi",
                                "isBaubleEquipped",
                                "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/Item;)I",
                                false));

                        method.instructions.insertBefore(node, newInstructions);
                        method.instructions.remove(node);
                    } else if (node.getOpcode() == Opcodes.ICONST_1) {

                        InsnList newInstructions = new InsnList();
                        newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        newInstructions.add(new FieldInsnNode(Opcodes.GETFIELD,
                                "net/minecraft/client/Minecraft",
                                getMcPlayerStr(),
                                "Lnet/minecraft/client/entity/EntityPlayerSP;"));
                        newInstructions.add(new FieldInsnNode(Opcodes.GETSTATIC,
                                "vazkii/botania/common/item/ModItems",
                                "dodgeRing",
                                "Lnet/minecraft/item/Item;"));
                        newInstructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                "baubles/api/BaublesApi",
                                "isBaubleEquipped",
                                "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/Item;)I",
                                false));

                        method.instructions.insertBefore(node, newInstructions);
                        method.instructions.remove(node);
                    } else if (node.getOpcode() == Opcodes.ICONST_2) {

                        InsnList newInstructions = new InsnList();
                        newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        newInstructions.add(new FieldInsnNode(Opcodes.GETFIELD,
                                "net/minecraft/client/Minecraft",
                                getMcPlayerStr(),
                                "Lnet/minecraft/client/entity/EntityPlayerSP;"));
                        newInstructions.add(new FieldInsnNode(Opcodes.GETSTATIC,
                                "vazkii/botania/common/item/ModItems",
                                "dodgeRing",
                                "Lnet/minecraft/item/Item;"));
                        newInstructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                "baubles/api/BaublesApi",
                                "isBaubleEquipped",
                                "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/Item;)I",
                                false));

                        method.instructions.insertBefore(node, newInstructions);
                        method.instructions.remove(node);

                    }
                }
            }
        }
        return write(cls);
    }

    private static String getMcPlayerStr() {
//        return "player";
        return Baubles.isDev() ? "player" : "field_71439_g";
    }

    public static ItemStack getTrueBelt(EntityPlayer player, ItemStack old) {
        ItemStack belt = BaublesApi.getBaublesHandler(player).getStackInSlot(BaublesApi.isBaubleEquipped(player, old.getItem()));

        if (belt.isEmpty()) {
            belt = BaublesApi.getBaublesHandler(player).getStackInSlot(BaublesApi.isBaubleEquipped(player, ModItems.superTravelBelt));
        }
        if (belt.isEmpty()) {
            belt = BaublesApi.getBaublesHandler(player).getStackInSlot(BaublesApi.isBaubleEquipped(player, ModItems.speedUpBelt));
        }

        return belt;
    }

    public static void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if(event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
//            ItemStack belt = BaublesApi.getBaublesHandler(player).getStackInSlot(3);
            List<ItemStack> belts = Arrays.stream(BaubleType.BELT.getValidSlotsArrays(player))
                    .mapToObj(slot -> BaublesApi.getBaublesHandler(player).getStackInSlot(slot))
                    .collect(Collectors.toList());


            for (ItemStack belt : belts) {
                if (!belt.isEmpty() && belt.getItem() instanceof ItemTravelBelt && ManaItemHandler.requestManaExact(belt, player, 1, false)) {
                    player.motionY += ((ItemTravelBelt) belt.getItem()).jump;
                    player.fallDistance = -((ItemTravelBelt) belt.getItem()).fallBuffer;
                    break;
                }
            }
        }
    }


//    public static byte[] transformItemBauble(byte[] basicClass) {
//        ClassNode cls = read(basicClass);
//        for (MethodNode method : cls.methods) {
//            if (method.name.equals("getBaubleUUID")) {
//                Iterator<AbstractInsnNode> iterator = method.instructions.iterator();
//                while (iterator.hasNext()) {
//                    AbstractInsnNode node = iterator.next();
//                    if (node.getOpcode() == ALOAD) {
//                        InsnList list = new InsnList();
//                        list.add(new VarInsnNode(ALOAD, 3));
//                        list.add(new MethodInsnNode(INVOKESTATIC, "baubles/api/BaublesApi", "getBaublesHandler", "(Lnet/minecraft/entity/player/EntityPlayer;)Lbaubles/api/cap/IBaublesItemHandler;", false));
//                        list.add(new MethodInsnNode(INVOKEINTERFACE, "baubles/api/cap/IBaublesItemHandler", "getSlots", "()I", true));
//                        method.instructions.insertBefore(node, list);
//                        method.instructions.remove(node);
//                        break;
//                    }
//                }
//                break;
//            }
//        }
//        return write(cls);
//    }
}
