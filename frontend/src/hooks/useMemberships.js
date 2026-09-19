import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import * as membershipService from '../services/membershipService'

export function useMemberships() {
  return useQuery({ queryKey: ['memberships'], queryFn: membershipService.getMemberships })
}

export function useActiveMemberships() {
  return useQuery({ queryKey: ['memberships', 'active'], queryFn: membershipService.getActiveMemberships })
}

export function useMembershipMutations() {
  const queryClient = useQueryClient()
  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['memberships'] })

  const createMembership = useMutation({ mutationFn: membershipService.createMembership, onSuccess: invalidate })
  const updateMembership = useMutation({
    mutationFn: ({ id, payload }) => membershipService.updateMembership(id, payload),
    onSuccess: invalidate,
  })
  const deleteMembership = useMutation({ mutationFn: membershipService.deleteMembership, onSuccess: invalidate })
  const toggleMembershipStatus = useMutation({ mutationFn: membershipService.toggleMembershipStatus, onSuccess: invalidate })

  return { createMembership, updateMembership, deleteMembership, toggleMembershipStatus }
}
