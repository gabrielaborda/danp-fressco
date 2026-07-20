import { useState } from "react"
import { useNavigate } from "react-router-dom"
//import { useAuthStore } from "../../store/authStore"
import InputField from "./InputField"

export default function LoginForm() {
  const navigate = useNavigate()
  //const login = useAuthStore((state) => state.login)

  const [username, setUsername] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(false)

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()

    setLoading(true)
    setError("")

    //const response = await window.electronAPI.login(username, password)

    /*if (response.success) {
      login()
      navigate("/home")
    } else {
      setError(response.message || "Error al iniciar sesión")
    }*/

    setLoading(false)
  }

  return (
    <form onSubmit={handleLogin} className="w-[360px]" autoComplete="off">
      <div className="flex justify-center">
        <img src="/assets/avatar.svg" className="h-[100px]" />
      </div>

      <h2 className="text-3xl font-bold text-primaryDark text-center my-4 uppercase">
        Bienvenido
      </h2>

      <InputField
        type="text"
        label="Username"
        value={username}
        onChange={(e) => setUsername(e.target.value)}
        icon={<img src="/icon/user-icon.svg" alt="User" className="w-5 h-5" />}
        autoComplete="nope"
      />

      <InputField
        type="password"
        label="Password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        icon={<img src="/icon/lock-icon.svg" alt="Lock" className="w-5 h-5" />}
        autoComplete="new-password"
      />

      {error && (
        <p className="text-red-500 text-sm mt-2 text-center">{error}</p>
      )}

      <button className="w-full h-[50px] rounded-full text-white uppercase mt-4 transition-all bg-[#11101D] hover:opacity-90 font-bold">
        {loading ? "Ingresando..." : "Iniciar Sesión"}
      </button>
    </form>
  )
}

