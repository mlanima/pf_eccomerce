export default function AdminDashboard() {
  return (
    <div>
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Admin Dashboard</h1>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {/* Dashboard stats will be implemented in later tasks */}
        {["Products", "Orders", "Users", "Categories"].map((item) => (
          <div
            key={item}
            className="bg-white rounded-lg shadow-sm border border-gray-200 p-6"
          >
            <h3 className="text-lg font-semibold text-gray-900 mb-2">{item}</h3>
            <p className="text-2xl font-bold text-primary-600">--</p>
            <p className="text-sm text-gray-600">Total {item.toLowerCase()}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
