function firstValue(value) {
  return Array.isArray(value) ? value[0] : value
}

function positiveInt(value, fallback) {
  const parsed = Number(firstValue(value))
  return Number.isInteger(parsed) && parsed > 0 ? parsed : fallback
}

export function normalizeBookingQuery(query = {}, defaults = {}) {
  return {
    roomTypeId: positiveInt(query.roomTypeId, positiveInt(defaults.roomTypeId, undefined)),
    checkInDate: String(firstValue(query.checkInDate ?? query.checkIn ?? defaults.checkInDate ?? '') || ''),
    checkOutDate: String(firstValue(query.checkOutDate ?? query.checkOut ?? defaults.checkOutDate ?? '') || ''),
    roomCount: positiveInt(query.roomCount ?? query.rooms, positiveInt(defaults.roomCount, 1)),
    guests: positiveInt(query.guests, positiveInt(defaults.guests, 1))
  }
}

export function toBookingQuery(state, roomTypeId) {
  const normalized = normalizeBookingQuery({ ...state, roomTypeId })
  const query = {
    checkInDate: normalized.checkInDate || undefined,
    checkOutDate: normalized.checkOutDate || undefined,
    roomCount: normalized.roomCount,
    guests: normalized.guests
  }
  if (normalized.roomTypeId) query.roomTypeId = normalized.roomTypeId
  return query
}
