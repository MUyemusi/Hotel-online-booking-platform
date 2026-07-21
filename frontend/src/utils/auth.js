export const TOKEN_KEY = 'zhisu_token'
export const USER_KEY = 'zhisu_user'

export function getToken() {
  return window.sessionStorage.getItem(TOKEN_KEY)
}

export function getStoredUser() {
  try {
    return JSON.parse(window.sessionStorage.getItem(USER_KEY) || 'null')
  } catch {
    return null
  }
}

export function saveSession(data) {
  window.sessionStorage.setItem(TOKEN_KEY, data.token)
  window.sessionStorage.setItem(USER_KEY, JSON.stringify({
    userId: data.userId,
    username: data.username,
    nickname: data.nickname,
    roles: data.roles || []
  }))
}

export function clearSession() {
  window.sessionStorage.removeItem(TOKEN_KEY)
  window.sessionStorage.removeItem(USER_KEY)
}

export function hasRole(...expected) {
  const roles = (getStoredUser()?.roles || []).map(role => role.replace(/^ROLE_/, ''))
  return expected.some(role => roles.includes(role.replace(/^ROLE_/, '')))
}

export function defaultRouteForCurrentUser() {
  if (hasRole('ADMIN')) return '/admin'
  if (hasRole('FRONT_DESK', 'FRONTDESK')) return '/frontdesk'
  return '/'
}
