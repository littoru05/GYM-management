import { useMutation } from '@tanstack/react-query'
import { showSuccessToast, showErrorToast } from '../utils/toastService'
import * as contactService from '../services/contactService'

export function useSubmitContactLead() {
  return useMutation({
    mutationFn: (payload) => contactService.createContactLead(payload),
    onSuccess: () => {
      showSuccessToast('Gửi thông tin liên hệ thành công! Chúng tôi sẽ phản hồi trong thời gian sớm nhất')
    },
    onError: (error) => {
      showErrorToast(error, 'Gửi thông tin liên hệ thất bại. Vui lòng thử lại.')
    },
  })
}
