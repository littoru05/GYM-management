import { BrowserRouter, Routes, Route } from 'react-router-dom'
import PublicLayout from './layouts/PublicLayout'

function HomePlaceholder() {
  return (
    <div className="mx-auto max-w-5xl px-4 py-16 text-center">
      <h1 className="text-2xl font-semibold text-gray-900">GYM Management</h1>
      <p className="mt-2 text-gray-500">Base layout is ready.</p>
    </div>
  )
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<PublicLayout />}>
          <Route index element={<HomePlaceholder />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
