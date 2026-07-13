
export default function LoginLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <div className="w-screen h-screen grid md:grid-cols-2 px-8">
      {/* Wave */}
      <img
        src="/assets/wave.png"
        className="fixed bottom-0 left-0 h-full -z-10 hidden md:block"
      />

      {/* Imagen izquierda */}
      <div className="hidden md:flex items-center justify-end">
        <img src="/assets/bg.svg" className="w-[500px]" />
      </div>

      {/* Form */}
      <div className="flex justify-center items-center">
        {children}
      </div>
    </div>
  )
}