package net.ccbluex.liquidbounce.injection.transformers;

import jline.internal.Nullable;
import net.ccbluex.liquidbounce.utils.ASMUtils;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class OptimizeTransformer implements IClassTransformer {

    private static final HashMap<String, String> transformMap = new HashMap<>();

    static {
        transformMap.put("net.minecraft.util.EnumFacing", "facings");
        transformMap.put("net.minecraft.util.EnumChatFormatting", "chatFormatting");
        transformMap.put("net.minecraft.util.EnumParticleTypes", "particleTypes");
        transformMap.put("net.minecraft.util.EnumWorldBlockLayer", "worldBlockLayers");
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(transformedName.startsWith("net.minecraft") && !transformMap.containsKey(transformedName)) {
            try {
                @Nullable
                final ClassNode classNode = ASMUtils.INSTANCE.toClassNode(basicClass);
                AtomicBoolean changed = new AtomicBoolean(false);

                classNode.methods.forEach(methodNode -> {
                    for (int i = 0; i < methodNode.instructions.size(); ++i) {
                        final AbstractInsnNode abstractInsnNode = methodNode.instructions.get(i);
                        if (abstractInsnNode instanceof MethodInsnNode) {
                            MethodInsnNode min = (MethodInsnNode) abstractInsnNode;
                            if(min.getOpcode() == Opcodes.INVOKESTATIC && min.name.equals("values")) {
                                final String owner = min.owner.replaceAll("/", ".");
                                if (transformMap.containsKey(owner)) {
                                    changed.set(true);
                                    min.owner = "net/ccbluex/liquidbounce/injection/access/StaticStorage";
                                    min.name = transformMap.get(owner);
                                }
                            }
                        }
                    }
                });

                if (changed.get()) {
                    return ASMUtils.INSTANCE.toBytes(classNode);
                }
            }catch(final Throwable throwable) {
                throwable.printStackTrace();
            }
        }

        return basicClass;
    }
}
