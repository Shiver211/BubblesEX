package baubles.core.transformers;

import org.objectweb.asm.tree.*;

import java.util.Iterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ExtraBotanyTransformer extends BaseTransformer {

    public static byte[] transformItemCoreGod(byte[] basicClass) {
        ClassNode cls = read(basicClass);
        for (MethodNode method : cls.methods) {
            if (method.name.equals("shouldPlayerHaveFlight") &&
                    method.desc.equals("(Lnet/minecraft/entity/player/EntityPlayer;)Z")) {

                Iterator<AbstractInsnNode> iterator = method.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode node = iterator.next();

                    if (node.getOpcode() == Opcodes.ICONST_5) {

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

            if (method.name.equals("updatePlayerFlyStatus") &&
                    method.desc.equals("(Lnet/minecraftforge/event/entity/living/LivingEvent$LivingUpdateEvent;)V")) {

                Iterator<AbstractInsnNode> iterator = method.instructions.iterator();

                while (iterator.hasNext()) {
                    AbstractInsnNode node = iterator.next();

                    if (node.getOpcode() == Opcodes.ICONST_5) {

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
        }
        return write(cls);
    }
}
