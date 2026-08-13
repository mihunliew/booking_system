import { BookingStatus, PaymentStatus, Role } from '../constants/enums'

export interface ColorBadge {
  bg: string;
  color: string;
  border?: string;
  label?: string;
}

export const getBookingStatusColor = (status: BookingStatus | string): ColorBadge => {
  switch (status) {
    case BookingStatus.PENDING:
      return {
        bg: 'rgba(245, 158, 11, 0.15)',
        color: '#fbbf24',
        border: '1px solid rgba(245, 158, 11, 0.3)',
        label: 'Pending'
      }
    case BookingStatus.CONFIRMED:
      return {
        bg: 'rgba(59, 130, 246, 0.15)',
        color: '#60a5fa',
        border: '1px solid rgba(59, 130, 246, 0.3)',
        label: 'Confirmed'
      }
    case BookingStatus.COMPLETED:
      return {
        bg: 'rgba(16, 185, 129, 0.15)',
        color: '#34d399',
        border: '1px solid rgba(16, 185, 129, 0.3)',
        label: 'Completed'
      }
    case BookingStatus.CANCELLED:
      return {
        bg: 'rgba(239, 68, 68, 0.15)',
        color: '#f87171',
        border: '1px solid rgba(239, 68, 68, 0.3)',
        label: 'Cancelled'
      }
    default:
      return {
        bg: 'rgba(156, 163, 175, 0.15)',
        color: '#9ca3af',
        border: '1px solid rgba(156, 163, 175, 0.3)',
        label: status || 'Unknown'
      }
  }
}

export const getPaymentStatusColor = (status: PaymentStatus | string): ColorBadge => {
  switch (status) {
    case PaymentStatus.PAID:
      return {
        bg: 'rgba(16, 185, 129, 0.15)',
        color: '#34d399',
        border: '1px solid rgba(16, 185, 129, 0.3)',
        label: 'Paid'
      }
    case PaymentStatus.PENDING:
    case PaymentStatus.UNPAID:
      return {
        bg: 'rgba(245, 158, 11, 0.15)',
        color: '#fbbf24',
        border: '1px solid rgba(245, 158, 11, 0.3)',
        label: status === PaymentStatus.PENDING ? 'Pending' : 'Unpaid'
      }
    case PaymentStatus.REFUNDED:
      return {
        bg: 'rgba(168, 85, 247, 0.15)',
        color: '#c084fc',
        border: '1px solid rgba(168, 85, 247, 0.3)',
        label: 'Refunded'
      }
    case PaymentStatus.PARTIALLY_REFUNDED:
      return {
        bg: 'rgba(249, 115, 22, 0.15)',
        color: '#fb923c',
        border: '1px solid rgba(249, 115, 22, 0.3)',
        label: 'Partially Refunded'
      }
    case PaymentStatus.FAILED:
      return {
        bg: 'rgba(244, 63, 94, 0.15)',
        color: '#fb7185',
        border: '1px solid rgba(244, 63, 94, 0.3)',
        label: 'Failed'
      }
    default:
      return {
        bg: 'rgba(156, 163, 175, 0.15)',
        color: '#9ca3af',
        border: '1px solid rgba(156, 163, 175, 0.3)',
        label: status || 'Unknown'
      }
  }
}

export const getRoleBadgeColor = (role: Role | string): ColorBadge => {
  if (role === Role.ADMIN || role === 'ROLE_ADMIN') {
    return {
      bg: 'linear-gradient(135deg, rgba(236, 72, 153, 0.2) 0%, rgba(168, 85, 247, 0.2) 100%)',
      color: '#f472b6',
      border: '1px solid rgba(236, 72, 153, 0.4)',
      label: 'Admin'
    }
  }
  if (role === Role.SUPERADMIN || role === 'ROLE_SUPERADMIN') {
    return {
      bg: 'linear-gradient(135deg, #ca0e47 0%, #ca0e47 100%)',
      color: '#ca0e47',
      border: '1px solid #ca0e47',
      label: 'Super Admin'
    }
  }
  return {
    bg: 'rgba(59, 130, 246, 0.15)',
    color: '#60a5fa',
    border: '1px solid rgba(59, 130, 246, 0.3)',
    label: 'User'
  }
}

export const getCategoryBadgeColor = (category?: string): ColorBadge => {
  const cat = (category || '').toLowerCase()
  if (cat.includes('room') || cat.includes('suite')) {
    return { bg: 'rgba(99, 102, 241, 0.2)', color: '#818cf8' }
  }
  if (cat.includes('venue') || cat.includes('hall')) {
    return { bg: 'rgba(236, 72, 153, 0.2)', color: '#f472b6' }
  }
  if (cat.includes('studio') || cat.includes('photo')) {
    return { bg: 'rgba(6, 182, 212, 0.2)', color: '#22d3ee' }
  }
  if (cat.includes('equip')) {
    return { bg: 'rgba(245, 158, 11, 0.2)', color: '#fbbf24' }
  }
  return { bg: 'rgba(168, 85, 247, 0.2)', color: '#c084fc' }
}
