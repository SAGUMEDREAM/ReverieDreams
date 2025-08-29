package cc.thonly.reverie_dreams.mixin;


import cc.thonly.reverie_dreams.test.AsyncCountdown;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Mixin(ComponentChanges.class)
@SuppressWarnings({"unchecked", "rawtypes"})
public class ComponentChangesMixin {

    @Shadow
    @Final
    @Mutable
    public static Codec<ComponentChanges> CODEC;

    @Shadow
    @Final
    @Mutable
    public static PacketCodec<RegistryByteBuf, ComponentChanges> PACKET_CODEC;

    @Shadow
    @Final
    @Mutable
    public static PacketCodec<RegistryByteBuf, ComponentChanges> LENGTH_PREPENDED_PACKET_CODEC;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();


    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onClinit(CallbackInfo ci) {
        System.out.println(123456);
        AsyncCountdown countdown = new AsyncCountdown();
        countdown.startCountdown(2,
                (remaining) -> System.out.println("剩余秒数: " + remaining),
                () -> {
                    System.out.println(111);
                    CODEC = makeFixedCodec();
                    PACKET_CODEC = new PacketCodec<RegistryByteBuf, ComponentChanges>() {
                        public ComponentChanges decode(RegistryByteBuf registryByteBuf) {
                            int i = registryByteBuf.readVarInt();
                            int j = registryByteBuf.readVarInt();
                            if (i == 0 && j == 0) {
                                return ComponentChanges.EMPTY;
                            } else {
                                int k = i + j;
                                Reference2ObjectMap<ComponentType<?>, Optional<?>> reference2ObjectMap = new Reference2ObjectArrayMap<>(Math.min(k, 65536));

                                int l;
                                ComponentType componentType;
                                for(l = 0; l < i; ++l) {
                                    try {
                                        componentType = ComponentType.PACKET_CODEC.decode(registryByteBuf);
                                        System.out.println(componentType);
                                        Object object = componentType.getPacketCodec().decode(registryByteBuf);
                                        System.out.println(object);
                                        reference2ObjectMap.put(componentType, Optional.ofNullable(object));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                for(l = 0; l < j; ++l) {
                                    componentType = ComponentType.PACKET_CODEC.decode(registryByteBuf);
                                    reference2ObjectMap.put(componentType, Optional.empty());
                                }

                                return invokeInit(reference2ObjectMap);
                            }
                        }

                        public void encode(RegistryByteBuf registryByteBuf, ComponentChanges componentChanges) {
                            if (componentChanges.isEmpty()) {
                                registryByteBuf.writeVarInt(0);
                                registryByteBuf.writeVarInt(0);
                            } else {
                                int i = 0;
                                int j = 0;
                                ObjectIterator var5 = Reference2ObjectMaps.fastIterable(componentChanges.changedComponents).iterator();

                                Reference2ObjectMap.Entry block;
                                while(var5.hasNext()) {
                                    block = (Reference2ObjectMap.Entry)var5.next();
                                    if (((Optional)block.getValue()).isPresent()) {
                                        ++i;
                                    } else {
                                        ++j;
                                    }
                                }

                                registryByteBuf.writeVarInt(i);
                                registryByteBuf.writeVarInt(j);
                                var5 = Reference2ObjectMaps.fastIterable(componentChanges.changedComponents).iterator();

                                while(var5.hasNext()) {
                                    block = (Reference2ObjectMap.Entry)var5.next();
                                    Optional<?> optional = (Optional)block.getValue();
                                    if (optional.isPresent()) {
                                        ComponentType<?> componentType = (ComponentType)block.getKey();
                                        ComponentType.PACKET_CODEC.encode(registryByteBuf, componentType);
                                        encode(registryByteBuf, componentType, optional.get());
                                    }
                                }

                                var5 = Reference2ObjectMaps.fastIterable(componentChanges.changedComponents).iterator();

                                while(var5.hasNext()) {
                                    block = (Reference2ObjectMap.Entry)var5.next();
                                    if (((Optional)block.getValue()).isEmpty()) {
                                        ComponentType<?> componentType2 = (ComponentType)block.getKey();
                                        ComponentType.PACKET_CODEC.encode(registryByteBuf, componentType2);
                                    }
                                }

                            }
                        }

                        private static <T> void encode(RegistryByteBuf buf, ComponentType<T> type, Object value) {
                            type.getPacketCodec().encode(buf, (T) value);
                        }
                    };
                });

    }

    @Invoker("<init>")
    static ComponentChanges invokeInit(Reference2ObjectMap<ComponentType<?>, Optional<?>> map) {
        throw new AssertionError(); // 实际调用时 Mixin 会替换
    }

    @Unique
    @SuppressWarnings("unchecked")
    private static Codec<ComponentChanges> makeFixedCodec() {
        Codec<Map<ComponentChanges.Type, Object>> baseCodec =
                (Codec<Map<ComponentChanges.Type, Object>>) (Object)
                        Codec.dispatchedMap(ComponentChanges.Type.CODEC, ComponentChanges.Type::getValueCodec);

        return baseCodec.xmap(
                (changes) -> {
                    if (changes.isEmpty()) {
                        return ComponentChanges.EMPTY;
                    } else {
                        Reference2ObjectMap<ComponentType<?>, Optional<?>> reference2ObjectMap = new Reference2ObjectArrayMap<>(changes.size());
                        for (Map.Entry<ComponentChanges.Type, Object> block : changes.entrySet()) {
                            ComponentChanges.Type type = block.getKey();
                            if (type.removed()) {
                                reference2ObjectMap.put(type.type(), Optional.empty());
                            } else {
                                if (block.getValue() == null) {
                                    System.out.println(123456789);
                                    System.out.println(Registries.DATA_COMPONENT_TYPE.getId(type.type()));
                                }
                                reference2ObjectMap.put(type.type(), Optional.ofNullable(block.getValue()));
                            }
                        }
                        return invokeInit(reference2ObjectMap);
                    }
                },
                (componentChanges) -> {
                    Reference2ObjectMap<ComponentChanges.Type, Object> reference2ObjectMap = new Reference2ObjectArrayMap<>(componentChanges.changedComponents.size());
                    for (Map.Entry<ComponentType<?>, Optional<?>> block : componentChanges.changedComponents.entrySet()) {
                        ComponentType<?> componentType = block.getKey();
                        if (!componentType.shouldSkipSerialization()) {
                            Optional<?> optional = block.getValue();
                            if (optional.isPresent()) {
                                reference2ObjectMap.put(new ComponentChanges.Type(componentType, false), optional.get());
                            } else {
                                reference2ObjectMap.put(new ComponentChanges.Type(componentType, true), Unit.INSTANCE);
                            }
                        }
                    }
                    return reference2ObjectMap;
                }
        );
    }


}
