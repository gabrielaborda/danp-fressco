import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { useAuthStore } from "../../../store/authStore"
import InputField from "./InputField"
import { api } from "../../../api/axios"

export default function LoginForm() {
  const navigate = useNavigate()
  const setAuth = useAuthStore((state) => state.setAuth)

  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  
  const [emailError, setEmailError] = useState("")
  const [passwordError, setPasswordError] = useState("")
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(false)

  const validate = () => {
    let isValid = true
    setEmailError("")
    setPasswordError("")
    setError("")

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!email) {
      setEmailError("El correo electrónico es requerido")
      isValid = false
    } else if (!emailRegex.test(email)) {
      setEmailError("Ingresa un correo válido (ej. usuario@dominio.com)")
      isValid = false
    }

    if (!password) {
      setPasswordError("La contraseña es requerida")
      isValid = false
    } else if (password.length < 6) {
      setPasswordError("La contraseña debe tener al menos 6 caracteres")
      isValid = false
    }

    return isValid
  }

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!validate()) return

    setLoading(true)

    try {
      const response = await api.post("/auth/login", {
        email,
        password,
      })

      setAuth(response.data)
      navigate("/") // Redirect to home/dashboard
    } catch (err: any) {
      if (err.response && err.response.data && err.response.data.detail) {
        setError(err.response.data.detail)
      } else {
        setError("Error al iniciar sesión. Verifica tus credenciales.")
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleLogin} className="w-full flex flex-col gap-2" autoComplete="off">
      <div className="flex justify-center mb-2">
        <div className="w-24 h-24 bg-primary-container/20 rounded-full flex items-center justify-center p-2 shadow-inner">
          <img src="/assets/avatar.svg" className="w-full h-full object-contain drop-shadow-md" alt="Avatar" />
        </div>
      </div>

      <h2 className="text-3xl font-bold text-primary text-center mb-2 uppercase tracking-wider">
        Bienvenido
      </h2>

      <InputField
        type="email"
        label="Correo Electrónico"
        value={email}
        onChange={(e) => {
          setEmail(e.target.value)
          if (emailError) setEmailError("")
        }}
        icon={<img src="/icon/user-icon.svg" alt="User" className="w-5 h-5 opacity-70" />}
        autoComplete="email"
        placeholder="usuario@dominio.com"
        error={emailError}
      />

      <InputField
        type="password"
        label="Contraseña"
        value={password}
        onChange={(e) => {
          setPassword(e.target.value)
          if (passwordError) setPasswordError("")
        }}
        icon={<img src="/icon/lock-icon.svg" alt="Lock" className="w-5 h-5 opacity-70" />}
        autoComplete="new-password"
        error={passwordError}
      />

      {error && (
        <p className="text-red-500 text-sm mt-2 text-center font-medium bg-red-500/10 p-2 rounded-lg">{error}</p>
      )}

      <button
        className="w-full h-[50px] rounded-full text-surface uppercase mt-8 transition-all duration-300 bg-primary hover:bg-primary-container hover:shadow-lg hover:shadow-primary/30 hover:-translate-y-0.5 font-bold tracking-wider flex items-center justify-center gap-2 disabled:opacity-70 disabled:pointer-events-none"
        disabled={loading}
      >
        {loading ? (
          <>
            <span className="w-5 h-5 border-2 border-surface/30 border-t-surface rounded-full animate-spin"></span>
            Ingresando...
          </>
        ) : (
          "Iniciar Sesión"
        )}
      </button>
    </form>
  )
}

