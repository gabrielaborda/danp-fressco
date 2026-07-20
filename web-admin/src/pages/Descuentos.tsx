import { Card } from "../components/ui/Card";
import { Percent } from "lucide-react";

export default function Descuentos() {
    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-textPrimary">Descuentos</h1>
                    <p className="text-sm text-textSecondary">Configura las reglas de descuento automáticas.</p>
                </div>
            </div>

            <Card className="p-6">
                <div className="flex flex-col items-center justify-center py-12 text-center">
                    <Percent className="h-12 w-12 text-textSecondary mb-4" />
                    <h3 className="text-lg font-medium text-textPrimary">Módulo en construcción</h3>
                    <p className="text-sm text-textSecondary mt-2">La gestión de descuentos estará disponible pronto.</p>
                </div>
            </Card>
        </div>
    );
}
