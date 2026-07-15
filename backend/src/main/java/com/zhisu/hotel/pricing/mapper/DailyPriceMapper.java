package com.zhisu.hotel.pricing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhisu.hotel.pricing.entity.DailyPrice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 房型每日价格数据访问接口。
 */
@Mapper
public interface DailyPriceMapper
        extends BaseMapper<DailyPrice> {
}