import { beforeEach, describe, expect, it } from 'vitest'
import { clearAuthSession, getAccessToken, getCurrentUser, hasRole, setAuthSession } from './request'

describe('auth session', () => {
  beforeEach(() => clearAuthSession())
  it('stores token and roles and clears them', () => {
    setAuthSession({ accessToken: 'token', user: { id: 1, roles: ['USER'] } })
    expect(getAccessToken()).toBe('token')
    expect(getCurrentUser().id).toBe(1)
    expect(hasRole('USER')).toBe(true)
    expect(hasRole('ADMIN')).toBe(false)
    clearAuthSession()
    expect(getAccessToken()).toBeNull()
  })
})