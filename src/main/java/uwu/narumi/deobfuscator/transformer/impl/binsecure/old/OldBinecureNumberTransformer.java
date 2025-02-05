package uwu.narumi.deobfuscator.transformer.impl.binsecure.old;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import uwu.narumi.deobfuscator.Deobfuscator;
import uwu.narumi.deobfuscator.helper.MathHelper;
import uwu.narumi.deobfuscator.transformer.Transformer;

/*
   TODO: Create UniversalNumberTransformer and put into that class sb27/binsecure removers lo
 */
public class OldBinecureNumberTransformer extends Transformer {

    @Override
    public void transform(Deobfuscator deobfuscator) throws Exception {
        deobfuscator.classes().stream()
                .flatMap(classNode -> classNode.methods.stream())
                .forEach(methodNode -> {
                    boolean modified;
                    do {
                        modified = false;

                        for (AbstractInsnNode node : methodNode.instructions.toArray()) {
                            if (isLong(node) && node.getNext().getOpcode() == L2I && isInteger(node.getNext().getNext())) {
                                methodNode.instructions.remove(node.getNext());
                                methodNode.instructions.set(node, getNumber((int) getLong(node)));
                                modified = true;
                            } else if (node instanceof MethodInsnNode
                                    && ((MethodInsnNode) node).name.equals("floatToIntBits")
                                    && ((MethodInsnNode) node).owner.equals("java/lang/Float")
                                    && ((MethodInsnNode) node).desc.equals("(F)I")
                                    && isFloat(node.getPrevious()) && isInteger(node.getNext()) && (node.getNext().getNext().getOpcode() >= IADD && node.getNext().getNext().getOpcode() <= LXOR)) {

                                float number = Float.floatToIntBits(getFloat(node.getPrevious()));
                                methodNode.instructions.remove(node.getPrevious());
                                methodNode.instructions.set(node, getNumber(number));
                                modified = true;
                            } else if (node instanceof MethodInsnNode
                                    && ((MethodInsnNode) node).name.equals("doubleToLongBits")
                                    && ((MethodInsnNode) node).owner.equals("java/lang/Double")
                                    && ((MethodInsnNode) node).desc.equals("(D)J")
                                    && isDouble(node.getPrevious()) && isLong(node.getNext()) && (node.getNext().getNext().getOpcode() >= IADD && node.getNext().getNext().getOpcode() <= LXOR)) {

                                long number = Double.doubleToLongBits(getDouble(node.getPrevious()));
                                methodNode.instructions.remove(node.getPrevious());
                                methodNode.instructions.set(node, getNumber(number));
                                modified = true;
                            } else if (node.getOpcode() == INEG || node.getOpcode() == LNEG) {
                                if (isInteger(node.getPrevious())) {
                                    int number = -getInteger(node.getPrevious());

                                    methodNode.instructions.remove(node.getPrevious());
                                    methodNode.instructions.set(node, getNumber(number));
                                    modified = true;
                                } else if (isLong(node.getPrevious())) {
                                    long number = -getLong(node.getPrevious());

                                    methodNode.instructions.remove(node.getPrevious());
                                    methodNode.instructions.set(node, getNumber(number));
                                    modified = true;
                                }
                            } else if ((node.getOpcode() >= IADD && node.getOpcode() <= LXOR)) {
                                if (isInteger(node.getPrevious().getPrevious()) && isInteger(node.getPrevious())) {
                                    int first = getInteger(node.getPrevious().getPrevious());
                                    int second = getInteger(node.getPrevious());

                                    Integer product = MathHelper.doMath(node.getOpcode(), first, second);
                                    if (product != null) {
                                        methodNode.instructions.remove(node.getPrevious().getPrevious());
                                        methodNode.instructions.remove(node.getPrevious());
                                        methodNode.instructions.set(node, getNumber(product));
                                        modified = true;
                                    }
                                } else if (isLong(node.getPrevious().getPrevious()) && isLong(node.getPrevious())) {
                                    long first = getLong(node.getPrevious().getPrevious());
                                    long second = getLong(node.getPrevious());

                                    Long product = MathHelper.doMath(node.getOpcode(), first, second);
                                    if (product != null) {
                                        methodNode.instructions.remove(node.getPrevious().getPrevious());
                                        methodNode.instructions.remove(node.getPrevious());
                                        methodNode.instructions.set(node, getNumber(product));
                                        modified = true;
                                    }
                                }
                            } else if (node instanceof MethodInsnNode
                                    && ((MethodInsnNode) node).name.equals("intBitsToFloat")
                                    && ((MethodInsnNode) node).owner.equals("java/lang/Float")
                                    && ((MethodInsnNode) node).desc.equals("(I)F")
                                    && isInteger(node.getPrevious())) {

                                float number = Float.intBitsToFloat(getInteger(node.getPrevious()));
                                methodNode.instructions.remove(node.getPrevious());
                                methodNode.instructions.set(node, getNumber(number));
                                modified = true;
                            } else if (node instanceof MethodInsnNode
                                    && ((MethodInsnNode) node).name.equals("longBitsToDouble")
                                    && ((MethodInsnNode) node).owner.equals("java/lang/Double")
                                    && ((MethodInsnNode) node).desc.equals("(J)D")
                                    && isLong(node.getPrevious())) {

                                double number = Double.longBitsToDouble(getLong(node.getPrevious()));
                                methodNode.instructions.remove(node.getPrevious());
                                methodNode.instructions.set(node, getNumber(number));
                                modified = true;
                            }
                        }
                    } while (modified);
                });
    }
}
