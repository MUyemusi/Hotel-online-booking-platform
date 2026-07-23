import { describe, expect, it } from 'vitest'
import { hotelToday, isEarlyMorningAvailable, nextDate, normalizeOneNight } from './bookingStay'

describe('住宿日期规则', () => {
  it('普通住宿自动补成一晚', () => {
    const state = { checkInDate: '2026-07-23', checkOutDate: '2026-07-23', stayType: 'STANDARD' }
    expect(normalizeOneNight(state).checkOutDate).toBe('2026-07-24')
  })

  it('正确处理跨年一晚', () => {
    expect(nextDate('2026-12-31')).toBe('2027-01-01')
  })

  it('按酒店时区判断凌晨窗口', () => {
    const twoAm = new Date('2026-07-22T18:00:00Z')
    const sixAm = new Date('2026-07-22T22:00:00Z')
    expect(hotelToday(twoAm)).toBe('2026-07-23')
    expect(isEarlyMorningAvailable(twoAm)).toBe(true)
    expect(isEarlyMorningAvailable(sixAm)).toBe(false)
  })
})
