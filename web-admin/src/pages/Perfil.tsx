import { Card } from "../components/ui/Card";
import { UserCircle } from "lucide-react";

export default function Perfil() {
    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-textPrimary">Perfil</h1>
                    <p className="text-sm text-textSecondary">Gestiona tu información personal y credenciales.</p>
                </div>
            </div>

            <Card className="p-6">
                <div className="flex flex-col items-center justify-center py-12 text-center">
                    <UserCircle className="h-12 w-12 text-textSecondary mb-4" />
                    <h3 className="text-lg font-medium text-textPrimary">Módulo en construcción</h3>
                    <p className="text-sm text-textSecondary mt-2">La edición de perfil estará disponible pronto.</p>
                </div>
            </Card>
        </div>
    );
}
