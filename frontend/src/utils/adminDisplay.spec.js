import { describe, expect, it } from 'vitest'
import { displayValue, weekdayName } from './adminDisplay'

describe('管理员中文显示映射', () => {
  it('翻译状态和价格来源但保留未知值', () => {
    expect(displayValue('status', 'ENABLED')).toBe('启用')
    expect(displayValue('priceSource', 'HOLIDAY')).toBe('节假日价格')
    expect(displayValue('status', 'CUSTOM_VALUE')).toBe('CUSTOM_VALUE')
  })

  it('显示中文星期', () => {
    expect(weekdayName('2026-07-23')).toBe('星期四')
  })
})
