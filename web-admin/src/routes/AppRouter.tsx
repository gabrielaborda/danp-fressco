import { Routes, Route } from "react-router-dom";

import LoginPage from "../pages/AuthPages/LoginPage";

export default function AppRouter() {
    return (
        <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="*" element={<LoginPage />} />
        </Routes>
    );
}
