
export default function LoginLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <div className="w-screen h-screen grid md:grid-cols-2 bg-background relative overflow-hidden">

      {/* Imagen izquierda */}
      <div className="hidden md:flex flex-col items-center justify-center p-12 z-10">
        <img src="/assets/bg.png" className="w-full max-w-[500px] drop-shadow-2xl hover:scale-105 transition-transform duration-500 ease-out" />
      </div>

      {/* Form */}
      <div className="flex justify-center items-center p-8 z-10">
        <div className="w-full max-w-[420px] bg-surface p-10 rounded-3xl shadow-xl border border-border/40 backdrop-blur-md">
          {children}
        </div>
      </div>
    </div>
  )
}