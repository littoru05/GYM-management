import { BrowserRouter, Routes, Route } from 'react-router-dom'
import PublicLayout from './layouts/PublicLayout'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<PublicLayout />}>
          <Route path="/" element={<div className="container mx-auto px-4 py-12"><h1 className="text-3xl font-bold">Welcome to PowerFit Gym</h1></div>} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
