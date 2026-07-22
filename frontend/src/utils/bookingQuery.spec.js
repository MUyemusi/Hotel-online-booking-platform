import { describe, expect, it } from 'vitest'
import { normalizeBookingQuery, toBookingQuery } from './bookingQuery'

describe('booking query contract', () => {
  it('normalizes canonical parameters', () => {
    expect(normalizeBookingQuery({ roomTypeId: '7', checkInDate: '2026-08-01', checkOutDate: '2026-08-03', roomCount: '2', guests: '3' })).toEqual({
      roomTypeId: 7, checkInDate: '2026-08-01', checkOutDate: '2026-08-03', roomCount: 2, guests: 3
    })
  })

  it('keeps legacy links compatible', () => {
    expect(normalizeBookingQuery({ roomTypeId: '2', checkIn: '2026-08-01', checkOut: '2026-08-02', rooms: '2', guests: '2' })).toEqual({
      roomTypeId: 2, checkInDate: '2026-08-01', checkOutDate: '2026-08-02', roomCount: 2, guests: 2
    })
  })

  it('emits only canonical parameters', () => {
    expect(toBookingQuery({ checkIn: '2026-08-01', checkOut: '2026-08-02', rooms: 1, guests: 2 }, 3)).toEqual({
      roomTypeId: 3, checkInDate: '2026-08-01', checkOutDate: '2026-08-02', roomCount: 1, guests: 2
    })
  })
})