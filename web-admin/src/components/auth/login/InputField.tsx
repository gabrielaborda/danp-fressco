import { useState } from "react"

interface Props {
  type?: string
  label: string
  value: string
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void
  icon?: React.ReactNode
  autoComplete?: string
  placeholder?: string
  error?: string
}

export default function InputField({
  type = "text",
  label,
  value,
  onChange,
  icon,
  autoComplete = "off",
  placeholder,
  error,
}: Props) {
  const [focused, setFocused] = useState(false)
  const [showPassword, setShowPassword] = useState(false)

  const isPassword = type === "password"
  const inputType = isPassword ? (showPassword ? "text" : "password") : type

  return (
    <div className="flex flex-col mb-2 w-full">
      <div
        className={`relative flex items-center gap-3 py-2 border-b-2 transition-colors duration-300 ${
          error ? "border-red-500" : focused || value ? "border-primary" : "border-border"
        }`}
      >
        {/* Icon */}
        {icon && (
          <div className={`flex items-center justify-center transition-colors duration-300 ${
            error ? "text-red-500 opacity-70" : focused || value ? "text-primary" : "text-text-secondary"
          }`}>
            {icon}
          </div>
        )}

        {/* Input container */}
        <div className="relative h-[45px] flex-1">
          <label
            className={`absolute left-0 transition-all duration-300 pointer-events-none font-medium ${
              focused || value
                ? "-top-3 text-xs text-primary"
                : "top-1/2 -translate-y-1/2 text-sm text-text-secondary"
            } ${error && (focused || value) ? "!text-red-500" : ""}`}
          >
            {label}
          </label>

          <input
            type={inputType}
            value={value}
            onChange={onChange}
            onFocus={() => setFocused(true)}
            onBlur={() => setFocused(false)}
            autoComplete={autoComplete}
            placeholder={focused ? placeholder : ""}
            className="w-full h-full bg-transparent outline-none pt-3 pb-1 text-text-primary font-medium"
          />
        </div>

        {isPassword && (
          <button
            type="button"
            onClick={() => setShowPassword(!showPassword)}
            className="p-1 opacity-50 hover:opacity-100 transition-opacity focus:outline-none"
            tabIndex={-1}
            title={showPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
          >
            {showPassword ? (
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-5 h-5 text-text-secondary">
                <path strokeLinecap="round" strokeLinejoin="round" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
              </svg>
            ) : (
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-5 h-5 text-text-secondary">
                <path strokeLinecap="round" strokeLinejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
                <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            )}
          </button>
        )}
      </div>
      {error && (
        <span className="text-red-500 text-xs mt-1 font-medium">{error}</span>
      )}
    </div>
  )
}