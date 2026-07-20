import { useState } from "react"

interface Props {
  type: string
  label: string
  value: string
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void
  icon: React.ReactNode
  autoComplete?: string
}

export default function InputField({
  type,
  label,
  value,
  onChange,
  icon,
  autoComplete = "off",
}: Props) {
  const [focused, setFocused] = useState(false)

  return (
    <div
      className={`relative grid grid-cols-[7%_93%] my-6 border-b-2 transition-all duration-300 ${
        focused || value ? "border-primary" : "border-gray-300"
      }`}
    >
      {/* Icon */}
      <div className="flex items-center justify-center text-gray-400">
        {icon}
      </div>

      {/* Input container */}
      <div className="relative h-[45px]">
        <label
          className={`absolute left-2 transition-all duration-300 pointer-events-none ${
            focused || value
              ? "-top-2 text-sm text-primary"
              : "top-1/2 -translate-y-1/2 text-gray-400"
          }`}
        >
          {label}
        </label>

        <input
          type={type}
          value={value}
          onChange={onChange}
          onFocus={() => setFocused(true)}
          onBlur={() => setFocused(false)}
          autoComplete={autoComplete}
          className="w-full h-full bg-transparent outline-none px-2 text-gray-700"
        />
      </div>
    </div>
  )
}