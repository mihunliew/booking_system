import { Role } from '../constants/enums'
import type { UserResponse, JwtResponse } from '../dto/auth.dto'

import CryptoJS from 'crypto-js'

const TOKEN_KEY = 'n2n_jwt_token'
const USER_KEY = 'n2n_user_info'
const HASH_KEY = 'n2n_user_hash'
const SECRET = 'n2n_secure_hash_secret_89234' // In production, consider loading from env variables if possible, or keeping it hidden

export const setStoredAuth = (token: string, user: UserResponse | JwtResponse) => {
  localStorage.setItem(TOKEN_KEY, token)
  const userStr = JSON.stringify(user)
  localStorage.setItem(USER_KEY, userStr)
  const hash = CryptoJS.HmacSHA256(userStr, SECRET).toString()
  localStorage.setItem(HASH_KEY, hash)
}

export const getStoredToken = (): string | null => {
  return localStorage.getItem(TOKEN_KEY)
}

export const getStoredUser = (): UserResponse | JwtResponse | null => {
  const userStr = localStorage.getItem(USER_KEY)
  const hashStr = localStorage.getItem(HASH_KEY)
  if (!userStr) return null

  if (hashStr) {
    const expectedHash = CryptoJS.HmacSHA256(userStr, SECRET).toString()
    if (expectedHash !== hashStr) {
      console.error("User information has been tampered with. Cleaning up.")
      removeStoredAuth()
      return null
    }
  } else {
    // If no hash string exists but user exists, it might be an old session. Best to clean it.
    removeStoredAuth()
    return null
  }

  try {
    return JSON.parse(userStr)
  } catch (e) {
    removeStoredAuth()
    return null
  }
}

export const removeStoredAuth = () => {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
  localStorage.removeItem(HASH_KEY)
}

export const isAuthenticated = (): boolean => {
  return !!getStoredToken()
}

export const isAdmin = (): boolean => {
  const user = getStoredUser()
  const r = user?.role as unknown as string
  return !!(user && (r === Role.ADMIN || r === 'ROLE_ADMIN' || r === 'ADMIN' || r === 'ROLE_SUPERADMIN' || r === 'SUPERADMIN'))
}

export const isSuperAdmin = (): boolean => {
  const user = getStoredUser()
  const r = user?.role as unknown as string
  return !!(user && (r === 'ROLE_SUPERADMIN' || r === 'SUPERADMIN'))
}

export const hasPermission = (moduleName: string, action: 'CREATE' | 'READ' | 'UPDATE' | 'DELETE'): boolean => {
  if (isSuperAdmin()) return true
  const user = getStoredUser()
  console.log(`Checking permission for ${moduleName} ${action}. User:`, user)
  if (!user || !user.permissions) {
    console.log('No user or permissions found')
    return false
  }

  const perm = user.permissions.find(p => p.moduleName === moduleName)
  console.log(`Permission record for ${moduleName}:`, perm)
  if (!perm) return false

  switch (action) {
    case 'CREATE': return perm.canCreate
    case 'READ': return perm.canRead
    case 'UPDATE': return perm.canUpdate
    case 'DELETE': return perm.canDelete
    default: return false
  }
}
