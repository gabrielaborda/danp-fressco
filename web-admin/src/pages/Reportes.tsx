import { Card } from "../components/ui/Card";
import { BarChart3 } from "lucide-react";

export default function Reportes() {
    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-textPrimary">Reportes</h1>
                    <p className="text-sm text-textSecondary">Analiza las estadísticas y métricas del negocio.</p>
                </div>
            </div>

            <Card className="p-6">
                <div className="flex flex-col items-center justify-center py-12 text-center">
                    <BarChart3 className="h-12 w-12 text-textSecondary mb-4" />
                    <h3 className="text-lg font-medium text-textPrimary">Módulo en construcción</h3>
                    <p className="text-sm text-textSecondary mt-2">El panel de analíticas estará disponible pronto.</p>
                </div>
            </Card>
        </div>
    );
}
