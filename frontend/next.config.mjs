import path from "node:path";
import { fileURLToPath } from "node:url";

const directorio = path.dirname(fileURLToPath(import.meta.url));

/** @type {import('next').NextConfig} */
const nextConfig = {
  output: "standalone",
  outputFileTracingRoot: directorio,
  poweredByHeader: false,
  devIndicators: false,
};

export default nextConfig;
