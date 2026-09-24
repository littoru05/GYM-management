import { useMutation } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import * as contactService from '../services/contactService'

export function useSubmitContactLead() {
  return useMutation({
    mutationFn: (payload) => contactService.createContactLead(payload),
    onSuccess: () => {
      toast.success('Gửi thông tin liên hệ thành công! Chúng tôi sẽ phản hồi trong thời gian sớm nhất')
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Gửi thông tin liên hệ thất bại. Vui lòng thử lại.')
    },
  })
}
