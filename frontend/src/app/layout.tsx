import type { Metadata } from "next";
import { Inter } from "next/font/google";
import Link from "next/link";
import "@/styles/globals.css";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "Horse Riding E-commerce",
  description:
    "Specialized online marketplace for equestrian equipment and accessories",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className={inter.className}>
        <header className="bg-white shadow-sm border-b border-gray-200">
          <nav className="container mx-auto px-4 py-4">
            <div className="flex justify-between items-center">
              <div className="flex items-center space-x-8">
                <h1 className="text-xl font-bold text-gray-900">
                  Horse Riding Store
                </h1>
                <div className="hidden md:flex space-x-6">
                  <Link href="/" className="text-gray-600 hover:text-gray-900">
                    Home
                  </Link>
                  <Link
                    href="/products"
                    className="text-gray-600 hover:text-gray-900"
                  >
                    Products
                  </Link>
                  <Link
                    href="/categories"
                    className="text-gray-600 hover:text-gray-900"
                  >
                    Categories
                  </Link>
                </div>
              </div>
              <div className="flex items-center space-x-4">
                <Link
                  href="/cart"
                  className="text-gray-600 hover:text-gray-900"
                >
                  Cart
                </Link>
                <Link
                  href="/login"
                  className="text-gray-600 hover:text-gray-900"
                >
                  Login
                </Link>
              </div>
            </div>
          </nav>
        </header>
        <main>{children}</main>
        <footer className="bg-gray-900 text-white py-8 mt-16">
          <div className="container mx-auto px-4 text-center">
            <p>&copy; 2025 Horse Riding E-commerce. All rights reserved.</p>
          </div>
        </footer>
      </body>
    </html>
  );
}
