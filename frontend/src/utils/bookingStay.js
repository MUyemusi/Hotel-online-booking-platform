const HOTEL_TIME_ZONE = 'Asia/Shanghai'

function hotelParts(now = new Date()) {
  const parts = new Intl.DateTimeFormat('zh-CN', {
    timeZone: HOTEL_TIME_ZONE,
    hourCycle: 'h23',
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', hour12: false
  }).formatToParts(now)
  return Object.fromEntries(parts.map(part => [part.type, part.value]))
}

export function hotelToday(now = new Date()) {
  const p = hotelParts(now)
  return `${p.year}-${p.month}-${p.day}`
}

export function isEarlyMorningAvailable(now = new Date(), endHour = 6) {
  return Number(hotelParts(now).hour) < endHour
}

export function nextDate(date) {
  if (!date) return ''
  const value = new Date(`${date}T12:00:00Z`)
  value.setUTCDate(value.getUTCDate() + 1)
  return value.toISOString().slice(0, 10)
}

export function applyStayType(state, stayType) {
  state.stayType = stayType === 'EARLY_MORNING' ? 'EARLY_MORNING' : 'STANDARD'
  if (state.stayType === 'EARLY_MORNING') {
    state.checkInDate = hotelToday()
    state.checkOutDate = hotelToday()
  } else if (state.checkInDate && (!state.checkOutDate || state.checkOutDate <= state.checkInDate)) {
    state.checkOutDate = nextDate(state.checkInDate)
  }
  return state
}

export function normalizeOneNight(state) {
  if (state.stayType !== 'EARLY_MORNING' && state.checkInDate
      && (!state.checkOutDate || state.checkOutDate <= state.checkInDate)) {
    state.checkOutDate = nextDate(state.checkInDate)
  }
  return state
}
