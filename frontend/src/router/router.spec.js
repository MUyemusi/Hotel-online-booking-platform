import { describe, expect, it, vi } from 'vitest'
vi.mock('@/api/auth', () => ({ refresh: vi.fn().mockRejectedValue(new Error('no session')) }))
import router from './index'

describe('router contract', () => {
  it('contains complete booking routes', () => {
    const names = router.getRoutes().map(route => route.name)
    expect(names).toEqual(expect.arrayContaining(['RoomSearch','RoomDetail','BookingConfirm','MyOrders','OrderDetail','OrderPayment']))
  })
  it('protects role areas', () => {
    expect(router.resolve('/admin').meta.roles).toContain('ADMIN')
    expect(router.resolve('/orders').meta.roles).toContain('USER')
    expect(router.resolve('/frontdesk').meta.roles).toEqual(expect.arrayContaining(['FRONT_DESK','ADMIN']))
  })
})