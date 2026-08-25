package com.renyigesai.bakeries.common.event;

//@EventBusSubscriber
public class AddPackEvent {
//    @SubscribeEvent
//    public static void onAddPack(AddPackFindersEvent event) {
//        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
//            IModFileInfo modFileInfo = ModList.get().getModFileById(BakeriesMod.MODID);
//            if (modFileInfo == null) {
//                return;
//            }
//            IModFile modFile = modFileInfo.getFile();
//            event.addRepositorySource(consumer -> {
//                Pack pack1 = Pack.readMetaAndCreate(Identifier.fromNamespaceAndPath(BakeriesMod.MODID,"b_2d_icon").toString(),
//                        Component.translatable("pack.bakeries.2d_icon"), false, id ->
//                                new BakeriesFilePackResource(id, modFile, "resourcepacks/b_2d_icon"),
//                        PackType.CLIENT_RESOURCES, Pack.Position.TOP, PackSource.BUILT_IN);
//                if (pack1 != null) {
//                    consumer.accept(pack1);
//                }
//            });
//        }
//    }
}
