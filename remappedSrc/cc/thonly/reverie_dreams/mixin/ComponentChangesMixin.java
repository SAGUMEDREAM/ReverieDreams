package cc.thonly.reverie_dreams.mixin;


import cc.thonly.reverie_dreams.test.AsyncCountdown;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
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

@Mixin(DataComponentPatch.class)
@SuppressWarnings({"unchecked", "rawtypes"})
public class ComponentChangesMixin {

    @Shadow
    @Final
    @Mutable
    public static Codec<DataComponentPatch> CODEC;

    @Shadow
    @Final
    @Mutable
    public static StreamCodec<RegistryFriendlyByteBuf, DataComponentPatch> PACKET_CODEC;

    @Shadow
    @Final
    @Mutable
    public static StreamCodec<RegistryFriendlyByteBuf, DataComponentPatch> LENGTH_PREPENDED_PACKET_CODEC;

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
                    PACKET_CODEC = new StreamCodec<RegistryFriendlyByteBuf, DataComponentPatch>() {
                        public DataComponentPatch decode(RegistryFriendlyByteBuf registryByteBuf) {
                            int i = registryByteBuf.readVarInt();
                            int j = registryByteBuf.readVarInt();
                            if (i == 0 && j == 0) {
                                return DataComponentPatch.EMPTY;
                            } else {
                                int k = i + j;
                                Reference2ObjectMap<DataComponentType<?>, Optional<?>> reference2ObjectMap = new Reference2ObjectArrayMap<>(Math.min(k, 65536));

                                int l;
                                DataComponentType componentType;
                                for(l = 0; l < i; ++l) {
                                    try {
                                        componentType = DataComponentType.STREAM_CODEC.decode(registryByteBuf);
                                        System.out.println(componentType);
                                        Object object = componentType.streamCodec().decode(registryByteBuf);
                                        System.out.println(object);
                                        reference2ObjectMap.put(componentType, Optional.ofNullable(object));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                for(l = 0; l < j; ++l) {
                                    componentType = DataComponentType.STREAM_CODEC.decode(registryByteBuf);
                                    reference2ObjectMap.put(componentType, Optional.empty());
                                }

                                return invokeInit(reference2ObjectMap);
                            }
                        }

                        public void encode(RegistryFriendlyByteBuf registryByteBuf, DataComponentPatch componentChanges) {
                            if (componentChanges.isEmpty()) {
                                registryByteBuf.writeVarInt(0);
                                registryByteBuf.writeVarInt(0);
                            } else {
                                int i = 0;
                                int j = 0;
                                ObjectIterator var5 = Reference2ObjectMaps.fastIterable(componentChanges.map).iterator();

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
                                var5 = Reference2ObjectMaps.fastIterable(componentChanges.map).iterator();

                                while(var5.hasNext()) {
                                    block = (Reference2ObjectMap.Entry)var5.next();
                                    Optional<?> optional = (Optional)block.getValue();
                                    if (optional.isPresent()) {
                                        DataComponentType<?> componentType = (DataComponentType)block.getKey();
                                        DataComponentType.STREAM_CODEC.encode(registryByteBuf, componentType);
                                        encode(registryByteBuf, componentType, optional.get());
                                    }
                                }

                                var5 = Reference2ObjectMaps.fastIterable(componentChanges.map).iterator();

                                while(var5.hasNext()) {
                                    block = (Reference2ObjectMap.Entry)var5.next();
                                    if (((Optional)block.getValue()).isEmpty()) {
                                        DataComponentType<?> componentType2 = (DataComponentType)block.getKey();
                                        DataComponentType.STREAM_CODEC.encode(registryByteBuf, componentType2);
                                    }
                                }

                            }
                        }

                        private static <T> void encode(RegistryFriendlyByteBuf buf, DataComponentType<T> type, Object value) {
                            type.streamCodec().encode(buf, (T) value);
                        }
                    };
                });

    }

    @Invoker("<init>")
    static DataComponentPatch invokeInit(Reference2ObjectMap<DataComponentType<?>, Optional<?>> map) {
        throw new AssertionError(); // 实际调用时 Mixin 会替换
    }

    @Unique
    @SuppressWarnings("unchecked")
    private static Codec<DataComponentPatch> makeFixedCodec() {
        Codec<Map<DataComponentPatch.PatchKey, Object>> baseCodec =
                (Codec<Map<DataComponentPatch.PatchKey, Object>>) (Object)
                        Codec.dispatchedMap(DataComponentPatch.PatchKey.CODEC, DataComponentPatch.PatchKey::valueCodec);

        return baseCodec.xmap(
                (changes) -> {
                    if (changes.isEmpty()) {
                        return DataComponentPatch.EMPTY;
                    } else {
                        Reference2ObjectMap<DataComponentType<?>, Optional<?>> reference2ObjectMap = new Reference2ObjectArrayMap<>(changes.size());
                        for (Map.Entry<DataComponentPatch.PatchKey, Object> block : changes.entrySet()) {
                            DataComponentPatch.PatchKey type = block.getKey();
                            if (type.removed()) {
                                reference2ObjectMap.put(type.type(), Optional.empty());
                            } else {
                                if (block.getValue() == null) {
                                    System.out.println(123456789);
                                    System.out.println(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type.type()));
                                }
                                reference2ObjectMap.put(type.type(), Optional.ofNullable(block.getValue()));
                            }
                        }
                        return invokeInit(reference2ObjectMap);
                    }
                },
                (componentChanges) -> {
                    Reference2ObjectMap<DataComponentPatch.PatchKey, Object> reference2ObjectMap = new Reference2ObjectArrayMap<>(componentChanges.map.size());
                    for (Map.Entry<DataComponentType<?>, Optional<?>> block : componentChanges.map.entrySet()) {
                        DataComponentType<?> componentType = block.getKey();
                        if (!componentType.isTransient()) {
                            Optional<?> optional = block.getValue();
                            if (optional.isPresent()) {
                                reference2ObjectMap.put(new DataComponentPatch.PatchKey(componentType, false), optional.get());
                            } else {
                                reference2ObjectMap.put(new DataComponentPatch.PatchKey(componentType, true), Unit.INSTANCE);
                            }
                        }
                    }
                    return reference2ObjectMap;
                }
        );
    }


}
