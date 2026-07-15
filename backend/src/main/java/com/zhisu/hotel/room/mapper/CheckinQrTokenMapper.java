package com.zhisu.hotel.room.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhisu.hotel.common.enums.QrTokenStatus;
import com.zhisu.hotel.room.entity.CheckinQrToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface CheckinQrTokenMapper extends BaseMapper<CheckinQrToken> {

    @Update("UPDATE checkin_qr_token " +
            "SET token_status = 'REVOKED', revoked_at = #{revokedAt}, revoked_reason = #{reason}, " +
            "    version = version + 1, updated_at = #{revokedAt} " +
            "WHERE order_id = #{orderId} AND token_status = 'ACTIVE'")
    int revokeActiveByOrderId(@Param("orderId") Long orderId,
                              @Param("revokedAt") LocalDateTime revokedAt,
                              @Param("reason") String reason);

    @Update("UPDATE checkin_qr_token " +
            "SET token_status = 'EXPIRED', version = version + 1, updated_at = #{expiredAt} " +
            "WHERE id = #{tokenId} AND token_status = 'ACTIVE'")
    int markExpiredIfActive(@Param("tokenId") Long tokenId,
                            @Param("expiredAt") LocalDateTime expiredAt);

    @Update("UPDATE checkin_qr_token " +
            "SET token_status = 'USED', used_at = #{usedAt}, version = version + 1, updated_at = #{usedAt} " +
            "WHERE id = #{tokenId} AND order_id = #{orderId} AND token_status = #{requiredStatus}")
    int markUsedForCheckin(@Param("tokenId") Long tokenId,
                           @Param("orderId") Long orderId,
                           @Param("requiredStatus") QrTokenStatus requiredStatus,
                           @Param("usedAt") LocalDateTime usedAt);
}
