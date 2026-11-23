import { Masthead } from "@/components/layout/Masthead";
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
  themeColor: "#E10098",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="es" className={`${sans.variable} ${display.variable}`}>
      <body>
        <a className="skip" href="#content">Saltar al contenido</a>
        <Masthead>{children}</Masthead>
      </body>
    </html>
  );
}
