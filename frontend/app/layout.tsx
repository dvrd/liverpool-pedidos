import { Mastil } from "@/components/layout/Mastil";
import type { Metadata, Viewport } from "next";
import { Figtree, Fraunces } from "next/font/google";
import type { ReactNode } from "react";
import "./globals.css";

const sans = Figtree({
  subsets: ["latin"],
  weight: ["400", "500", "600", "700"],
  variable: "--font-figtree",
  display: "swap",
});

const display = Fraunces({
  subsets: ["latin"],
  weight: ["500", "600", "700"],
  style: ["normal", "italic"],
  variable: "--font-fraunces",
  display: "swap",
});

export const metadata: Metadata = {
  title: "Mesa de pedidos · Liverpool",
  description: "Gestión de pedidos de la aplicación y del sitio web para el equipo de ventas.",
};

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  themeColor: "#6e243c",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="es" className={`${sans.variable} ${display.variable}`}>
      <body>
        <a className="saltar" href="#contenido">Saltar al contenido</a>
        <Mastil>{children}</Mastil>
      </body>
    </html>
  );
}
