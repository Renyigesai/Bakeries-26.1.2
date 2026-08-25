package com.renyigesai.bakeries.api;


import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PathPackResources;

import java.nio.file.Path;

public class BakeriesFilePackResource extends PathPackResources {

	public BakeriesFilePackResource(PackLocationInfo location, Path root) {
		super(location, root);
	}

//	public BakeriesFilePackResource(String name, IModFile modFile, String sourcePath) {
//		super(name, true, modFile);
//		this.modFile = modFile;
//		this.sourcePath = sourcePath;
//	}
}
