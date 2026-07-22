import { describe, expect, it } from 'vitest'
import Login from '@/modules/auth/Login.vue'
import Home from '@/modules/home/Home.vue'
import RoomList from '@/views/user/roomtypes/RoomTypeListView.vue'
import RoomDetail from '@/views/user/roomtypes/RoomTypeDetailView.vue'
import RoomSearch from '@/views/user/RoomSearchView.vue'
import BookingConfirm from '@/views/user/BookingConfirmView.vue'
import Payment from '@/views/user/PaymentView.vue'
import Dashboard from '@/views/admin/dashboard/DashboardOverviewView.vue'
import PriceRules from '@/views/admin/pricing/PriceRuleView.vue'
import SystemConfig from '@/views/admin/systemconfig/SystemConfigView.vue'

describe('critical views', () => {
  it.each([
    ['login', Login], ['home', Home], ['room list', RoomList], ['room detail', RoomDetail], ['room search', RoomSearch], ['booking', BookingConfirm],
    ['payment', Payment], ['dashboard', Dashboard], ['price rules', PriceRules], ['system config', SystemConfig]
  ])('compiles %s view', (_name, component) => {
    expect(component).toBeTruthy()
  })
})