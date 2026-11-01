import { MessageCircle, Phone, Send } from 'lucide-react'

function FloatingContactButtons() {
  return (
    <div className="fixed bottom-5 right-5 z-40 flex flex-col gap-3">
      <a
        href="tel:19001234"
        className="flex h-12 w-12 items-center justify-center rounded-full bg-primary-500 text-white shadow-lg transition-transform hover:scale-105"
        aria-label="Gọi hotline"
      >
        <Phone size={20} />
      </a>
      <a
        href="https://zalo.me"
        target="_blank"
        rel="noreferrer"
        className="flex h-12 w-12 items-center justify-center rounded-full bg-blue-500 text-white shadow-lg transition-transform hover:scale-105"
        aria-label="Chat Zalo"
      >
        <Send size={20} />
      </a>
      <a
        href="https://m.me"
        target="_blank"
        rel="noreferrer"
        className="flex h-12 w-12 items-center justify-center rounded-full bg-indigo-500 text-white shadow-lg transition-transform hover:scale-105"
        aria-label="Chat Messenger"
      >
        <MessageCircle size={20} />
      </a>
    </div>
  )
}

export default FloatingContactButtons
