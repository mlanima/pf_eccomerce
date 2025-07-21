export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="admin-layout min-h-screen bg-gray-100">
      <div className="flex">
        {/* Admin sidebar will be implemented in later tasks */}
        <aside className="w-64 bg-white shadow-sm border-r border-gray-200">
          <div className="p-6">
            <h2 className="text-lg font-semibold text-gray-900">Admin Panel</h2>
            <nav className="mt-6 space-y-2">
              <p className="text-sm text-gray-600">
                Navigation will be implemented...
              </p>
            </nav>
          </div>
        </aside>

        {/* Main admin content */}
        <main className="flex-1 p-8">{children}</main>
      </div>
    </div>
  );
}
