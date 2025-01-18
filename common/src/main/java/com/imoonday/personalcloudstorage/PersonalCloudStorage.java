package com.imoonday.personalcloudstorage;

import com.imoonday.personalcloudstorage.config.ServerConfig;
import com.imoonday.personalcloudstorage.init.ModItems;
import com.imoonday.personalcloudstorage.init.ModMenuType;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

/**
 * Bugs:
 * 1. 1000000页时会有明显卡顿 X
 * 2. 打开界面时修改行数不会更新界面
 * 3. 修改时需要显示修改的名称或者UUID √
 * 4. 删除页面时改成返还下界合金碎片 √
 * 5. 按钮不受设置上限限制 √
 * 6. 优先访问在线玩家的存储空间
 * */
public final class PersonalCloudStorage {

    public static final String MOD_ID = "personalcloudstorage";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        ServerConfig.load();
        ModItems.init();
        ModMenuType.init();
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
