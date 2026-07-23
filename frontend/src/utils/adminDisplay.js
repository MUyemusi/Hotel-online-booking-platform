

const maps = {
  status: { ENABLED: '启用', DISABLED: '停用', PUBLISHED: '已发布', OFFLINE: '已下架', ONLINE: '已上架', ACTIVE: '正常', PAUSED: '暂停' },
  dayType: { HOLIDAY: '法定节假日', MAKEUP_WORKDAY: '调休工作日' },
  sourceStatus: { OFFICIAL: '正式', PROVISIONAL: '暂定' },
  holidayCode: { MID_AUTUMN: '中秋节', NATIONAL_DAY: '国庆节', NEW_YEAR: '元旦', SPRING_FESTIVAL: '春节', QINGMING: '清明节', LABOR_DAY: '劳动节', DRAGON_BOAT: '端午节', CUSTOM: '自定义' },
  priceSource: { MANUAL: '人工价格', ROOM_RULE: '房型价格规则', GLOBAL_RULE: '全局价格规则', HOLIDAY: '节假日价格', MAKEUP_WORKDAY: '调休工作日', FRIDAY: '周五价格', SATURDAY: '周六价格', SUNDAY: '周日价格', WEEKEND: '周末价格', NORMAL_DAY: '普通日期', BASE: '基础价格' },
  inventoryChange: { LOCK: '锁定库存', CONFIRM: '确认预订', RELEASE: '释放库存', SYSTEM_INIT: '初始化库存', MANUAL_UPDATE: '人工调整', PAUSE: '暂停销售', RESUME: '恢复销售' },
  paymentMethod: { MOCK: '模拟支付', WECHAT: '微信支付', ALIPAY: '支付宝', CASH: '现金', CARD: '银行卡' },
  configGroup: { BOOKING: '预订', ORDER: '订单', CREDIT: '信用', FRONTDESK: '前台', INVENTORY: '库存', CONTENT: '内容', PRICING: '价格' },
  valueType: { INTEGER: '整数', DECIMAL: '小数', TIME: '时间', STRING: '文本' },
  inventoryReason: { 'Initialize rolling inventory': '初始化滚动库存', 'Lock inventory for order': '为订单锁定库存', 'Confirm booking inventory': '确认订单库存', 'Release inventory': '释放订单库存' },
  orderStatus: { PENDING_PAYMENT: '待支付', PENDING_CHECK_IN: '待入住', CHECKED_IN: '已入住', COMPLETED: '已完成', CANCELLED: '已取消', CLOSED: '已关闭' }
}

export function displayValue(group, value) {
  if (value == null || value === '') return '-'
  return maps[group]?.[value] || value
}

export const weekdayName = date => ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'][new Date(`${date}T12:00:00`).getDay()]
export const currencyCny = value => `￥${Number(value || 0).toFixed(2)}`
