USE zhisu_hotel;

-- =========================================================
-- 成员1：用户、角色、信用和操作日志相关约束
-- 其他成员的约束在最终集成时继续追加到本文件
-- =========================================================

-- 用户角色表关联用户
ALTER TABLE sys_user_role
    ADD CONSTRAINT fk_user_role_user
        FOREIGN KEY (user_id)
            REFERENCES sys_user (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE;

-- 用户角色表关联角色
ALTER TABLE sys_user_role
    ADD CONSTRAINT fk_user_role_role
        FOREIGN KEY (role_id)
            REFERENCES sys_role (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT;

-- 信用记录关联用户
ALTER TABLE credit_record
    ADD CONSTRAINT fk_credit_record_user
        FOREIGN KEY (user_id)
            REFERENCES sys_user (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT;

-- 信用记录中的管理员操作人
ALTER TABLE credit_record
    ADD CONSTRAINT fk_credit_record_operator
        FOREIGN KEY (operator_id)
            REFERENCES sys_user (id)
            ON UPDATE CASCADE
            ON DELETE SET NULL;

-- 操作日志关联操作人员
ALTER TABLE operation_log
    ADD CONSTRAINT fk_operation_log_operator
        FOREIGN KEY (operator_id)
            REFERENCES sys_user (id)
            ON UPDATE CASCADE
            ON DELETE SET NULL;

-- 用户账户状态只能是正常或禁用
ALTER TABLE sys_user
    ADD CONSTRAINT chk_user_status
        CHECK (status IN ('ENABLED', 'DISABLED'));

-- 连续取消次数不能小于0
ALTER TABLE sys_user
    ADD CONSTRAINT chk_cancel_count
        CHECK (consecutive_cancel_count >= 0);

-- 信用变动类型限制
ALTER TABLE credit_record
    ADD CONSTRAINT chk_credit_change_type
        CHECK (change_type IN ('CANCEL', 'COMPLETE', 'BAN', 'UNBAN'));

-- 操作结果限制
ALTER TABLE operation_log
    ADD CONSTRAINT chk_operation_result
        CHECK (result IN ('SUCCESS', 'FAIL'));

-- order_id暂时只建立普通索引。
-- 等成员3的订单表确定后，再决定是否添加物理外键。
