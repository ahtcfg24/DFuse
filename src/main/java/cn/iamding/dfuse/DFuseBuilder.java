package cn.iamding.dfuse;

import cn.iamding.dfuse.config.DFuseProperties;

/**
 * Builder of DFuse
 */
public class DFuseBuilder {

    public static IDFuse build(DFuseProperties properties) {
        return new DFuseImpl(properties);
    }

    private DFuseBuilder() {
    }


}
