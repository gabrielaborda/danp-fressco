import { Card } from "../components/ui/Card";
import { Users as UsersIcon } from "lucide-react";

export default function Usuarios() {
    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-textPrimary">Usuarios</h1>
                    <p className="text-sm text-textSecondary">Gestiona los accesos y roles de usuarios.</p>
                </div>
            </div>

            <Card className="p-6">
                <div className="flex flex-col items-center justify-center py-12 text-center">
                    <UsersIcon className="h-12 w-12 text-textSecondary mb-4" />
                    <h3 className="text-lg font-medium text-textPrimary">Módulo en construcción</h3>
                    <p className="text-sm text-textSecondary mt-2">La administración de usuarios estará disponible pronto.</p>
                </div>
            </Card>
        </div>
    );
}
